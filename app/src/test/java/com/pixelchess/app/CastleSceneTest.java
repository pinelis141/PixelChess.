package com.pixelchess.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class CastleSceneTest {
  @Test public void sceneAndHudFitSupportedPortraitSizes() {
    float[][] sizes={{320,480},{360,640},{393,760},{412,870},{600,960}};
    for(float[] size:sizes) {
      SceneGeometry g=new SceneGeometry();g.update(size[0],size[1],1,true);
      BoardTheme.Scene scene=BoardThemes.CASTLE.scene;
      assertEquals(g.upperEnd,g.mapY(scene.boardTop,scene),.001f);
      assertEquals(g.lowerStart,g.mapY(scene.boardBottom,scene),.001f);
      assertEquals(size[1],g.mapY(1,scene),.001f);
      assertTrue(g.left>=18 && g.size>0);
      assertTrue(g.top+g.size+140<=size[1]);
      float clockY=g.mapY(scene.topClock,scene);
      float clockHalfHeight=Math.min(190*Math.min(1,g.upperEnd/170),size[0]*.54f)/6;
      assertTrue(clockY-clockHalfHeight>=0);
      // Crown begins near 51% of the upper scene; clock must not cover it.
      assertTrue(clockY+clockHalfHeight<g.upperEnd*.51f);
      for(BoardTheme.Torch torch:scene.torches) {
        float y=g.mapY(torch.y,scene);
        assertTrue(y<g.top || y>g.top+g.size);
      }
    }
  }
  @Test public void forestKeepsItsOriginalBoardGeometry() {
    SceneGeometry g=new SceneGeometry();g.update(412,870,1,false);
    assertEquals(18,g.left,0);assertEquals(376,g.size,0);assertEquals(217,g.top,0);
  }
  @Test public void fireRemainsBoundedAndAsynchronousOverAnHour() {
    boolean different=false;
    for(int step=0;step<36000;step+=11) {
      double t=step*.1;
      for(int i=0;i<4;i++) {
        float light=TorchMotion.brightness(i,t);
        assertTrue(light>=.52f && light<=1);
        assertTrue(Math.abs(TorchMotion.sway(i,t))<=1.001f);
        assertTrue(Math.abs(light-TorchMotion.brightness(i,t+.05))<.06f);
        for(int col=-3;col<=3;col++) {
          float h=TorchMotion.height(i,col,t);assertTrue(h>0 && h<=1.001f);
        }
      }
      if(Math.abs(TorchMotion.brightness(0,t)-TorchMotion.brightness(1,t))>.1) different=true;
    }
    assertTrue(different);
  }
  @Test public void sceneCopiesItsAnchors() {
    BoardTheme.Torch[] input={new BoardTheme.Torch(.1f,.1f)};
    BoardTheme.Scene scene=new BoardTheme.Scene(.25f,.75f,.08f,input);
    input[0]=new BoardTheme.Torch(.9f,.9f);
    assertEquals(.1f,scene.torches.get(0).x,0);
  }
  @Test(expected=IllegalArgumentException.class) public void rejectTorchInPlayableRegion() {
    new BoardTheme.Scene(.25f,.75f,.08f,new BoardTheme.Torch(.1f,.5f));
  }
  @Test(expected=IllegalArgumentException.class) public void rejectFireWithoutScene() {
    BoardTheme.builder("invalid","Invalid",1)
        .glow(new BoardTheme.Glow(BoardTheme.Effect.TORCHES,0xffaa4422,1,1)).build();
  }
}
