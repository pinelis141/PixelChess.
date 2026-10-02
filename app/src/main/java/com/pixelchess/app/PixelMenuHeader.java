package com.pixelchess.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A reusable raster-crisp steel knight header for every screen in the same world. */
final class PixelMenuHeader extends LinearLayout {
  PixelMenuHeader(Context c,String section){
    super(c);setOrientation(VERTICAL);setGravity(Gravity.CENTER_HORIZONTAL);
    int crestSize=RoyalUi.dp(c,54);
    Crest crest=new Crest(c);
    addView(crest,new LinearLayout.LayoutParams(crestSize,crestSize));
    TextView logo=new TextView(c);logo.setText("PIXEL CHESS");RoyalUi.text(logo,section==null?27:21,true);
    logo.setGravity(Gravity.CENTER);logo.setTextColor(0xffe4e9ed);logo.setShadowLayer(2,1,2,0xff9d1b25);
    addView(logo,new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,section==null?43:33)));
    if(section!=null){
      TextView subtitle=new TextView(c);subtitle.setText(section);
      RoyalUi.text(subtitle,section.length()>18?19:23,true);subtitle.setTextColor(RoyalUi.CREAM);
      subtitle.setGravity(Gravity.CENTER);subtitle.setBackground(RoyalUi.panel(c,true));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,52));
      lp.topMargin=RoyalUi.dp(c,7);addView(subtitle,lp);
    }
  }
  /** Pixelated knight sprite is sourced from the game's existing freely distributable pieces. */
  private static final class Crest extends View {
    private final Paint paint=new Paint();
    private final Bitmap knight;
    Crest(Context c){super(c);knight=BitmapFactory.decodeResource(getResources(),R.drawable.w_knight);
      setContentDescription("Emblema do Pixel Chess");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas c){
      super.onDraw(c);float step=Math.min(getWidth(),getHeight())/64f;
      c.save();c.translate((getWidth()-64*step)/2,(getHeight()-64*step)/2);c.scale(step,step);
      paint.setAntiAlias(false);paint.setFilterBitmap(false);paint.setStyle(Paint.Style.FILL);
      Path shield=new Path();shield.moveTo(32,0);shield.lineTo(64,32);
      shield.lineTo(32,64);shield.lineTo(0,32);shield.close();
      paint.setColor(0xff0b0e14);c.drawPath(shield,paint);
      paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);
      paint.setColor(0xffa2a7b0);c.drawPath(shield,paint);paint.setStyle(Paint.Style.FILL);
      paint.setColor(0xffe63b43);
      c.drawRect(30,0,34,5,paint);c.drawRect(59,30,64,34,paint);
      c.drawRect(30,59,34,64,paint);c.drawRect(0,30,5,34,paint);
      if(knight!=null){paint.setColor(0xffffffff);c.drawBitmap(knight,null,new RectF(12,9,52,55),paint);}
      c.restore();
    }
  }
}
