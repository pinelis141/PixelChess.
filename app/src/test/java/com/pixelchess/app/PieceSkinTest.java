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
}
