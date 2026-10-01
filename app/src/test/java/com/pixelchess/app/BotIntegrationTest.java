package com.pixelchess.app;

import com.pixelchess.app.bot.*;
import java.time.Duration;
import java.util.concurrent.*;
import android.os.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.shadows.ShadowSystemClock;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class BotIntegrationTest {
  static class Fake implements ChessEngine {
    volatile boolean closed;volatile int searches;volatile String command;
    String reply;boolean fail,blocking;
    final CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
    Fake(String reply){this.reply=reply;}
    public String search(EnginePosition p,BotDifficulty d,long ms)throws Exception{
      searches++;command=p.command;entered.countDown();
      if(blocking)release.await();if(fail)throw new java.io.IOException("broken");return reply;
    }
    public void close(){closed=true;release.countDown();}
  }
  static class Actions extends ChessViewSessionTest.Actions {
    int failures;public void botFailed(String message){failures++;}
  }
  ChessView view;Fake engine;Actions actions;
  @After public void cleanup(){if(view!=null){view.stopBot();view.clock.removeCallbacksAndMessages(null);}}
  void create(ChessGame state,boolean human,String reply){
    actions=new Actions();engine=new Fake(reply);
    view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,false,true,actions);
    view.restore(state,300000,300000);view.configureBot(human,BotDifficulty.NORMAL,()->engine);
  }
  static ChessGame position(String... moves){
    ChessGame g=new ChessGame();for(String uci:moves){EngineMove m=EngineMove.parse(uci);assertTrue(uci,g.move(m.fromRow,m.fromCol,m.toRow,m.toCol,m.promotion));}return g;
  }
  void finishAsync()throws Exception{
    assertTrue(engine.entered.await(2,TimeUnit.SECONDS));
    long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
    while(System.nanoTime()<until){
      Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
      if(engine.closed||view.humanCanPlay()||actions.failures>0)return;
      Thread.sleep(5);
    }
    fail("Bot callback did not complete");
  }
  @Test public void illegalBotMoveNeverMutatesGame()throws Exception{
    create(new ChessGame(),false,"e2e5");view.resumeBot();finishAsync();
    assertEquals("-",view.gameState.transcript());assertEquals(1,actions.failures);assertTrue(engine.closed);
  }
  @Test public void invalidEngineOutputFailsWithoutCrash()throws Exception{
    create(new ChessGame(),false,"bestmove garbage");view.resumeBot();finishAsync();assertEquals(1,actions.failures);assertEquals("-",view.gameState.transcript());
  }
  @Test public void engineFailureDoesNotCrashAndStopsThinking()throws Exception{
    create(new ChessGame(),false,"e2e4");engine.fail=true;view.resumeBot();finishAsync();assertEquals(1,actions.failures);assertTrue(engine.closed);assertFalse(view.humanCanPlay());
  }
  @Test public void humanCannotMoveBotSideEvenWhileThinking()throws Exception{
    create(new ChessGame(),false,"e2e4");engine.blocking=true;view.resumeBot();assertTrue(engine.entered.await(2,TimeUnit.SECONDS));
    assertFalse(view.move(6,4,4,4,"-"));view.select(6,4);assertEquals(-1,view.sr);assertEquals("-",view.gameState.transcript());
  }
  @Test public void humanWhiteOrientationIgnoresLocalPreference(){
    new GamePreferences(RuntimeEnvironment.getApplication()).blackAtBottom(true);
    create(new ChessGame(),true,"e7e5");assertFalse(view.boardFlipped());assertTrue(view.humanCanPlay());
  }
  @Test public void humanBlackOrientationIgnoresLocalPreference(){
    new GamePreferences(RuntimeEnvironment.getApplication()).blackAtBottom(false);
    create(new ChessGame(),false,"e2e4");assertTrue(view.boardFlipped());assertFalse(view.humanCanPlay());
  }
  @Test public void humanBlackCanSelectOwnPawnUsingFlippedTouchAfterBotMoves()throws Exception{
    create(new ChessGame(),false,"e2e4");view.layout(0,0,1080,1920);view.resumeBot();finishAsync();
    float density=view.getResources().getDisplayMetrics().density;view.boardGeometry.update(1080,1920,density,view.themeRenderer.hasScene());
    float s=view.boardGeometry.size/8;
    android.view.MotionEvent tap=android.view.MotionEvent.obtain(0,0,android.view.MotionEvent.ACTION_UP,view.boardGeometry.left+3.5f*s,view.boardGeometry.top+6.5f*s,0);
    view.onTouchEvent(tap);tap.recycle();assertEquals(1,view.sr);assertEquals(4,view.sc);
  }
  @Test public void botCastlesThroughNormalMovePath()throws Exception{
    create(position("e2e4","e7e5","g1f3","b8c6","f1e2","g8f6"),false,"e1g1");view.resumeBot();finishAsync();
    assertEquals("K",view.gameState.pieceAt(7,6));assertEquals("R",view.gameState.pieceAt(7,5));assertTrue(view.animating);
  }
  @Test public void botEnPassantThroughNormalMovePath()throws Exception{
    create(position("e2e4","a7a6","e4e5","d7d5"),false,"e5d6");view.resumeBot();finishAsync();
    assertEquals("P",view.gameState.pieceAt(2,3));assertNull(view.gameState.pieceAt(3,3));assertTrue(engine.command.endsWith("d7d5"));
  }
  @Test public void botCanPromoteToAllFourPieces()throws Exception{
    for(String suffix:new String[]{"q","r","b","n"}){
      create(position("a2a4","b8c6","a4a5","h7h5","a5a6","h5h4","a6b7","c6a5"),false,"b7b8"+suffix);
      view.resumeBot();finishAsync();assertEquals(suffix.toUpperCase(java.util.Locale.ROOT),view.gameState.pieceAt(0,1));
      view.stopBot();view.clock.removeCallbacksAndMessages(null);
    }
  }
  @Test public void botCheckmateClosesEngine()throws Exception{
    create(position("f2f3","e7e5","g2g4"),true,"d8h4");view.resumeBot();finishAsync();
    assertTrue(view.gameState.gameOver());assertEquals("XEQUE-MATE • PRETAS VENCEM",view.gameState.status());assertTrue(engine.closed);
  }
  @Test public void botThinkingCountsAgainstItsClockAndCanFlag()throws Exception{
    create(new ChessGame(),false,"e2e4");engine.blocking=true;view.matchClock.sync(100,300000,SystemClock.elapsedRealtime());view.resumeBot();
    assertTrue(engine.entered.await(2,TimeUnit.SECONDS));ShadowSystemClock.advanceBy(Duration.ofMillis(150));view.ticker.run();
    assertTrue(view.gameState.gameOver());assertEquals("TEMPO • PRETAS VENCEM",view.gameState.status());assertTrue(engine.closed);
    Shadows.shadowOf(Looper.getMainLooper()).idle();assertEquals("-",view.gameState.transcript());
  }
  @Test public void exitCancelsCalculationAndRejectsLateResult()throws Exception{
    create(new ChessGame(),false,"e2e4");engine.blocking=true;view.resumeBot();assertTrue(engine.entered.await(2,TimeUnit.SECONDS));
    view.pauseBot();assertTrue(engine.closed);engine.release.countDown();Shadows.shadowOf(Looper.getMainLooper()).idle();assertEquals("-",view.gameState.transcript());
  }
  @Test public void terminalPositionNeverStartsEngine(){
    create(position("f2f3","e7e5","g2g4","d8h4"),true,"a2a3");view.resumeBot();assertEquals(0,engine.searches);
  }
  @Test public void repeatedRequestsNeverRunConcurrentSearches()throws Exception{
    create(new ChessGame(),false,"e2e4");engine.blocking=true;view.resumeBot();assertTrue(engine.entered.await(2,TimeUnit.SECONDS));view.requestBotMove();view.requestBotMove();assertEquals(1,engine.searches);
  }
  @Test public void blackBotPromotionUsesCorrectCase()throws Exception{
    create(position("b1c3","a7a5","h2h4","a5a4","h4h5","a4a3","h5h6","a3b2","c3a4"),true,"b2b1n");
    view.resumeBot();finishAsync();assertEquals("n",view.gameState.pieceAt(7,1));
  }
  @Test public void blackBotCastlingUsesSameRules()throws Exception{
    create(position("e2e4","e7e5","g1f3","g8f6","f1e2","f8e7","d2d3"),true,"e8g8");
    view.resumeBot();finishAsync();assertEquals("k",view.gameState.pieceAt(0,6));assertEquals("r",view.gameState.pieceAt(0,5));
  }
  @Test public void blackBotEnPassantUsesSameRules()throws Exception{
    create(position("a2a3","e7e5","a3a4","e5e4","d2d4"),true,"e4d3");
    view.resumeBot();finishAsync();assertEquals("p",view.gameState.pieceAt(5,3));assertNull(view.gameState.pieceAt(4,3));
  }
  @Test public void botRepetitionDrawClosesEngineWithoutAnotherSearch()throws Exception{
    create(position("g1f3","g8f6","f3g1","f6g8","g1f3","g8f6","f3g1"),true,"f6g8");
    view.resumeBot();finishAsync();assertTrue(view.gameState.gameOver());assertEquals("EMPATE • REPETIÇÃO TRIPLA",view.gameState.status());assertTrue(engine.closed);
  }

  void awaitQueuedMove()throws Exception{
    assertTrue(engine.entered.await(2,TimeUnit.SECONDS));
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
    while(System.nanoTime()<deadline){
      Shadows.shadowOf(Looper.getMainLooper()).idle();
      if(view.botMovePending())return;
      Thread.sleep(5);
    }
    fail("Bot result was not queued");
  }
  @Test public void instantEngineWaitsTwoSecondsAndChargesBotClock()throws Exception{
    create(new ChessGame(),false,"e2e4");view.resumeBot();awaitQueuedMove();
    assertEquals("-",view.gameState.transcript());assertFalse(view.humanCanPlay());
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1999));
    assertEquals("-",view.gameState.transcript());
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1));
    assertEquals("P",view.gameState.pieceAt(4,4));assertEquals(298000,view.matchClock.whiteMs());
    assertEquals(300000,view.matchClock.blackMs());assertEquals(500,view.animationDuration());
    assertFalse(view.humanCanPlay());
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500));assertTrue(view.humanCanPlay());
  }
  @Test public void humanAnimationIsNotOverwrittenByInstantBot()throws Exception{
    create(new ChessGame(),true,"e7e5");
    assertTrue(view.move(6,4,4,4,"-"));Shadows.shadowOf(Looper.getMainLooper()).idle();awaitQueuedMove();
    assertEquals("P",view.animPiece);assertEquals(220,view.animationDuration());
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(360));assertEquals("P",view.animPiece);
    assertNull(view.gameState.pieceAt(3,4));
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1640));
    assertEquals("p",view.animPiece);assertEquals(500,view.animationDuration());
  }
  @Test public void queuedResponseBlocksDuplicateSearches()throws Exception{
    create(new ChessGame(),false,"e2e4");view.resumeBot();awaitQueuedMove();
    view.requestBotMove();view.resumeBot();assertEquals(1,engine.searches);
  }
  @Test public void exitCancelsAlreadyCalculatedDelayedMove()throws Exception{
    create(new ChessGame(),false,"e2e4");view.resumeBot();awaitQueuedMove();view.stopBot();
    assertFalse(view.botMovePending());assertTrue(engine.closed);
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3));assertEquals("-",view.gameState.transcript());
  }
  @Test public void timeoutCancelsQueuedMoveWithoutChangingBoard()throws Exception{
    create(new ChessGame(),false,"e2e4");view.resumeBot();awaitQueuedMove();
    view.matchClock.sync(100,300000,SystemClock.elapsedRealtime());
    ShadowSystemClock.advanceBy(Duration.ofMillis(150));view.ticker.run();
    assertTrue(view.gameState.gameOver());assertTrue(engine.closed);assertFalse(view.botMovePending());
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3));assertEquals("-",view.gameState.transcript());
  }
  @Test public void lowClockShortensPresentationWait()throws Exception{
    create(new ChessGame(),false,"e2e4");view.matchClock.sync(1000,300000,SystemClock.elapsedRealtime());
    view.resumeBot();awaitQueuedMove();
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(249));assertEquals("-",view.gameState.transcript());
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1));assertEquals("P",view.gameState.pieceAt(4,4));
    assertEquals(750,view.matchClock.whiteMs());assertFalse(view.gameState.gameOver());
  }
  @Test public void botKnightAnimationIsSlowerWithoutChangingLocalAnimation(){
    create(new ChessGame(),true,"e7e5");view.startMoveAnimation("n",0,1,2,2);
    assertEquals(650,view.animationDuration());
    view.startMoveAnimation("N",7,1,5,2);assertEquals(360,view.animationDuration());
    ChessView local=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,false,true,actions);
    local.startMoveAnimation("n",0,1,2,2);assertEquals(360,local.animationDuration());
    local.clock.removeCallbacksAndMessages(null);
  }
}
