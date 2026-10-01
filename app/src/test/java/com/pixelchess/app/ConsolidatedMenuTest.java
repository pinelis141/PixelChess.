package com.pixelchess.app;

import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class ConsolidatedMenuTest {
  static View find(View root,String text){
    if(root instanceof TextView && ((TextView)root).getText().toString().contains(text))return root;
    if(root instanceof ViewGroup)for(int i=0;i<((ViewGroup)root).getChildCount();i++){
      View found=find(((ViewGroup)root).getChildAt(i),text);if(found!=null)return found;
    }
    return null;
  }
  @Test public void redesignedMenuRoutesAllThreeModesAndExistingPreferences(){
    int[] calls=new int[6];
    MainMenuView menu=new MainMenuView(RuntimeEnvironment.getApplication(),"0.27.1",BoardThemes.FOREST,false,new MainMenuView.Actions(){
      public void playLocal(){calls[0]++;}public void playBot(){calls[1]++;}
      public void playBluetooth(){calls[2]++;}public void chooseSkin(){calls[3]++;}
      public void openSettings(){calls[4]++;}public void toggleMusic(){calls[5]++;}
    });
    String[] titles={"PARTIDA LOCAL","JOGAR CONTRA BOT","MULTIPLAYER BLUETOOTH","SKINS DO TABULEIRO","CONFIGURAÇÕES"};
    for(int i=0;i<titles.length;i++){
      View label=find(menu,titles[i]);assertNotNull(titles[i],label);
      assertTrue(((View)label.getParent()).performClick());assertEquals(1,calls[i]);
    }
    assertNotNull(find(menu,BoardThemes.FOREST.name));
    View music=menu.getChildAt(1);assertEquals("Desativar música",music.getContentDescription());
    assertTrue(music.performClick());assertEquals(1,calls[5]);
  }
  @Test public void botCardOpensRoyalScreenAndStartsNormalBotMatch(){
    ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
    try{
      MainActivity activity=controller.get();View root=activity.getWindow().getDecorView();
      View bot=find(root,"JOGAR CONTRA BOT");assertNotNull(bot);assertTrue(((View)bot.getParent()).performClick());
      assertTrue(activity.findViewById(android.R.id.content).getRootView()!=null);
      View difficulty=find(activity.getWindow().getDecorView(),"2");assertNotNull(difficulty);assertTrue(difficulty.performClick());
      View side=find(activity.getWindow().getDecorView(),"BRANCAS");assertNotNull(side);assertTrue(side.performClick());
      View time=find(activity.getWindow().getDecorView(),"5 MIN");assertNotNull(time);assertTrue(time.performClick());
      View start=find(activity.getWindow().getDecorView(),"JOGAR AGORA");assertNotNull(start);assertTrue(start.performClick());
      assertNotNull(activity.game);assertTrue(activity.game.botGame());assertTrue(activity.game.humanWhite());
      assertEquals(com.pixelchess.app.bot.BotDifficulty.NORMAL,activity.game.botDifficulty());
      assertEquals(5,activity.selectedMinutes);
      activity.showMenu();assertNull(activity.game);assertNotNull(find(activity.getWindow().getDecorView(),"JOGAR CONTRA BOT"));
    }finally{controller.pause().stop().destroy();}
  }

}
