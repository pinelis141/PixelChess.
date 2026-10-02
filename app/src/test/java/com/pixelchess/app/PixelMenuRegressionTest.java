package com.pixelchess.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import com.pixelchess.app.bot.BotDifficulty;
import static org.junit.Assert.*;

/** Functional protection for the approved pixel-art menus and their shared game flows. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class PixelMenuRegressionTest {
  private Context context;
  @Before public void clear(){
    context=RuntimeEnvironment.getApplication();
    context.getSharedPreferences("game_options",Context.MODE_PRIVATE).edit().clear().commit();
  }
  private MainMenuView.Actions actions(int[] calls){
    return new MainMenuView.Actions(){
      public void playLocal(){calls[0]++;}public void playBot(){calls[1]++;}
      public void playBluetooth(){calls[2]++;}public void chooseSkin(){calls[3]++;}
      public void openSettings(){calls[4]++;}public void toggleMusic(){calls[5]++;}
    };
  }
  private View clickTarget(View root,String text){
    View v=ConsolidatedMenuTest.find(root,text);assertNotNull(text,v);
    while(v!=null&&!v.isClickable()&&v.getParent() instanceof View)v=(View)v.getParent();
    assertNotNull(text+" needs a click target",v);return v;
  }
  @Test public void selectionReturnsToTheLastMainMenuItem(){
    int[] calls=new int[6];
    MainMenuView first=new MainMenuView(context,"0.27.1",BoardThemes.FOREST,false,actions(calls));
    View bot=clickTarget(first,"JOGAR CONTRA BOT");bot.performClick();
    assertEquals(1,calls[1]);assertEquals(1,new GamePreferences(context).lastMenuIndex());
    MainMenuView again=new MainMenuView(context,"0.27.1",BoardThemes.FOREST,false,actions(calls));
    assertEquals(1,again.selectedIndex());
    assertTrue(((View)ConsolidatedMenuTest.find(again,"JOGAR CONTRA BOT").getParent()).isSelected());
  }
  @Test public void localAndBotUseTheSamePixelHeadingAndPanelFrames(){
    GamePreferences p=new GamePreferences(context);
    RoyalSetupView.Actions a=callbacks();
    RoyalSetupView local=new RoyalSetupView(context,RoyalSetupView.Mode.LOCAL,p,a);
    RoyalSetupView bot=new RoyalSetupView(context,RoyalSetupView.Mode.BOT,p,a);
    assertTrue(local.getBackground() instanceof RoyalLibraryDrawable);
    assertTrue(bot.getBackground() instanceof RoyalLibraryDrawable);
    assertNotNull(ConsolidatedMenuTest.find(local,"ORIENTAÇÃO"));
    assertNotNull(ConsolidatedMenuTest.find(bot,"DIFICULDADE"));
    assertNotNull(ConsolidatedMenuTest.find(bot,"COR"));
    assertNotNull(ConsolidatedMenuTest.find(local,"TEMPO"));
    assertNotNull(ConsolidatedMenuTest.find(bot,"TEMPO"));
    assertTrue(((View)ConsolidatedMenuTest.find(local,"INICIAR PARTIDA")).getBackground() instanceof RoyalPanelDrawable);
    assertTrue(((View)ConsolidatedMenuTest.find(bot,"JOGAR AGORA")).getBackground() instanceof RoyalPanelDrawable);
  }
  @Test public void botPresetSurvivesReopeningWithoutTouchingStockfish(){
    GamePreferences p=new GamePreferences(context);
    RoyalSetupView bot=new RoyalSetupView(context,RoyalSetupView.Mode.BOT,p,callbacks());
    clickTarget(bot,"4").performClick();
    clickTarget(bot,"PRETAS").performClick();
    clickTarget(bot,"5 MIN").performClick();
    GamePreferences restored=new GamePreferences(context);
    assertEquals(3,restored.botLevel());assertEquals(1,restored.botColor());assertEquals(5,restored.botMinutes());
    RoyalSetupView again=new RoyalSetupView(context,RoyalSetupView.Mode.BOT,restored,callbacks());
    assertTrue(clickTarget(again,"4").isSelected()||((View)clickTarget(again,"4").getParent()).isSelected());
  }
  @Test public void applyingSkinsRequiresConfirmAndReturnsRealTheme(){
    BoardTheme[] chosen={null};
    ThemeSelectorView screen=new ThemeSelectorView(context,BoardThemes.ALL,BoardThemes.CLASSIC.id,
      new ThemeSelectorView.Listener(){
        public void onThemeSelected(BoardTheme b){chosen[0]=b;} public void onClose(){}
      });
    clickTarget(screen,BoardThemes.FOREST.name).performClick();
    assertNull(chosen[0]);
    clickTarget(screen,"APLICAR SKIN").performClick();
    assertSame(BoardThemes.FOREST,chosen[0]);
  }
  private RoyalSetupView.Actions callbacks(){
    return new RoyalSetupView.Actions(){
      public void back(){}public void local(int m,boolean b){}
      public void bot(int m,BotDifficulty difficulty,int color){}
      public void host(int m){}public void join(){}public void pairDevices(){}
    };
  }
}
