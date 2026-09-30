package com.pixelchess.app;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Product contracts that should remain true even when UI/protocol callers misbehave. */
public class GameplayContractAuditTest {
  private static ChessGame promotionPosition()throws Exception{
    ChessGame g=new ChessGame();
    Field boardField=ChessGame.class.getDeclaredField("board");boardField.setAccessible(true);
    String[][] b=(String[][])boardField.get(g);
    for(String[] row:b)Arrays.fill(row,null);
    b[0][7]="k";b[1][0]="P";b[7][0]="K";
    for(String n:new String[]{"whiteKingMoved","blackKingMoved","whiteRookA","whiteRookH","blackRookA","blackRookH"}){
      Field f=ChessGame.class.getDeclaredField(n);f.setAccessible(true);f.set(g,true);
    }
    Field rep=ChessGame.class.getDeclaredField("repetitions");rep.setAccessible(true);((Map<?,?>)rep.get(g)).clear();
    return g;
  }

  @Test public void promotionMustRequireAnExplicitPieceChoice()throws Exception{
    ChessGame g=promotionPosition();
    assertTrue(g.isLegal(1,0,0,0));
    assertFalse("A promoting move with '-' must not silently become a queen",g.move(1,0,0,0,"-"));
    assertEquals("P",g.pieceAt(1,0));
    assertNull(g.pieceAt(0,0));
  }
}
