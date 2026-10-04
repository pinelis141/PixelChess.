package com.pixelchess.app;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Decodes the ORIGINAL approved Pixel Chess PNG frames.
 * Atlas zip: app/src/main/assets/approved_piece_atlases.zip
 * 2 skins x 4 viewpoints; each 1024x768 PNG consists of
 * 8 animation columns and 6 piece rows, each cell exactly 128x128.
 */
final class PixelPieceArt {
  static final int WIDTH=128,HEIGHT=128,IDLE_FRAMES=8,FRAME_MS=180;
  private static final String BUNDLE="approved_piece_atlases.zip";
  private static final String ROLES="prnbqk";
  private static final HashMap<String,Bitmap> atlasCache=new HashMap<>();
  private final PieceSkin skin;
  private final Context context;
  private final HashMap<String,Bitmap> frameCache=new HashMap<>();
  PixelPieceArt(Context context,PieceSkin skin){
    if(skin==PieceSkin.CLASSIC)throw new IllegalArgumentException("Classic uses the original game sprites");
    this.context=context.getApplicationContext();this.skin=skin;
  }
  static boolean packaged(Context context){
    try(InputStream input=context.getAssets().open(BUNDLE)){
      return input.read()!=-1;
    }catch(IOException missing){return false;}
  }
  static boolean rearView(boolean pieceWhite,boolean flipped){return pieceWhite!=flipped;}
  Bitmap get(char piece,boolean rear,int frame){
    char role=Character.toLowerCase(piece);
    int row=ROLES.indexOf(role);
    if(row<0)throw new IllegalArgumentException("Unknown chess piece");
    boolean white=Character.isUpperCase(piece);
    int pose=Math.floorMod(frame,IDLE_FRAMES);
    String key=""+piece+(rear?"R":"F")+pose;
    Bitmap cached=frameCache.get(key);
    if(cached!=null)return cached;
    String name=skin.id+"/"+(white?"claras_":"escuras_")+(rear?"costas":"frente")+".png";
    Bitmap strip=loadAtlas(name);
    if(strip.getWidth()!=8*WIDTH||strip.getHeight()!=6*HEIGHT)
      throw new IllegalStateException("Approved atlas dimensions do not match: "+name);
    Bitmap result=Bitmap.createBitmap(strip,pose*WIDTH,row*HEIGHT,WIDTH,HEIGHT);
    frameCache.put(key,result);
    return result;
  }
  int cachedSprites(){return frameCache.size();}
  private Bitmap loadAtlas(String name){
    synchronized(atlasCache){
      Bitmap bmp=atlasCache.get(name);
      if(bmp!=null)return bmp;
      try(ZipInputStream zip=new ZipInputStream(context.getAssets().open(BUNDLE))){
        ZipEntry entry;
        while((entry=zip.getNextEntry())!=null){
          if(entry.getName().equals(name)){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;
            bmp=BitmapFactory.decodeStream(zip,null,options);
            if(bmp==null)throw new IllegalStateException("Cannot decode original art: "+name);
            atlasCache.put(name,bmp);
            return bmp;
          }
          zip.closeEntry();
        }
      }catch(IOException missing){
        throw new IllegalStateException("Approved original art bundle is not installed in the APK",missing);
      }
      throw new IllegalStateException("Approved sprite sheet not found: "+name);
    }
  }
}