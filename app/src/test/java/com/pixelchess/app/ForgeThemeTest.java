package com.pixelchess.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class ForgeThemeTest {
  @Test public void approvedForgeIsSelectableWithSeparateAssets() {
    assertSame(BoardThemes.FORGE,BoardThemes.find("forge"));
    assertEquals(R.drawable.forge_board,BoardThemes.FORGE.boardRes);
    assertEquals(BoardTheme.Effect.LAVA,BoardThemes.FORGE.glow.effect);
    assertTrue(BoardThemes.FORGE.glow.animated());
    assertNotEquals(BoardThemes.FORGE.boardRes,BoardThemes.FORGE.frameRes);
    assertNotNull(BoardThemes.FORGE.furnace);
  }
  @Test public void flowAndFurnaceRepeatWithoutJumping() {
    for(int i=0;i<800;i++) {
      double t=i*.01;
      assertEquals(LavaMotion.phase(t),LavaMotion.phase(t+LavaMotion.PERIOD),.00001f);
      assertEquals(LavaMotion.furnace(t),LavaMotion.furnace(t+LavaMotion.PERIOD),.00001f);
      assertTrue(LavaMotion.furnace(t)>=.48f && LavaMotion.furnace(t)<=.96f);
    }
    assertEquals(LavaMotion.furnace(0),LavaMotion.furnace(7.99999),.0001f);
    assertTrue(LavaMotion.phase(1)<LavaMotion.phase(2));
  }
  @Test public void maskKeepsHotLavaAndRejectsStoneAndMetal() {
    assertTrue(LavaMotion.maskAlpha(0xffff8015)>150);
    assertEquals(0,LavaMotion.maskAlpha(0xff141b22));
    assertEquals(0,LavaMotion.maskAlpha(0xffbbaa88));
    assertEquals(0,LavaMotion.maskAlpha(0xffc07840));
    assertEquals(0,LavaMotion.maskAlpha(0x00ff8015));
    assertTrue(LavaMotion.maskAlpha(0x80ff8015)<=128);
  }
  @Test public void furnaceAndClockFitPortraitLayout() {
    float[][] sizes={{320,480},{360,640},{393,760},{412,870},{600,960}};
    BoardTheme t=BoardThemes.FORGE;
    for(float[] size:sizes) {
      SceneGeometry g=new SceneGeometry();g.update(size[0],size[1],1,true);
      float scale=Math.min(1,g.upperEnd/170);
      float width=Math.min(t.clockAppearance.widthDp*scale,size[0]*.54f);
      float halfHeight=width/t.clockAppearance.aspectRatio/2;
      float cy=g.mapY(t.scene.topClock,t.scene),cx=size[0]*t.scene.topClockX;
      assertTrue(cx-width/2>0 && cx+width/2<size[0]);
      assertTrue(cy-halfHeight>0 && cy+halfHeight<g.upperEnd);
      assertTrue(g.mapY(t.furnace.y,t.scene)<g.top);
    }
  }
}
