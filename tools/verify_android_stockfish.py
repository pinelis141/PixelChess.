#!/usr/bin/env python3
"""Test the exact APK-packaged native executable under the installed app's Android UID."""
import collections
import os
import re
import selectors
import subprocess
import time


def adb(*args):
    return subprocess.check_output(['adb', *args], text=True, timeout=30).strip()

apk = adb('shell', 'pm', 'path', 'com.pixelchess.app').removeprefix('package:')
engine_path = apk.rsplit('/', 1)[0] + '/lib/x86_64/libstockfish.so'
process = subprocess.Popen(['adb', 'shell', 'run-as', 'com.pixelchess.app', engine_path],
                           stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
selector = selectors.DefaultSelector()
selector.register(process.stdout, selectors.EVENT_READ)
pending = collections.deque()
buffer = b''


def send(command):
    process.stdin.write((command + '\n').encode())
    process.stdin.flush()


def until(prefix, seconds=20):
    global buffer
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        while pending:
            line = pending.popleft()
            if line.startswith(prefix):
                print(line, flush=True)
                return line
        if selector.select(timeout=0.25):
            chunk = os.read(process.stdout.fileno(), 65536)
            if not chunk:
                raise RuntimeError('Packaged engine exited before ' + prefix)
            buffer += chunk
            lines = buffer.split(b'\n')
            buffer = lines.pop()
            pending.extend(line.decode('utf-8', errors='strict').strip() for line in lines)
        if process.poll() is not None:
            raise RuntimeError('Packaged engine stopped')
    raise RuntimeError('Packaged engine response timeout: ' + prefix)


try:
    send('uci')
    assert 'Stockfish 19' in until('id name')
    until('uciok')
    for option in ['Threads value 1', 'Hash value 32', 'Ponder value false', 'UCI_Chess960 value false']:
        send('setoption name ' + option)
    send('isready')
    until('readyok')
    # The same five settings as BotDifficulty, exercising real native option transitions.
    for skill, elo, depth, nodes, ms in [(0, 0, 1, 500, 150), (4, 0, 6, 10000, 350),
                                       (20, 1800, 14, 100000, 700), (20, 2400, 20, 500000, 1500),
                                       (20, 0, 64, 2000000, 2500)]:
        send('setoption name UCI_LimitStrength value ' + ('true' if elo else 'false'))
        send('setoption name Skill Level value ' + str(skill))
        if elo:
            send('setoption name UCI_Elo value ' + str(elo))
        send('isready')
        until('readyok')
        send('position startpos')
        send(f'go depth {depth} nodes {nodes} movetime {ms}')
        best = until('bestmove ', 30).split()[1]
        assert re.fullmatch(r'[a-h][1-8][a-h][1-8][qrbn]?', best), best
    send('setoption name UCI_LimitStrength value false')
    send('setoption name Skill Level value 20')
    # Reference transcripts shared with Java integration tests; forced search validates transport.
    for moves, expected in [
        ('e2e4 e7e5 g1f3 b8c6 f1e2 g8f6', 'e1g1'),
        ('e2e4 a7a6 e4e5 d7d5', 'e5d6'),
        ('a2a4 b8c6 a4a5 h7h5 a5a6 h5h4 a6b7 c6a5', 'b7b8n'),
        ('f2f3 e7e5 g2g4', 'd8h4')]:
        send('position startpos moves ' + moves)
        send('go depth 1 movetime 150 searchmoves ' + expected)
        assert until('bestmove ').split()[1] == expected
    send('position startpos moves f2f3 e7e5 g2g4 d8h4')
    send('go depth 1 movetime 150')
    assert until('bestmove ').split()[1] in ['(none)', '0000']
    send('quit')
    assert process.wait(timeout=10) == 0
    print('Android packaged engine: five levels, castling, en passant, underpromotion, mate and clean quit PASS', flush=True)
finally:
    selector.close()
    if process.poll() is None:
        process.kill()
        process.wait(timeout=10)
    adb('shell', 'am', 'force-stop', 'com.pixelchess.app')
    remaining = adb('shell', 'ps', '-A')
    assert 'libstockfish' not in remaining, 'Stockfish process remained after teardown'
