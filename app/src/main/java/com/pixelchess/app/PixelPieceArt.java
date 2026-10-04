package com.pixelchess.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.HashMap;

/**
 * Original 32 x 40 pixel sprites. Shapes are drawn at native pixel resolution
 * and enlarged with nearest-neighbour filtering by ChessView.
 *
 * Every non-classic set has twelve color-specific figures and separate front
 * (opponent's rank) / rear (player's near rank) views. Guardians contain four
 * hand-directed idle poses; Obsidian sculptures are intentionally motionless.
 * The cache is visual-only and has no relationship with the chess engine.
 */
final class PixelPieceArt {
  static final int WIDTH=32, HEIGHT=40, IDLE_FRAMES=4;
  private final PieceSkin skin;
  private final HashMap<String,Bitmap> cache=new HashMap<>();
  PixelPieceArt(PieceSkin skin){
    if(skin==PieceSkin.CLASSIC)throw new IllegalArgumentException("Classic uses original PNGs");
    this.skin=skin;
  }
  Bitmap get(char piece,boolean rear,int frame){
    boolean white=Character.isUpperCase(piece);
    char role=Character.toLowerCase(piece);
    if("prnbqk".indexOf(role)<0)throw new IllegalArgumentException("Unknown chess piece");
    int pose=skin==PieceSkin.GUARDIANS?Math.floorMod(frame,IDLE_FRAMES):0;
    String key=""+piece+(rear?"R":"F")+pose;
    Bitmap value=cache.get(key);
    if(value==null){
      value=render(role,white,rear,pose);
      value=white?SpriteOutline.thinDark(value):SpriteOutline.thinLight(value);
      cache.put(key,value);
    }
    return value;
  }
  int cachedSprites(){return cache.size();}

