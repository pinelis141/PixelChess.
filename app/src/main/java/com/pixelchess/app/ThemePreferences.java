package com.pixelchess.app;

import android.content.SharedPreferences;

/** Retains the Activity preferences file and the original integer selection fallback. */
final class ThemePreferences {
  private final SharedPreferences preferences;
  ThemePreferences(SharedPreferences preferences){this.preferences=preferences;}
  BoardTheme load(){
    String legacy=BoardThemes.fromLegacyIndex(preferences.getInt("skin",0)).id;
    return BoardThemes.find(preferences.getString("theme_id",legacy));
  }
  void save(BoardTheme theme){preferences.edit().putString("theme_id",theme.id).apply();}
}
