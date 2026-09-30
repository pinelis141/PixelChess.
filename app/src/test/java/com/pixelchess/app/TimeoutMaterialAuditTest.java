package com.pixelchess.app;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Timeout must not award a win to a side that cannot possibly mate with a bare king. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class TimeoutMaterialAuditTest {
  private ChessGame whiteQueenVersusBareBlackKing()throws Exception{
    ChessGame g=new ChessGame();
    Field boardField=ChessGame.class.getDeclaredField("board");boardField.setAccessible(true);
    String[][] b=(String[][])boardField.get(g);
    for(String[] row:b)Arrays.fill(row,null);
    b[7][4]="K";b[5][4]="Q";b[0][4]="k";
    for(String n:new String[]{"whiteKingMoved","blackKingMoved","whiteRookA","whiteRookH","blackRookA","blackRookH"}){
      Field f=ChessGame.class.getDeclaredField(n);f.setAccessible(true);f.set(g,true);
    }
    Field rep=ChessGame.class.getDeclaredField("repetitions");rep.setAccessible(true);((Map<?,?>)rep.get(g)).clear();
    return g;
  }

  @Test public void flagAgainstBareKingMustBeDraw()throws Exception{
    ChessView view=new ChessView(RuntimeEnvironment.getApplication(),BoardThemes.CLASSIC,5,false,true,new ChessViewSessionTest.Actions());
    view.gameState=whiteQueenVersusBareBlackKing();
    view.finishOnTime(true);
    assertTrue("Bare king cannot win on time",view.gameState.status().startsWith("EMPATE"));
    view.clock.removeCallbacksAndMessages(null);
  }
}
