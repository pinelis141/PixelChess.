package com.pixelchess.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class BluetoothGameProtocolTest {
  @Test public void moveRoundTrip(){
    String raw=BluetoothGameProtocol.move(6,4,4,4,"-",599000,600000);
    BluetoothGameProtocol.Message parsed=BluetoothGameProtocol.parse(raw);
    assertTrue(parsed instanceof BluetoothGameProtocol.Move);
    BluetoothGameProtocol.Move move=(BluetoothGameProtocol.Move)parsed;
    assertEquals(6,move.r1);assertEquals(4,move.c1);
    assertEquals(4,move.r2);assertEquals(4,move.c2);
    assertEquals("-",move.promotion);
    assertEquals(599000,move.whiteMs);
    assertEquals(600000,move.blackMs);
  }

  @Test public void syncAndFlagRoundTrip(){
    BluetoothGameProtocol.Sync sync=(BluetoothGameProtocol.Sync)BluetoothGameProtocol.parse(BluetoothGameProtocol.sync(100,200,false));
    assertFalse(sync.whiteTurn);
    assertEquals(100,sync.whiteMs);
    assertEquals(200,sync.blackMs);
    BluetoothGameProtocol.Flag flag=(BluetoothGameProtocol.Flag)BluetoothGameProtocol.parse(BluetoothGameProtocol.flag(true));
    assertTrue(flag.loserWhite);
  }

  @Test public void malformedMessageIsIgnored(){
    assertNull(BluetoothGameProtocol.parse("MOVE,broken"));
    assertNull(BluetoothGameProtocol.parse("UNKNOWN,1"));
  }
}
