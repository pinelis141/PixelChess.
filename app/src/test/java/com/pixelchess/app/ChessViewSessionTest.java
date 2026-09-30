package com.pixelchess.app;

import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowSystemClock;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class ChessViewSessionTest {
  static class Actions implements ChessView.Actions {
    int desync;
    public void requestMove(int a,int b,int c,int d,String p){}
    public void sendAuthorityMove(int a,int b,int c,int d,String p){}
    public void sendClockSync(){} public void sendFlag(boolean white){} public void sendReject(){}
    public boolean acceptSequence(long s){return true;} public void playSound(boolean c,boolean t){}
    public void desynchronized(){desync++;}
  }
  @Test public void disconnectBlocksMovesAndResumePreservesClockBudget(){
    ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,true,true,new Actions());
    view.matchClock.sync(12000,15000,System.currentTimeMillis());view.applyConnectionLost();long left=view.matchClock.whiteMs();
    ShadowSystemClock.advanceBy(Duration.ofSeconds(30));view.ticker.run();assertEquals(left,view.matchClock.whiteMs());assertFalse(view.move(6,4,4,4,"-"));
    view.restore(new ChessGame(),left,15000);assertFalse(view.paused());assertTrue(view.move(6,4,4,4,"-"));assertEquals(left,view.matchClock.whiteMs());view.clock.removeCallbacksAndMessages(null);
  }
  @Test public void turnDisagreementRequiresFullResyncInsteadOfChangingBoardTurn(){
    Actions actions=new Actions();ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,true,false,actions);
    BluetoothGameProtocol.Sync sync=(BluetoothGameProtocol.Sync)BluetoothGameProtocol.parse(BluetoothGameProtocol.sync(5000,5000,false,1));view.applyAuthoritySync(sync);
    assertEquals(1,actions.desync);assertTrue(view.gameState.whiteTurn());view.clock.removeCallbacksAndMessages(null);
  }
}
