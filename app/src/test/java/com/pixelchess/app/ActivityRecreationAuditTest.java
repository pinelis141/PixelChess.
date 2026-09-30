package com.pixelchess.app;

import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @LooperMode(LooperMode.Mode.PAUSED)
public class ActivityRecreationAuditTest {
  @Test public void localMatchShouldSurviveOrdinaryActivityRecreation(){
    ActivityController<MainActivity> first=Robolectric.buildActivity(MainActivity.class).setup();
    MainActivity activity=first.get();
    Bundle state=new Bundle();
    try{
      activity.mainMenuVisible=false;
      activity.game=new ChessView(activity,BoardThemes.CLASSIC,5,false,true,activity);
      activity.setContentView(activity.game);
      assertTrue(activity.game.move(6,4,4,4,"-"));
      activity.onSaveInstanceState(state);
    }finally{
      first.pause().stop().destroy();
    }

    ActivityController<MainActivity> second=Robolectric.buildActivity(MainActivity.class).create(state).start().resume().visible();
    try{
      MainActivity restored=second.get();
      assertNotNull("Activity recreation must not silently discard an active local match",restored.game);
      assertEquals("P",restored.game.gameState.pieceAt(4,4));
    }finally{
      second.pause().stop().destroy();
    }
  }
}
