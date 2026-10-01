package com.pixelchess.app;

import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class RoyalMenuTest {
  @Test public void redSelectionMovesExclusivelyAndRoutesEveryButton(){
    int[] calls=new int[5];
    MainMenuView menu=new MainMenuView(RuntimeEnvironment.getApplication(),"0.27.1",BoardThemes.FOREST,false,new MainMenuView.Actions(){
      public void playLocal(){calls[0]++;}public void playBot(){calls[1]++;}public void playBluetooth(){calls[2]++;}
      public void chooseSkin(){calls[3]++;}public void openSettings(){calls[4]++;}public void toggleMusic(){}
    });
    String[] names={"PARTIDA LOCAL","JOGAR CONTRA BOT","MULTIPLAYER BLUETOOTH","SKINS DO TABULEIRO","CONFIGURAÇÕES"};
    for(int i=0;i<names.length;i++){
      View card=(View)ConsolidatedMenuTest.find(menu,names[i]).getParent();card.performClick();
      assertEquals(i,menu.selectedIndex());assertEquals(1,calls[i]);
      for(int j=0;j<names.length;j++)assertEquals(j==i,((View)ConsolidatedMenuTest.find(menu,names[j]).getParent()).isSelected());
    }
    menu.setMusicMuted(true);assertEquals(4,menu.selectedIndex());assertEquals("Ativar música",menu.getChildAt(1).getContentDescription());
  }
  @Test public void candleHaloOscillatesInRadiusAndIntensityWithIndependentPhases(){
    float min=1,max=0;
    for(int i=0;i<200;i++){float p=RoyalMenuMotion.pulse(0,i*.02);min=Math.min(min,p);max=Math.max(max,p);assertTrue(p>=.039f&&p<=.961f);}
    assertTrue(max-min>.6f);
    assertNotEquals(RoyalMenuMotion.haloRadius(0,0,36),RoyalMenuMotion.haloRadius(0,.4,36),.01f);
    assertNotEquals(RoyalMenuMotion.haloAlpha(0,0),RoyalMenuMotion.haloAlpha(0,.4),.001f);
    assertNotEquals(RoyalMenuMotion.pulse(0,.4),RoyalMenuMotion.pulse(1,.4),.001f);
  }
  @Test public void shortScreensScrollAndAllMenuTargetsRemainTouchable(){
    MainMenuView menu=new MainMenuView(RuntimeEnvironment.getApplication(),"0.27.1",BoardThemes.FOREST,false,new MainMenuView.Actions(){
      public void playLocal(){}public void playBot(){}public void playBluetooth(){}public void chooseSkin(){}public void openSettings(){}public void toggleMusic(){}
    });
    float density=menu.getResources().getDisplayMetrics().density;
    int w=Math.round(360*density),h=Math.round(560*density);
    menu.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));menu.layout(0,0,w,h);
    android.widget.ScrollView scroll=(android.widget.ScrollView)menu.getChildAt(0);assertTrue(scroll.getChildAt(0).getHeight()>h);
    View card=(View)ConsolidatedMenuTest.find(menu,"CONFIGURAÇÕES").getParent();assertTrue(card.getHeight()>=48*density);
  }
}
