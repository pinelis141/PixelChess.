package com.pixelchess.app;

import android.content.Context;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class StoneAudioTest {
  static class Actions extends ChessViewSessionTest.Actions {
    int plays;boolean capture,terminal;
    @Override public void playSound(boolean c,boolean t){plays++;capture=c;terminal=t;}
  }
  Context context;ChessView view;Actions actions;
  @Before public void setup(){context=RuntimeEnvironment.getApplication();new GamePreferences(context).sound(true);actions=new Actions();}
  @After public void cleanup(){new GamePreferences(context).sound(false);if(view!=null){view.stopBot();view.clock.removeCallbacksAndMessages(null);}}
  void create(ChessGame game){view=new ChessView(context,BoardThemes.CLASSIC,5,false,true,actions);view.restore(game,300000,300000);}
  @Test public void realAssetsHaveSafePcmLevelsAndBoundedDuration()throws Exception{
    int[] resources={R.raw.stone_move,R.raw.stone_capture,R.raw.stone_terminal};
    int[] expectedSamples={4410,4410,12348};
    for(int k=0;k<resources.length;k++){
      byte[] bytes;
      try(InputStream in=context.getResources().openRawResource(resources[k]);ByteArrayOutputStream out=new ByteArrayOutputStream()){
        byte[] block=new byte[4096];int n;while((n=in.read(block))!=-1)out.write(block,0,n);bytes=out.toByteArray();
      }
      ByteBuffer data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
      assertEquals("RIFF",new String(bytes,0,4,StandardCharsets.US_ASCII));
      assertEquals("WAVE",new String(bytes,8,4,StandardCharsets.US_ASCII));
      assertEquals(bytes.length-8,data.getInt(4));assertEquals(1,data.getShort(20));assertEquals(1,data.getShort(22));
      assertEquals(44100,data.getInt(24));assertEquals(16,data.getShort(34));
      assertEquals(expectedSamples[k]*2,data.getInt(40));assertEquals(44+expectedSamples[k]*2,bytes.length);
      int peak=0;long energy=0;
      for(int i=44;i<bytes.length;i+=2){int sample=data.getShort(i);peak=Math.max(peak,Math.abs(sample));energy+=(long)sample*sample;}
      assertTrue(peak>1000);assertTrue(peak<19000);assertTrue(energy>1000000);
      assertEquals(0,data.getShort(bytes.length-2));
    }
  }
  byte[] asset(int resource)throws IOException{
    try(InputStream in=context.getResources().openRawResource(resource);ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] block=new byte[4096];int n;while((n=in.read(block))!=-1)out.write(block,0,n);return out.toByteArray();
    }
  }
  @Test public void allCuesUseIdenticalRecordedHitAndGain()throws Exception{
    byte[] move=asset(R.raw.stone_move),capture=asset(R.raw.stone_capture),terminal=asset(R.raw.stone_terminal);
    assertArrayEquals(move,capture);
    byte[] hit=java.util.Arrays.copyOfRange(move,44,move.length);
    assertArrayEquals(hit,java.util.Arrays.copyOfRange(terminal,44,44+hit.length));
    int second=44+hit.length+3528*2;
    for(int i=44+hit.length;i<second;i++)assertEquals(0,terminal[i]);
    assertArrayEquals(hit,java.util.Arrays.copyOfRange(terminal,second,terminal.length));
    assertEquals(.65f,ChessSounds.PLAYBACK_VOLUME,0f);
  }
  @Test public void whiteAndBlackMovesUseSameCue(){
    create(new ChessGame());
    assertTrue(view.move(6,4,4,4,"-"));assertEquals(1,actions.plays);assertFalse(actions.capture);assertFalse(actions.terminal);
    assertTrue(view.move(1,4,3,4,"-"));assertEquals(2,actions.plays);assertFalse(actions.capture);assertFalse(actions.terminal);
  }
  @Test public void onlySuccessfulUnmutedMovesPlay(){
    create(new ChessGame());assertFalse(view.move(6,4,3,4,"-"));assertEquals(0,actions.plays);
    assertTrue(view.move(6,4,4,4,"-"));assertEquals(1,actions.plays);assertFalse(actions.capture);
    new GamePreferences(context).sound(false);
    assertTrue(view.move(1,4,3,4,"-"));assertEquals(1,actions.plays);
  }
  @Test public void capturesAndEnPassantUseCaptureSound(){
    create(BotIntegrationTest.position("e2e4","d7d5"));
    assertTrue(view.move(4,4,3,3,"-"));assertEquals(1,actions.plays);assertTrue(actions.capture);
    view.restore(BotIntegrationTest.position("e2e4","a7a6","e4e5","d7d5"),300000,300000);
    assertTrue(view.move(3,4,2,3,"-"));assertEquals(2,actions.plays);assertTrue(actions.capture);
  }
  @Test public void terminalMoveUsesTerminalCue(){
    create(BotIntegrationTest.position("f2f3","e7e5","g2g4"));
    assertTrue(view.move(0,3,4,7,"-"));assertEquals(1,actions.plays);assertTrue(actions.terminal);
  }
  @Test public void botUsesSameAudioPathAfterApprovedPresentationDelay()throws Exception{
    create(new ChessGame());BotIntegrationTest.Fake engine=new BotIntegrationTest.Fake("e2e4");
    view.configureBot(false,com.pixelchess.app.bot.BotDifficulty.EASY,()->engine);view.resumeBot();
    assertTrue(engine.entered.await(2,java.util.concurrent.TimeUnit.SECONDS));
    long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(3);
    while(!view.botMovePending()&&System.nanoTime()<deadline){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();Thread.sleep(5);}
    assertTrue(view.botMovePending());assertEquals(0,actions.plays);
    Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(2));
    assertEquals(1,actions.plays);assertFalse(actions.capture);assertEquals(500,view.animationDuration());
  }
}
