package com.pixelchess.app;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/** Shared pixel-cut black-steel frame. All sizes are in device-independent units. */
final class RoyalMenuFrame {
  private final Paint p=new Paint();
  private final Path cut=new Path();
  void draw(Canvas c,RectF bounds,boolean selected){drawPanel(c,bounds,selected,1f);}
  void drawPanel(Canvas c,RectF b,boolean selected,float density){
    if(b.width()<=1||b.height()<=1)return;
    final int save=c.save();c.clipRect(b);c.translate(b.left,b.top);
    final float w=b.width(),h=b.height(),s=Math.max(1f,Math.min(density*1.3f,Math.min(w,h)/22f));
    p.setAntiAlias(false);p.setStyle(Paint.Style.FILL);p.setShader(null);p.setAlpha(255);
    // The charcoal face is translucent so the medieval room remains visible.
    cut.reset();float k=9*s;cut.moveTo(k,0);cut.lineTo(w-k,0);
    cut.lineTo(w,k);cut.lineTo(w,h-k);cut.lineTo(w-k,h);
    cut.lineTo(k,h);cut.lineTo(0,h-k);cut.lineTo(0,k);cut.close();
    p.setColor(selected?0xd05b070b:0xd51a1d24);c.drawPath(cut,p);
    // Angular steel bevel with dark outer edge and pixel-sharp highlights.
    p.setShader(null);p.setColor(0xff07080d);p.setStrokeWidth(5*s);p.setStyle(Paint.Style.STROKE);
    c.drawPath(cut,p);
    p.setColor(0xff7c848e);p.setStrokeWidth(2*s);c.drawPath(cut,p);
    p.setColor(selected?0xfffb3936:0xff39414d);p.setStrokeWidth(s);
    c.drawRect(12*s,5*s,w-12*s,6*s,p);
    c.drawRect(12*s,h-6*s,w-12*s,h-5*s,p);
    p.setStyle(Paint.Style.FILL);
    // Forged-metal corner tabs; intentionally squared for pixel-art coherence.
    corners(c,w,h,s);
    if(selected){
      p.setColor(0x88d82424);
      c.drawRect(17*s,8*s,w-17*s,10*s,p);
      c.drawRect(17*s,h-10*s,w-17*s,h-8*s,p);
    }
    ruby(c,w*.5f,2*s,4.5f*s,selected);
    ruby(c,w*.5f,h-2*s,4.5f*s,selected);
    p.setShader(null);p.setAlpha(255);c.restoreToCount(save);
  }
  private void corners(Canvas c,float w,float h,float s){
    p.setStyle(Paint.Style.FILL);
    for(int i=0;i<4;i++){
      int save=c.save();c.translate((i&1)!=0?w:0,(i&2)!=0?h:0);
      c.scale((i&1)!=0?-1:1,(i&2)!=0?-1:1);
      p.setColor(0xff11151c);c.drawRect(3*s,3*s,13*s,6*s,p);c.drawRect(3*s,6*s,7*s,13*s,p);
      p.setColor(0xffa8adb3);c.drawRect(6*s,2*s,12*s,3*s,p);c.drawRect(2*s,6*s,3*s,12*s,p);
      p.setColor(0xffd92a31);c.drawRect(6*s,6*s,9*s,9*s,p);
      p.setColor(0xffff5b51);c.drawRect(6*s,6*s,7*s,7*s,p);
      c.restoreToCount(save);
    }
  }
  private void ruby(Canvas c,float x,float y,float r,boolean glow){
    if(r<=0)return;
    p.setStyle(Paint.Style.FILL);
    cut.reset();cut.moveTo(x,y-r);cut.lineTo(x+r,y);cut.lineTo(x,y+r);cut.lineTo(x-r,y);cut.close();
    p.setColor(glow?0xffff4e4e:0xff96262d);c.drawPath(cut,p);
    p.setColor(glow?0xffffb99d:0xffc35451);c.drawRect(x-1,y-1,x+1,y+1,p);
  }
}
