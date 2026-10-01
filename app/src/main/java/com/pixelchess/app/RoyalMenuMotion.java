package com.pixelchess.app;

/** Independent candle phases, with a clearly visible but bounded halo pulse. */
final class RoyalMenuMotion {
  private RoyalMenuMotion(){}
  static float pulse(int candle,double seconds){
    return (float)(.5+.3*Math.sin(seconds*4.7+candle*1.9)+.16*Math.sin(seconds*8.3+candle*2.7));
  }
  static float haloRadius(int candle,double seconds,float flameHeight){return flameHeight*(1.6f+pulse(candle,seconds)*.85f);}
  static float haloAlpha(int candle,double seconds){return .2f+pulse(candle,seconds)*.28f;}
}
