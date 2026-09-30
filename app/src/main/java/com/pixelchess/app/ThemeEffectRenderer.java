package com.pixelchess.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;

/** Decorative effects own their paint and never draw inside playable squares. */
final class ThemeEffectRenderer {
  private final BoardTheme.Glow glow;
  private final Paint effectPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final long startedAt=SystemClock.uptimeMillis();
  ThemeEffectRenderer(BoardTheme.Glow glow){this.glow=glow;}
  void draw(Canvas canvas,float left,float top,float size,float density,float margin) {
    if (glow.effect == BoardTheme.Effect.FIREFLIES && glow.intensity > 0 && margin > 0) {
      // Clip both layers of every particle out of all 64 playable squares.
      int save = canvas.save();
      canvas.clipOutRect(left, top, left+size, top+size);
      double seconds = (SystemClock.uptimeMillis()-startedAt) / 1000.0 * glow.speed;
      for (int i=0; i<FireflyMotion.COUNT; i++) {
        float distance = margin * FireflyMotion.distance(i, seconds);
        float x = FireflyMotion.left(i) ? left-distance : left+size+distance;
        float y = top+size*FireflyMotion.height(i, seconds);
        float light = glow.intensity*FireflyMotion.brightness(i, seconds);
        float radius = (2.4f + (i%3)*0.45f)*density;
        for (int layer=3; layer>=1; layer--) {
          effectPaint.setColor(glow.color);
          effectPaint.setAlpha(Math.round(12*light*(4-layer)));
          canvas.drawCircle(x, y, radius*layer/2f, effectPaint);
        }
        effectPaint.setAlpha(Math.round(190*light));
        float dot = Math.max(1f, density*0.65f);
        canvas.drawRect(x-dot/2, y-dot/2, x+dot/2, y+dot/2, effectPaint);
      }
      canvas.restoreToCount(save);
    }
  }
  boolean animated(){return glow.animated();}
}
