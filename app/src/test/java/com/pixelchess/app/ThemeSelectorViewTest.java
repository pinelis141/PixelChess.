package com.pixelchess.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ThemeSelectorViewTest {
  @Test public void rendersAllRegisteredThemesAndSelection() {
    Context context=RuntimeEnvironment.getApplication();
    ThemeSelectorView view=new ThemeSelectorView(context,BoardThemes.ALL,BoardThemes.CASTLE.id,new ThemeSelectorView.Listener(){
      public void onThemeSelected(BoardTheme theme){}
      public void onClose(){}
    });
    for(BoardTheme theme:BoardThemes.ALL) assertTrue(hasText(view,theme.name));
    assertTrue(hasText(view,"✓  SELECIONADO"));
    assertEquals(4,BoardThemes.ALL.size());
  }

  private boolean hasText(View view,String expected) {
    if(view instanceof TextView && expected.equals(((TextView)view).getText().toString())) return true;
    if(view instanceof ViewGroup) {
      ViewGroup group=(ViewGroup)view;
      for(int i=0;i<group.getChildCount();i++) if(hasText(group.getChildAt(i),expected)) return true;
    }
    return false;
  }
}