  private Bitmap render(char role,boolean white,boolean rear,int pose){
    Bitmap bmp=Bitmap.createBitmap(WIDTH,HEIGHT,Bitmap.Config.ARGB_8888);
    Painter p=new Painter(new Canvas(bmp));
    Palette color=skin==PieceSkin.GUARDIANS?Palette.guardian(white):Palette.stone(white);
    if(skin==PieceSkin.GUARDIANS)guardian(p,color,role,rear,pose);
    else obsidian(p,color,role,rear);
    return bmp;
  }
  private static void guardian(Painter p,Palette c,char role,boolean rear,int pose){
    int breath=pose==1?-1:0;
    // Boots, cape, armoured torso. Near-side view reveals cloak rather than face.
    p.r(8,35,7,3,c.edge);p.r(17,35,7,3,c.edge);
    p.r(9,35,5,2,c.dark);p.r(18,35,5,2,c.dark);
    p.poly(c.edge,7,22+breath,12,17+breath,21,17+breath,26,23+breath,26,34,6,34);
    if(rear){
      p.poly(c.fabric,9,23+breath,13,19+breath,20,19+breath,24,23+breath,22,33,10,33);
      p.line(16,22+breath,16,32,c.trim);
      p.r(11,20+breath,11,3,c.metal);
    }else{
      p.poly(c.fabric,9,22+breath,13,19+breath,20,19+breath,23,22+breath,22,32,10,32);
      p.r(12,21+breath,9,9,c.metal);
      p.r(14,23+breath,5,3,c.light);
      p.r(12,30,9,2,c.trim);
    }
    p.r(6,23+breath,4,9,c.dark);p.r(23,23+breath,4,9,c.dark);
    p.r(6,23+breath,3,4,c.metal);p.r(24,23+breath,3,4,c.metal);
    if(pose==3){p.r(5,22,4,5,c.metal);p.r(25,22,3,5,c.trim);}
    // Each chess role is a different fantasy character, not a palette swap.
    switch(role){
      case 'p':
        p.r(12,12+breath,9,9,c.edge);
        p.r(13,12+breath,7,8,c.metal);
        p.r(14,13+breath,5,2,c.light);
        if(rear){p.r(13,15+breath,7,4,c.dark);}
        else {p.r(14,17+breath,5,2,c.skin);eyes(p,c,pose,15,17+breath);}
        p.line(27,10,27,35,c.edge);p.line(26,10,26,34,c.trim);
        p.poly(c.light,25,7,28,7,29,12,26,14,24,12);
        break;
      case 'r':
        // Tower guard: battlement helmet and large shield.
        p.r(10,10+breath,13,11,c.edge);
        p.r(11,8+breath,3,5,c.metal);p.r(16,8+breath,3,5,c.metal);p.r(20,8+breath,3,5,c.metal);
        p.r(11,13+breath,11,7,c.metal);
        if(rear)p.r(12,15+breath,9,4,c.dark);
        else {p.r(13,16+breath,7,2,c.skin);eyes(p,c,pose,14,16+breath);}
        p.r(4,25,9,10,c.edge);p.r(5,26,7,8,c.metal);
        p.r(7,28,3,5,c.trim);p.r(8,27,1,7,c.light);
        break;
      case 'n':
        // Rider with a forward-facing horse profile, distinct from the guard.
        p.poly(c.edge,9,17+breath,11,10+breath,19,8+breath,23,12+breath,26,12+breath,28,20+breath,23,24,12,22);
        p.poly(c.metal,11,17+breath,13,12+breath,19,10+breath,22,14+breath,25,15+breath,26,19+breath,21,21,13,20);
        p.r(19,14+breath,2,2,c.trim);p.r(25,18+breath,2,2,c.skin);
        p.r(9,11+breath,4,5,c.dark);
        p.r(8,10+breath,3,4,c.trim);
        if(rear){p.r(13,14+breath,7,4,c.fabric);}
        else {p.r(13,15+breath,4,3,c.skin);eyes(p,c,pose,15,15+breath);}
        break;
      case 'b':
        // Hooded mage with staff and luminous rune.
        p.poly(c.edge,16,6+breath,23,15+breath,22,21,10,21,9,15+breath);
        p.poly(c.fabric,16,8+breath,21,15+breath,20,20,12,20,11,15+breath);
        if(rear){p.r(14,15+breath,5,4,c.dark);}
        else {p.r(13,14+breath,7,5,c.skin);eyes(p,c,pose,14,15+breath);}
        p.line(27,9,27,35,c.trim);
        p.poly(c.light,27,4,30,8,27,12,24,8);
        p.r(13,26,7,3,c.trim);
        break;
      case 'q':
        // Empress with five-tip tiara and layered dress.
        p.poly(c.edge,10,16+breath,9,7+breath,13,10+breath,16,5+breath,19,10+breath,23,7+breath,22,16+breath);
        p.poly(c.trim,11,13+breath,11,9+breath,14,12+breath,16,7+breath,18,12+breath,21,9+breath,21,13+breath);
        p.r(12,15+breath,9,6,c.skin);
        if(rear)p.r(12,16+breath,9,6,c.dark);
        else eyes(p,c,pose,14,17+breath);
        p.poly(c.trim,12,22+breath,16,20+breath,20,22+breath,24,31,8,31);
        p.r(15,23,3,6,c.light);
        break;
      case 'k':
        // Crown, beard and ceremonial blade identify the sovereign.
        p.poly(c.edge,9,14+breath,9,6+breath,13,9+breath,16,4+breath,19,9+breath,23,6+breath,23,14+breath);
        p.poly(c.trim,11,12+breath,11,9+breath,14,11+breath,16,7+breath,18,11+breath,21,9+breath,21,12+breath);
        p.r(12,14+breath,9,8,c.skin);
        if(rear)p.r(12,15+breath,9,8,c.dark);
        else {eyes(p,c,pose,14,16+breath);p.poly(c.light,14,20+breath,19,20+breath,18,25,16,27,14,25);}
        p.r(26,21,2,14,c.trim);p.poly(c.light,27,13,30,20,27,24,24,20);
        p.r(14,27,5,4,c.trim);
        break;
      default: throw new IllegalArgumentException("Missing character");
    }
    // Cloak clasp is at the back and heraldic crest faces the opponent.
    if(rear){p.r(14,25,5,2,c.light);}
    else if(role!='n'){p.r(15,24,3,3,c.trim);}
  }
  private static void eyes(Painter p,Palette c,int pose,int x,int y){
    if(pose==2)p.r(x,y,4,1,c.dark); // blink on one of four authored frames
    else {p.r(x,y,1,1,c.dark);p.r(x+3,y,1,1,c.dark);}
  }
  private static void obsidian(Painter p,Palette c,char role,boolean rear){
    // Carved sculptures use hard facets and glowing mineral seams, not humanoids.
    p.poly(c.edge,8,34,24,34,27,37,5,37);
    p.r(7,35,19,2,c.stone);p.r(10,32,13,3,c.dark);
    switch(role){
      case 'p':
        p.poly(c.edge,16,10,22,16,22,23,25,29,23,34,9,34,7,29,10,23,10,16);
        p.poly(c.stone,16,12,20,17,20,24,23,30,11,30,12,23,12,17);
        p.poly(c.light,16,14,19,19,16,23,13,19);
        break;
      case 'r':
        p.poly(c.edge,7,11,11,11,11,7,15,7,15,11,18,11,18,7,22,7,22,11,26,11,23,33,9,33);
        p.poly(c.stone,10,13,23,13,21,30,12,30);
        p.r(13,17,7,3,c.light);p.r(14,23,5,7,c.dark);
        break;
      case 'n':
        p.poly(c.edge,10,11,15,6,22,8,23,12,27,16,26,21,22,24,23,32,9,32,11,24,7,20);
        p.poly(c.stone,12,12,16,9,21,10,21,15,24,18,20,22,20,30,12,30,13,24,9,19);
        p.r(19,13,3,2,c.light);p.r(23,19,3,2,c.trim);
        break;
      case 'b':
        p.poly(c.edge,16,4,22,13,21,20,26,29,23,34,9,34,6,29,11,20,10,13);
        p.poly(c.stone,16,8,20,14,19,23,23,29,11,29,14,23,13,14);
        p.poly(c.light,16,13,19,17,16,22,13,17);
        p.r(15,24,3,5,c.trim);
        break;
      case 'q':
        p.poly(c.edge,5,11,10,16,11,7,16,13,21,7,22,16,27,11,24,25,25,34,7,34,8,25);
        p.poly(c.stone,9,15,13,19,16,16,20,19,23,15,22,30,10,30);
        p.poly(c.light,16,18,20,23,16,28,12,23);
        p.r(14,11,4,4,c.trim);
        break;
      case 'k':
        p.poly(c.edge,5,13,9,15,10,6,15,11,16,3,17,11,22,6,23,15,27,13,24,23,25,34,7,34,8,23);
        p.poly(c.stone,9,16,13,17,16,13,19,17,23,16,22,30,10,30);
        p.r(15,4,2,12,c.light);p.r(11,8,10,2,c.trim);
        p.poly(c.light,16,20,20,24,16,29,12,24);
        break;
      default: throw new IllegalArgumentException("Missing sculpture");
    }
    // Rear view: hide front gem and display split crystal ridge.
    if(rear){
      p.poly(c.dark,14,19,18,19,20,27,17,32,12,27);
      p.line(16,21,16,30,c.trim);
    }else{
      p.r(15,24,3,4,c.trim);
    }
    p.r(10,33,13,2,c.light);
  }
  private static final class Palette {
    final int edge,dark,metal,light,trim,skin,fabric,stone;
    Palette(int edge,int dark,int metal,int light,int trim,int skin,int fabric,int stone){
      this.edge=edge;this.dark=dark;this.metal=metal;this.light=light;
      this.trim=trim;this.skin=skin;this.fabric=fabric;this.stone=stone;
    }
    static Palette guardian(boolean white){
      return white
        ?new Palette(0xff292f39,0xff455268,0xffcbd1be,0xfff8e8c1,0xffd5a64e,0xffdbb698,0xff4c7066,0)
        :new Palette(0xffc9c4b7,0xff212936,0xff4f5268,0xff99abbb,0xffdf8570,0xffb18a80,0xff443448,0);
    }
    static Palette stone(boolean white){
      return white
        ?new Palette(0xff29394b,0xff40586b,0,0xffe9faff,0xff70d9ec,0,0,0xffb7ccd8)
        :new Palette(0xffc7cfdf,0xff212432,0,0xffffda98,0xffff7249,0,0,0xff52586a);
    }
  }
  private static final class Painter {
    final Canvas canvas;final Paint paint=new Paint();
    Painter(Canvas canvas){this.canvas=canvas;paint.setAntiAlias(false);paint.setFilterBitmap(false);}
    void r(int x,int y,int w,int h,int color){
      paint.setColor(color);canvas.drawRect(x,y,x+w,y+h,paint);
    }
    void poly(int color,int... xy){
      paint.setColor(color);Path path=new Path();path.moveTo(xy[0],xy[1]);
      for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
      path.close();canvas.drawPath(path,paint);
    }
    void line(int x1,int y1,int x2,int y2,int color){
      int dx=Math.abs(x2-x1),sx=x1<x2?1:-1;
      int dy=-Math.abs(y2-y1),sy=y1<y2?1:-1,err=dx+dy;
      while(true){r(x1,y1,1,1,color);if(x1==x2&&y1==y2)break;
        int e=err*2;if(e>=dy){err+=dy;x1+=sx;}if(e<=dx){err+=dx;y1+=sy;}}
    }
  }
}
