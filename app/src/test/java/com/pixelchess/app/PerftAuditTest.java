package com.pixelchess.app;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Audit tests against established chess perft reference positions. */
public class PerftAuditTest {
  private int legalMoveCount(ChessGame g){
    int count=0;
    for(int r1=0;r1<8;r1++)for(int c1=0;c1<8;c1++){
      String p=g.pieceAt(r1,c1);
      if(p==null||ChessGame.isWhitePiece(p)!=g.whiteTurn())continue;
      for(int r2=0;r2<8;r2++)for(int c2=0;c2<8;c2++)if(g.isLegal(r1,c1,r2,c2)){
        boolean promotion=Character.toLowerCase(p.charAt(0))=='p'&&(r2==0||r2==7);
        count+=promotion?4:1;
      }
    }
    return count;
  }

  private long perft(ChessGame g,int depth){
    if(depth==0)return 1;
    long nodes=0;
    String transcript=g.transcript();
    for(int r1=0;r1<8;r1++)for(int c1=0;c1<8;c1++){
      String p=g.pieceAt(r1,c1);
      if(p==null||ChessGame.isWhitePiece(p)!=g.whiteTurn())continue;
      for(int r2=0;r2<8;r2++)for(int c2=0;c2<8;c2++){
        if(!g.isLegal(r1,c1,r2,c2))continue;
        boolean promotion=Character.toLowerCase(p.charAt(0))=='p'&&(r2==0||r2==7);
        String[] promos=promotion?new String[]{"Q","R","B","N"}:new String[]{"-"};
        for(String promo:promos){
          ChessGame copy=ChessGame.replay(transcript);
          assertTrue(copy.move(r1,c1,r2,c2,promo));
          nodes+=perft(copy,depth-1);
        }
      }
    }
    return nodes;
  }

  private ChessGame fen(String fen)throws Exception{
    String[] parts=fen.split(" ");
    ChessGame g=new ChessGame();
    Field boardField=ChessGame.class.getDeclaredField("board");boardField.setAccessible(true);
    String[][] board=(String[][])boardField.get(g);
    for(String[] row:board)Arrays.fill(row,null);
    String[] ranks=parts[0].split("/");
    for(int r=0;r<8;r++){
      int c=0;
      for(char ch:ranks[r].toCharArray()){
        if(Character.isDigit(ch))c+=ch-'0';
        else board[r][c++]=""+ch;
      }
    }
    set(g,"whiteTurn",parts[1].equals("w"));
    String rights=parts[2];
    set(g,"whiteKingMoved",!rights.contains("K")&&!rights.contains("Q"));
    set(g,"blackKingMoved",!rights.contains("k")&&!rights.contains("q"));
    set(g,"whiteRookH",!rights.contains("K")); set(g,"whiteRookA",!rights.contains("Q"));
    set(g,"blackRookH",!rights.contains("k")); set(g,"blackRookA",!rights.contains("q"));
    if(parts[3].equals("-")){set(g,"epRow",-1);set(g,"epCol",-1);}
    else{set(g,"epCol",parts[3].charAt(0)-'a');set(g,"epRow",8-(parts[3].charAt(1)-'0'));}
    set(g,"gameOver",false);set(g,"halfmove",0);
    Field rep=ChessGame.class.getDeclaredField("repetitions");rep.setAccessible(true);((Map<?,?>)rep.get(g)).clear();
    return g;
  }

  private void set(ChessGame g,String name,Object value)throws Exception{
    Field f=ChessGame.class.getDeclaredField(name);f.setAccessible(true);f.set(g,value);
  }

  @Test public void initialPositionMatchesReferencePerft(){
    ChessGame g=new ChessGame();
    assertEquals(20,perft(g,1));
    assertEquals(400,perft(g,2));
    assertEquals(8902,perft(g,3));
  }

  @Test public void kiwipeteLegalMoveCountMatchesReference()throws Exception{
    ChessGame g=fen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
    assertEquals(48,legalMoveCount(g));
  }

  @Test public void rookPawnEndgameLegalMoveCountMatchesReference()throws Exception{
    ChessGame g=fen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
    assertEquals(14,legalMoveCount(g));
  }
  @Test public void additionalReferencePositionsMatchDepthOne()throws Exception{
    assertEquals(6,legalMoveCount(fen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1")));
    assertEquals(44,legalMoveCount(fen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8")));
    assertEquals(46,legalMoveCount(fen("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10")));
  }

}
