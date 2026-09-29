# Forja Vulcânica — 0.19.0

All four assets were individually approved by the user on 2026-09-29 before integration:

- `forge_board.webp`: version 1 with distributed fine lava fissures, near-black obsidian and beige refractory stone (exec-ec6a0844-ff09-4969-8d77-5c6de860da87).
- `forge_frame.webp`: continuous flowing lava channels bounded by obsidian and iron, replacing the rejected isolated-fissure frame (exec-ead598e6-2471-491a-beb6-f427c3e70004).
- `forge_scene.webp`: grounded stone forge with upper-left anvil, upper-right furnace and sparse lateral lava feeds (exec-c83f4fcf-6f03-42b8-884b-9fda553363cb).
- `forge_clock.webp`: dark clock plaque with lateral lava channels (exec-44a93374-88a1-48c4-b96d-c7929ee4c5bf).

Original generated pixels and alpha are preserved in lossless WebP. No new art was generated during integration. All final resources are under `app/src/main/res/drawable-nodpi/`.

The separate `forge` theme uses the shared board/scene geometry and selector. Background regions align to source y=510 and y=1165; upper clock sits in the open area between the tools and board. Other themes retain their configuration. No game-rule, Bluetooth or clock-accounting changes.

## Animation

`LavaSurface` creates a hot-color alpha mask once from each decorative asset and caches its shaders. A repeating light pattern moves along the existing channels on an eight-second loop: down vertical frame strips and clock edges, horizontally across upper/lower frame strips and lateral scene feeds. The stone geometry and approved channel paths do not move. This is moving illumination inside the illustrated liquid, not a fluid simulation. Endpoint colors match so the loop wraps without a visible jump.

The furnace's hot-pixel highlights and warm local light pulse together. The spill is anchored to the source furnace through the same scene mapping. Background effects are clipped out of the playable board; frame effects are confined to the eight outer strips. The approved board's fine fissures remain static, preserving piece readability. Intensity and speed use the existing theme effect parameters; zero intensity hides overlays and zero speed freezes them.

Paints, shaders and emissive masks are reused. Effects use the existing 50 ms visible-view invalidation cadence. Tests cover loop continuity, mask discrimination, theme registration and clock/furnace fit across five portrait sizes. CI also runs existing Castle/Forest tests and checks frozen Forest asset hashes. On-device appearance/performance requires review on the S23 Ultra.
