package com.pixelchess.app;

import android.graphics.*;
import android.os.Looper;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE) @LooperMode(LooperMode.Mode.PAUSED)
public class ForestLeafRenderingTest {
  @Test public void packagedAtlasHasEightDifferentTransparentFrames(){
    BitmapFactory.Options opts=new BitmapFactory.Options();opts.inScaled=false;opts.inSampleSize=8;
    Bitmap strip=BitmapFactory.decodeResource(RuntimeEnvironment.getApplication().getResources(),R.drawable.forest_leaf_strip,opts);
    assertNotNull(strip);assertTrue(strip.getAllocationByteCount()<150000);
    java.util.Set<Integer> hashes=new java.util.HashSet<>();
    for(int i=0;i<8;i++){
      int l=(i%4)*strip.getWidth()/4,t=(i/4)*strip.getHeight()/2;
      int r=(i%4+1)*strip.getWidth()/4,b=(i/4+1)*strip.getHeight()/2;
      assertEquals(0,Color.alpha(strip.getPixel(l,t)));
      int hash=1,visible=0;for(int y=t;y<b;y++)for(int x=l;x<r;x++){
        int pixel=strip.getPixel(x,y);hash=31*hash+pixel;if(Color.alpha(pixel)>128)visible++;
      }
      assertTrue(visible>30);hashes.add(hash);
    }
    assertEquals(8,hashes.size());
  }
  @Test public void leavesActuallyRenderMoveAndNeverCoverBoardOrClockText(){
    ForestLeafRenderer leaves=new ForestLeafRenderer(RuntimeEnvironment.getApplication().getResources());
    SceneGeometry geometry=new SceneGeometry();geometry.update(393,820,1,false);
    int visible=0,differences=0;Bitmap previous=null;
    for(double seconds:new double[]{1,1.2,8.5,9,10,15,30,40}){
      Bitmap bitmap=Bitmap.createBitmap(393,820,Bitmap.Config.ARGB_8888);
      leaves.draw(new Canvas(bitmap),geometry,393,1,seconds);
      for(int y=0;y<820;y++)for(int x=0;x<393;x++){
        int pixel=bitmap.getPixel(x,y);if(Color.alpha(pixel)>0)visible++;
        if(previous!=null && pixel!=previous.getPixel(x,y))differences++;
        if((x>=geometry.left && x<geometry.left+geometry.size && y>=geometry.top && y<geometry.top+geometry.size)
            || (x>=83 && x<310 && y>=geometry.top-146 && y<geometry.top-46))
          assertEquals("Scenery must not cover board/clock",0,Color.alpha(pixel));
      }
      if(previous!=null)previous.recycle();previous=bitmap;
    }
    assertTrue("Leaves must be visible",visible>100);
    assertTrue("Different times must show movement",differences>100);
  }
  @Test public void disablingEffectsAndOtherSkinsNeverLoadLeaves()throws Exception{
    java.lang.reflect.Field field=BoardThemeRenderer.class.getDeclaredField("leaves");field.setAccessible(true);
    for(BoardTheme theme:BoardThemes.ALL){
      BoardThemeRenderer disabled=new BoardThemeRenderer(RuntimeEnvironment.getApplication().getResources(),theme,false);
      assertNull(field.get(disabled));assertFalse(disabled.animated());
      BoardThemeRenderer enabled=new BoardThemeRenderer(RuntimeEnvironment.getApplication().getResources(),theme,true);
      assertEquals(theme==BoardThemes.FOREST,field.get(enabled)!=null);
    }
  }
  @Test public void pausingEffectsFreezesLeafTimeAndResumeDoesNotJump()throws Exception{
    BoardThemeRenderer renderer=new BoardThemeRenderer(RuntimeEnvironment.getApplication().getResources(),BoardThemes.FOREST);
    java.lang.reflect.Method time=BoardThemeRenderer.class.getDeclaredMethod("leafSeconds");time.setAccessible(true);
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
    renderer.setAnimationActive(false);double before=(double)time.invoke(renderer);
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(20));
    assertEquals(before,(double)time.invoke(renderer),.0001);
    renderer.setAnimationActive(true);assertEquals(before,(double)time.invoke(renderer),.0001);
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
    assertEquals(before+1,(double)time.invoke(renderer),.0001);
  }
  @Test public void previewForestAtPhoneScale()throws Exception{
    ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.FOREST,5,false,true,new ChessViewSessionTest.Actions());
    view.layout(0,0,393,820);
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
    Bitmap preview=Bitmap.createBitmap(393,820,Bitmap.Config.ARGB_8888);view.draw(new Canvas(preview));
    java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();preview.compress(Bitmap.CompressFormat.PNG,100,out);
    System.out.println("FOREST_LEAF_PREVIEW="+java.util.Base64.getEncoder().encodeToString(out.toByteArray()));
    view.clock.removeCallbacksAndMessages(null);
  }
}
