package com.pixelchess.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/** Deterministic property-style games to catch state/replay drift not covered by scripted positions. */
public class RandomGameAuditTest {
  static final class Candidate{
    final int r1,c1,r2,c2;final String promotion;
    Candidate(int r1,int c1,int r2,int c2,String p){this.r1=r1;this.c1=c1;this.r2=r2;this.c2=c2;promotion=p;}
  }

  private List<Candidate> legal(ChessGame g){
    ArrayList<Candidate> out=new ArrayList<>();
    for(int r1=0;r1<8;r1++)for(int c1=0;c1<8;c1++){
      String p=g.pieceAt(r1,c1);
      if(p==null||ChessGame.isWhitePiece(p)!=g.whiteTurn())continue;
      for(int r2=0;r2<8;r2++)for(int c2=0;c2<8;c2++){
        if(!g.isLegal(r1,c1,r2,c2))continue;
        boolean promotion=Character.toLowerCase(p.charAt(0))=='p'&&(r2==0||r2==7);
        if(promotion)for(String q:new String[]{"Q","R","B","N"})out.add(new Candidate(r1,c1,r2,c2,q));
        else out.add(new Candidate(r1,c1,r2,c2,"-"));
      }
    }
    return out;
  }

  private String board(ChessGame g){
    StringBuilder b=new StringBuilder();
    for(int r=0;r<8;r++)for(int c=0;c<8;c++)b.append(g.pieceAt(r,c)==null?".":g.pieceAt(r,c));
    return b.toString();
  }

  private void assertExactlyOneKingEach(ChessGame g){
    int white=0,black=0;
    for(int r=0;r<8;r++)for(int c=0;c<8;c++){
      String p=g.pieceAt(r,c);
      if("K".equals(p))white++;
      if("k".equals(p))black++;
    }
    assertEquals(1,white);assertEquals(1,black);
  }

  private void assertReplayEqual(ChessGame g){
    ChessGame replay=ChessGame.replay(g.transcript());
    assertEquals(board(g),board(replay));
    assertEquals(g.whiteTurn(),replay.whiteTurn());
    assertEquals(g.gameOver(),replay.gameOver());
    assertEquals(g.status(),replay.status());
    assertEquals(g.history(),replay.history());
    assertEquals(g.hasLastMove(),replay.hasLastMove());
    if(g.hasLastMove()){
      assertEquals(g.lastFromRow(),replay.lastFromRow());assertEquals(g.lastFromCol(),replay.lastFromCol());
      assertEquals(g.lastToRow(),replay.lastToRow());assertEquals(g.lastToCol(),replay.lastToCol());
    }
  }

  @Test public void deterministicRandomGamesPreserveAllCoreInvariants(){
    for(int gameIndex=0;gameIndex<60;gameIndex++){
      Random random=new Random(0x5EEDC0DEL+gameIndex);
      ChessGame g=new ChessGame();
      for(int ply=0;ply<220&&!g.gameOver();ply++){
        List<Candidate> moves=legal(g);
        assertFalse("Non-terminal game must have a legal move",moves.isEmpty());
        Candidate m=moves.get(random.nextInt(moves.size()));
        boolean moverWhite=g.whiteTurn();
        assertTrue(g.move(m.r1,m.c1,m.r2,m.c2,m.promotion));
        assertExactlyOneKingEach(g);
        assertFalse("A legal move may not leave the mover in check",g.inCheck(moverWhite));
        if((ply%7)==0||g.gameOver())assertReplayEqual(g);
      }
      assertReplayEqual(g);
    }
  }
}
