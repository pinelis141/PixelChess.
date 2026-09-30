package com.pixelchess.app;

/** Encoding and parsing for the existing PixelChess Bluetooth wire format. */
public final class BluetoothGameProtocol {
  private BluetoothGameProtocol(){}

  public interface Message {}
  public static final class Time implements Message {
    public final int minutes;
    Time(int minutes){this.minutes=minutes;}
  }
  public static final class Move implements Message {
    public final int r1,c1,r2,c2;
    public final String promotion;
    public final long whiteMs,blackMs;
    Move(int r1,int c1,int r2,int c2,String promotion,long whiteMs,long blackMs){
      this.r1=r1;this.c1=c1;this.r2=r2;this.c2=c2;this.promotion=promotion;this.whiteMs=whiteMs;this.blackMs=blackMs;
    }
  }
  public static final class Sync implements Message {
    public final long whiteMs,blackMs;
    public final boolean whiteTurn;
    Sync(long whiteMs,long blackMs,boolean whiteTurn){this.whiteMs=whiteMs;this.blackMs=blackMs;this.whiteTurn=whiteTurn;}
  }
  public static final class Flag implements Message {
    public final boolean loserWhite;
    Flag(boolean loserWhite){this.loserWhite=loserWhite;}
  }

  public static String time(int minutes){return "TIME,"+minutes;}
  public static String move(int r1,int c1,int r2,int c2,String promotion,long whiteMs,long blackMs){
    return "MOVE,"+r1+","+c1+","+r2+","+c2+","+promotion+","+whiteMs+","+blackMs;
  }
  public static String sync(long whiteMs,long blackMs,boolean whiteTurn){
    return "SYNC,"+whiteMs+","+blackMs+","+(whiteTurn?"W":"B");
  }
  public static String flag(boolean loserWhite){return "FLAG,"+(loserWhite?"W":"B");}

  public static Message parse(String line){
    if(line==null)return null;
    String[] a=line.split(",");
    try{
      if(a.length==2&&"TIME".equals(a[0]))return new Time(Integer.parseInt(a[1]));
      if(a.length>=8&&"MOVE".equals(a[0]))return new Move(
        Integer.parseInt(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]),Integer.parseInt(a[4]),
        a[5],Long.parseLong(a[6]),Long.parseLong(a[7]));
      if(a.length>=4&&"SYNC".equals(a[0]))return new Sync(Long.parseLong(a[1]),Long.parseLong(a[2]),"W".equals(a[3]));
      if(a.length>=2&&"FLAG".equals(a[0]))return new Flag("W".equals(a[1]));
    }catch(NumberFormatException ignored){}
    return null;
  }
}
