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
  @Test public void surfaceFeaturesActuallyTravelDownstream() {
    Bitmap art=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
    art.eraseColor(0xffea6c12);
    LavaSurface lava=new LavaSurface(art);
    for(boolean down:new boolean[]{true,false}) {
      Bitmap first=render(art,lava,0,down,1),next=render(art,lava,.5,down,1);
      // 48 source px/sec: after half a second the SAME surface feature must move 24 pixels.
      int stationaryDifferences=0;
      for(int y=0;y<100;y++)for(int x=0;x<100;x++) {
        assertEquals(first.getPixel(x,y),next.getPixel(x+(down?0:24),y+(down?24:0)));
        if(first.getPixel(x,y)!=next.getPixel(x,y))stationaryDifferences++;
      }
      assertTrue(stationaryDifferences>5000);
    }
  }
  @Test public void previewApprovedFrameAtPhoneScale() throws Exception {
    Bitmap art=BitmapFactory.decodeFile("src/main/res/drawable-nodpi/forge_frame.webp");
    assertNotNull(art);
    LavaSurface lava=new LavaSurface(art);
    Bitmap preview=Bitmap.createBitmap(384,500,Bitmap.Config.ARGB_8888);
    Canvas c=new Canvas(preview);Rect src=new Rect(79,205,204,1049);
    for(int i=0;i<8;i++) {
      RectF dst=new RectF(i*48,0,(i+1)*48,500);
      c.drawBitmap(art,src,dst,null);lava.draw(c,src,dst,i*.5,true,.95f);
    }
    java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
    preview.compress(Bitmap.CompressFormat.PNG,100,bytes);
    System.out.println("LAVA_PREVIEW="+java.util.Base64.getEncoder().encodeToString(bytes.toByteArray()));
  }
  private Bitmap render(Bitmap art,LavaSurface lava,double seconds,boolean down,float intensity) {
    Bitmap result=art.copy(Bitmap.Config.ARGB_8888,true);
    lava.draw(new Canvas(result),null,new RectF(0,0,128,128),seconds,down,intensity);
    return result;
  }
}
