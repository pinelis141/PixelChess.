package com.pixelchess.app;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Positions belong only to tests; production has no backdoor to alter a board. */
public class EndgameRegressionTest {
  static ChessGame position(String... rows)throws Exception{
    ChessGame g=new ChessGame();Field f=ChessGame.class.getDeclaredField("board");f.setAccessible(true);
    String[][] b=(String[][])f.get(g);
    for(int r=0;r<8;r++){Arrays.fill(b[r],null);for(int c=0;c<8;c++)if(rows[r].charAt(c)!='.')b[r][c]=""+rows[r].charAt(c);}
    for(String right:new String[]{"whiteKingMoved","blackKingMoved","whiteRookA","whiteRookH","blackRookA","blackRookH"})set(g,right,true);
    Field rep=ChessGame.class.getDeclaredField("repetitions");rep.setAccessible(true);((Map<?,?>)rep.get(g)).clear();return g;
  }
  static void set(ChessGame g,String name,Object value)throws Exception{Field f=ChessGame.class.getDeclaredField(name);f.setAccessible(true);f.set(g,value);}
  @Test public void stalemateIsDrawWithoutCheck()throws Exception{
    ChessGame g=position("k.......","........","..K.....",".Q......","........","........","........","........");
    assertTrue(g.move(3,1,2,1,"-"));assertFalse(g.inCheck(false));assertTrue(g.gameOver());assertEquals("EMPATE • AFOGAMENTO",g.status());
  }
  @Test public void sameKingCageWithCheckIsMate()throws Exception{
    ChessGame g=position("k.......","........","..K.....",".Q......","........","........","........","........");
    assertTrue(g.move(3,1,1,1,"-"));assertTrue(g.inCheck(false));assertEquals("XEQUE-MATE • BRANCAS VENCEM",g.status());
  }
  @Test public void fiftyMoveThresholdIsExactlyOneHundredHalfmoves()throws Exception{
    ChessGame g=position("......rk","........","........","........","........","........","........","KR......");
    set(g,"halfmove",98);assertTrue(g.move(7,1,6,1,"-"));assertFalse(g.gameOver());assertTrue(g.move(0,6,1,6,"-"));assertEquals("EMPATE • REGRA DOS 50 LANCES",g.status());
  }
  @Test public void pawnMoveAndCaptureResetFiftyMoveCounter()throws Exception{
    ChessGame g=position("......rk","........","........","........","........","........","..P.....","KR......");
    set(g,"halfmove",99);assertTrue(g.move(6,2,5,2,"-"));assertFalse(g.gameOver());
    g=position("......rk","........","........","........","........",".n......","........","KR......");
    set(g,"halfmove",99);assertTrue(g.move(7,1,5,1,"-"));assertFalse(g.gameOver());
  }
  @Test public void bareKingsAndSingleMinorPieceAreInsufficient()throws Exception{
    for(String back:new String[]{"K.......","K.B.....","K.N....."}){
      ChessGame g=position(".......k","........","........","........","........","........","........",back);
      assertTrue(g.move(7,0,6,0,"-"));assertEquals("EMPATE • MATERIAL INSUFICIENTE",g.status());
    }
  }
  @Test public void bishopsOnSameColorAreInsufficientButOppositeColorsAreNot()throws Exception{
    ChessGame g=position(".....b.k","........","........","........","........","........","........","K.B.....");
    assertTrue(g.move(7,0,6,0,"-"));assertEquals("EMPATE • MATERIAL INSUFICIENTE",g.status());
    g=position("....b..k","........","........","........","........","........","........","K.B.....");
    assertTrue(g.move(7,0,6,0,"-"));assertFalse(g.gameOver());
  }
  @Test public void wrongTurnAndInvalidPromotionDoNotMutateBoard()throws Exception{
    ChessGame g=new ChessGame();assertFalse(g.move(1,4,3,4,"-"));assertEquals("p",g.pieceAt(1,4));assertTrue(g.whiteTurn());
    g=position(".......k","P.......","........","........","........","........","........","K.......");
    assertFalse(g.move(1,0,0,0,"K"));assertEquals("P",g.pieceAt(1,0));assertTrue(g.move(1,0,0,0,"r"));assertEquals("R",g.pieceAt(0,0));
  }
}
