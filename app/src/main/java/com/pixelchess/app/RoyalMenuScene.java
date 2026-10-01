package com.pixelchess.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.Base64;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Native library backdrop, candle light and one moving red selection. No WebView. */
final class RoyalMenuScene extends View {
  static final float[] ROW_TOPS={.3225f,.409f,.492f,.621f,.703f};
  private static final float[][] CANDLES={{89,223,10,36},{130,424,11,39},{93,1097,15,63},
      {26,1186,11,48},{15,1182,8,43},{794,856,8,34},{813,824,7,31}};
  private static Bitmap backdrop;
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  private final Paint halo=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path flame=new Path();
  private final RectF bounds=new RectF(),row=new RectF();
  private final RoyalMenuFrame frame=new RoyalMenuFrame();
  private final long epoch=SystemClock.uptimeMillis();
  private final Matrix glowMatrix=new Matrix();
  private final RadialGradient glow=new RadialGradient(0,0,1,new int[]{0xffffb130,0x65ff7910,0x00ff6400},
      new float[]{0,.35f,1},Shader.TileMode.CLAMP);
  private ValueAnimator selection;
  private float selectedTop=ROW_TOPS[0];
  private int selectedIndex;
  private boolean attached;
  RoyalMenuScene(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);loadBackdrop(context);halo.setShader(glow);}
  private static synchronized void loadBackdrop(Context context){
    if(backdrop!=null)return;
    try(InputStream in=context.getAssets().open("royal_library_menu.b64");ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
      byte[] image=Base64.decode(out.toByteArray(),Base64.DEFAULT);
      BitmapFactory.Options options=new BitmapFactory.Options();options.inPreferredConfig=Bitmap.Config.RGB_565;
      backdrop=BitmapFactory.decodeByteArray(image,0,image.length,options);
    }catch(IOException|IllegalArgumentException e){android.util.Log.w("RoyalMenu","Library backdrop unavailable",e);}
  }
  void select(int index){
    if(index<0||index>=ROW_TOPS.length||index==selectedIndex)return;
    selectedIndex=index;if(selection!=null)selection.cancel();
    if(!ValueAnimator.areAnimatorsEnabled()){selectedTop=ROW_TOPS[index];invalidate();return;}
    selection=ValueAnimator.ofFloat(selectedTop,ROW_TOPS[index]);selection.setDuration(260);
    selection.addUpdateListener(a->{selectedTop=(float)a.getAnimatedValue();invalidate();});selection.start();
  }
  int selectedIndex(){return selectedIndex;}
  boolean animationActive(){return attached&&getWindowVisibility()==VISIBLE&&isShown()&&ValueAnimator.areAnimatorsEnabled();}
  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);bounds.set(0,0,getWidth(),getHeight());
    if(backdrop!=null)canvas.drawBitmap(backdrop,null,bounds,paint);else canvas.drawColor(0xff180d09);
    double seconds=ValueAnimator.areAnimatorsEnabled()?(SystemClock.uptimeMillis()-epoch)*.001:0;
    int save=canvas.save();canvas.scale(getWidth()/864f,getHeight()/1536f);drawAtmosphere(canvas,seconds);
    // Clear the baked version/credits and music icon; live Android controls occupy these places.
    paint.setColor(0xee180d09);canvas.drawRect(230,1380,634,1536,paint);canvas.drawRect(724,1380,820,1485,paint);
    canvas.restoreToCount(save);
    for(float top:ROW_TOPS){row.set(getWidth()*.172f,getHeight()*top,getWidth()*.828f,getHeight()*(top+.0765f));frame.draw(canvas,row,false);}
    row.set(getWidth()*.172f,getHeight()*selectedTop,getWidth()*.828f,getHeight()*(selectedTop+.0765f));frame.draw(canvas,row,true);
    if(animationActive())postInvalidateOnAnimation();
  }
  private void drawAtmosphere(Canvas c,double t){
    for(int i=0;i<CANDLES.length;i++){
      float[] a=CANDLES[i];float x=a[0],y=a[1],w=a[2],h=a[3];
      float radius=RoyalMenuMotion.haloRadius(i,t,h),alpha=RoyalMenuMotion.haloAlpha(i,t);
      glowMatrix.setScale(radius,radius);glowMatrix.postTranslate(x,y-h*.4f);glow.setLocalMatrix(glowMatrix);
      halo.setAlpha(Math.round(alpha*255));c.drawCircle(x,y-h*.4f,radius,halo);
      float f=(float)(Math.sin(t*14+i*2)*.13+Math.sin(t*23+i)*.08),lean=(float)Math.sin(t*9+i)*w*.45f;
      flame.reset();flame.moveTo(x,y);flame.cubicTo(x-w*1.3f,y-h*.25f,x+lean-w*.7f,y-h*.68f,x+lean,y-h*(1+f));
      flame.cubicTo(x+lean+w*.5f,y-h*.53f,x+w*1.2f,y-h*.22f,x,y);paint.setColor(0xffff8520);c.drawPath(flame,paint);
      flame.reset();flame.moveTo(x,y);flame.quadTo(x-w*.8f,y-h*.3f,x+lean*.65f,y-h*.8f*(1+f));
      flame.quadTo(x+w*.65f,y-h*.25f,x,y);paint.setColor(0xffffd356);c.drawPath(flame,paint);
      paint.setColor(0xfffff7c5);c.drawOval(x-w*.33f,y-h*.31f,x+w*.33f,y+h*.07f,paint);
    }
    for(int i=0;i<24;i++){
      float x=(float)(689+(Math.sin(i*18.8)*.5+.5)*142),y=(float)(25+(Math.sin(i*13.1)*.5+.5)*124);
      float alpha=(float)(.2+.65*Math.pow(Math.sin(t*1.1+i),8));paint.setColor(0xffcce7ff);paint.setAlpha(Math.round(alpha*255));
      c.drawRect(x,y,x+1.6f,y+1.6f,paint);if(alpha>.65f){c.drawRect(x-2,y+.5f,x+3,y+1.5f,paint);c.drawRect(x+.5f,y-2,x+1.5f,y+3,paint);}
    }
    for(int i=0;i<22;i++){
      float phase=(float)((t*.095+i*.173)%1),x=(float)(30+(i*107)%780+Math.sin(t*.5+i)*8),y=1320-phase*1050;
      paint.setColor(0xffffce73);paint.setAlpha(Math.round((float)Math.sin(phase*Math.PI)*66));c.drawRect(x,y,x+2,y+2,paint);
    }
    paint.setAlpha(255);
  }
  @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;invalidate();}
  @Override protected void onDetachedFromWindow(){attached=false;if(selection!=null)selection.cancel();super.onDetachedFromWindow();}
  @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE)invalidate();}
}
