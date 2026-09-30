# Offline bot integration (review branch)

Base: PixelChess 0.27.1, commit 98ce908. Main is not changed by this integration.

## Architecture and audit

ChessGame remains the sole rules authority. Its legal transcript reconstructs castling rights,
en passant, halfmove counters and repetition history. Neither ChessGame nor GameClock nor
BluetoothGameProtocol/BluetoothMatchController/BluetoothManager is changed.

ChessView -> BotController -> ChessEngine -> StockfishEngine -> official Stockfish process.
BotController uses one worker and posts callbacks to the main Handler. Immutable EnginePosition
uses `position startpos moves ...`, including the whole game (not just a FEN that loses repetitions).
EngineMove accepts only UCI coordinate moves with optional q/r/b/n. A replayed ChessGame validates
that response on the worker, then ChessView checks that the live transcript still matches and
applies it through the same move/clock/animation/audio path. Full transcript replay does not block UI.
Human input is restricted to the chosen color. Bluetooth selection and authority code remain intact.

The bot uses the existing monotonic GameClock, including thinking/startup time and the existing
bare-king timeout draw. No clock reset on bot moves. Terminal moves and flag events close the
controller/process. Detachment, exit and Activity destruction also close it. On background pause,
the engine is killed; resume starts a new engine if needed and charges elapsed monotonic time.
Bot color, difficulty, transcript and clock budgets survive recreation. Local restoration is kept.
Failure freezes bot input and its ticker, displays a menu-return dialog, and never fabricates a move.

UCI startup: `uci`/`uciok`, configure Threads/Hash/Ponder/Chess960, `ucinewgame`, `isready`/`readyok`.
Every calculation updates strength, checks ready, sends position then bounded `go`. Startup waits
have 8-second bounds, ready has 3 seconds and search has movetime + 2 seconds. stdout is drained
on a separate daemon thread with bounded line length and queue; unexpected protocol replies,
EOF, invalid moves, launch failure and timeout all fail closed. Teardown uses destroyForcibly,
never a UI-thread pipe write or waitFor. Late callbacks are invalidated when a controller closes.

## Android build and packaging

Stockfish 19, official tag `sf_19`, revision `edb0d9db6731067ec50ce619ff372b463bc4dd5d`.
Origin: https://github.com/official-stockfish/Stockfish/tree/edb0d9db6731067ec50ce619ff372b463bc4dd5d
No changes to upstream engine source, no third-party wrapper, no JNI linkage.

`tools/build_stockfish.sh` fetches the fixed revision; `tools/compile_stockfish.sh` uses its Makefile.
The source archive includes a standalone compile script and offline rebuild instructions. NDK 28.2.13676358,
API 26, baseline armv8 (arm64-v8a), armv7-neon (armeabi-v7a), x86-64 (x86_64).
CXX overrides the upstream API 29 compiler default to match the application's minSdk 26.
Static C++ runtime and 16 KB ELF page alignment. The upstream NNUE network is validated by its
SHA-256 filename. The distribution manifest records full SHA-256 checksums.
The NNUE network is embedded, so runtime needs no internet, downloaded data or external account.

The standalone PIE executable is named `libstockfish.so` only for Android packaging. It is NOT
loaded by System.loadLibrary. `useLegacyPackaging` and `extractNativeLibs` make it executable in
ApplicationInfo.nativeLibraryDir; it is not copied to writable app storage (blocked by Android 10+).
It is launched with ProcessBuilder and stdin/stdout pipes. Binary builds are ignored by git.

Build on Linux:

```
export ANDROID_HOME=/path/to/sdk
sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'ndk;28.2.13676358'
tools/build_stockfish.sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

CI preserves existing tests, lint, debug/release APK and release AAB tasks. It adds native builds
and uploads `Stockfish-19-corresponding-source` together with APK/AAB artifacts. CI also installs the debug APK on an Android 35 emulator and exercises the actual packaged
engine under the app UID: all five difficulties, castling, en passant, underpromotion, terminal mate
and clean process teardown. Physical smartphone playtesting is still needed before public release.

## Difficulty

All levels: Threads=1, Hash=32 MB, Ponder=false, UCI_Chess960=false. No artificial delays.
Search ends when ANY depth, node or movetime limit is reached. Actual movetime is the smaller
of the table limit and remaining clock / 20, clamped to at least 1 ms.

| Level | UCI_LimitStrength | UCI_Elo | Skill Level | Depth cap | Node cap | Movetime cap |
|---|---|---|---|---|---|---|
| Fácil | false | unused | 0 | 1 | 500 | 150 ms |
| Normal | true | 1320 | 20 (overridden) | 8 | 20,000 | 350 ms |
| Difícil | true | 1800 | 20 (overridden) | 14 | 100,000 | 700 ms |
| Especialista | true | 2400 | 20 (overridden) | 20 | 500,000 | 1,500 ms |
| Máximo | false | unused | 20 | 64 | 2,000,000 | 2,500 ms |

Official Stockfish 19 UCI_Elo range is 1320..3190. LimitStrength overrides Skill Level.
The Easy setting instead uses native skill reduction and one-ply search to be markedly weaker;
it does not claim a measured beginner Elo. Playtesting is still needed to confirm beginner fit.
The other Elo values are strength targets, not ratings calibrated for these mobile time/node caps.
Maximum enables full strength within a single-thread bounded smartphone resource budget.

## License and distribution decision

Stockfish is GPL-3.0-or-later. Its original full GPL v3 text and AUTHORS are included inside the
APK under assets/stockfish, plus a notice/source link available in the bot selection dialog.
The integration does NOT change or assign a license to PixelChess. The base repo has no LICENSE
file, so a public GitHub repository must not be confused with an express open-source license.

When distributing the engine binary, preserve copyright and license notices and supply the
exact Corresponding Source, including modifications (none here), embedded NNUE network, build
recipe and compilation settings. CI produces a corresponding-source archive and checksums.
When publishing APKs/AABs outside Actions, provide that archive with equivalent access at the
same download location; an upstream moving branch or latest release is not sufficient. Maintain
availability for recipients and the rights to modify/redistribute under GPL. Do not add licensing
restrictions that contradict the GPL for the engine. Review GPL section 6 installation-information
requirements if a future distribution qualifies as a covered User Product.

Separate processes communicating by UCI reduce coupling, but do NOT automatically prove that
an APK is mere aggregation. Whether the complete application forms a combined derivative work
is a distribution/licensing question; GPL coverage could extend to it. Review this question and
choose an explicit compatible PixelChess license before public distribution if required. No main
merge, PixelChess relicensing or public release is performed by this branch. APK artifacts are for
review, with corresponding engine source made available alongside them.

Sources verified 2026-09-30:
- https://stockfishchess.org/download/
- https://github.com/official-stockfish/Stockfish (README Terms of use, Copying.txt)
- https://github.com/official-stockfish/Stockfish/wiki/Compiling-from-source
- https://github.com/official-stockfish/Stockfish/wiki/UCI-Protocol-and-Stockfish-Commands
- https://developer.android.com/about/versions/10/behavior-changes-10#execute-permission
- GPL text bundled with the pinned engine, sections 5 and 6 (aggregation/Corresponding Source).
