package com.pixelchess.app;
import android.graphics.Bitmap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.*;
/** Ensures original approved binary artwork, not generated stand-ins, is bundled. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PixelPieceArtTest {
 int hash(Bitmap image){
   int[] pixels=new int[image.getWidth()*image.getHeight()];
   image.getPixels(pixels,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
   return Arrays.hashCode(pixels);
 }
 @Test public void approvedBinaryArtRequired(){
   assertTrue("Bundled original art is required before release",PixelPieceArt.packaged(RuntimeEnvironment.getApplication()));
 }
 @Test public void allOriginalFramesViewsColorsAreDifferent(){
   assertTrue(PixelPieceArt.packaged(RuntimeEnvironment.getApplication()));
   for(PieceSkin skin:new PieceSkin[]{PieceSkin.MEDIEVAL,PieceSkin.FOREST}){
     PixelPieceArt art=new PixelPieceArt(RuntimeEnvironment.getApplication(),skin);
     for(boolean white:new boolean[]{true,false})for(boolean rear:new boolean[]{true,false}){
       HashSet<Integer> characters=new HashSet<>();
       for(char symbol:"PRNBQK".toCharArray()){
         char piece=white?symbol:Character.toLowerCase(symbol);
         Bitmap frame=art.get(piece,rear,0);
         assertEquals(128,frame.getWidth());assertEquals(128,frame.getHeight());
         characters.add(hash(frame));
         assertNotEquals(hash(art.get(piece,false,0)),hash(art.get(piece,true,0)));
         assertNotEquals(hash(frame),hash(art.get(piece,rear,1)));
       }
       assertEquals(6,characters.size());
     }
   }
 }
 @Test public void perspectiveFollowsPlayersBoardSide(){
   assertTrue(PixelPieceArt.rearView(true,false));
   assertFalse(PixelPieceArt.rearView(false,false));
   assertTrue(PixelPieceArt.rearView(false,true));
   assertFalse(PixelPieceArt.rearView(true,true));
 }
}