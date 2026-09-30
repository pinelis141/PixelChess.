package com.pixelchess.app;

import android.graphics.*;

/** Themed clock plates and standard panels share one rendering entry point. */
final class ThemeClockRenderer {
  private final Bitmap clock;
  private final LavaSurface lava;
  private final BoardTheme.Glow glow;
  private final long epoch=android.os.SystemClock.uptimeMillis();
  private final BoardTheme.ClockAppearance appearance;
  private final Paint bitmapPaint=new Paint();
  private final Paint clockText=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint panelPaint=new Paint(3);
  private final Paint auraPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect=new RectF();
  private final LightingColorFilter activeTint;
  ThemeClockRenderer(Bitmap clock,BoardTheme.ClockAppearance appearance,BoardTheme.Glow glow){
    this.glow=glow;lava=clock!=null && glow.effect==BoardTheme.Effect.LAVA?new LavaSurface(clock):null;
    this.clock=clock;this.appearance=appearance;
    activeTint=appearance==null?null:new LightingColorFilter(appearance.multiply,appearance.add);
    clockText.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
    clockText.setTextAlign(Paint.Align.CENTER);
    panelPaint.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
    bitmapPaint.setFilterBitmap(false);bitmapPaint.setAntiAlias(false);bitmapPaint.setDither(false);
    if(appearance!=null){
      int transparent=appearance.aura & 0x00ffffff;
      auraPaint.setShader(new RadialGradient(0,0,1,new int[]{transparent,transparent,appearance.aura,transparent},
          new float[]{0f,0.65f,0.82f,1f},Shader.TileMode.CLAMP));
    }
  }
  public void draw(Canvas canvas,float cx,float cy,String name,String time,boolean active,
                           float density,float scaledDensity,float viewWidth) {
    if(clock==null){drawPanel(canvas,cx,cy,name,time,active,density,scaledDensity);return;}
    float width=Math.min(appearance.widthDp*density,viewWidth*0.54f),height=width/appearance.aspectRatio;
    if(active) {
      int save=canvas.save();canvas.translate(cx,cy);canvas.scale(width*0.6f,height*0.7f);
      canvas.drawCircle(0,0,1,auraPaint);canvas.restoreToCount(save);
    }
    rect.set(cx-width/2,cy-height/2,cx+width/2,cy+height/2);
    bitmapPaint.setColorFilter(active?activeTint:null);
    canvas.drawBitmap(clock,null,rect,bitmapPaint);
    bitmapPaint.setColorFilter(null);
    if(lava!=null)lava.draw(canvas,null,rect,(android.os.SystemClock.uptimeMillis()-epoch)*.001*glow.speed,true,glow.intensity);
    clockText.setColor(active?appearance.activeText:appearance.inactiveText);
    clockText.setTextSize(Math.min(11*scaledDensity,height*0.18f));
    canvas.drawText(name,cx,cy-height*0.17f,clockText);
    clockText.setTextSize(Math.min(25*scaledDensity,height*0.40f));
    canvas.drawText(time,cx,cy+height*0.28f,clockText);
  }
  private void drawPanel(Canvas c,float cx,float cy,String name,String time,boolean active,float den,float scaledDensity){
      float pw=156*den,ph=58*den,l=cx-pw/2f,t=cy-ph/2f;
      panelPaint.setStyle(Paint.Style.FILL);panelPaint.setColor(Color.rgb(25,30,33));c.drawRect(l,t,l+pw,t+ph,panelPaint);
      panelPaint.setStyle(Paint.Style.STROKE);panelPaint.setStrokeWidth(Math.max(1f,den));panelPaint.setColor(active?Color.rgb(184,148,70):Color.rgb(55,62,63));c.drawRect(l+.5f*den,t+.5f*den,l+pw-.5f*den,t+ph-.5f*den,panelPaint);
      panelPaint.setStyle(Paint.Style.FILL);panelPaint.setColor(active?Color.rgb(210,171,82):Color.rgb(73,79,79));c.drawRect(l,t,l+3*den,t+ph,panelPaint);
      panelPaint.setTextAlign(Paint.Align.CENTER);panelPaint.setTextSize(11*scaledDensity);panelPaint.setColor(active?Color.rgb(226,211,173):Color.rgb(154,158,156));c.drawText(name,cx,t+18*den,panelPaint);
      panelPaint.setTextSize(24*scaledDensity);panelPaint.setColor(active?Color.rgb(235,221,184):Color.rgb(196,194,184));c.drawText(time,cx,t+47*den,panelPaint);
    }
}
