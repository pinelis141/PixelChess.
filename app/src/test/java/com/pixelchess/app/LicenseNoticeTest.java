package com.pixelchess.app;

import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class LicenseNoticeTest {
  @Test public void mainMenuOpensActualBundledAppEngineAndAudioNotices(){
    ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
    try{
      MainActivity activity=controller.get();
      View entry=ConsolidatedMenuTest.find(activity.getWindow().getDecorView(),"LICENÇAS E CRÉDITOS");
      assertNotNull(entry);assertTrue(entry.performClick());
      AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertTrue(dialog.isShowing());
      View content=ConsolidatedMenuTest.find(dialog.getWindow().getDecorView(),"PixelChess — código-fonte livre");
      assertNotNull(content);String notice=((TextView)content).getText().toString();
      assertTrue(notice.contains("GPL-3.0-or-later"));assertTrue(notice.contains("GNU GENERAL PUBLIC LICENSE"));
      assertTrue(notice.contains("Stockfish 19"));assertTrue(notice.contains("edb0d9db6731067ec50ce619ff372b463bc4dd5d"));
      assertTrue(notice.contains("mh2o"));assertTrue(notice.contains("CC0 1.0"));assertTrue(notice.contains("The Quiet Gambit"));
      if(BuildConfig.SOURCE_REVISION.matches("[0-9a-f]{40}"))assertTrue(notice.contains("/tree/"+BuildConfig.SOURCE_REVISION));
      dialog.dismiss();assertTrue(activity.mainMenuVisible);
    }finally{controller.pause().stop().destroy();}
  }
}
