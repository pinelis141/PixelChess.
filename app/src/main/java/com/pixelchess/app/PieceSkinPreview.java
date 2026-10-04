package com.pixelchess.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** In-menu previews use the exact bitmaps rendered by the match, not mockups. */
final class PieceSkinPreview extends View {
  private final PieceSkin skin;
  private final PixelPieceArt art;
  private final Paint paint=new Paint();
  private final Paint backdrop=new Paint();
  private final int[] original={R.drawable.w_pawn,R.drawable.w_knight,R.drawable.b_queen,R.drawable.b_king};
  private final Bitmap[] classic=new Bitmap[4];
  PieceSkinPreview(Context c,PieceSkin skin){
    super(c);this.skin=skin;art=skin==PieceSkin.CLASSIC?null:new PixelPieceArt(skin);
    paint.setAntiAlias(false);paint.setFilterBitmap(false);paint.setDither(false);
    if(art==null)for(int i=0;i<4;i++)classic[i]=BitmapFactory.decodeResource(getResources(),original[i]);
    setContentDescription("Prévia das peças "+skin.label+": dois lados, vista frontal e traseira");
  }
  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);
    float w=getWidth()/2f,h=getHeight()/2f;
    char[] figures={'P','N','q','k'};
    for(int i=0;i<4;i++){
      backdrop.setColor(i%2==0?0xffb5a990:0xff4a504c);
      float x=(i%2)*w,y=(i/2)*h;
      canvas.drawRect(x,y,x+w,y+h,backdrop);
      Bitmap bmp=art==null?classic[i]:art.get(figures[i],i<2,0);
      if(bmp==null)continue;
      float drawH=h*.88f,drawW=drawH*(art==null?.75f:PixelPieceArt.WIDTH/(float)PixelPieceArt.HEIGHT);
      RectF target=new RectF(x+(w-drawW)/2f,y+h-drawH,x+(w+drawW)/2f,y+h);
      canvas.drawBitmap(bmp,null,target,paint);
    }
  }
}
