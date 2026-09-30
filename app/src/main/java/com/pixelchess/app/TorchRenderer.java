package com.pixelchess.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.SystemClock;

/** Pixel flames and soft local illumination, clipped away from all playable squares. */
final class TorchRenderer {
  private final BoardTheme.Glow glow;
  private final BoardTheme.Scene scene;
  private final Paint halo=new Paint(Paint.ANTI_ALIAS_FLAG),pixel=new Paint();
  private final long epoch=SystemClock.uptimeMillis();
  TorchRenderer(BoardTheme theme) {
    glow=theme.glow;scene=theme.scene;
    int rgb=glow.color & 0x00ffffff;
    halo.setShader(new RadialGradient(0,0,1,new int[]{0x90000000|rgb,0x40000000|rgb,rgb},
        new float[]{0f,.35f,1f},Shader.TileMode.CLAMP));
  }
  void draw(Canvas canvas,SceneGeometry geometry,float width,float density) {
    if(scene==null || glow.effect!=BoardTheme.Effect.TORCHES || glow.intensity<=0) return;
    double seconds=(SystemClock.uptimeMillis()-epoch)*.001*glow.speed;
    int save=canvas.save();
    canvas.clipOutRect(geometry.left,geometry.top,geometry.left+geometry.size,geometry.top+geometry.size);
    for(int i=0;i<scene.torches.size();i++) {
      BoardTheme.Torch torch=scene.torches.get(i);
      float x=torch.x*width,y=geometry.mapY(torch.y,scene);
      float brightness=TorchMotion.brightness(i,seconds)*glow.intensity;
      float unit=Math.max(1,width/440f);
      float flameHeight=Math.min(23*density,geometry.upperEnd*.20f);
      float radius=width*(i<2?.23f:.20f);
      int light=canvas.save();
      canvas.translate(x,y-flameHeight*.35f);canvas.scale(radius,radius*1.15f);
      halo.setAlpha(Math.round(150*brightness));canvas.drawCircle(0,0,1,halo);
      canvas.restoreToCount(light);
      float sway=TorchMotion.sway(i,seconds)*unit*2;
      // Stepped silhouettes use hard pixel edges, while the surrounding light is soft.
      pixel.setAlpha(255);
      for(int col=-3;col<=3;col++) {
        float h=flameHeight*TorchMotion.height(i,col,seconds);
        float px=Math.round((x+col*unit*1.5f)/unit)*unit;
        pixel.setColor(0xffdb571b);pixel.setAlpha(Math.round(220*glow.intensity));
        canvas.drawRect(px,y-h*.65f,px+unit*2,y,pixel);
        pixel.setColor(0xffffaf35);pixel.setAlpha(Math.round(245*glow.intensity));
        canvas.drawRect(px+sway,y-h,px+sway+unit*1.5f,y-unit,pixel);
        if(Math.abs(col)<2) {
          pixel.setColor(0xffffe6a0);pixel.setAlpha(Math.round(255*glow.intensity));
          canvas.drawRect(px,y-h*.50f,px+unit*1.5f,y,pixel);
        }
      }
      // Two faint rising embers, offset in phase and confined to each brazier.
      for(int e=0;e<2;e++) {
        float t=(float)((seconds*.43+i*.27+e*.51)%1);
        float ex=x+(float)Math.sin(t*5+i+e)*unit*4;
        float ey=y-flameHeight*(.5f+t*1.1f);
        pixel.setColor(0xffffc56b);pixel.setAlpha(Math.round(130*(1-t)*brightness));
        canvas.drawRect(ex,ey,ex+unit,ey+unit,pixel);
      }
    }
    canvas.restoreToCount(save);
  }
}
