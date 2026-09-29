package com.pixelchess.app;

/** Immutable visual configuration. IDs are stable saved preferences, never list indices. */
public final class BoardTheme {
  public enum Effect { NONE, FIREFLIES }
  public final String id, name;
  public final int boardRes, frameRes, boardTint, darkSquareTint;
  public final float frameMarginDp;
  public final Glow glow;
  public final int backgroundRes, clockRes;
  public final FrameSlices frameSlices;

  /** Pixel coordinates in the source frame; maps eight strips around the playable area. */
  public static final class FrameSlices {
    public final int outerLeft, outerTop, innerLeft, innerTop, innerRight, innerBottom, outerRight, outerBottom;
    public FrameSlices(int ol,int ot,int il,int it,int ir,int ib,int or,int ob) {
      if (!(0<=ol && ol<il && il<ir && ir<or && 0<=ot && ot<it && it<ib && ib<ob))
        throw new IllegalArgumentException("Invalid frame slices");
      outerLeft=ol;outerTop=ot;innerLeft=il;innerTop=it;innerRight=ir;innerBottom=ib;outerRight=or;outerBottom=ob;
    }
  }

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
  public final int backgroundShade;
  public final ClockAppearance clockAppearance;

  /** Colors and geometry of an illustrated clock, independent from the match timer. */
  public static final class ClockAppearance {
    public final float widthDp, aspectRatio;
    public final int activeText, inactiveText, multiply, add, aura;
    public ClockAppearance(float widthDp,float aspectRatio,int activeText,int inactiveText,int multiply,int add,int aura) {
      if(!Float.isFinite(widthDp)||widthDp<=0||!Float.isFinite(aspectRatio)||aspectRatio<=0)
        throw new IllegalArgumentException("Invalid clock geometry");
      this.widthDp=widthDp;this.aspectRatio=aspectRatio;this.activeText=activeText;
      this.inactiveText=inactiveText;this.multiply=multiply;this.add=add;this.aura=aura;
    }
  }

  private BoardTheme(Builder b) {
    if(b.id==null || !b.id.matches("[a-z][a-z0-9_]*") || b.name==null || b.name.trim().isEmpty()
        || b.boardRes==0 || b.glow==null || !Float.isFinite(b.frameMarginDp) || b.frameMarginDp<0
        || (b.frameSlices!=null && b.frameRes==0) || (b.clockRes!=0 && b.clockAppearance==null))
      throw new IllegalArgumentException("Incomplete theme configuration");
    id=b.id;name=b.name;boardRes=b.boardRes;frameRes=b.frameRes;frameMarginDp=b.frameMarginDp;
    boardTint=b.boardTint;darkSquareTint=b.darkSquareTint;glow=b.glow;
    backgroundRes=b.backgroundRes;clockRes=b.clockRes;frameSlices=b.frameSlices;
    backgroundShade=b.backgroundShade;clockAppearance=b.clockAppearance;
  }

  public static Builder builder(String id,String name,int boardRes) { return new Builder(id,name,boardRes); }
  public static final class Builder {
    private final String id,name;
    private final int boardRes;
    private int frameRes,boardTint,darkSquareTint,backgroundRes,clockRes,backgroundShade;
    private float frameMarginDp;
    private FrameSlices frameSlices;
    private ClockAppearance clockAppearance;
    private Glow glow=new Glow(Effect.NONE,0,0,0);
    private Builder(String id,String name,int boardRes){this.id=id;this.name=name;this.boardRes=boardRes;}
    public Builder frame(int resource,float marginDp,FrameSlices slices){frameRes=resource;frameMarginDp=marginDp;frameSlices=slices;return this;}
    public Builder background(int resource,int shade){backgroundRes=resource;backgroundShade=shade;return this;}
    public Builder overlays(int board,int darkSquares){boardTint=board;darkSquareTint=darkSquares;return this;}
    public Builder glow(Glow config){glow=config;return this;}
    public Builder clock(int resource,ClockAppearance appearance){clockRes=resource;clockAppearance=appearance;return this;}
    public BoardTheme build(){return new BoardTheme(this);}
  }
}
