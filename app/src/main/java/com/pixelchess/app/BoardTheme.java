package com.pixelchess.app;

/** Immutable visual configuration. IDs are stable saved preferences, never list indices. */
public final class BoardTheme {
  public enum Effect { NONE, FIREFLIES }
  public final String id, name;
  public final int boardRes, frameRes, boardTint, darkSquareTint;
  public final float frameMarginDp;
  public final Glow glow;

  public static final class Glow {
    public final Effect effect;
    public final int color;
    public final float intensity, speed;
    public Glow(Effect effect, int color, float intensity, float speed) {
      if (effect == null || !Float.isFinite(intensity) || intensity < 0 || intensity > 1
          || !Float.isFinite(speed) || speed < 0) throw new IllegalArgumentException("Invalid glow");
      this.effect=effect; this.color=color; this.intensity=intensity; this.speed=speed;
    }
    public boolean animated() { return effect != Effect.NONE && intensity > 0 && speed > 0; }
  }
  public BoardTheme(String id, String name, int boardRes, int frameRes, float frameMarginDp,
                    int boardTint, int darkSquareTint, Glow glow) {
    if (id == null || id.isEmpty() || name == null || glow == null || boardRes == 0
        || !Float.isFinite(frameMarginDp) || frameMarginDp < 0) throw new IllegalArgumentException("Invalid theme");
    this.id=id; this.name=name; this.boardRes=boardRes; this.frameRes=frameRes;
    this.frameMarginDp=frameMarginDp; this.boardTint=boardTint;
    this.darkSquareTint=darkSquareTint; this.glow=glow;
  }
}
