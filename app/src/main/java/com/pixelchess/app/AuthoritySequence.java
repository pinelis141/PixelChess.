package com.pixelchess.app;

/** Monotonic sequence for host-authoritative packets and stale-packet rejection. */
public final class AuthoritySequence {
  private long local;
  private long lastRemote=-1;

  public synchronized long next(){return ++local;}

  public synchronized boolean accept(long sequence){
    if(sequence<=lastRemote)return false;
    lastRemote=sequence;
    return true;
  }

  public synchronized void reset(){
    local=0;
    lastRemote=-1;
  }

  public synchronized long lastRemote(){return lastRemote;}
}
