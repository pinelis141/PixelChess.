package com.pixelchess.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/** Short optional interface/game cues; no looping playback or audio resource dependency. */
final class ChessSounds {
  private final ToneGenerator tones;
  ChessSounds(Context c){tones=new ToneGenerator(AudioManager.STREAM_MUSIC,35);}
  void move(boolean capture,boolean terminal){tones.startTone(terminal?ToneGenerator.TONE_PROP_ACK:capture?ToneGenerator.TONE_PROP_BEEP2:ToneGenerator.TONE_PROP_BEEP,terminal?110:capture?90:45);}
  void release(){tones.release();}
}
