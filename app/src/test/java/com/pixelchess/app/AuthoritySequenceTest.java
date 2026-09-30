package com.pixelchess.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class AuthoritySequenceTest {
  @Test public void hostSequenceAlwaysIncreases(){
    AuthoritySequence sequence=new AuthoritySequence();
    assertEquals(1,sequence.next());
    assertEquals(2,sequence.next());
    assertEquals(3,sequence.next());
  }

  @Test public void guestRejectsStaleOrDuplicatePackets(){
    AuthoritySequence sequence=new AuthoritySequence();
    assertTrue(sequence.accept(5));
    assertFalse(sequence.accept(5));
    assertFalse(sequence.accept(4));
    assertTrue(sequence.accept(6));
    assertEquals(6,sequence.lastRemote());
  }

  @Test public void resetStartsFreshSession(){
    AuthoritySequence sequence=new AuthoritySequence();
    sequence.next();
    sequence.accept(9);
    sequence.reset();
    assertEquals(1,sequence.next());
    assertTrue(sequence.accept(1));
  }
}
