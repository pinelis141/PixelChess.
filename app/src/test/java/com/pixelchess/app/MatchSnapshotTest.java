package com.pixelchess.app;

import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

public class MatchSnapshotTest {
  private static final String ID="12345678-1234-1234-1234-123456789abc";
  private void move(ChessGame g,String from,String to){assertTrue(g.move(8-(from.charAt(1)-'0'),from.charAt(0)-'a',8-(to.charAt(1)-'0'),to.charAt(0)-'a',"-"));}
  private ChessGame restored(ChessGame g){return MatchSnapshot.parse(new MatchSnapshot(ID,5,240000,250000,g.transcript()).encode(),ID).restore();}
  @Test public void replayRetainsBoardTurnHistoryAndEnPassant(){
    ChessGame g=new ChessGame();move(g,"e2","e4");move(g,"a7","a6");move(g,"e4","e5");move(g,"d7","d5");ChessGame copy=restored(g);
    assertEquals(g.history(),copy.history());assertEquals(g.whiteTurn(),copy.whiteTurn());assertTrue(copy.isLegal(3,4,2,3));
    move(copy,"e5","d6");assertNull(copy.pieceAt(3,3));assertEquals("p",g.pieceAt(3,3));
  }
  @Test public void replayRetainsCastlingRights(){
    ChessGame g=new ChessGame();move(g,"e2","e4");move(g,"e7","e5");move(g,"g1","f3");move(g,"b8","c6");move(g,"f1","e2");move(g,"g8","f6");
    ChessGame copy=restored(g);assertTrue(copy.isLegal(7,4,7,6));move(copy,"e1","g1");assertEquals("R",copy.pieceAt(7,5));
  }
  @Test public void replayRetainsRepetitionCount(){
    ChessGame g=new ChessGame();move(g,"g1","f3");move(g,"g8","f6");move(g,"f3","g1");move(g,"f6","g8");
    g=restored(g);move(g,"g1","f3");move(g,"g8","f6");move(g,"f3","g1");move(g,"f6","g8");assertEquals("EMPATE • REPETIÇÃO TRIPLA",g.status());
  }
  @Test public void nonCapturableEnPassantDoesNotChangeRepetitionIdentity(){
    ChessGame g=new ChessGame();move(g,"h2","h4");for(int i=0;i<2;i++){move(g,"g8","f6");move(g,"g1","f3");move(g,"f6","g8");move(g,"f3","g1");}assertEquals("EMPATE • REPETIÇÃO TRIPLA",g.status());
  }
  @Test public void timeoutSurvivesResume(){MatchSnapshot s=new MatchSnapshot(ID,5,0,10000,"-");assertEquals("TEMPO • PRETAS VENCEM",s.restore().status());}
  @Test(expected=IllegalArgumentException.class) public void rejectAnotherGame(){MatchSnapshot.parse(new MatchSnapshot(ID,5,1,1,"-").encode(),"another-game");}
  @Test(expected=IllegalArgumentException.class) public void rejectIllegalReplay(){ChessGame.replay("1434-");}
  @Test(expected=IllegalArgumentException.class) public void rejectNegativeClock(){new MatchSnapshot(ID,5,-1,1,"-");}
  @Test(expected=IllegalArgumentException.class) public void rejectDifferentProtocolVersion(){MatchSnapshot.parse("STATE,2,"+ID+",5,1,1,-",ID);}
  @Test public void exactFramesAndInvalidGameplayPackets()throws Exception{
    BufferedReader reader=new BufferedReader(new StringReader("HELLO,3,NEW\nACK,test\n"));assertEquals("HELLO,3,NEW",BluetoothMatchController.frame(reader));assertEquals("ACK,test",BluetoothMatchController.frame(reader));assertNull(BluetoothMatchController.frame(reader));
    assertNull(BluetoothGameProtocol.parse("PLAY,9,0,4,4,-"));assertNull(BluetoothGameProtocol.parse("PLAY,6,0,4,0,K"));assertNull(BluetoothGameProtocol.parse("SYNC,-1,100,W,1"));assertNull(BluetoothGameProtocol.parse("SYNC,1,100,X,1"));assertNull(BluetoothGameProtocol.parse("FLAG,W,0"));
  }
}
