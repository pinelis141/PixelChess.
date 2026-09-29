package com.pixelchess.app;

import android.graphics.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LavaRenderingTest {
  @Test public void renderedLavaMovesButStoneStaysStillAndLoopRepeats() {
    Bitmap art=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
    art.eraseColor(0xff202329);
    Canvas c=new Canvas(art);Paint p=new Paint();p.setColor(0xffea6c12);
    c.drawRect(48,0,80,128,p);
    LavaSurface lava=new LavaSurface(art);
    for(boolean down:new boolean[]{true,false}) {
      Bitmap first=render(art,lava,0,down,1),next=render(art,lava,2,down,1);
      Bitmap loop=render(art,lava,8,down,1),disabled=render(art,lava,2,down,0);
      int changes=0;
      for(int y=0;y<128;y++)for(int x=0;x<128;x++) {
        if(first.getPixel(x,y)!=next.getPixel(x,y))changes++;
        assertEquals(first.getPixel(x,y),loop.getPixel(x,y));
        assertEquals(art.getPixel(x,y),disabled.getPixel(x,y));
        if(x<48 || x>=80)assertEquals(art.getPixel(x,y),next.getPixel(x,y));
      }
      assertTrue("Lava must visibly change between frames",changes>1000);
    }
  }
  private Bitmap render(Bitmap art,LavaSurface lava,double seconds,boolean down,float intensity) {
    Bitmap result=art.copy(Bitmap.Config.ARGB_8888,true);
    lava.draw(new Canvas(result),null,new RectF(0,0,128,128),seconds,down,intensity);
    return result;
  }
}
