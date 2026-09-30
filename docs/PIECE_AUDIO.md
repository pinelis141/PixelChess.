# Stone piece sound effects

The player's feedback identified all boards as stone. Generic ToneGenerator beeps are replaced
by original synthesized stone impacts: a hard transient and short, inharmonic mineral resonance.
There are no downloaded samples or third-party audio dependencies.

- stone_move.wav: 160 ms, normal piece placement.
- stone_capture.wav: 220 ms, slightly fuller/lower capture impact.
- stone_terminal.wav: 360 ms, restrained double impact for a terminal move.
- All files: PCM WAV, mono, 44,100 Hz, signed 16 bit, zero final sample and 8 ms fade.
- Peak amplitude: 45% (move/terminal), 55% (capture); SoundPool playback volume: 65%.
- Combined asset size: 65,400 bytes. No synthesis or file writing during gameplay.

Rebuild with Node: `node tools/generate_stone_sounds.mjs`. The fixed PRNG and synthesis
parameters generate the checked-in assets without network access. The script is original project
source. No license is assigned to PixelChess by this change.

ChessSounds preloads all three files through SoundPool, with USAGE_GAME / sonification attributes.
Loading is asynchronous; not-yet-loaded or failed samples are skipped, never queued as stale moves.
One stream prevents rapid local moves from stacking loud effects. Audio failures are contained.
Activity pause stops an effect without resuming it later; destruction releases the pool.

All modes already share the normal successful-move audio call, including captures, castling,
en passant and explicit promotion. Terminal cues take precedence over capture cues. Invalid moves
do not sound. Bot pacing and animation durations remain approved and unchanged.
GamePreferences.sound remains the existing persisted option (default false). Enable it under
Configurações → Sons. Menu music and its separate mute preference are unchanged.

Validation includes playback routing/lifecycle/failure tests, real WAV header/level checks,
and successful/illegal/muted/capture/en-passant/bot move integration. Subjective timbre and volume
still need listening on the phone; synthetic stone is a design approximation, not a recording.
