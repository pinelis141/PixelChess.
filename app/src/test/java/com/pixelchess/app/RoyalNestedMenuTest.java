package com.pixelchess.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import com.pixelchess.app.bot.BotDifficulty;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class RoyalNestedMenuTest {
  private static View clickable(View root,String label){
    View item=ConsolidatedMenuTest.find(root,label);
    assertNotNull("Missing control: "+label,item);
    while(item!=null&&!item.isClickable() && item.getParent() instanceof View)
      item=(View)item.getParent();
    assertNotNull("Control not actionable: "+label,item);
    assertTrue("Not clickable: "+label,item.isClickable());
    return item;
  }
  private static RoyalSetupView.Actions callbacks(int[] calls,int[] saved){
    return new RoyalSetupView.Actions(){
      public void back(){calls[0]++;}
      public void local(int minutes,boolean black){calls[1]++;saved[0]=minutes;saved[1]=black?1:0;}
      public void bot(int minutes,BotDifficulty level,int color){calls[2]++;saved[0]=minutes;saved[1]=level.ordinal();saved[2]=color;}
      public void host(int minutes){calls[3]++;saved[0]=minutes;}
      public void join(){calls[4]++;}
      public void pairDevices(){calls[5]++;}
    };
  }
  @Test public void localSetupUsesRealControlsAndPreservesGameRules(){
    Context c=RuntimeEnvironment.getApplication();GamePreferences p=new GamePreferences(c);
    p.defaultMinutes(10);p.blackAtBottom(false);
    int[] calls=new int[6],saved=new int[3];
    RoyalSetupView screen=new RoyalSetupView(c,RoyalSetupView.Mode.LOCAL,p,callbacks(calls,saved));
    assertTrue(screen.getBackground() instanceof RoyalLibraryDrawable);
    clickable(screen,"PRETAS").performClick();
    clickable(screen,"3 MIN").performClick();
    clickable(screen,"INICIAR PARTIDA").performClick();
    assertEquals(1,calls[1]);assertEquals(3,saved[0]);assertEquals(1,saved[1]);
    assertTrue(p.defaultMinutes()==3);
  }
  @Test public void botSetupSelectsFiveDifficultiesAndColorAndTime(){
    Context c=RuntimeEnvironment.getApplication();GamePreferences p=new GamePreferences(c);
    p.defaultMinutes(10);int[] calls=new int[6],saved=new int[3];
    RoyalSetupView screen=new RoyalSetupView(c,RoyalSetupView.Mode.BOT,p,callbacks(calls,saved));
    clickable(screen,"4").performClick();clickable(screen,"ALEATÓRIO").performClick();
    clickable(screen,"5 MIN").performClick();clickable(screen,"JOGAR AGORA").performClick();
    assertEquals(1,calls[2]);assertEquals(5,saved[0]);
    assertEquals(BotDifficulty.EXPERT.ordinal(),saved[1]);assertEquals(2,saved[2]);
  }
  @Test public void bluetoothCreatorOwnsClockAndNestedChoices(){
    Context c=RuntimeEnvironment.getApplication();GamePreferences p=new GamePreferences(c);
    p.defaultMinutes(10);int[] calls=new int[6],saved=new int[3];
    RoyalSetupView screen=new RoyalSetupView(c,RoyalSetupView.Mode.BLUETOOTH,p,callbacks(calls,saved));
    clickable(screen,"5 MIN").performClick();clickable(screen,"CRIAR SALA").performClick();
    assertEquals(1,calls[3]);assertEquals(5,saved[0]);
    clickable(screen,"ENTRAR EM SALA").performClick();
    clickable(screen,"DISPOSITIVOS PAREADOS").performClick();
    assertEquals(1,calls[4]);assertEquals(1,calls[5]);
  }
  @Test public void settingsSwitchesSaveAndCreditsRemainReachable(){
    Context c=RuntimeEnvironment.getApplication();GamePreferences prefs=new GamePreferences(c);
    prefs.vibration(true);int[] calls=new int[2];
    MenuMusicController music=new MenuMusicController(c);
    SettingsView screen=new SettingsView(c,prefs,music,new SettingsView.Listener(){
      public void back(){calls[0]++;}public void credits(){calls[1]++;}
    });
    assertTrue(screen.getBackground() instanceof RoyalLibraryDrawable);
    clickable(screen,"VIBRAÇÃO").performClick();assertFalse(new GamePreferences(c).vibration());
    clickable(screen,"CRÉDITOS E LICENÇAS").performClick();assertEquals(1,calls[1]);
    music.release();
  }
}
