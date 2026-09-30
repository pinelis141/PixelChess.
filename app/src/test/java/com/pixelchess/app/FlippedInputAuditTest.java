package com.pixelchess.app;

import android.content.Context;
import android.view.MotionEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class FlippedInputAuditTest {
  static class Actions implements ChessView.Actions{
    int requests,r1,c1,r2,c2;String promotion;
    public void requestMove(int a,int b,int c,int d,String p){requests++;r1=a;c1=b;r2=c;c2=d;promotion=p;}
    public void sendAuthorityMove(int a,int b,int c,int d,String p){}
    public void sendClockSync(){}public void sendFlag(boolean w){}public void sendReject(){}
    public boolean acceptSequence(long s){return true;}public void playSound(boolean c,boolean t){}public void desynchronized(){}
  }
  Context context;
  @Before public void prefs(){
    context=RuntimeEnvironment.getApplication();
    context.getSharedPreferences("game_options",Context.MODE_PRIVATE).edit().clear().commit();
  }
  private void tapLogical(ChessView v,int row,int col,boolean flipped){
    float den=v.getResources().getDisplayMetrics().density;
    v.boardGeometry.update(v.getWidth(),v.getHeight(),den,v.themeRenderer.hasScene());
    float square=v.boardGeometry.size/8f;
    int vr=flipped?7-row:row,vx=flipped?7-col:col;
    float x=v.boardGeometry.left+(vx+.5f)*square,y=v.boardGeometry.top+(vr+.5f)*square;
    MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_UP,x,y,0);
    v.onTouchEvent(e);e.recycle();
  }

  @Test public void localBlackAtBottomMapsBothSidesToCorrectLogicalSquares(){
    new GamePreferences(context).blackAtBottom(true);
    Actions actions=new Actions();
    ChessView v=new ChessView(context,BoardThemes.CLASSIC,5,false,true,actions);v.layout(0,0,1080,1920);
    tapLogical(v,6,4,true);tapLogical(v,4,4,true);
    assertEquals("P",v.gameState.pieceAt(4,4));
    tapLogical(v,1,4,true);tapLogical(v,3,4,true);
    assertEquals("p",v.gameState.pieceAt(3,4));
    v.clock.removeCallbacksAndMessages(null);
  }

  @Test public void bluetoothGuestUsesFlippedBoardAndRequestsBlackMove(){
    Actions actions=new Actions();
    ChessView v=new ChessView(context,BoardThemes.CLASSIC,5,true,false,actions);v.layout(0,0,1080,1920);
    BluetoothGameProtocol.Move white=(BluetoothGameProtocol.Move)BluetoothGameProtocol.parse(
        BluetoothGameProtocol.move(6,4,4,4,"-",299000,300000,false,1));
    v.applyAuthorityMove(white);
    assertFalse(v.gameState.whiteTurn());
    tapLogical(v,1,4,true);tapLogical(v,3,4,true);
    assertEquals(1,actions.requests);
    assertEquals(1,actions.r1);assertEquals(4,actions.c1);assertEquals(3,actions.r2);assertEquals(4,actions.c2);
    v.clock.removeCallbacksAndMessages(null);
  }
}
