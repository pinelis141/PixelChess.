package com.pixelchess.app;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class ForestLeafMotionTest {
  @Test public void longRunningPathsStayFiniteAndFramesStayValid(){
    for(int i=0;i<ForestLeafMotion.COUNT;i++)for(double t=0;t<3600;t+=.37){
      assertTrue(Float.isFinite(ForestLeafMotion.x(i,t)));
      assertTrue(ForestLeafMotion.x(i,t)>-.15f && ForestLeafMotion.x(i,t)<1.15f);
      assertTrue(ForestLeafMotion.alpha(i,t)>=0 && ForestLeafMotion.alpha(i,t)<=1);
      assertTrue(ForestLeafMotion.frame(i,t)>=0 && ForestLeafMotion.frame(i,t)<8);
      if(ForestLeafMotion.visible(i,t))assertTrue(ForestLeafMotion.y(i,t)>=-.051f && ForestLeafMotion.y(i,t)<=1.051f);
    }
  }
  @Test public void actualImageSequenceAdvancesThroughAllEightFrames(){
    for(int i=0;i<4;i++){
      Set<Integer> frames=new HashSet<>();
      for(double t=i*2.63;t<i*2.63+3;t+=.05)frames.add(ForestLeafMotion.frame(i,t));
      assertEquals(8,frames.size());
    }
  }
  @Test public void leavesFadeAndHaveQuietIntervalsInsteadOfContinuousRespawn(){
    assertEquals(0,ForestLeafMotion.alpha(0,0),.0001);
    assertTrue(ForestLeafMotion.visible(0,1));assertEquals(1,ForestLeafMotion.alpha(0,1),.0001);
    assertTrue(ForestLeafMotion.alpha(0,9)<.3);
    assertFalse(ForestLeafMotion.visible(0,12));assertEquals(0,ForestLeafMotion.alpha(0,12),.0001);
    assertNotEquals(ForestLeafMotion.y(0,8),ForestLeafMotion.y(1,8),.01);
  }
  @Test public void occasionalWindIsSmoothAndAlternatesDirection(){
    assertEquals(0,ForestLeafMotion.gust(0),0);assertEquals(0,ForestLeafMotion.gust(20),0);
    assertTrue(ForestLeafMotion.gust(10)>0);assertTrue(ForestLeafMotion.gust(39)<0);
    assertEquals(ForestLeafMotion.x(3,8-.001),ForestLeafMotion.x(3,8+.001),.001);
    assertEquals(ForestLeafMotion.x(3,12.5-.001),ForestLeafMotion.x(3,12.5+.001),.001);
  }
}
