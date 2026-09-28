package com.pixelchess.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;

/** Owns theme bitmaps and decoration; never reads or changes match state. */
public final class BoardThemeRenderer {
  private final BoardTheme theme;
  private final Bitmap board, frame;
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
    bitmapPaint.setFilterBitmap(false);
    bitmapPaint.setAntiAlias(false);
    bitmapPaint.setDither(false);
  }

  public void draw(Canvas canvas, float left, float top, float size, float density) {
    float margin = Math.min(theme.frameMarginDp * density, Math.max(0, left - 3 * density));
    if (frame != null) {
      rect.set(left-margin, top-margin, left+size+margin, top+size+margin);
      canvas.drawBitmap(frame, null, rect, bitmapPaint);
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
  public boolean animated() { return theme.glow.animated(); }
}
