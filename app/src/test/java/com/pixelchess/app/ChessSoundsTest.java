package com.pixelchess.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChessSoundsTest {
  static class Fake implements ChessSounds.Player {
    int resource,plays,stops,releases;boolean broken;
    public void play(int value){if(broken)throw new IllegalStateException("audio unavailable");resource=value;plays++;}
    public void stop(){stops++;}
    public void release(){releases++;}
  }
  @Test public void routesMoveCaptureAndTerminalToDistinctStoneAssets(){
    Fake player=new Fake();ChessSounds sounds=new ChessSounds(player);
    sounds.move(false,false);assertEquals(R.raw.stone_move,player.resource);
    sounds.move(true,false);assertEquals(R.raw.stone_capture,player.resource);
    sounds.move(true,true);assertEquals(R.raw.stone_terminal,player.resource);
    assertEquals(3,player.plays);sounds.release();
  }
  @Test public void pauseStopsSoundAndResumeDoesNotReplayIt(){
    Fake player=new Fake();ChessSounds sounds=new ChessSounds(player);
    sounds.move(false,false);sounds.setForeground(false);assertEquals(1,player.stops);
    sounds.move(true,false);assertEquals(1,player.plays);
    sounds.setForeground(true);assertEquals(1,player.plays);
    sounds.move(true,false);assertEquals(2,player.plays);sounds.release();
  }
  @Test public void releaseIsIdempotentAndPreventsLaterPlayback(){
    Fake player=new Fake();ChessSounds sounds=new ChessSounds(player);
    sounds.release();sounds.release();sounds.setForeground(true);sounds.move(false,false);
    assertEquals(1,player.releases);assertEquals(0,player.plays);
  }
  @Test public void playbackFailureDoesNotCrashOrRepeatedlyRetry(){
    Fake player=new Fake();player.broken=true;ChessSounds sounds=new ChessSounds(player);
    sounds.move(false,false);sounds.move(true,false);assertEquals(1,player.releases);assertEquals(0,player.plays);
  }
}
