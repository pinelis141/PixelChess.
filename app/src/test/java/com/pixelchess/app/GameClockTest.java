package com.pixelchess.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class GameClockTest {
  @Test public void onlyActiveSideLosesTime(){
    GameClock clock=new GameClock(1,1000);
    GameClock.Tick tick=clock.tick(true,2500);
    assertFalse(tick.timedOut);
    assertEquals(58500,clock.whiteMs());
    assertEquals(60000,clock.blackMs());
  }

  @Test public void timeoutIdentifiesLoser(){
    GameClock clock=new GameClock(1,0);
    GameClock.Tick tick=clock.tick(false,61000);
    assertTrue(tick.timedOut);
    assertFalse(tick.loserWhite);
    assertEquals(0,clock.blackMs());
  }

  @Test public void remoteSyncResetsClockBaseline(){
    GameClock clock=new GameClock(10,1000);
    clock.sync(12000,15000,5000);
    clock.tick(true,5500);
    assertEquals(11500,clock.whiteMs());
    assertEquals(15000,clock.blackMs());
  }
}
