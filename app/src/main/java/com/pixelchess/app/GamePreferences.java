package com.pixelchess.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent player options shared by the menu and match renderer. */
final class GamePreferences {
  private final SharedPreferences values;
  GamePreferences(Context context){values=context.getApplicationContext().getSharedPreferences("game_options",Context.MODE_PRIVATE);}
  int defaultMinutes(){int saved=values.getInt("default_minutes",10);return saved==3||saved==5||saved==10?saved:10;}
  void defaultMinutes(int minutes){if(minutes!=3&&minutes!=5&&minutes!=10)throw new IllegalArgumentException("Invalid clock preset");values.edit().putInt("default_minutes",minutes).apply();}
  int lastMenuIndex(){int v=values.getInt("last_menu",0);return v>=0&&v<=4?v:0;}
  void lastMenuIndex(int v){if(v>=0&&v<=4)values.edit().putInt("last_menu",v).apply();}
  int botLevel(){int v=values.getInt("bot_level",2);return v>=0&&v<=4?v:2;}
  void botLevel(int v){if(v<0||v>4)throw new IllegalArgumentException("Invalid bot difficulty");values.edit().putInt("bot_level",v).apply();}
  int botColor(){int v=values.getInt("bot_color",0);return v>=0&&v<=2?v:0;}
  void botColor(int v){if(v<0||v>2)throw new IllegalArgumentException("Invalid bot color");values.edit().putInt("bot_color",v).apply();}
  int botMinutes(){int v=values.getInt("bot_minutes",defaultMinutes());return v==3||v==5||v==10?v:10;}
  void botMinutes(int v){if(v!=3&&v!=5&&v!=10)throw new IllegalArgumentException("Invalid bot clock");values.edit().putInt("bot_minutes",v).apply();}
  boolean vibration(){return values.getBoolean("vibration",true);}
  void vibration(boolean enabled){values.edit().putBoolean("vibration",enabled).apply();}
  boolean effects(){return values.getBoolean("effects",true);}
  void effects(boolean enabled){values.edit().putBoolean("effects",enabled).apply();}
  boolean blackAtBottom(){return values.getBoolean("black_at_bottom",false);}
  void blackAtBottom(boolean enabled){values.edit().putBoolean("black_at_bottom",enabled).apply();}
  boolean sound(){return values.getBoolean("sound",false);}
  void sound(boolean enabled){values.edit().putBoolean("sound",enabled).apply();}
}
