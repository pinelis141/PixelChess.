#!/usr/bin/env python3
"""Convert the checked-in CC0 Freesound preview; Python 3 and ffmpeg required."""
import array
import hashlib
from pathlib import Path
import subprocess
import sys
import wave

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "tools/audio_sources/alabaster_preview.mp3"
SOURCE_SHA256 = "c13803890bd531fee08b167e780b0a7bab2c4e21b1b232366b8df3860792b25b"
RATE = 44100
HIT_SAMPLES = 4410  # 100 ms including trailing silence
GAP_SAMPLES = 3528  # 80 ms between the terminal cue's two hits
PEAK = round(32767 * 0.45)


def prepare():
    if hashlib.sha256(SOURCE.read_bytes()).hexdigest() != SOURCE_SHA256:
        raise ValueError("Audio source checksum mismatch")
    raw = subprocess.check_output([
        "ffmpeg", "-v", "error", "-i", str(SOURCE), "-ac", "1", "-ar", str(RATE),
        "-f", "s16le", "-acodec", "pcm_s16le", "pipe:1",
    ])
    decoded = array.array("h", raw)
    if sys.byteorder != "little":
        decoded.byteswap()
    if not 0 < len(decoded) <= HIT_SAMPLES:
        raise ValueError("Unexpected preview duration")
    # Remove DC; soften only the first 0.2 ms and last 3 ms of the recorded hit.
    mean = sum(decoded) / len(decoded)
    attack, release = 9, 132
    shaped = [(v - mean) * min(1.0, i / attack, (len(decoded) - 1 - i) / release)
              for i, v in enumerate(decoded)]
    peak = max(abs(v) for v in shaped)
    if peak == 0:
        raise ValueError("Silent audio source")
    hit = [round(v * PEAK / peak) for v in shaped]
    hit += [0] * (HIT_SAMPLES - len(hit))
    out = ROOT / "app/src/main/res/raw"
    for name, values in {
        "stone_move": hit,
        "stone_capture": hit,  # Exact same intensity for either color and move type.
        "stone_terminal": hit + [0] * GAP_SAMPLES + hit,
    }.items():
        pcm = array.array("h", values)
        if sys.byteorder != "little":
            pcm.byteswap()
        with wave.open(str(out / (name + ".wav")), "wb") as wav:
            wav.setnchannels(1)
            wav.setsampwidth(2)
            wav.setframerate(RATE)
            wav.writeframes(pcm.tobytes())
        print(f"{name}: {len(values) / RATE:.3f}s, peak {PEAK}/32767")


if __name__ == "__main__":
    prepare()
