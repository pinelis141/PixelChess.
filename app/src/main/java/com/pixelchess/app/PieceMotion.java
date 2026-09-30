package com.pixelchess.app;

/** Deterministic, short piece arc used by the pixel renderer. */
final class PieceMotion {
  private PieceMotion(){}
  static float progress(long elapsedMs,long durationMs){return Math.max(0f,Math.min(1f,elapsedMs/(float)Math.max(1,durationMs)));}
  static float eased(float t){return 1f-(1f-t)*(1f-t);}
  static float arc(float t,float squareSize){return (float)Math.sin(Math.PI*Math.max(0,Math.min(1,t)))*squareSize*.10f;}
  static float captureScale(float t){return Math.max(0f,1f-Math.max(0f,Math.min(1f,t)));}
}
