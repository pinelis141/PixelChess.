package com.pixelchess.app;

import android.graphics.Bitmap;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(manifest=Config.NONE,sdk=28)
public class SpriteOutlineTest {
  @Test public void addsOnlyAThinContourAroundOpaquePixels(){
    Bitmap source=Bitmap.createBitmap(5,5,Bitmap.Config.ARGB_8888);
    source.setPixel(2,2,Color.WHITE);
    Bitmap result=SpriteOutline.thinDark(source);
    assertEquals(Color.WHITE,result.getPixel(2,2));
    assertTrue(Color.alpha(result.getPixel(2,1))>0);
    assertTrue(Color.alpha(result.getPixel(1,1))>0);
    assertEquals(0,Color.alpha(result.getPixel(0,0)));
  }

  @Test public void lightOutlinePreservesBlackSpriteInterior(){
    Bitmap source=Bitmap.createBitmap(5,5,Bitmap.Config.ARGB_8888);
    int blackDetail=Color.rgb(24,27,31);
    source.setPixel(2,2,blackDetail);
    Bitmap result=SpriteOutline.thinLight(source);
    assertEquals(blackDetail,result.getPixel(2,2));
    int edge=result.getPixel(2,1);
    assertTrue(Color.alpha(edge)>=160&&Color.alpha(edge)<=180);
    assertTrue(Color.red(edge)>Color.red(blackDetail));
    assertEquals(0,Color.alpha(result.getPixel(0,0)));
  }
}
