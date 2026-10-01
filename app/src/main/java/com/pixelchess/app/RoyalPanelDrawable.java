package com.pixelchess.app;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/** One ornament system for cards, toggles, buttons and full-size dialogs. */
final class RoyalPanelDrawable extends Drawable {
  private final RoyalMenuFrame frame=new RoyalMenuFrame();
  private final float density;
  private final boolean selected;
  private boolean active;
  RoyalPanelDrawable(float density,boolean selected){this.density=density;this.selected=selected;}
  @Override public void draw(Canvas canvas){frame.drawPanel(canvas,new RectF(getBounds()),selected||active,density);}
  @Override public boolean isStateful(){return true;}
  @Override protected boolean onStateChange(int[] states){
    boolean next=false;for(int state:states)if(state==android.R.attr.state_pressed||state==android.R.attr.state_focused||state==android.R.attr.state_selected||state==android.R.attr.state_checked)next=true;
    if(next==active)return false;active=next;invalidateSelf();return true;
  }
  @Override public void setAlpha(int alpha){}
  @Override public void setColorFilter(ColorFilter filter){}
  @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
