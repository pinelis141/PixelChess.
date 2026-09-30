package com.pixelchess.app;

/** Versioned Bluetooth wire format. The host is the only authority for board state and clocks. */
public final class BluetoothGameProtocol {
  public static final int VERSION=2;
  private BluetoothGameProtocol(){}

  public interface Message {}

  public static final class Time implements Message {
    public final int minutes,version;
    Time(int minutes,int version){this.minutes=minutes;this.version=version;}
  }

  /** Guest -> host move request. Contains no clock data because the guest is not authoritative. */
  public static final class Play implements Message {
    public final int r1,c1,r2,c2;
    public final String promotion;
    Play(int r1,int c1,int r2,int c2,String promotion){
      this.r1=r1;this.c1=c1;this.r2=r2;this.c2=c2;this.promotion=promotion;
    }
  }

  /** Host -> guest accepted move with authoritative post-move clock snapshot. */
  public static final class Move implements Message {
    public final int r1,c1,r2,c2;
    public final String promotion;
    public final long whiteMs,blackMs,sequence;
    public final boolean whiteTurn;
    Move(int r1,int c1,int r2,int c2,String promotion,long whiteMs,long blackMs,boolean whiteTurn,long sequence){
      this.r1=r1;this.c1=c1;this.r2=r2;this.c2=c2;this.promotion=promotion;
      this.whiteMs=whiteMs;this.blackMs=blackMs;this.whiteTurn=whiteTurn;this.sequence=sequence;
    }
  }

  public static final class Sync implements Message {
    public final long whiteMs,blackMs,sequence;
    public final boolean whiteTurn;
    Sync(long whiteMs,long blackMs,boolean whiteTurn,long sequence){
      this.whiteMs=whiteMs;this.blackMs=blackMs;this.whiteTurn=whiteTurn;this.sequence=sequence;
    }
  }

  public static final class Flag implements Message {
    public final boolean loserWhite;
    public final long sequence;
    Flag(boolean loserWhite,long sequence){this.loserWhite=loserWhite;this.sequence=sequence;}
  }

  public static String time(int minutes){return "TIME,"+minutes+","+VERSION;}
  public static String play(int r1,int c1,int r2,int c2,String promotion){
    return "PLAY,"+r1+","+c1+","+r2+","+c2+","+promotion;
  }
  public static String move(int r1,int c1,int r2,int c2,String promotion,long whiteMs,long blackMs,boolean whiteTurn,long sequence){
    return "MOVE,"+r1+","+c1+","+r2+","+c2+","+promotion+","+whiteMs+","+blackMs+","+(whiteTurn?"W":"B")+","+sequence;
  }
  public static String sync(long whiteMs,long blackMs,boolean whiteTurn,long sequence){
    return "SYNC,"+whiteMs+","+blackMs+","+(whiteTurn?"W":"B")+","+sequence;
  }
  public static String flag(boolean loserWhite,long sequence){return "FLAG,"+(loserWhite?"W":"B")+","+sequence;}

  public static Message parse(String line){
    if(line==null)return null;
    String[] a=line.split(",");
    try{
      if("TIME".equals(a[0])&&(a.length==2||a.length==3))
        return new Time(Integer.parseInt(a[1]),a.length>=3?Integer.parseInt(a[2]):1);
      if(a.length>=6&&"PLAY".equals(a[0]))
        return new Play(Integer.parseInt(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]),Integer.parseInt(a[4]),a[5]);
      if(a.length>=10&&"MOVE".equals(a[0]))
        return new Move(Integer.parseInt(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]),Integer.parseInt(a[4]),
          a[5],Long.parseLong(a[6]),Long.parseLong(a[7]),"W".equals(a[8]),Long.parseLong(a[9]));
      if(a.length>=5&&"SYNC".equals(a[0]))
        return new Sync(Long.parseLong(a[1]),Long.parseLong(a[2]),"W".equals(a[3]),Long.parseLong(a[4]));
      if(a.length>=3&&"FLAG".equals(a[0]))
        return new Flag("W".equals(a[1]),Long.parseLong(a[2]));
    }catch(NumberFormatException|ArrayIndexOutOfBoundsException ignored){}
    return null;
  }
}
