package com.pixelchess.app;

/** Pure match clock state. Scheduling and rendering stay outside this class. */
public final class GameClock {
  public static final class Tick {
    public final boolean timedOut;
    public final boolean loserWhite;
    Tick(boolean timedOut,boolean loserWhite){this.timedOut=timedOut;this.loserWhite=loserWhite;}
  }

  private long whiteMs,blackMs,lastTick,lastSyncSent;

  public GameClock(int minutes,long now){reset(minutes,now);}

  public void reset(int minutes,long now){
    whiteMs=blackMs=minutes*60000L;
    lastTick=lastSyncSent=now;
  }

  public Tick tick(boolean whiteTurn,long now){
    long dt=Math.max(0,now-lastTick);
    lastTick=now;
    if(whiteTurn)whiteMs-=dt;else blackMs-=dt;
    if(whiteMs<=0){whiteMs=0;return new Tick(true,true);}
    if(blackMs<=0){blackMs=0;return new Tick(true,false);}
    return new Tick(false,false);
  }

  public void markTurnChanged(long now){lastTick=now;}

  public boolean shouldSync(long now,long intervalMs){
    if(now-lastSyncSent<intervalMs)return false;
    lastSyncSent=now;
    return true;
  }

  public void sync(long white,long black,long now){
    whiteMs=Math.max(0,white);
    blackMs=Math.max(0,black);
    lastTick=now;
  }

  public void flag(boolean loserWhite){
    if(loserWhite)whiteMs=0;else blackMs=0;
  }

  public long whiteMs(){return whiteMs;}
  public long blackMs(){return blackMs;}
}
