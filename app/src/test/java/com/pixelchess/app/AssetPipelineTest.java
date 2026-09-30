package com.pixelchess.app;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.RuntimeEnvironment;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(manifest=Config.NONE,sdk=28)
public class AssetPipelineTest {
 @Test public void registeredBoardsAreSquareAndExactlyEightCells(){Context c=RuntimeEnvironment.getApplication();for(BoardTheme t:BoardThemes.ALL){Bitmap b=BitmapFactory.decodeResource(c.getResources(),t.boardRes);assertNotNull(t.id+" must decode board resource "+t.boardRes,b);assertEquals(t.id+" board dimensions",b.getWidth(),b.getHeight());assertEquals(t.id+" width must divide into eight files",0,b.getWidth()%8);assertEquals(t.id+" height must divide into eight ranks",0,b.getHeight()%8);}}
 @Test public void decorativeFramesKeepARealTransparentCenter(){Context c=RuntimeEnvironment.getApplication();for(BoardTheme t:BoardThemes.ALL)if(t.frameRes!=0){Bitmap b=BitmapFactory.decodeResource(c.getResources(),t.frameRes);assertNotNull(t.id+" must decode frame resource "+t.frameRes,b);assertEquals(t.id+" frame must have a transparent opening",0,Color.alpha(b.getPixel(b.getWidth()/2,b.getHeight()/2)));}}
}
