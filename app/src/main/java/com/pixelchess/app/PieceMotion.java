package com.pixelchess.app;

/** Deterministic movement paths used by the pixel renderer. */
final class PieceMotion {
  private static final float KNIGHT_SPLIT=.67f;
  private PieceMotion(){}

  static float progress(long elapsedMs,long durationMs){
    return Math.max(0f,Math.min(1f,elapsedMs/(float)Math.max(1,durationMs)));
  }

  static float eased(float t){
    t=Math.max(0f,Math.min(1f,t));
    return 1f-(1f-t)*(1f-t);
  }

  static float arc(float t,float squareSize){
    return (float)Math.sin(Math.PI*Math.max(0,Math.min(1,t)))*squareSize*.10f;
  }

  static float captureScale(float t){
    return Math.max(0f,1f-Math.max(0f,Math.min(1f,t)));
  }

  static boolean isKnightMove(int r1,int c1,int r2,int c2){
    return Math.abs(r2-r1)*Math.abs(c2-c1)==2;
  }

  static float knightColumn(int r1,int c1,int r2,int c2,float t){
    float corner=Math.abs(c2-c1)==2?c2:c1;
    return lCoordinate(c1,corner,c2,t);
  }

  static float knightRow(int r1,int c1,int r2,int c2,float t){
    float corner=Math.abs(r2-r1)==2?r2:r1;
    return lCoordinate(r1,corner,r2,t);
  }

  private static float lCoordinate(float start,float corner,float end,float t){
    t=Math.max(0f,Math.min(1f,t));
    if(t<=KNIGHT_SPLIT){
      float u=eased(t/KNIGHT_SPLIT);
      return start+(corner-start)*u;
    }
    float u=eased((t-KNIGHT_SPLIT)/(1f-KNIGHT_SPLIT));
    return corner+(end-corner)*u;
  }
}
