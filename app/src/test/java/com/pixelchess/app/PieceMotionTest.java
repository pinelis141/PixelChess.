package com.pixelchess.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class PieceMotionTest {
  @Test public void arcPeaksMidMoveAndReturnsToSquare(){assertEquals(0f,PieceMotion.arc(0,100),.001f);assertEquals(10f,PieceMotion.arc(.5f,100),.01f);assertEquals(0f,PieceMotion.arc(1,100),.001f);}
  @Test public void easingAndProgressStayBounded(){assertEquals(0f,PieceMotion.progress(-4,200),0);assertEquals(1f,PieceMotion.progress(300,200),0);assertTrue(PieceMotion.eased(.5f)>.5f);assertEquals(0f,PieceMotion.captureScale(1),0);}
}
