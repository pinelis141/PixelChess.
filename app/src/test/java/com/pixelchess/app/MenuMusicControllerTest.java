package com.pixelchess.app;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE, sdk=28)
public class MenuMusicControllerTest {
  private Context context;
  @Before public void resetPreference(){
    context=RuntimeEnvironment.getApplication();
    context.getSharedPreferences("menu_audio",Context.MODE_PRIVATE).edit().clear().commit();
  }
  @Test public void playbackRequiresVisibleMenuAndForeground(){
    MenuMusicController music=new MenuMusicController(context);
    assertFalse(music.wantsPlayback());
    music.setMenuVisible(true);
    assertFalse(music.wantsPlayback());
    music.setForeground(true);
    assertTrue(music.wantsPlayback());
    music.setMenuVisible(false);
    assertFalse(music.wantsPlayback());
    music.setMenuVisible(true);
    music.setForeground(false);
    assertFalse(music.wantsPlayback());
    music.release();
  }
  @Test public void muteSurvivesControllerRecreation(){
    MenuMusicController music=new MenuMusicController(context);
    music.toggleMuted();
    assertTrue(music.isMuted());
    music.release();
    music=new MenuMusicController(context);
    music.setMenuVisible(true);
    music.setForeground(true);
    assertTrue(music.isMuted());
    assertFalse(music.wantsPlayback());
    music.toggleMuted();
    assertTrue(music.wantsPlayback());
    music.release();
    assertFalse(music.wantsPlayback());
  }
}
