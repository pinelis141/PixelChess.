#!/usr/bin/env python3
"""Assemble byte-exact approved Pixel Chess art from Git-tracked binary fragments."""
import hashlib
from pathlib import Path
import struct
import zipfile

ROOT = Path(__file__).resolve().parent.parent
PARTS = ROOT / 'art/approved_atlas_fragments'
OUTPUT = ROOT / 'app/src/main/assets/approved_piece_atlases.zip'
EXPECTED_SHA256 = 'f2cfe45b0d0073783f47305b3b4615bd679a0afbfd1f90cf596a251cd2feec69'
EXPECTED_SIZE = 7856061


def restore():
    fragments = sorted(PARTS.glob('part_*.bin'))
    if len(fragments) != 9 or [f.name for f in fragments] != [f'part_{i:02d}.bin' for i in range(9)]:
        raise SystemExit('Missing an original Pixel Chess approved atlas fragment')
    data = b''.join(f.read_bytes() for f in fragments)
    if len(data) != EXPECTED_SIZE or hashlib.sha256(data).hexdigest() != EXPECTED_SHA256:
        raise SystemExit('Original Pixel Chess sprite ZIP differs from the approved reference')
    with zipfile.ZipFile(__import__('io').BytesIO(data)) as archive:
        names = {f'{skin}/{side}_{angle}.png' for skin in ('medieval', 'floresta')
                 for side in ('claras', 'escuras') for angle in ('frente', 'costas')}
        if set(archive.namelist()) != names or archive.testzip() is not None:
            raise SystemExit('Original Pixel Chess sprite bundle is invalid')
        for name in names:
            png = archive.read(name)
            if png[:8] != b'\x89PNG\r\n\x1a\n' or struct.unpack('>II', png[16:24]) != (1024, 768):
                raise SystemExit(f'Invalid approved PNG atlas: {name}')
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    if not OUTPUT.exists() or hashlib.sha256(OUTPUT.read_bytes()).hexdigest() != EXPECTED_SHA256:
        OUTPUT.write_bytes(data)
    print(f'Approved Medieval + Forest sprite ZIP verified: {len(data)} bytes; sha256 {EXPECTED_SHA256}')


if __name__ == '__main__':
    restore()