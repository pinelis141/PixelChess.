package com.pixelchess.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent player options shared by the menu and match renderer. */
final class GamePreferences {
  private final SharedPreferences values;
  GamePreferences(Context context){values=context.getApplicationContext().getSharedPreferences("game_options",Context.MODE_PRIVATE);}
  boolean vibration(){return values.getBoolean("vibration",true);}
  void vibration(boolean enabled){values.edit().putBoolean("vibration",enabled).apply();}
  boolean effects(){return values.getBoolean("effects",true);}
  void effects(boolean enabled){values.edit().putBoolean("effects",enabled).apply();}
  boolean blackAtBottom(){return values.getBoolean("black_at_bottom",false);}
  void blackAtBottom(boolean enabled){values.edit().putBoolean("black_at_bottom",enabled).apply();}
  boolean sound(){return values.getBoolean("sound",false);}
  void sound(boolean enabled){values.edit().putBoolean("sound",enabled).apply();}
}
