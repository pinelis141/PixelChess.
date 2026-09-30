package com.pixelchess.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Rect;

/** Owns theme bitmaps and decoration; never reads or changes match state. */
public final class BoardThemeRenderer {
  private final BoardTheme theme;
  private final Bitmap board, frame, background;
  private final ThemeClockRenderer clocks;
  private final ThemeEffectRenderer effects;
  private final TorchRenderer torches;
  private final LavaSurface frameLava,backgroundLava;
  private final ForgeLightRenderer furnace;
  private final long epoch=android.os.SystemClock.uptimeMillis();
  private double effectSeconds(){return (android.os.SystemClock.uptimeMillis()-epoch)*.001*theme.glow.speed;}
  private final SceneGeometry geometry=new SceneGeometry();
  private float sceneWidth;
  private final Rect source = new Rect();
  private final int[] frameX, frameY;
  private final float[] targetX = new float[4], targetY = new float[4];
  private final Paint bitmapPaint = new Paint();
  private final Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect = new RectF();

  public BoardThemeRenderer(Resources resources, BoardTheme theme) {
    this.theme = theme;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    board = BitmapFactory.decodeResource(resources, theme.boardRes, options);
    frame = theme.frameRes == 0 ? null : BitmapFactory.decodeResource(resources, theme.frameRes, options);
    background = theme.backgroundRes == 0 ? null : BitmapFactory.decodeResource(resources, theme.backgroundRes, options);
    Bitmap clock = theme.clockRes == 0 ? null : BitmapFactory.decodeResource(resources, theme.clockRes, options);
    BoardTheme.FrameSlices f=theme.frameSlices;
    frameX=f==null?null:new int[]{f.outerLeft,f.innerLeft,f.innerRight,f.outerRight};
    frameY=f==null?null:new int[]{f.outerTop,f.innerTop,f.innerBottom,f.outerBottom};
    clocks=new ThemeClockRenderer(clock,theme.clockAppearance,theme.glow);
    boolean lava=theme.glow.effect==BoardTheme.Effect.LAVA;
    frameLava=lava && frame!=null?new LavaSurface(frame):null;
    backgroundLava=lava && background!=null?new LavaSurface(background):null;
    furnace=new ForgeLightRenderer(theme);
    effects=new ThemeEffectRenderer(theme.glow);
    torches=new TorchRenderer(theme);
    bitmapPaint.setFilterBitmap(false);
    bitmapPaint.setAntiAlias(false);
    bitmapPaint.setDither(false);
  }

  public void draw(Canvas canvas, float left, float top, float size, float density) {
    float margin = Math.min(theme.frameMarginDp * density, Math.max(0, left - 3 * density));
    if (frame != null) {
      if (frameX == null) {
        rect.set(left-margin, top-margin, left+size+margin, top+size+margin);
        canvas.drawBitmap(frame, null, rect, bitmapPaint);
      } else {
        targetX[0]=left-margin; targetX[1]=left; targetX[2]=left+size; targetX[3]=left+size+margin;
        targetY[0]=top-margin; targetY[1]=top; targetY[2]=top+size; targetY[3]=top+size+margin;
        for(int row=0;row<3;row++) for(int col=0;col<3;col++) {
          if(row==1 && col==1) continue;
          source.set(frameX[col],frameY[row],frameX[col+1],frameY[row+1]);
          rect.set(targetX[col],targetY[row],targetX[col+1],targetY[row+1]);
          canvas.drawBitmap(frame,source,rect,bitmapPaint);
          if(frameLava!=null)frameLava.draw(canvas,source,rect,effectSeconds(),row==1,theme.glow.intensity);
        }
      }
    }
    effects.draw(canvas,left,top,size,density,margin);
    torches.draw(canvas,geometry,sceneWidth,density);
    rect.set(left, top, left+size, top+size);
    if (board != null) canvas.drawBitmap(board, null, rect, bitmapPaint);
    else {
      for (int row=0; row<8; row++) for (int col=0; col<8; col++) {
        effectPaint.setColor(((row+col)&1)==0 ? 0xffb7bd9c : 0xff30433b);
        canvas.drawRect(left+col*size/8, top+row*size/8, left+(col+1)*size/8, top+(row+1)*size/8, effectPaint);
      }
    }
    if (Color.alpha(theme.boardTint)>0) {
      effectPaint.setColor(theme.boardTint);
      canvas.drawRect(rect,effectPaint);
    }
    if (Color.alpha(theme.darkSquareTint)>0) {
      effectPaint.setColor(theme.darkSquareTint);
      for (int row=0; row<8; row++) for (int col=0; col<8; col++) if (((row+col)&1)==1)
        canvas.drawRect(left+col*size/8, top+row*size/8, left+(col+1)*size/8, top+(row+1)*size/8, effectPaint);
    }
  }
  public void drawBackground(Canvas canvas, int width, int height, int fallback, float density) {
    geometry.update(width,height,density,theme.scene!=null);
    sceneWidth=width;
    canvas.drawColor(fallback);
    if(background==null || width<=0 || height<=0) return;
    if(theme.scene!=null) {
      int cutTop=Math.round(background.getHeight()*theme.scene.boardTop);
      int cutBottom=Math.round(background.getHeight()*theme.scene.boardBottom);
      drawSceneRegion(canvas,0,cutTop,0,geometry.upperEnd,width);
      drawSceneRegion(canvas,cutTop,cutBottom,geometry.upperEnd,geometry.lowerStart,width);
      drawSceneRegion(canvas,cutBottom,background.getHeight(),geometry.lowerStart,height,width);
      canvas.drawColor(theme.backgroundShade);
      furnace.draw(canvas,geometry,width,effectSeconds());
      return;
    }
    float scale=Math.max(width/(float)background.getWidth(),height/(float)background.getHeight());
    float w=background.getWidth()*scale,h=background.getHeight()*scale;
    rect.set((width-w)/2f,(height-h)/2f,(width+w)/2f,(height+h)/2f);
    canvas.drawBitmap(background,null,rect,bitmapPaint);
    canvas.drawColor(theme.backgroundShade);
  }

  private void drawSceneRegion(Canvas canvas,int sourceTop,int sourceBottom,float top,float bottom,int width) {
    source.set(0,sourceTop,background.getWidth(),sourceBottom);
    rect.set(0,top,width,bottom);
    canvas.drawBitmap(background,source,rect,bitmapPaint);
    if(backgroundLava!=null) {
      int save=canvas.save();
      canvas.clipOutRect(geometry.left,geometry.top,geometry.left+geometry.size,geometry.top+geometry.size);
      backgroundLava.draw(canvas,source,rect,effectSeconds(),false,theme.glow.intensity*(sourceTop==0?LavaMotion.furnace(effectSeconds()):1f),sourceTop!=0);
      canvas.restoreToCount(save);
    }
  }
  public boolean hasScene() { return theme.scene!=null; }
  public float topClockX(float width){return hasScene()?theme.scene.topClockX*width:width/2;}
  public float topClockY(float defaultY) { return hasScene()?geometry.mapY(theme.scene.topClock,theme.scene):defaultY; }
  public float topClockScale(float density) { return hasScene()?Math.min(1f,geometry.upperEnd/(170*density)):1f; }

  public void drawClock(Canvas canvas,float cx,float cy,String name,String time,boolean active,
                        float density,float scaledDensity,float viewWidth){
    clocks.draw(canvas,cx,cy,name,time,active,density,scaledDensity,viewWidth);
  }
  public boolean hasBackground() { return background!=null; }
  public boolean animated() { return effects.animated(); }
}
