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

## 0.19.1 — visible flow correction

User reported the animation was not visible on-device. Redraw scheduling was present, but the original translucent highlight had low contrast against baked emissive artwork. Replace nested ComposeShader masking with explicit SRC_IN layer compositing and a moving dark-red-to-hot-yellow pattern. Increase decorative effect strength and furnace pulse range. Existing channels, asset pixels and board geometry are unchanged.

Add a Robolectric native-graphics rendering regression: actual rendered pixels must differ between t=0 and t=2 for horizontal and vertical flow; t=8 must match t=0; cold stone pixels must remain unchanged and intensity=0 must reproduce the base art. This validates image changes in Android native software rendering; the physical device's hardware renderer still needs review.

## 0.19.2 — directional molten surface

The user's device recording confirmed furnace pulsing but showed that moving brightness alone did not read as flowing lava. Frame, clock and lateral-feed channels now contain a cached repeating pixel texture with dark cooled patches and elongated incandescent details, advected at 48 source pixels per second. The surface travels down vertical channels and across horizontal channels, restricted to the existing approved hot-pixel mask. Fixed stone/metal and the furnace's accepted ember shimmer are preserved.

The eight-second wrap moves exactly one texture period. A native-rendered regression checks that the same surface features move 24 pixels after 0.5 seconds in both directions, rather than only changing brightness. A phone-scale filmstrip of the approved frame is emitted by the graphics test for visual inspection. No per-frame texture generation or changes to approved asset files.
