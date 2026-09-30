package com.pixelchess.app;

import android.os.Bundle;
import android.os.Looper;
import com.pixelchess.app.bot.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @LooperMode(LooperMode.Mode.PAUSED)
public class BotActivityRecreationTest {
  public static class TestActivity extends MainActivity {
    BotIntegrationTest.Fake lastEngine;
    @Override ChessEngine createEngine(){lastEngine=new BotIntegrationTest.Fake("e7e5");lastEngine.blocking=true;return lastEngine;}
  }
  @Test public void recreationPreservesBotSettingsBoardClocksAndKillsOldEngine()throws Exception{
    ActivityController<TestActivity> first=Robolectric.buildActivity(TestActivity.class).setup();
    Bundle state=new Bundle();BotIntegrationTest.Fake old;
    try{
      TestActivity a=first.get();a.selectedMinutes=5;a.startBotMatch(true,BotDifficulty.EXPERT);
      assertTrue(a.game.move(6,4,4,4,"-"));Shadows.shadowOf(Looper.getMainLooper()).idle();
      old=a.lastEngine;assertNotNull(old);assertTrue(old.entered.await(2,java.util.concurrent.TimeUnit.SECONDS));
      a.game.matchClock.sync(14000,17000,android.os.SystemClock.elapsedRealtime());first.saveInstanceState(state);
    }finally{first.pause().stop().destroy();}
    assertTrue(old.closed);
    ActivityController<TestActivity> second=Robolectric.buildActivity(TestActivity.class).create(state).start().resume().visible();
    try{
      TestActivity a=second.get();assertTrue(a.game.botGame());assertTrue(a.game.humanWhite());assertFalse(a.game.boardFlipped());
      assertEquals(BotDifficulty.EXPERT,a.game.botDifficulty());assertEquals("P",a.game.gameState.pieceAt(4,4));
      assertTrue(a.game.matchClock.whiteMs()<=14000);assertTrue(a.game.matchClock.blackMs()<=17000);assertFalse(a.game.humanCanPlay());
      assertNotNull(a.lastEngine);a.showMenu();assertTrue(a.lastEngine.closed);assertNull(a.game);
    }finally{second.pause().stop().destroy();}
  }
  @Test public void humanBlackOrientationSurvivesRecreation()throws Exception{
    ActivityController<TestActivity> first=Robolectric.buildActivity(TestActivity.class).setup();Bundle state=new Bundle();
    try{first.get().startBotMatch(false,BotDifficulty.EASY);first.saveInstanceState(state);}finally{first.pause().stop().destroy();}
    ActivityController<TestActivity> second=Robolectric.buildActivity(TestActivity.class).create(state).start().resume().visible();
    try{assertTrue(second.get().game.botGame());assertFalse(second.get().game.humanWhite());assertTrue(second.get().game.boardFlipped());assertEquals(BotDifficulty.EASY,second.get().game.botDifficulty());}
    finally{second.pause().stop().destroy();}
  }
  @Test public void failedEngineDialogOffersMenuAndLeavesActivityAlive(){
    ActivityController<TestActivity> controller=Robolectric.buildActivity(TestActivity.class).setup();
    try{
      TestActivity a=controller.get();a.startBotMatch(true,BotDifficulty.NORMAL);a.botFailed("Teste de falha");
      android.app.AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertTrue(dialog.isShowing());
      dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();assertNull(a.game);assertTrue(a.mainMenuVisible);assertFalse(a.isFinishing());
    }finally{controller.pause().stop().destroy();}
  }
}
