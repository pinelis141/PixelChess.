package com.pixelchess.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/** Short optional interface/game cues; no looping playback or audio resource dependency. */
final class ChessSounds {
  private ToneGenerator tones;
  ChessSounds(Context c){}

  void move(boolean capture,boolean terminal){
    try{
      if(tones==null)tones=new ToneGenerator(AudioManager.STREAM_MUSIC,35);
      tones.startTone(terminal?ToneGenerator.TONE_PROP_ACK:capture?ToneGenerator.TONE_PROP_BEEP2:ToneGenerator.TONE_PROP_BEEP,
          terminal?110:capture?90:45);
    }catch(RuntimeException audioFailure){
      release();
    }
  }

  void release(){
    if(tones!=null){
      try{tones.release();}catch(RuntimeException ignored){}
      tones=null;
    }
  }
}
