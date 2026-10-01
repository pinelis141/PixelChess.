package com.pixelchess.app;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/** Resolution-independent gold carving, drawn above opaque menu panels. */
final class RoyalMenuFrame {
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path outer=new Path(),inner=new Path(),curl=new Path(),leaves=new Path();
  private final RectF panel=new RectF(0,0,600,120);
  private final Shader gold=new LinearGradient(0,0,0,120,
      new int[]{0xffffefaa,0xffb77b28,0xffffdfa0,0xff9f621e,0xffefc878},
      new float[]{0,.28f,.52f,.8f,1},Shader.TileMode.CLAMP);
  RoyalMenuFrame(){
    outer.moveTo(4,43);outer.lineTo(4,23);outer.quadTo(4,16,13,16);
    outer.quadTo(16,4,28,4);outer.lineTo(50,4);
    inner.moveTo(11,46);inner.lineTo(11,29);inner.quadTo(11,22,21,23);
    inner.quadTo(19,12,32,12);inner.lineTo(50,12);
    curl.moveTo(5,45);curl.cubicTo(23,49,28,35,23,31);curl.cubicTo(20,22,15,24,15,28);
    curl.cubicTo(11,34,16,38,19,35);
    curl.moveTo(49,5);curl.cubicTo(46,24,36,26,33,23);curl.cubicTo(23,22,24,16,29,15);
    curl.cubicTo(35,11,39,16,36,19);
    curl.moveTo(17,18);curl.quadTo(28,31,39,28);curl.quadTo(44,25,43,21);
    curl.moveTo(12,47);curl.quadTo(27,55,33,44);curl.quadTo(37,39,31,36);
    curl.moveTo(49,12);curl.quadTo(53,27,44,33);curl.quadTo(39,37,36,31);
    leaves.moveTo(11,14);leaves.lineTo(16,6);leaves.lineTo(20,14);leaves.lineTo(16,20);leaves.close();
    leaves.moveTo(27,30);leaves.quadTo(22,43,37,42);leaves.quadTo(36,33,27,30);leaves.close();
    leaves.moveTo(38,13);leaves.quadTo(47,12,48,23);leaves.quadTo(40,24,38,13);leaves.close();
  }
  void draw(Canvas canvas,RectF bounds,boolean selected){
    int save=canvas.save();canvas.translate(bounds.left,bounds.top);canvas.scale(bounds.width()/600,bounds.height()/120);
    paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);
    paint.setColor(selected?0xff5e1c13:0xff17181d);canvas.drawRoundRect(panel,5,5,paint);
    if(selected){
      paint.setShader(new LinearGradient(0,0,600,120,new int[]{0xff8b2b12,0xff551b13,0xff3d1010},null,Shader.TileMode.CLAMP));
      canvas.drawRoundRect(panel,5,5,paint);
    }
    paint.setShader(gold);paint.setAlpha(selected?255:175);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);
    canvas.drawLine(48,4,552,4,paint);canvas.drawLine(48,116,552,116,paint);
    canvas.drawLine(4,43,4,77,paint);canvas.drawLine(596,43,596,77,paint);
    paint.setStrokeWidth(1);canvas.drawLine(48,11,552,11,paint);canvas.drawLine(48,109,552,109,paint);
    canvas.drawLine(11,46,11,74,paint);canvas.drawLine(589,46,589,74,paint);
    corner(canvas,false,false);corner(canvas,true,false);corner(canvas,false,true);corner(canvas,true,true);
    paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);
    Path flourish=new Path();flourish.moveTo(245,4);flourish.quadTo(264,13,277,4);
    flourish.moveTo(323,4);flourish.quadTo(336,13,355,4);flourish.moveTo(245,116);
    flourish.quadTo(264,107,277,116);flourish.moveTo(323,116);flourish.quadTo(336,107,355,116);
    canvas.drawPath(flourish,paint);paint.setStyle(Paint.Style.FILL);
    diamond(canvas,300,5,7,5);diamond(canvas,300,115,7,5);diamond(canvas,5,60,5,7);diamond(canvas,595,60,5,7);
    paint.setShader(null);paint.setAlpha(255);canvas.restoreToCount(save);
  }
  /** Fixed-size corners keep tall dialogs from stretching the carvings. */
  void drawPanel(Canvas c,RectF bounds,boolean selected,float density){
    int save=c.save();c.translate(bounds.left,bounds.top);float w=bounds.width(),h=bounds.height();
    float unit=Math.min(w/600f,density*.65f);
    paint.setStyle(Paint.Style.FILL);paint.setAlpha(255);paint.setShader(new LinearGradient(0,0,w,h,
        selected?new int[]{0xff812817,0xff4b1712}:new int[]{0xff242127,0xff141317},null,Shader.TileMode.CLAMP));
    c.drawRoundRect(0,0,w,h,5*unit,5*unit,paint);
    paint.setShader(gold);paint.setAlpha(selected?255:205);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3*unit);
    c.drawLine(50*unit,4*unit,w-50*unit,4*unit,paint);c.drawLine(50*unit,h-4*unit,w-50*unit,h-4*unit,paint);
    c.drawLine(4*unit,43*unit,4*unit,h-43*unit,paint);c.drawLine(w-4*unit,43*unit,w-4*unit,h-43*unit,paint);
    paint.setStrokeWidth(unit);c.drawLine(50*unit,11*unit,w-50*unit,11*unit,paint);
    c.drawLine(50*unit,h-11*unit,w-50*unit,h-11*unit,paint);
    c.drawLine(11*unit,46*unit,11*unit,h-46*unit,paint);c.drawLine(w-11*unit,46*unit,w-11*unit,h-46*unit,paint);
    for(int corner=0;corner<4;corner++){
      int cs=c.save();boolean right=(corner&1)!=0,bottom=(corner&2)!=0;
      c.translate(right?w:0,bottom?h:0);c.scale(right?-unit:unit,bottom?-unit:unit);
      corner(c,false,false);c.restoreToCount(cs);
    }
    paint.setStyle(Paint.Style.FILL);diamond(c,w*.5f,5*unit,7*unit,5*unit);diamond(c,w*.5f,h-5*unit,7*unit,5*unit);
    paint.setShader(null);paint.setAlpha(255);c.restoreToCount(save);
  }
  private void corner(Canvas c,boolean right,boolean bottom){
    int save=c.save();c.translate(right?600:0,bottom?120:0);c.scale(right?-1:1,bottom?-1:1);
    paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);c.drawPath(outer,paint);
    paint.setStrokeWidth(1.7f);c.drawPath(inner,paint);c.drawPath(curl,paint);
    paint.setStyle(Paint.Style.FILL);c.drawPath(leaves,paint);c.drawCircle(16,16,2.1f,paint);c.restoreToCount(save);
  }
  private void diamond(Canvas c,float x,float y,float w,float h){
    Path p=new Path();p.moveTo(x,y-h);p.lineTo(x+w,y);p.lineTo(x,y+h);p.lineTo(x-w,y);p.close();c.drawPath(p,paint);
  }
}
