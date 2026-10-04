package com.pixelchess.app;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class PieceSkinTest {
  @Test public void unknownSkinFallsBackWithoutCrashing(){
    assertEquals(PieceSkin.CLASSIC,PieceSkin.from("missing"));
  }
  @Test public void pieceChoicePersistsIndependentlyOfBoardTheme(){
    android.content.Context context=RuntimeEnvironment.getApplication();
    PieceSkin original=PieceSkin.load(context);
    try{
      PieceSkin.GUARDIANS.save(context);
      assertEquals(PieceSkin.GUARDIANS,PieceSkin.load(context));
      PieceSkin.OBSIDIAN.save(context);
      assertEquals(PieceSkin.OBSIDIAN,PieceSkin.load(context));
    }finally{
      original.save(context);
    }
  }

  @Test public void combinedSelectorShowsActualSpritesAndRequiresApply(){
    android.content.Context context=RuntimeEnvironment.getApplication();
    PieceSkin original=PieceSkin.load(context);
    try{
      PieceSkin.CLASSIC.save(context);
      final int[] closes={0};
      ThemeSelectorView menu=new ThemeSelectorView(context,BoardThemes.ALL,BoardThemes.CLASSIC.id,
        new ThemeSelectorView.Listener(){
          public void onThemeSelected(BoardTheme theme){}
          public void onClose(){closes[0]++;}
        });
      android.view.View label=ConsolidatedMenuTest.find(menu,"GUARDIÕES");
      assertNotNull(label);
      android.view.View clickable=label;
      while(!clickable.isClickable()&&clickable.getParent() instanceof android.view.View)
        clickable=(android.view.View)clickable.getParent();
      assertTrue(clickable.performClick());
      assertEquals(PieceSkin.CLASSIC,PieceSkin.load(context));
      android.view.View apply=ConsolidatedMenuTest.find(menu,"APLICAR PEÇAS");
      assertNotNull(apply);
      assertTrue(apply.performClick());
      assertEquals(PieceSkin.GUARDIANS,PieceSkin.load(context));
      assertEquals(1,closes[0]);
    }finally{original.save(context);}
  }
}
