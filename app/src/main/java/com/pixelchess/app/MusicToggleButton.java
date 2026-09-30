package com.pixelchess.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/** Pixel speaker icon, with a 48 dp touch target and explicit accessible state. */
final class MusicToggleButton extends View {
  private final Paint paint=new Paint();
  private boolean muted;
  MusicToggleButton(Context context){super(context);setClickable(true);setFocusable(true);}
  void setMuted(boolean value){muted=value;setContentDescription(value?"Ativar música do menu":"Silenciar música do menu");invalidate();}
  @Override protected void onDraw(Canvas c){
    super.onDraw(c);float unit=Math.min(getWidth(),getHeight())/24f;
    c.save();c.translate((getWidth()-24*unit)/2,(getHeight()-24*unit)/2);c.scale(unit,unit);
    paint.setColor(isPressed()?0xff46443b:0xff292e31);c.drawRect(0,0,24,24,paint);
    paint.setColor(0xffebddb8);c.drawRect(5,10,8,15,paint);c.drawRect(8,9,10,16,paint);c.drawRect(10,7,12,18,paint);
    if(muted){
      for(int i=0;i<5;i++){c.drawRect(15+i,10+i,16+i,11+i,paint);c.drawRect(19-i,10+i,20-i,11+i,paint);}
    }else{
      c.drawRect(14,10,15,15,paint);c.drawRect(16,8,17,10,paint);c.drawRect(17,10,18,15,paint);c.drawRect(16,15,17,17,paint);
    }
    c.restore();
  }
  @Override public boolean performClick(){super.performClick();return true;}
}
