package com.pixelchess.app;

import android.graphics.Bitmap;
import android.graphics.Color;

/** Adds a one-pixel contrast contour around opaque pixel-art sprites without filtering. */
final class SpriteOutline {
  private static final int EDGE=Color.rgb(32,35,38);
  private SpriteOutline(){}

  static Bitmap thinDark(Bitmap source){
    if(source==null)return null;
    int w=source.getWidth(),h=source.getHeight(),n=w*h;
    int[] src=new int[n],out=new int[n];
    source.getPixels(src,0,w,0,0,w,h);
    System.arraycopy(src,0,out,0,n);
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){
      int i=y*w+x;
      if(Color.alpha(src[i])>10)continue;
      boolean near=false;
      for(int dy=-1;dy<=1&&!near;dy++)for(int dx=-1;dx<=1;dx++){
        if(dx==0&&dy==0)continue;
        int xx=x+dx,yy=y+dy;
        if(xx>=0&&xx<w&&yy>=0&&yy<h&&Color.alpha(src[yy*w+xx])>10){near=true;break;}
      }
      if(near)out[i]=EDGE;
    }
    Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
    result.setPixels(out,0,w,0,0,w,h);
    return result;
  }
}
