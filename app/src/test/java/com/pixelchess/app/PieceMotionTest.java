package com.pixelchess.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class PieceMotionTest {
  @Test public void arcPeaksMidMoveAndReturnsToSquare(){assertEquals(0f,PieceMotion.arc(0,100),.001f);assertEquals(10f,PieceMotion.arc(.5f,100),.01f);assertEquals(0f,PieceMotion.arc(1,100),.001f);}
  @Test public void easingAndProgressStayBounded(){assertEquals(0f,PieceMotion.progress(-4,200),0);assertEquals(1f,PieceMotion.progress(300,200),0);assertTrue(PieceMotion.eased(.5f)>.5f);assertEquals(0f,PieceMotion.captureScale(1),0);}

  @Test public void knightUsesLongAxisBeforeTurning(){
    assertTrue(PieceMotion.isKnightMove(7,1,5,2));
    assertEquals(1f,PieceMotion.knightColumn(7,1,5,2,0f),.001f);
    assertEquals(7f,PieceMotion.knightRow(7,1,5,2,0f),.001f);

    // At the corner the two-square vertical leg is complete; horizontal leg has not started.
    assertEquals(1f,PieceMotion.knightColumn(7,1,5,2,.67f),.001f);
    assertEquals(5f,PieceMotion.knightRow(7,1,5,2,.67f),.001f);

    assertEquals(2f,PieceMotion.knightColumn(7,1,5,2,1f),.001f);
    assertEquals(5f,PieceMotion.knightRow(7,1,5,2,1f),.001f);
  }

  @Test public void knightPathAlsoWorksWhenBoardCoordinatesAreFlipped(){
    assertTrue(PieceMotion.isKnightMove(0,6,2,5));
    assertEquals(6f,PieceMotion.knightColumn(0,6,2,5,.67f),.001f);
    assertEquals(2f,PieceMotion.knightRow(0,6,2,5,.67f),.001f);
    assertEquals(5f,PieceMotion.knightColumn(0,6,2,5,1f),.001f);
  }

  @Test public void nonKnightMoveIsNotClassifiedAsKnight(){
    assertFalse(PieceMotion.isKnightMove(6,4,4,4));
    assertFalse(PieceMotion.isKnightMove(7,0,7,7));
  }
}
