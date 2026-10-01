package com.pixelchess.app;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.animation.ValueAnimator;
import android.util.Base64;
import java.io.*;

/** Clean shared library background; schedules only while the host is actually drawing. */
final class RoyalLibraryDrawable extends Drawable {
  private static Bitmap image;
  private static final float[][] CANDLES={{89,223,36},{130,424,39},{93,1097,63},{26,1186,48},{15,1182,43},{794,856,34},{813,824,31}};
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  private final Paint light=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RadialGradient halo=new RadialGradient(0,0,1,new int[]{0xffffb130,0x65ff7910,0x00ff6400},new float[]{0,.35f,1},Shader.TileMode.CLAMP);
  private final Matrix matrix=new Matrix();
  private final Runnable tick=this::invalidateSelf;
  private final long epoch=SystemClock.uptimeMillis();
  RoyalLibraryDrawable(Context context){load(context);light.setShader(halo);}
  private static synchronized void load(Context context){
    if(image!=null)return;
    try(InputStream in=context.getAssets().open("royal_library_clean.b64");ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
      byte[] bytes=Base64.decode(out.toByteArray(),Base64.DEFAULT);BitmapFactory.Options opts=new BitmapFactory.Options();opts.inPreferredConfig=Bitmap.Config.RGB_565;
      image=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
    }catch(IOException|IllegalArgumentException e){android.util.Log.w("RoyalLibrary","Backdrop unavailable",e);}
  }
  @Override public void draw(Canvas c){
    Rect bounds=getBounds();paint.setShader(null);paint.setAlpha(255);
    if(image!=null)c.drawBitmap(image,null,bounds,paint);else{paint.setColor(0xff1b100c);c.drawRect(bounds,paint);}
    int save=c.save();c.translate(bounds.left,bounds.top);c.scale(bounds.width()/864f,bounds.height()/1536f);
    double seconds=ValueAnimator.areAnimatorsEnabled()?(SystemClock.uptimeMillis()-epoch)*.001:0;
    for(int i=0;i<CANDLES.length;i++){
      float[] candle=CANDLES[i];float r=RoyalMenuMotion.haloRadius(i,seconds,candle[2]);matrix.setScale(r,r);matrix.postTranslate(candle[0],candle[1]-candle[2]*.4f);halo.setLocalMatrix(matrix);
      light.setAlpha(Math.round(RoyalMenuMotion.haloAlpha(i,seconds)*255));c.drawCircle(candle[0],candle[1]-candle[2]*.4f,r,light);
    }
    c.restoreToCount(save);paint.setColor(0x9a100a08);c.drawRect(bounds,paint);
    unscheduleSelf(tick);if(isVisible()&&getCallback()!=null&&ValueAnimator.areAnimatorsEnabled())scheduleSelf(tick,SystemClock.uptimeMillis()+40);
  }
  @Override public boolean setVisible(boolean visible,boolean restart){if(!visible)unscheduleSelf(tick);return super.setVisible(visible,restart);}
  @Override public void setAlpha(int alpha){}
  @Override public void setColorFilter(ColorFilter filter){}
  @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}
