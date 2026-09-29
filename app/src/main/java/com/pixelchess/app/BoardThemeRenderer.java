package com.pixelchess.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.LightingColorFilter;
import android.os.SystemClock;

/** Owns theme bitmaps and decoration; never reads or changes match state. */
public final class BoardThemeRenderer {
  private final BoardTheme theme;
  private final Bitmap board, frame, background, clock;
  private final Rect source = new Rect();
  private final int[] frameX, frameY;
  private final float[] targetX = new float[4], targetY = new float[4];
  private final LightingColorFilter activeTint = new LightingColorFilter(0xffffe4af, 0x00140d00);
  private final Paint clockText = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint auraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint bitmapPaint = new Paint();
  private final Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect = new RectF();
  private final long startedAt = SystemClock.uptimeMillis();

  public BoardThemeRenderer(Resources resources, BoardTheme theme) {
    this.theme = theme;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    board = BitmapFactory.decodeResource(resources, theme.boardRes, options);
    frame = theme.frameRes == 0 ? null : BitmapFactory.decodeResource(resources, theme.frameRes, options);
    background = theme.backgroundRes == 0 ? null : BitmapFactory.decodeResource(resources, theme.backgroundRes, options);
    clock = theme.clockRes == 0 ? null : BitmapFactory.decodeResource(resources, theme.clockRes, options);
    BoardTheme.FrameSlices f=theme.frameSlices;
    frameX=f==null?null:new int[]{f.outerLeft,f.innerLeft,f.innerRight,f.outerRight};
    frameY=f==null?null:new int[]{f.outerTop,f.innerTop,f.innerBottom,f.outerBottom};
    clockText.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE,android.graphics.Typeface.BOLD));
    clockText.setTextAlign(Paint.Align.CENTER);
    auraPaint.setShader(new RadialGradient(0,0,1,new int[]{0x00e6bc55,0x00e6bc55,0x35e6bc55,0x00e6bc55},new float[]{0f,0.65f,0.82f,1f},Shader.TileMode.CLAMP));
    bitmapPaint.setFilterBitmap(false);
    bitmapPaint.setAntiAlias(false);
    bitmapPaint.setDither(false);
  }

  public void draw(Canvas canvas, float left, float top, float size, float density) {
    float margin = Math.min(theme.frameMarginDp * density, Math.max(0, left - 3 * density));
    if (frame != null) {
      if (frameX == null) {
        rect.set(left-margin, top-margin, left+size+margin, top+size+margin);
        canvas.drawBitmap(frame, null, rect, bitmapPaint);
      } else {
        targetX[0]=left-margin; targetX[1]=left; targetX[2]=left+size; targetX[3]=left+size+margin;
        targetY[0]=top-margin; targetY[1]=top; targetY[2]=top+size; targetY[3]=top+size+margin;
        for(int row=0;row<3;row++) for(int col=0;col<3;col++) {
          if(row==1 && col==1) continue;
          source.set(frameX[col],frameY[row],frameX[col+1],frameY[row+1]);
          rect.set(targetX[col],targetY[row],targetX[col+1],targetY[row+1]);
          canvas.drawBitmap(frame,source,rect,bitmapPaint);
        }
      }
    }
    if (theme.glow.effect == BoardTheme.Effect.FIREFLIES && theme.glow.intensity > 0 && margin > 0) {
      // Clip both layers of every particle out of all 64 playable squares.
      int save = canvas.save();
      canvas.clipOutRect(left, top, left+size, top+size);
      double seconds = (SystemClock.uptimeMillis()-startedAt) / 1000.0 * theme.glow.speed;
      for (int i=0; i<FireflyMotion.COUNT; i++) {
        float distance = margin * FireflyMotion.distance(i, seconds);
        float x = FireflyMotion.left(i) ? left-distance : left+size+distance;
        float y = top+size*FireflyMotion.height(i, seconds);
        float light = theme.glow.intensity*FireflyMotion.brightness(i, seconds);
        float radius = (2.4f + (i%3)*0.45f)*density;
        for (int layer=3; layer>=1; layer--) {
          effectPaint.setColor(theme.glow.color);
          effectPaint.setAlpha(Math.round(12*light*(4-layer)));
          canvas.drawCircle(x, y, radius*layer/2f, effectPaint);
        }
        effectPaint.setAlpha(Math.round(190*light));
        float dot = Math.max(1f, density*0.65f);
        canvas.drawRect(x-dot/2, y-dot/2, x+dot/2, y+dot/2, effectPaint);
      }
      canvas.restoreToCount(save);
    }
    rect.set(left, top, left+size, top+size);
    if (board != null) canvas.drawBitmap(board, null, rect, bitmapPaint);
    else {
      for (int row=0; row<8; row++) for (int col=0; col<8; col++) {
        effectPaint.setColor(((row+col)&1)==0 ? 0xffb7bd9c : 0xff30433b);
        canvas.drawRect(left+col*size/8, top+row*size/8, left+(col+1)*size/8, top+(row+1)*size/8, effectPaint);
      }
    }
    if (Color.alpha(theme.boardTint)>0) {
      effectPaint.setColor(theme.boardTint);
      canvas.drawRect(rect,effectPaint);
    }
    if (Color.alpha(theme.darkSquareTint)>0) {
      effectPaint.setColor(theme.darkSquareTint);
      for (int row=0; row<8; row++) for (int col=0; col<8; col++) if (((row+col)&1)==1)
        canvas.drawRect(left+col*size/8, top+row*size/8, left+(col+1)*size/8, top+(row+1)*size/8, effectPaint);
    }
  }
  public void drawBackground(Canvas canvas, int width, int height, int fallback) {
    canvas.drawColor(fallback);
    if(background==null || width<=0 || height<=0) return;
    float scale=Math.max(width/(float)background.getWidth(),height/(float)background.getHeight());
    float w=background.getWidth()*scale,h=background.getHeight()*scale;
    rect.set((width-w)/2f,(height-h)/2f,(width+w)/2f,(height+h)/2f);
    canvas.drawBitmap(background,null,rect,bitmapPaint);
    canvas.drawColor(0x25000000);
  }

  /** Returns false when the caller should draw its traditional clock panel. */
  public boolean drawClock(Canvas canvas,float cx,float cy,String name,String time,boolean active,
                           float density,float scaledDensity,float viewWidth) {
    if(clock==null) return false;
    float width=Math.min(190*density,viewWidth*0.54f),height=width/3f;
    if(active) {
      int save=canvas.save();canvas.translate(cx,cy);canvas.scale(width*0.6f,height*0.7f);
      canvas.drawCircle(0,0,1,auraPaint);canvas.restoreToCount(save);
    }
    rect.set(cx-width/2,cy-height/2,cx+width/2,cy+height/2);
    bitmapPaint.setColorFilter(active?activeTint:null);
    canvas.drawBitmap(clock,null,rect,bitmapPaint);
    bitmapPaint.setColorFilter(null);
    clockText.setColor(active?0xffffe7a7:0xffcecec5);
    clockText.setTextSize(Math.min(11*scaledDensity,height*0.18f));
    canvas.drawText(name,cx,cy-height*0.17f,clockText);
    clockText.setTextSize(Math.min(25*scaledDensity,height*0.40f));
    canvas.drawText(time,cx,cy+height*0.28f,clockText);
    return true;
  }
  public boolean hasBackground() { return background!=null; }
  public boolean animated() { return theme.glow.animated(); }
}
