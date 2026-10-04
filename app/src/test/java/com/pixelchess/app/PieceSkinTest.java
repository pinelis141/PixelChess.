package com.pixelchess.app;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
public class PieceSkinTest {
 @Test public void neverOfferGeneratedPlaceholders(){
   assertEquals(3,PieceSkin.values().length);
   assertEquals(PieceSkin.CLASSIC,PieceSkin.from("guardians"));
   assertEquals(PieceSkin.CLASSIC,PieceSkin.from("obsidian"));
   assertEquals(PieceSkin.MEDIEVAL,PieceSkin.from("medieval"));
   assertEquals(PieceSkin.FOREST,PieceSkin.from("floresta"));
 }
 @Test public void independentChoicesPersist(){
   Context c=RuntimeEnvironment.getApplication();PieceSkin old=PieceSkin.load(c);
   try{
     PieceSkin.MEDIEVAL.save(c);assertEquals(PieceSkin.MEDIEVAL,PieceSkin.load(c));
     PieceSkin.FOREST.save(c);assertEquals(PieceSkin.FOREST,PieceSkin.load(c));
   }finally{old.save(c);}
 }
}