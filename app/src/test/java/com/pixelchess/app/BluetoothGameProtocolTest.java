package com.pixelchess.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class BluetoothGameProtocolTest {
  @Test public void setupCarriesProtocolVersion(){
    BluetoothGameProtocol.Time time=(BluetoothGameProtocol.Time)BluetoothGameProtocol.parse(BluetoothGameProtocol.time(10));
    assertEquals(10,time.minutes);
    assertEquals(BluetoothGameProtocol.VERSION,time.version);
  }

  @Test public void guestPlayContainsNoClockAuthority(){
    String raw=BluetoothGameProtocol.play(6,4,4,4,"-");
    BluetoothGameProtocol.Message parsed=BluetoothGameProtocol.parse(raw);
    assertTrue(parsed instanceof BluetoothGameProtocol.Play);
    BluetoothGameProtocol.Play play=(BluetoothGameProtocol.Play)parsed;
    assertEquals(6,play.r1);assertEquals(4,play.c1);
    assertEquals(4,play.r2);assertEquals(4,play.c2);
    assertEquals("-",play.promotion);
  }

  @Test public void authoritativeMoveRoundTrip(){
    String raw=BluetoothGameProtocol.move(6,4,4,4,"-",599000,600000,false,42);
    BluetoothGameProtocol.Move move=(BluetoothGameProtocol.Move)BluetoothGameProtocol.parse(raw);
    assertEquals(599000,move.whiteMs);
    assertEquals(600000,move.blackMs);
    assertFalse(move.whiteTurn);
    assertEquals(42,move.sequence);
  }

  @Test public void syncAndFlagCarrySequence(){
    BluetoothGameProtocol.Sync sync=(BluetoothGameProtocol.Sync)BluetoothGameProtocol.parse(
      BluetoothGameProtocol.sync(100,200,false,7));
    assertFalse(sync.whiteTurn);
    assertEquals(100,sync.whiteMs);
    assertEquals(200,sync.blackMs);
    assertEquals(7,sync.sequence);

    BluetoothGameProtocol.Flag flag=(BluetoothGameProtocol.Flag)BluetoothGameProtocol.parse(
      BluetoothGameProtocol.flag(true,8));
    assertTrue(flag.loserWhite);
    assertEquals(8,flag.sequence);
  }

  @Test public void malformedMessageIsIgnored(){
    assertNull(BluetoothGameProtocol.parse("MOVE,broken"));
    assertNull(BluetoothGameProtocol.parse("UNKNOWN,1"));
  }
}
