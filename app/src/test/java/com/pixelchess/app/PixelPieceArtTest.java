package com.pixelchess.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Native graphics regression and lossless per-piece PNG export for artists. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PixelPieceArtTest {
  private static final String ROLES="PRNBQK";
  private static int pixels(Bitmap bitmap){
    int[] data=new int[bitmap.getWidth()*bitmap.getHeight()];
    bitmap.getPixels(data,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
    return Arrays.hashCode(data);
  }
  @Test public void everyCharacterAndSculptureHasItsOwnShapeAndSide(){
    for(PieceSkin skin:new PieceSkin[]{PieceSkin.GUARDIANS,PieceSkin.OBSIDIAN}){
      PixelPieceArt art=new PixelPieceArt(skin);
      for(boolean rear:new boolean[]{false,true}){
        for(boolean white:new boolean[]{false,true}){
          Set<Integer> unique=new HashSet<>();
          for(char role:ROLES.toCharArray()){
            char piece=white?role:Character.toLowerCase(role);
            Bitmap image=art.get(piece,rear,0);
            assertEquals(32,image.getWidth());assertEquals(40,image.getHeight());
            unique.add(pixels(image));
            assertNotEquals("Front and rear must be independently drawn for "+piece,
              pixels(art.get(piece,false,0)),pixels(art.get(piece,true,0)));
          }
          assertEquals("Every role must have original art in "+skin+" "+white,6,unique.size());
        }
      }
    }
  }
  @Test public void guardianIdleAndWalkFramesActuallyChangePixels(){
    PixelPieceArt art=new PixelPieceArt(PieceSkin.GUARDIANS);
    for(char piece:new char[]{'P','R','N','B','Q','K','p','r','n','b','q','k'}){
      assertNotEquals("Visible idle animation is required for "+piece,
        pixels(art.get(piece,false,0)),pixels(art.get(piece,false,1)));
      assertNotEquals("Blink/arm animation is required for "+piece,
        pixels(art.get(piece,false,0)),pixels(art.get(piece,false,3)));
    }
    PixelPieceArt rock=new PixelPieceArt(PieceSkin.OBSIDIAN);
    assertSame(rock.get('k',false,0),rock.get('k',false,3));
  }
  @Test public void perspectiveAlwaysShowsNearSideFromBehind(){
    assertTrue(PixelPieceArt.rearView(true,false));
    assertFalse(PixelPieceArt.rearView(false,false));
    assertTrue(PixelPieceArt.rearView(false,true));
    assertFalse(PixelPieceArt.rearView(true,true));
  }
  @Test public void exportRealBitmapsAndTwoContactSheets()throws Exception{
    File root=new File("build/piece-skin-export");
    assertTrue(root.isDirectory()||root.mkdirs());
    String[] names={"pawn","rook","knight","bishop","queen","king"};
    for(PieceSkin skin:new PieceSkin[]{PieceSkin.GUARDIANS,PieceSkin.OBSIDIAN}){
      PixelPieceArt art=new PixelPieceArt(skin);
      Bitmap gallery=Bitmap.createBitmap(6*108,4*112,Bitmap.Config.ARGB_8888);
      Canvas canvas=new Canvas(gallery);
      Paint paint=new Paint();paint.setAntiAlias(false);paint.setFilterBitmap(false);
      Paint text=new Paint();text.setAntiAlias(false);text.setColor(0xfff2eee1);text.setTextSize(10);
      String[] labels={"BRANCAS - FRENTE","BRANCAS - COSTAS","PRETAS - FRENTE","PRETAS - COSTAS"};
      for(int side=0;side<2;side++)for(int angle=0;angle<2;angle++){
        boolean white=side==0,rear=angle==1;
        int row=side*2+angle;
        for(int type=0;type<6;type++){
          char role=ROLES.charAt(type),piece=white?role:Character.toLowerCase(role);
          int index=type*108;int top=row*112;
          paint.setColor((row+type)%2==0?0xffb6a791:0xff41494a);
          canvas.drawRect(index,top,index+108,top+112,paint);
          paint.setColor(0xff1c242d);
          canvas.drawRect(index,top,index+108,top+17,paint);
          canvas.drawRect(index,top+96,index+108,top+112,paint);
          Bitmap still=art.get(piece,rear,0);
          paint.setColor(0xffe9e6dd);
          canvas.drawBitmap(still,null,
            new RectF(index+24,top+21,index+84,top+96),paint);
          canvas.drawText(names[type].toUpperCase(),index+8,top+12,text);
          text.setTextSize(8);
          canvas.drawText(labels[row],index+7,top+107,text);
          text.setTextSize(10);
          for(int frame=0;frame<(skin==PieceSkin.GUARDIANS?4:1);frame++){
            Bitmap sprite=art.get(piece,rear,frame);
            File folder=new File(root,skin.id+"/"+(white?"white":"black")+"/"+(rear?"rear":"front"));
            assertTrue(folder.isDirectory()||folder.mkdirs());
            File out=new File(folder,names[type]+"_"+frame+".png");
            try(FileOutputStream stream=new FileOutputStream(out)){
              assertTrue(sprite.compress(Bitmap.CompressFormat.PNG,100,stream));
            }
          }
        }
      }
      File png=new File(root,skin.id+"-gallery.png");
      try(FileOutputStream stream=new FileOutputStream(png)){
        assertTrue(gallery.compress(Bitmap.CompressFormat.PNG,100,stream));
      }
    }
    assertTrue(new File(root,"guardians-gallery.png").length()>0);
    assertTrue(new File(root,"obsidian-gallery.png").length()>0);
  }
}
