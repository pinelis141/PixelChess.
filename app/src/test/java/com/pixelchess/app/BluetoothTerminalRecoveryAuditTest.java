package com.pixelchess.app;

import android.app.AlertDialog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowAlertDialog;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @LooperMode(LooperMode.Mode.PAUSED)
public class BluetoothTerminalRecoveryAuditTest {
  @Test public void interruptedTerminalMatchStillOffersARecoveryHandshake(){
    ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
    MainActivity activity=controller.get();
    try{
      ChessView view=new ChessView(activity,BoardThemes.CLASSIC,5,true,true,activity);
      activity.game=view;
      view.gameState.finish("XEQUE-MATE • BRANCAS VENCEM");

      activity.onInterrupted("A conexão Bluetooth foi interrompida.");

      AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();
      assertNotNull(dialog);
      assertNotNull("A terminal result may not have reached the peer; reconnect must remain possible",
          dialog.getButton(AlertDialog.BUTTON_POSITIVE));
      assertEquals("RECONECTAR",dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
    }finally{
      controller.pause().stop().destroy();
    }
  }
}
