package com.pixelchess.app;

import android.content.res.Resources;
import android.graphics.*;

/** Predecoded eight-image tumble; only the scenery is painted, never game or clock UI. */
final class ForestLeafRenderer {
  private final Bitmap strip;
  private final Paint paint=new Paint();
  private final Rect source=new Rect();
  private final RectF target=new RectF();
  ForestLeafRenderer(Resources resources){
    BitmapFactory.Options options=new BitmapFactory.Options();
    options.inScaled=false;options.inSampleSize=8;
    strip=BitmapFactory.decodeResource(resources,R.drawable.forest_leaf_strip,options);
    paint.setFilterBitmap(false);paint.setAntiAlias(false);paint.setDither(false);
  }
  void draw(Canvas canvas,SceneGeometry geometry,float width,float density,double seconds){
    if(strip==null||width<=0||geometry.height<=0)return;
    int save=canvas.save();
    float left=geometry.left,top=geometry.top,bottom=top+geometry.size;
    canvas.clipOutRect(left,top,left+geometry.size,bottom);
    // Generous exclusions include the clock plaques/aura, status, coordinates and history.
    float half=Math.min(190*density,width*.54f)*.65f;
    canvas.clipOutRect(width/2-half,top-146*density,width/2+half,top-46*density);
    canvas.clipOutRect(0,top-56*density,width,top-20*density);
    canvas.clipOutRect(width/2-half,bottom+20*density,width/2+half,bottom+96*density);
    canvas.clipOutRect(0,bottom,width,bottom+22*density);
    canvas.clipOutRect(0,Math.min(geometry.height-48*density,bottom+112*density)-18*density,
        width,Math.min(geometry.height-48*density,bottom+112*density)+8*density);
    canvas.clipOutRect(0,Math.min(geometry.height-20*density,bottom+140*density)-12*density,
        width,Math.min(geometry.height-20*density,bottom+140*density)+6*density);
    for(int i=0;i<ForestLeafMotion.COUNT;i++){
      if(!ForestLeafMotion.visible(i,seconds))continue;
      int frame=ForestLeafMotion.frame(i,seconds),col=frame%4,row=frame/4;
      // Fractional source divisions support the generator's actual atlas dimensions.
      source.set(col*strip.getWidth()/4,row*strip.getHeight()/2,
          (col+1)*strip.getWidth()/4,(row+1)*strip.getHeight()/2);
      float x=width*ForestLeafMotion.x(i,seconds),y=geometry.height*ForestLeafMotion.y(i,seconds);
      float size=(28+i*2)*density;
      target.set(x-size/2,y-size/2,x+size/2,y+size/2);
      paint.setAlpha(Math.round((160+i*10)*ForestLeafMotion.alpha(i,seconds)));
      int leaf=canvas.save();canvas.rotate(ForestLeafMotion.angle(i,seconds),x,y);
      canvas.drawBitmap(strip,source,target,paint);canvas.restoreToCount(leaf);
    }
    canvas.restoreToCount(save);
  }
}
