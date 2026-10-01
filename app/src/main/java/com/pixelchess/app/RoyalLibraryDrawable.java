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
  private final Path flame=new Path();
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
    Rect bounds=getBounds();paint.setShader(null);paint.setAlpha(255);paint.setColor(0xffffffff);
    if(image!=null)c.drawBitmap(image,null,bounds,paint);else{paint.setColor(0xff1b100c);c.drawRect(bounds,paint);}
    // Dim the clean scenery BEFORE painting the living candles, so their glow remains readable.
    paint.setColor(0x90100a08);c.drawRect(bounds,paint);
    int save=c.save();c.translate(bounds.left,bounds.top);c.scale(bounds.width()/864f,bounds.height()/1536f);
    double seconds=ValueAnimator.areAnimatorsEnabled()?(SystemClock.uptimeMillis()-epoch)*.001:0;
    for(int i=0;i<CANDLES.length;i++){
      float[] candle=CANDLES[i];float r=RoyalMenuMotion.haloRadius(i,seconds,candle[2]);matrix.setScale(r,r);matrix.postTranslate(candle[0],candle[1]-candle[2]*.4f);halo.setLocalMatrix(matrix);
      light.setAlpha(Math.round(RoyalMenuMotion.haloAlpha(i,seconds)*255));c.drawCircle(candle[0],candle[1]-candle[2]*.4f,r,light);
      float x=candle[0],y=candle[1],h=candle[2],w=h*.26f;
      float flicker=(float)(Math.sin(seconds*13+i*1.9)*.12+Math.sin(seconds*21+i*.7)*.09);
      float lean=(float)Math.sin(seconds*8+i*1.7)*w*.42f;
      flame.reset();flame.moveTo(x,y);
      flame.cubicTo(x-w*1.3f,y-h*.25f,x+lean-w*.7f,y-h*.68f,x+lean,y-h*(1+flicker));
      flame.cubicTo(x+lean+w*.5f,y-h*.53f,x+w*1.2f,y-h*.22f,x,y);
      paint.setColor(0xffff8320);c.drawPath(flame,paint);
      flame.reset();flame.moveTo(x,y);flame.quadTo(x-w*.8f,y-h*.3f,x+lean*.65f,y-h*.8f*(1+flicker));
      flame.quadTo(x+w*.65f,y-h*.25f,x,y);
      paint.setColor(0xffffd65e);c.drawPath(flame,paint);
      paint.setColor(0xfffff3c9);c.drawOval(x-w*.27f,y-h*.28f,x+w*.27f,y+h*.04f,paint);
    }
    c.restoreToCount(save);
    unscheduleSelf(tick);if(isVisible()&&getCallback()!=null&&ValueAnimator.areAnimatorsEnabled())scheduleSelf(tick,SystemClock.uptimeMillis()+40);
  }
  @Override public boolean setVisible(boolean visible,boolean restart){if(!visible)unscheduleSelf(tick);return super.setVisible(visible,restart);}
  @Override public void setAlpha(int alpha){}
  @Override public void setColorFilter(ColorFilter filter){}
  @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}
