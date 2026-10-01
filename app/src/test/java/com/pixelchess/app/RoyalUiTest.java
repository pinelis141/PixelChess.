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
    SettingsView settings=new SettingsView(context,new GamePreferences(context),()->{});
    assertTrue(settings.getBackground() instanceof RoyalLibraryDrawable);
    View vibration=ConsolidatedMenuTest.find(settings,"Vibração nos turnos");assertNotNull(vibration);
    assertTrue(vibration instanceof Switch);assertTrue(vibration.getBackground() instanceof RoyalPanelDrawable);
    assertTrue(vibration.getMinimumHeight()>=RoyalUi.dp(context,56));
    View back=ConsolidatedMenuTest.find(settings,"VOLTAR");assertNotNull(back);assertTrue(back.getBackground() instanceof RoyalPanelDrawable);
    ThemeSelectorView skins=new ThemeSelectorView(context,BoardThemes.ALL,BoardThemes.CLASSIC.id,new ThemeSelectorView.Listener(){
      public void onThemeSelected(BoardTheme theme){}public void onClose(){}
    });
    assertTrue(skins.getBackground() instanceof RoyalLibraryDrawable);
    View classic=ConsolidatedMenuTest.find(skins,BoardThemes.CLASSIC.name);assertNotNull(classic);
    View classicCard=(View)classic.getParent();assertTrue(classicCard.getBackground() instanceof RoyalPanelDrawable);
    assertTrue(classicCard.isSelected());
  }
  @Test public void allSelectionDialogsKeepTheirOptionsAndUseTheOrnateFrame(){
    Context context=RuntimeEnvironment.getApplication();
    AlertDialog dialog=RoyalUi.dialog(context).setTitle("Dificuldade")
      .setItems(new String[]{"Iniciante","Fácil","Normal","Difícil","Mestre"},(d,i)->{}).setNegativeButton("VOLTAR",null).show();
    assertTrue(dialog.isShowing());assertEquals(5,dialog.getListView().getAdapter().getCount());
    assertTrue(dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getBackground() instanceof RoyalPanelDrawable);
    int titleId=context.getResources().getIdentifier("alertTitle","id","android");
    View title=dialog.getWindow().getDecorView().findViewById(titleId);assertNotNull(title);
    assertEquals(RoyalUi.GOLD,((TextView)title).getCurrentTextColor());
    assertTrue(hasFrame(dialog.getWindow().getDecorView()));dialog.dismiss();
  }
  @Test public void sharedPanelDrawableChangesStateAndSelectionIsCrimson(){
    Context context=RuntimeEnvironment.getApplication();RoyalPanelDrawable panel=RoyalUi.panel(context,false);
    panel.setBounds(0,0,600,120);android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(600,120,android.graphics.Bitmap.Config.ARGB_8888);
    android.graphics.Canvas canvas=new android.graphics.Canvas(bitmap);panel.draw(canvas);int idle=bitmap.getPixel(300,60);
    panel.setState(new int[]{android.R.attr.state_checked});panel.draw(canvas);int checked=bitmap.getPixel(300,60);
    assertTrue("checked cards receive the crimson selection",android.graphics.Color.red(checked)>android.graphics.Color.red(idle));
    RoyalPanelDrawable selected=RoyalUi.panel(context,true);selected.setBounds(0,0,600,120);selected.draw(canvas);
    int selectedRed=bitmap.getPixel(300,60);assertTrue(android.graphics.Color.red(selectedRed)>android.graphics.Color.green(selectedRed));bitmap.recycle();
  }
  private boolean hasFrame(View view){
    if(view.getBackground() instanceof RoyalPanelDrawable)return true;
    if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++)if(hasFrame(group.getChildAt(i)))return true;}
    return false;
  }
}
