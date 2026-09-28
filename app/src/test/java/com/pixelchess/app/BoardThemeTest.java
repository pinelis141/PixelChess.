package com.pixelchess.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class BoardThemeTest {
  @Test public void savedSelectionsSurviveMigrationAndUnknownIds() {
    assertSame(BoardThemes.FOREST, BoardThemes.fromLegacyIndex(1));
    assertSame(BoardThemes.CLASSIC, BoardThemes.fromLegacyIndex(99));
    assertSame(BoardThemes.FOREST, BoardThemes.find("forest"));
    assertSame(BoardThemes.CLASSIC, BoardThemes.find("removed-theme"));
    assertSame(BoardThemes.CLASSIC, BoardThemes.find(null));
  }
  @Test public void onlyVisibleMovingEffectsRequestFrames() {
    assertFalse(BoardThemes.CLASSIC.glow.animated());
    assertTrue(BoardThemes.FOREST.glow.animated());
    assertFalse(new BoardTheme.Glow(BoardTheme.Effect.FIREFLIES,0,0,1).animated());
    assertFalse(new BoardTheme.Glow(BoardTheme.Effect.FIREFLIES,0,1,0).animated());
  }
  @Test(expected=IllegalArgumentException.class) public void rejectInvalidIntensity() {
    new BoardTheme.Glow(BoardTheme.Effect.FIREFLIES,0,Float.NaN,1);
  }
  @Test public void particlesStayInsideSideStripsThroughLongAnimation() {
    int left=0;
    for(int i=0;i<FireflyMotion.COUNT;i++) {
      if(FireflyMotion.left(i)) left++;
      for(int t=0;t<36000;t+=7) {
        float y=FireflyMotion.height(i,t/10.0), x=FireflyMotion.distance(i,t/10.0);
        float light=FireflyMotion.brightness(i,t/10.0);
        assertTrue(y>0 && y<1);
        assertTrue(x>0 && x<1);
        assertTrue(light>=0 && light<=1);
      }
    }
    assertNotEquals(left,FireflyMotion.COUNT-left);
    assertNotEquals(FireflyMotion.height(0,10),FireflyMotion.height(6,10),0.001f);
  }
}
