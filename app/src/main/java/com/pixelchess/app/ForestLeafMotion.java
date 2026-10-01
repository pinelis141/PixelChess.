package com.pixelchess.app;

/** Four bounded slots, irregular quiet intervals and smooth, occasional shared wind. */
final class ForestLeafMotion {
  static final int COUNT=4,FRAMES=8;
  private static final double[] ORIGINS={.07,.91,.16,.81};
  private static double period(int i){return 17.3+i*2.71;}
  private static double duration(int i){return 9.2+i*.83;}
  private static double age(int i,double seconds){
    double value=seconds-i*2.63;
    return value-Math.floor(value/period(i))*period(i);
  }
  static boolean visible(int i,double seconds){return age(i,seconds)<duration(i);}
  static float alpha(int i,double seconds){
    double t=age(i,seconds),life=duration(i);
    return (float)Math.max(0,Math.min(1,Math.min(t/.65,(life-t)/.9)));
  }
  static float y(int i,double seconds){return (float)(-.05+1.10*age(i,seconds)/duration(i));}
  static float x(int i,double seconds){
    double t=age(i,seconds),start=seconds-t;
    double base=ORIGINS[i];
    return (float)(base+.025*Math.sin(t*.8+i*1.9)
        +.045*(windTravel(seconds)-windTravel(start)));
  }
  static int frame(int i,double seconds){return (int)(age(i,seconds)/(.14+i*.013)+i*2)%FRAMES;}
  static float angle(int i,double seconds){return (float)(18*Math.sin(age(i,seconds)*.65+i)+12*gust(seconds));}
  static float gust(double seconds){
    double cycle=Math.floor(seconds/29),t=seconds-cycle*29-8;
    if(t<=0||t>=4.5)return 0;
    double wave=Math.sin(Math.PI*t/4.5);
    return (float)(((long)cycle%2==0?1:-1)*wave*wave);
  }
  // Integral of the gust: leaves keep their displacement instead of snapping back.
  private static double windTravel(double seconds){
    long cycle=(long)Math.floor(seconds/29);
    double t=Math.max(0,Math.min(4.5,seconds-cycle*29-8));
    double travel=t/2-4.5/(4*Math.PI)*Math.sin(2*Math.PI*t/4.5);
    return cycle%2==0?travel:2.25-travel;
  }
}
