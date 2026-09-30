# Recorded alabaster piece effects

Source: `chess_move_on_alabaster.wav` by **mh2o**, published 2016-08-05.
https://freesound.org/people/mh2o/sounds/351518/
License: **CC0 1.0 Universal**, verified on the source page 2026-09-30.
https://creativecommons.org/publicdomain/zero/1.0/

The checked-in source is Freesound's public MP3 preview, not the original WAV (which
requires a login). Original recording: mono, 8 kHz, about 72 ms; converting to 44.1 kHz
does not add fidelity. Preview URL:
https://cdn.freesound.org/previews/351/351518_4502687-hq.mp3
SHA-256: `c13803890bd531fee08b167e780b0a7bab2c4e21b1b232366b8df3860792b25b`.
No Lichess or YouTube audio is copied. This replaces the earlier synthetic impacts.

## Processing and volume

Rebuild offline with `python3 tools/prepare_piece_audio.py` (Python 3 and ffmpeg).
The script checks the source digest, decodes mono PCM, removes DC, applies a 0.2 ms
attack and 3 ms ending fade, normalizes the peak to 45%, then pads with silence.
Playback is preloaded PCM WAV, mono 44,100 Hz / 16 bit. No decoding, synthesis or
file writing during gameplay.

| Asset | Duration | Content |
|---|---|---|
| stone_move.wav | 100 ms | One recorded hit |
| stone_capture.wav | 100 ms | Exact same bytes as movement |
| stone_terminal.wav | 280 ms | Two identical hits, 80 ms silence between them |

All hits have identical PCM amplitude and a shared SoundPool gain of 65% in both
channels. White, black, human and bot use the same audio path. Terminal cues differ
only by repetition; they do not receive a louder gain. Device media volume still
applies. Per-hit equality avoids using whole-file RMS normalization, which would
inflate the terminal cue because of its silent gap. Combined WAV size: 42,528 bytes.

SoundPool uses USAGE_GAME / sonification and one stream, preventing stacked effects.
Loading is asynchronous; unavailable samples are skipped rather than queued as stale
moves. Pause stops playback, destruction releases the pool, and audio failures are
contained. Valid moves, captures, castling, en passant and promotion share the normal
move callback. Terminal cues take precedence; illegal and muted moves do not sound.

Existing preference: **Configurações → Sons de jogadas**, default off. Menu music,
approved bot pacing, difficulty and animations remain unchanged by this audio update.

Tests check actual WAV headers, bounded levels/durations, byte-identical movement and
capture, identical terminal hits, white/black routing, mute, illegal moves, capture,
en passant, mate, bot presentation and playback lifecycle/failure handling. Listening
on the physical phone is still required to judge timbre and perceived volume.

CC0 permits copying, adaptation and commercial redistribution without attribution
requirements. Provenance is retained here and in `assets/audio/NOTICE.txt`; no license
is assigned to PixelChess. GPL obligations for Stockfish are documented separately
in `docs/STOCKFISH.md`.
