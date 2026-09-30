package com.pixelchess.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

/** Activity-owned music: never runs outside the foreground main menu. */
final class MenuMusicController {
  private final Context context;
  private final SharedPreferences prefs;
  private final AudioManager audio;
  private final AudioFocusRequest focus;
  private MediaPlayer player;
  private boolean muted,menu,foreground,prepared,hasFocus,suspended,released,failed;
  MenuMusicController(Context context) {
    this.context=context.getApplicationContext();
    prefs=this.context.getSharedPreferences("menu_audio",Context.MODE_PRIVATE);
    muted=prefs.getBoolean("muted",false);
    audio=(AudioManager)this.context.getSystemService(Context.AUDIO_SERVICE);
    focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes()).setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(this::focusChanged,new Handler(Looper.getMainLooper())).build();
  }
  private AudioAttributes attributes() {
    return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
  }
  boolean isMuted(){return muted;}
  void setMenuVisible(boolean value){menu=value;if(value){suspended=false;failed=false;}update();}
  void setForeground(boolean value){foreground=value;if(value){suspended=false;failed=false;}update();}
  void toggleMuted(){muted=!muted;prefs.edit().putBoolean("muted",muted).apply();if(!muted)suspended=false;update();}
  boolean wantsPlayback(){return !released && menu && foreground && !muted;}
  private void update(){
    if(!wantsPlayback()){pause();abandon();return;}
    if(suspended || failed)return;
    if(!hasFocus){
      hasFocus=audio!=null && audio.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
      if(!hasFocus)return;
    }
    if(player==null)prepare();
    if(player!=null && prepared && !player.isPlaying())player.start();
  }
  private void prepare(){
    MediaPlayer candidate=new MediaPlayer();player=candidate;
    try(AssetFileDescriptor fd=context.getResources().openRawResourceFd(R.raw.the_quiet_gambit_loop)){
      candidate.setAudioAttributes(attributes());
      candidate.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
      candidate.setLooping(true);candidate.setVolume(.55f,.55f);
      candidate.setOnPreparedListener(p->{if(player!=p || released)return;prepared=true;update();});
      candidate.setOnErrorListener((p,what,extra)->{if(player==p){failed=true;releasePlayer();abandon();}return true;});
      candidate.prepareAsync();
    }catch(Exception e){failed=true;releasePlayer();abandon();}
  }
  private void focusChanged(int change){
    if(released)return;
    if(change==AudioManager.AUDIOFOCUS_GAIN){hasFocus=true;suspended=false;update();}
    else{ suspended=true;pause();if(change==AudioManager.AUDIOFOCUS_LOSS)abandon(); }
  }
  private void pause(){if(player!=null && prepared && player.isPlaying())player.pause();}
  private void abandon(){if(hasFocus && audio!=null)audio.abandonAudioFocusRequest(focus);hasFocus=false;}
  private void releasePlayer(){if(player!=null){player.release();player=null;}prepared=false;}
  void release(){released=true;abandon();releasePlayer();}
}
