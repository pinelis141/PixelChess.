package com.pixelchess.app;

import android.graphics.*;

/** A cached emissive mask follows the approved lava artwork; only highlights move. */
final class LavaSurface {
  private final Bitmap mask;
  private final Paint paint=new Paint();
  private final LinearGradient horizontal,vertical;
  private final Paint maskPaint=new Paint();
  private final PorterDuffXfermode inside=new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
  private final Matrix motion=new Matrix();
  private final Rect full;
  private static final float STRIDE=180f;
  LavaSurface(Bitmap art) {
    int w=art.getWidth(),h=art.getHeight();
    int[] row=new int[w];
    mask=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
    for(int y=0;y<h;y++) {
      art.getPixels(row,0,w,0,y,w,1);
      for(int x=0;x<w;x++)row[x]=(LavaMotion.maskAlpha(row[x])<<24)|0x00ffffff;
      mask.setPixels(row,0,w,0,y,w,1);
    }
    full=new Rect(0,0,w,h);
    int[] colors={0xff8a2008,0xffc53b0c,0xffffdb72,0xffff761c,0xff8a2008};
    float[] stops={0,.35f,.55f,.72f,1};
    horizontal=new LinearGradient(0,0,STRIDE,0,colors,stops,Shader.TileMode.REPEAT);
    vertical=new LinearGradient(0,0,0,STRIDE,colors,stops,Shader.TileMode.REPEAT);

    paint.setAntiAlias(false);paint.setFilterBitmap(false);
  }
  void draw(Canvas canvas,Rect source,RectF dest,double seconds,boolean down,float intensity) {
    if(intensity<=0 || dest.width()<=0 || dest.height()<=0)return;
    Rect src=source==null?full:source;
    float shift=LavaMotion.phase(seconds)*STRIDE;
    motion.setTranslate(down?0:shift,down?shift:0);
    (down?vertical:horizontal).setLocalMatrix(motion);
    paint.setShader(down?vertical:horizontal);paint.setAlpha(255);
    int save=canvas.save();canvas.clipRect(dest);
    canvas.translate(dest.left,dest.top);
    canvas.scale(dest.width()/src.width(),dest.height()/src.height());
    canvas.translate(-src.left,-src.top);
    // Explicit offscreen compositing avoids relying on a nested shader's child matrix updates.
    int layer=canvas.saveLayer(src.left,src.top,src.right,src.bottom,null);
    maskPaint.setAlpha(Math.round(255*intensity));
    canvas.drawBitmap(mask,0,0,maskPaint);
    paint.setXfermode(inside);
    canvas.drawRect(src,paint);
    paint.setXfermode(null);
    canvas.restoreToCount(layer);
    canvas.restoreToCount(save);
  }
}
