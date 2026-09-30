package com.pixelchess.app;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Second-pass adversarial audit. Never ships production backdoors. */
public class DeepRulesAuditTest {
  private static final String[] BOOLS={
    "whiteTurn","whiteKingMoved","blackKingMoved","whiteRookA","whiteRookH",
    "blackRookA","blackRookH","gameOver"
  };
  private static final String[] INTS={"epRow","epCol","halfmove","lastR1","lastC1","lastR2","lastC2"};

  private static Field field(String name)throws Exception{
    Field f=ChessGame.class.getDeclaredField(name);f.setAccessible(true);return f;
  }
  private static void set(ChessGame g,String name,Object value)throws Exception{field(name).set(g,value);}

  private ChessGame fen(String fen)throws Exception{
    String[] parts=fen.split(" ");
    ChessGame g=new ChessGame();
    String[][] board=(String[][])field("board").get(g);
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
    set(g,"whiteRookH",!rights.contains("K"));set(g,"whiteRookA",!rights.contains("Q"));
    set(g,"blackRookH",!rights.contains("k"));set(g,"blackRookA",!rights.contains("q"));
    if(parts[3].equals("-")){set(g,"epRow",-1);set(g,"epCol",-1);}
    else{set(g,"epCol",parts[3].charAt(0)-'a');set(g,"epRow",8-(parts[3].charAt(1)-'0'));}
    set(g,"gameOver",false);set(g,"halfmove",Integer.parseInt(parts[4]));
    ((Map<?,?>)field("repetitions").get(g)).clear();
    ((java.util.List<?>)field("history").get(g)).clear();
    ((java.util.List<?>)field("moves").get(g)).clear();
    return g;
  }

  @SuppressWarnings("unchecked")
  private ChessGame copy(ChessGame source)throws Exception{
    ChessGame g=new ChessGame();
    String[][] src=(String[][])field("board").get(source),dst=(String[][])field("board").get(g);
    for(int r=0;r<8;r++)System.arraycopy(src[r],0,dst[r],0,8);
    for(String n:BOOLS)set(g,n,field(n).get(source));
    for(String n:INTS)set(g,n,field(n).get(source));
    set(g,"status",field("status").get(source));
    Map<String,Integer> srcRep=(Map<String,Integer>)field("repetitions").get(source);
    Map<String,Integer> dstRep=(Map<String,Integer>)field("repetitions").get(g);
    dstRep.clear();dstRep.putAll(srcRep);
    java.util.List<String> sh=(java.util.List<String>)field("history").get(source);
    java.util.List<String> dh=(java.util.List<String>)field("history").get(g);
    dh.clear();dh.addAll(sh);
    java.util.List<String> sm=(java.util.List<String>)field("moves").get(source);
    java.util.List<String> dm=(java.util.List<String>)field("moves").get(g);
    dm.clear();dm.addAll(sm);
    return g;
  }

  private long perft(ChessGame g,int depth)throws Exception{
    if(depth==0)return 1;
    long nodes=0;
    for(int r1=0;r1<8;r1++)for(int c1=0;c1<8;c1++){
      String p=g.pieceAt(r1,c1);
      if(p==null||ChessGame.isWhitePiece(p)!=g.whiteTurn())continue;
      for(int r2=0;r2<8;r2++)for(int c2=0;c2<8;c2++){
        if(!g.isLegal(r1,c1,r2,c2))continue;
        boolean promotion=Character.toLowerCase(p.charAt(0))=='p'&&(r2==0||r2==7);
        String[] promos=promotion?new String[]{"Q","R","B","N"}:new String[]{"-"};
        for(String promo:promos){
          ChessGame child=copy(g);
          assertTrue(child.move(r1,c1,r2,c2,promo));
          nodes+=perft(child,depth-1);
        }
      }
    }
    return nodes;
  }

  private String boardKey(ChessGame g){
    StringBuilder b=new StringBuilder();
    for(int r=0;r<8;r++)for(int c=0;c<8;c++)b.append(g.pieceAt(r,c)==null?".":g.pieceAt(r,c));
    return b.toString();
  }

  @Test public void initialPositionMatchesReferenceThroughDepthFour()throws Exception{
    assertEquals(197281L,perft(new ChessGame(),4));
  }

  @Test public void complexReferencePositionsMatchDeeperPerft()throws Exception{
    assertEquals(2039L,perft(fen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"),2));
    assertEquals(97862L,perft(fen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"),3));
    assertEquals(2812L,perft(fen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"),3));
    assertEquals(9467L,perft(fen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"),3));
    assertEquals(62379L,perft(fen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"),3));
    assertEquals(89890L,perft(fen("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10"),3));
  }

  @Test public void legalityQueriesNeverMutateLiveState(){
    ChessGame g=new ChessGame();
    assertTrue(g.move(6,4,4,4,"-"));
    assertTrue(g.move(1,3,3,3,"-"));
    String board=boardKey(g),transcript=g.transcript(),status=g.status();
    boolean turn=g.whiteTurn();
    java.util.List<String> history=new java.util.ArrayList<>(g.history());
    for(int repeat=0;repeat<5;repeat++)
      for(int r1=0;r1<8;r1++)for(int c1=0;c1<8;c1++)
        for(int r2=0;r2<8;r2++)for(int c2=0;c2<8;c2++)g.isLegal(r1,c1,r2,c2);
    assertEquals(board,boardKey(g));
    assertEquals(transcript,g.transcript());
    assertEquals(status,g.status());
    assertEquals(turn,g.whiteTurn());
    assertEquals(history,g.history());
  }
}
