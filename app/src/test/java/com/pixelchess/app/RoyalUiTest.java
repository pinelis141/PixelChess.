package com.pixelchess.app;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class RoyalUiTest {
  @Test public void settingsAndThemeScreensShareTheLibrarySurfaceAndGoldCardFrames(){
    Context context=RuntimeEnvironment.getApplication();
    SettingsView settings=new SettingsView(context,new GamePreferences(context),new MenuMusicController(context),new SettingsView.Listener(){
      public void back(){} public void credits(){}
    });
    assertTrue(settings.getBackground() instanceof RoyalLibraryDrawable);
    View vibration=ConsolidatedMenuTest.find(settings,"VIBRAÇÃO");assertNotNull(vibration);
    View vibrationCard=(View)vibration.getParent().getParent().getParent();
    assertTrue(vibrationCard.getBackground() instanceof RoyalPanelDrawable);
    View back=ConsolidatedMenuTest.find(settings,"VOLTAR");assertNotNull(back);assertTrue(back.getBackground() instanceof RoyalPanelDrawable);
    ThemeSelectorView skins=new ThemeSelectorView(context,BoardThemes.ALL,BoardThemes.CLASSIC.id,new ThemeSelectorView.Listener(){
      public void onThemeSelected(BoardTheme theme){}public void onClose(){}
    });
    assertTrue(skins.getBackground() instanceof RoyalLibraryDrawable);
    View classic=ConsolidatedMenuTest.find(skins,BoardThemes.CLASSIC.name);assertNotNull(classic);
    View classicCard=(View)classic.getParent().getParent();assertTrue(classicCard.getBackground() instanceof RoyalPanelDrawable);
    assertTrue(classicCard.isSelected());
  }
  @Test public void difficultyDialogKeepsEveryOption(){
    Context context=RuntimeEnvironment.getApplication();
    AlertDialog dialog=RoyalUi.dialog(context).setTitle("Dificuldade")
      .setItems(new String[]{"Iniciante","Fácil","Normal","Difícil","Mestre"},(d,i)->{}).setNegativeButton("VOLTAR",null).show();
    assertTrue(dialog.isShowing());assertEquals(5,dialog.getListView().getAdapter().getCount());
    dialog.dismiss();
  }
}
