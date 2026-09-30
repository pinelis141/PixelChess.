package com.pixelchess.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class ChessGameTest {
  @Test public void initialPositionAndBasicMoveRemainValid(){
    ChessGame game=new ChessGame();
    assertTrue(game.whiteTurn());
    assertEquals("P",game.pieceAt(6,4));
    assertTrue(game.isLegal(6,4,4,4));
    assertFalse(game.isLegal(6,4,3,4));
    assertTrue(game.move(6,4,4,4,"-"));
    assertFalse(game.whiteTurn());
    assertEquals("P",game.pieceAt(4,4));
    assertNull(game.pieceAt(6,4));
  }

  @Test public void foolsMateIsDetected(){
    ChessGame game=new ChessGame();
    assertTrue(game.move(6,5,5,5,"-")); // f2-f3
    assertTrue(game.move(1,4,3,4,"-")); // e7-e5
    assertTrue(game.move(6,6,4,6,"-")); // g2-g4
    assertTrue(game.move(0,3,4,7,"-")); // Qd8-h4
    assertTrue(game.gameOver());
    assertEquals("XEQUE-MATE • PRETAS VENCEM",game.status());
  }

  @Test public void resetRestoresFreshGame(){
    ChessGame game=new ChessGame();
    game.move(6,4,4,4,"-");
    game.reset();
    assertTrue(game.whiteTurn());
    assertFalse(game.gameOver());
    assertFalse(game.hasHistory());
    assertEquals("P",game.pieceAt(6,4));
  }
}
