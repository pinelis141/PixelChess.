package com.pixelchess.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoveHistoryPresentationTest {
  private void move(ChessGame g,String from,String to){
    assertTrue(g.move(8-(from.charAt(1)-'0'),from.charAt(0)-'a',8-(to.charAt(1)-'0'),to.charAt(0)-'a',"-"));
  }

  @Test public void capturesUseXInsteadOfDash(){
    ChessGame g=new ChessGame();
    move(g,"e2","e4");
    move(g,"d7","d5");
    move(g,"e4","d5");
    assertEquals("2.e4xd5",g.history().get(2));
  }

  @Test public void truncatedHistoryShowsThatEarlierMovesExist(){
    ChessGame g=new ChessGame();
    move(g,"e2","e4");
    move(g,"d7","d5");
    move(g,"e4","d5");
    move(g,"g8","f6");
    move(g,"g1","f3");
    assertTrue(g.historyLine().startsWith("JOGADAS: …"));
    assertTrue(g.historyLine().contains("e4xd5"));
  }
}
