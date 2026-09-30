package com.pixelchess.app;

/** Bounded, continuous flicker; each torch has a separate phase and frequency. */
final class TorchMotion {
  private TorchMotion() {}
  static float brightness(int index,double seconds) {
    double p=index*2.39996323;
    return (float)(0.76+0.12*Math.sin(seconds*(1.8+index*.13)+p)
        +0.07*Math.sin(seconds*4.7+p*1.7)+0.04*Math.sin(seconds*8.3+p*.6));
  }
  static float height(int index,int column,double seconds) {
    float taper=1-Math.abs(column)*.18f;
    return taper*(float)(0.80+0.12*Math.sin(seconds*7.1+index*2.4+column*.9)
        +0.08*Math.sin(seconds*11.3+column*1.8+index));
  }
  static float sway(int index,double seconds) {
    return (float)(Math.sin(seconds*3.1+index*2.4)*.7+Math.sin(seconds*6.7+index)*.3);
  }
}
