package com.pixelchess.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Activity-owned, preloaded stone impacts. No tone generator, loops or runtime synthesis. */
final class ChessSounds {
  // Shared by every cue, side and game mode; device media volume still applies.
  static final float PLAYBACK_VOLUME=.65f;
  interface Player { void play(int resource); void stop(); void release(); }
  private Player player;
  private boolean foreground=true,released;
  ChessSounds(Context context){
    try{player=new PoolPlayer(context.getApplicationContext());}
    catch(RuntimeException unavailable){player=null;}
  }
  ChessSounds(Player player){this.player=player;}
  void move(boolean capture,boolean terminal){
    if(released||!foreground||player==null)return;
    try{player.play(terminal?R.raw.stone_terminal:capture?R.raw.stone_capture:R.raw.stone_move);}
    catch(RuntimeException unavailable){release();}
  }
  void setForeground(boolean value){
    foreground=value;
    if(!value&&!released&&player!=null){
      try{player.stop();}catch(RuntimeException unavailable){release();}
    }
  }
  void release(){
    if(released)return;released=true;
    Player old=player;player=null;
    if(old!=null)try{old.release();}catch(RuntimeException ignored){}
  }

  private static final class PoolPlayer implements Player {
    private final SoundPool pool;
    private final Map<Integer,Integer> samples=new HashMap<>();
    private final Set<Integer> ready=ConcurrentHashMap.newKeySet();
    private volatile boolean closed;
    private int stream;
    PoolPlayer(Context context){
      AudioAttributes attributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
      pool=new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attributes).build();
      try{
        pool.setOnLoadCompleteListener((p,sample,status)->{if(!closed&&status==0)ready.add(sample);});
        for(int resource:new int[]{R.raw.stone_move,R.raw.stone_capture,R.raw.stone_terminal})
          samples.put(resource,pool.load(context,resource,1));
      }catch(RuntimeException failure){pool.release();throw failure;}
    }
    public void play(int resource){
      if(closed)return;
      Integer sample=samples.get(resource);
      // Never queue a stale move if loading failed or has not finished.
      if(sample==null||sample==0||!ready.contains(sample))return;
      stop();
      stream=pool.play(sample,PLAYBACK_VOLUME,PLAYBACK_VOLUME,1,0,1f);
    }
    public void stop(){if(!closed&&stream!=0){pool.stop(stream);stream=0;}}
    public void release(){
      if(closed)return;closed=true;pool.setOnLoadCompleteListener(null);pool.release();ready.clear();
    }
  }
}
