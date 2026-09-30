package com.pixelchess.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class ChessRulesRegressionTest {
  private void move(ChessGame g,String from,String to,String promotion){int c1=from.charAt(0)-'a',r1=8-(from.charAt(1)-'0'),c2=to.charAt(0)-'a',r2=8-(to.charAt(1)-'0');assertTrue(from+to,g.move(r1,c1,r2,c2,promotion));}
  @Test public void castlesKingsideAfterPathIsCleared(){ChessGame g=new ChessGame();move(g,"e2","e4","-");move(g,"e7","e5","-");move(g,"g1","f3","-");move(g,"b8","c6","-");move(g,"f1","e2","-");move(g,"g8","f6","-");assertTrue(g.isLegal(7,4,7,6));move(g,"e1","g1","-");assertEquals("K",g.pieceAt(7,6));assertEquals("R",g.pieceAt(7,5));}
  @Test public void enPassantRemovesThePassedPawn(){ChessGame g=new ChessGame();move(g,"e2","e4","-");move(g,"a7","a6","-");move(g,"e4","e5","-");move(g,"d7","d5","-");assertTrue(g.isLegal(3,4,2,3));move(g,"e5","d6","-");assertEquals("P",g.pieceAt(2,3));assertNull(g.pieceAt(3,3));}
  @Test public void promotionAllowsAChosenPiece(){ChessGame g=new ChessGame();move(g,"a2","a4","-");move(g,"b8","c6","-");move(g,"a4","a5","-");move(g,"h7","h5","-");move(g,"a5","a6","-");move(g,"h5","h4","-");move(g,"a6","b7","-");move(g,"c6","a5","-");move(g,"b7","b8","Q");assertEquals("Q",g.pieceAt(0,1));}
  @Test public void repetitionThreeTimesEndsDraw(){ChessGame g=new ChessGame();for(int i=0;i<2;i++){move(g,"g1","f3","-");move(g,"g8","f6","-");move(g,"f3","g1","-");move(g,"f6","g8","-");}assertTrue(g.gameOver());assertEquals("EMPATE • REPETIÇÃO TRIPLA",g.status());}
  @Test public void openSlidingPiecesRemainLegalForBothColors(){
    ChessGame bishops=new ChessGame();
    move(bishops,"e2","e4","-");move(bishops,"e7","e5","-");
    assertTrue("white bishop path",bishops.isLegal(7,5,4,2));
    assertTrue("black bishop path",bishops.isLegal(0,5,3,2));

    ChessGame queens=new ChessGame();
    move(queens,"d2","d4","-");move(queens,"d7","d5","-");
    assertTrue("white queen path",queens.isLegal(7,3,5,3));
    assertTrue("black queen path",queens.isLegal(0,3,2,3));

    ChessGame rooks=new ChessGame();
    move(rooks,"a2","a4","-");move(rooks,"a7","a5","-");
    assertTrue("white rook path",rooks.isLegal(7,0,5,0));
    assertTrue("black rook path",rooks.isLegal(0,0,2,0));
  }

}
