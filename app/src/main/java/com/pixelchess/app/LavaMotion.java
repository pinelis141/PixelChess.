package com.pixelchess.app;

/** Loop phase and hot-pixel selection, independent of Android rendering. */
final class LavaMotion {
  static final double PERIOD=8.0;
  static float phase(double seconds) { return (float)((seconds/PERIOD)%1.0); }
  static float furnace(double seconds) {
    double angle=2*Math.PI*phase(seconds);
    return (float)(.65+.23*Math.sin(angle)+.08*Math.sin(3*angle+.8));
  }
  static int maskAlpha(int color) {
    int a=color>>>24,r=(color>>16)&255,g=(color>>8)&255,b=color&255;
    // Reject neutral stone, bronze, ivory and transparent pixels; retain hot orange/yellow cores.
    if(a==0 || r<180 || g<35 || r-g<25 || g-b<25 || b>g*.45f) return 0;
    return Math.round(a*Math.min(1f,(r-145)/90f)*Math.min(1f,(g-b)/65f));
  }
}
