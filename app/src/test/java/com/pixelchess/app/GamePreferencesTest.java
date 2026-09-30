package com.pixelchess.app;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(manifest=Config.NONE,sdk=28)
public class GamePreferencesTest {
 private Context context;
 @Before public void clear(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("game_options",Context.MODE_PRIVATE).edit().clear().commit();}
 @Test public void defaultsKeepSoundQuietAndVibrationOn(){GamePreferences p=new GamePreferences(context);assertTrue(p.vibration());assertTrue(p.effects());assertFalse(p.blackAtBottom());assertFalse(p.sound());}
 @Test public void choicesPersistAcrossPreferenceInstances(){GamePreferences p=new GamePreferences(context);p.vibration(false);p.effects(false);p.blackAtBottom(true);p.sound(true);GamePreferences reloaded=new GamePreferences(context);assertFalse(reloaded.vibration());assertFalse(reloaded.effects());assertTrue(reloaded.blackAtBottom());assertTrue(reloaded.sound());}
}
