# Themes integration baseline

This branch consolidates the current approved theme work into one development line.

Included:
- Traditional / Classic
- Floresta Ancestral
- Castelo Medieval
- Forja Vulcânica
- BoardTheme registry and renderers
- Theme preference persistence
- Theme clocks and animated effects
- Forest asset hash verification
- Unit tests for themes, castle scene, forge theme and lava rendering

Explicitly not included:
- Menu soundtrack (kept in PR #7)
- Reino Congelado
- Necrópole
- Core game refactor
- Bluetooth clock redesign
- Stockfish / bot mode

Source chain:
main -> Floresta -> theme-system -> Castelo -> Forja

This branch is the single integration baseline for the theme work before merging to main.
