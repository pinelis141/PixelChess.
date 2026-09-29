package com.pixelchess.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

final class MenuMusicController {
  private static final String PREFS="menu_audio";
  private static final String KEY_MUTED="muted";
  private static final float VOLUME=.45f;

  private final Context context;
  private final SharedPreferences preferences;
  private MediaPlayer player;
  private boolean muted;

  MenuMusicController(Context context){
    this.context=context.getApplicationContext();
    preferences=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    muted=preferences.getBoolean(KEY_MUTED,false);
  }

  boolean isMuted(){return muted;}

  void start(){
    if(muted)return;
    ensurePlayer();
    if(player!=null&&!player.isPlaying())player.start();
  }

  void pause(){
    if(player!=null&&player.isPlaying())player.pause();
  }

  boolean toggleMuted(){
    muted=!muted;
    preferences.edit().putBoolean(KEY_MUTED,muted).apply();
    if(muted)pause();else start();
    return muted;
  }

  void release(){
    if(player==null)return;
    try{player.stop();}catch(Exception ignored){}
    player.release();
    player=null;
  }

  private void ensurePlayer(){
    if(player!=null)return;
    int resId=context.getResources().getIdentifier("the_quiet_gambit_loop","raw",context.getPackageName());
    if(resId==0)return;
    player=MediaPlayer.create(context,resId);
    if(player!=null){
      player.setLooping(true);
      player.setVolume(VOLUME,VOLUME);
    }
  }
}
