package com.pixelchess.app;

import android.graphics.*;

/** A cached emissive mask follows the approved lava artwork; molten surface details travel through the fixed channels. */
final class LavaSurface {
  private final Bitmap mask;
  private final Paint paint=new Paint();
  private final LinearGradient horizontal,vertical;
  private final BitmapShader streamDown,streamAcross;
  private static final int TILE_WIDTH=64,TILE_HEIGHT=128;
  private static final float TEXEL=3f;
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

    Bitmap texture=Bitmap.createBitmap(TILE_WIDTH,TILE_HEIGHT,Bitmap.Config.ARGB_8888);
    Bitmap across=Bitmap.createBitmap(TILE_HEIGHT,TILE_WIDTH,Bitmap.Config.ARGB_8888);
    for(int y=0;y<TILE_HEIGHT;y++)for(int x=0;x<TILE_WIDTH;x++) {
      double u=2*Math.PI*x/TILE_WIDTH,v=2*Math.PI*y/TILE_HEIGHT;
      double field=.52+.24*Math.sin(2*u+1.2*Math.sin(2*v))
          +.18*Math.sin(5*v+u+.8*Math.sin(3*u))+.10*Math.sin(11*v-3*u);
      int color=field<.28?0xff541208:field<.38?0xffa52908:field<.55?0xffe74b0c:
          field<.72?0xffff871b:field<.86?0xffffbe45:0xffffdf83;
      texture.setPixel(x,y,color);across.setPixel(y,x,color);
    }
    streamDown=new BitmapShader(texture,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
    streamAcross=new BitmapShader(across,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
    paint.setAntiAlias(false);paint.setFilterBitmap(false);
  }
  void draw(Canvas canvas,Rect source,RectF dest,double seconds,boolean down,float intensity) {
    draw(canvas,source,dest,seconds,down,intensity,true);
  }
  void draw(Canvas canvas,Rect source,RectF dest,double seconds,boolean down,float intensity,boolean flowing) {
    if(intensity<=0 || dest.width()<=0 || dest.height()<=0)return;
    Rect src=source==null?full:source;
    Shader shader;
    if(flowing) {
      // One complete texture period per loop: 48 source pixels/sec, visibly advected crust and filaments.
      float shift=LavaMotion.phase(seconds)*TILE_HEIGHT*TEXEL;
      motion.setScale(TEXEL,TEXEL);
      motion.postTranslate(down?0:shift,down?shift:0);
      shader=down?streamDown:streamAcross;
    } else {
      // Preserve the approved furnace's ember shimmer, without sliding its coals.
      float shift=LavaMotion.phase(seconds)*STRIDE;
      motion.setTranslate(down?0:shift,down?shift:0);
      shader=down?vertical:horizontal;
    }
    shader.setLocalMatrix(motion);
    paint.setShader(shader);paint.setAlpha(255);
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
