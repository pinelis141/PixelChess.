package com.pixelchess.app;

import android.graphics.*;

/** Local furnace spill; the scene's normalized anchor shares its board-relative mapping. */
final class ForgeLightRenderer {
  private final BoardTheme theme;
  private final Paint light=new Paint(Paint.ANTI_ALIAS_FLAG);
  ForgeLightRenderer(BoardTheme theme) {
    this.theme=theme;
    int rgb=theme.glow.color&0xffffff;
    light.setShader(new RadialGradient(0,0,1,new int[]{0x80000000|rgb,0x30000000|rgb,rgb},
        new float[]{0,.4f,1},Shader.TileMode.CLAMP));
  }
  void draw(Canvas canvas,SceneGeometry geometry,float width,double seconds) {
    if(theme.furnace==null || theme.glow.intensity<=0)return;
    float x=width*theme.furnace.x,y=geometry.mapY(theme.furnace.y,theme.scene);
    float radius=width*.22f;
    int save=canvas.save();
    canvas.clipOutRect(geometry.left,geometry.top,geometry.left+geometry.size,geometry.top+geometry.size);
    canvas.translate(x,y);canvas.scale(radius,radius*.85f);
    light.setAlpha(Math.round(230*theme.glow.intensity*LavaMotion.furnace(seconds)));
    canvas.drawCircle(0,0,1,light);canvas.restoreToCount(save);
  }
}
