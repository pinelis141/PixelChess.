package com.pixelchess.app;

import java.time.Duration;
import org.junit.Test;
import android.view.MotionEvent;
import android.os.SystemClock;
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
    view.matchClock.sync(12000,15000,SystemClock.elapsedRealtime());view.applyConnectionLost();long left=view.matchClock.whiteMs();
    ShadowSystemClock.advanceBy(Duration.ofSeconds(30));view.ticker.run();assertEquals(left,view.matchClock.whiteMs());assertFalse(view.move(6,4,4,4,"-"));
    view.restore(new ChessGame(),left,15000);assertFalse(view.paused());assertTrue(view.move(6,4,4,4,"-"));assertEquals(left,view.matchClock.whiteMs());view.clock.removeCallbacksAndMessages(null);
  }
  @Test public void turnDisagreementRequiresFullResyncInsteadOfChangingBoardTurn(){
    Actions actions=new Actions();ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,true,false,actions);
    BluetoothGameProtocol.Sync sync=(BluetoothGameProtocol.Sync)BluetoothGameProtocol.parse(BluetoothGameProtocol.sync(5000,5000,false,1));view.applyAuthoritySync(sync);
    assertEquals(1,actions.desync);assertTrue(view.gameState.whiteTurn());view.clock.removeCallbacksAndMessages(null);
  }
  private void tap(ChessView view,int row,int col){
    float den=view.getResources().getDisplayMetrics().density;
    view.boardGeometry.update(view.getWidth(),view.getHeight(),den,view.themeRenderer.hasScene());
    float square=view.boardGeometry.size/8f;
    float x=view.boardGeometry.left+(col+.5f)*square;
    float y=view.boardGeometry.top+(row+.5f)*square;
    MotionEvent event=MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,x,y,0);
    view.onTouchEvent(event);
    event.recycle();
  }

  @Test public void localPlayersCanMoveRapidlyBeforePreviousAnimationEnds(){
    ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,false,true,new Actions());
    view.layout(0,0,1080,1920);

    assertTrue(view.move(6,4,4,4,"-")); // white e2-e4 starts animation
    assertTrue(view.animating);
    assertFalse(view.gameState.whiteTurn());

    tap(view,1,4); // black e7 must still be selectable during white animation
    assertEquals(1,view.sr);
    assertEquals(4,view.sc);
    tap(view,3,4); // black e7-e5
    assertEquals("p",view.gameState.pieceAt(3,4));
    assertTrue(view.gameState.whiteTurn());

    tap(view,7,6); // white g1 must be selectable during black animation
    assertEquals(7,view.sr);
    assertEquals(6,view.sc);
    tap(view,5,5); // white Ng1-f3
    assertEquals("N",view.gameState.pieceAt(5,5));
    assertFalse(view.gameState.whiteTurn());
    view.clock.removeCallbacksAndMessages(null);
  }

  @Test public void timeoutClearsSelectionAndStopsAnimation(){
    ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,false,true,new Actions());
    view.sr=6;view.sc=4;view.animating=true;view.capturedPiece="p";
    long now=SystemClock.elapsedRealtime();
    view.matchClock.sync(0,15000,now);
    view.ticker.run();
    assertTrue(view.gameState.gameOver());
    assertEquals("TEMPO • PRETAS VENCEM",view.gameState.status());
    assertEquals(-1,view.sr);assertEquals(-1,view.sc);
    assertFalse(view.animating);assertNull(view.capturedPiece);
    view.clock.removeCallbacksAndMessages(null);
  }

}
