package com.pixelchess.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Accessible, compact pixel-art entry point. No baked button text or hit regions. */
public final class MainMenuView extends FrameLayout {
  public interface Actions {
    void playLocal();void playBot();void playBluetooth();void chooseSkin();void openSettings();void toggleMusic();
    default void openLicenses(){}
  }
  private final LinearLayout[] cards=new LinearLayout[5];
  private final TextView music;
  private final GamePreferences preferences;
  private int active;

  public MainMenuView(Context c,String version,BoardTheme theme,boolean musicMuted,Actions actions){
    super(c);setBackground(new RoyalLibraryDrawable(c));
    preferences=new GamePreferences(c);active=preferences.lastMenuIndex();
    ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);scroll.setClipToPadding(false);
    scroll.setVerticalScrollBarEnabled(false);
    LinearLayout root=new LinearLayout(c);root.setOrientation(LinearLayout.VERTICAL);
    root.setGravity(Gravity.CENTER_HORIZONTAL);
    root.setPadding(dp(19),dp(21),dp(19),dp(62));root.setMinimumHeight(dp(650));
    scroll.addView(root,new ScrollView.LayoutParams(-1,-2));addView(scroll,new LayoutParams(-1,-1));

    LinearLayout.LayoutParams logoLp=new LinearLayout.LayoutParams(-1,-2);
    logoLp.bottomMargin=dp(23);root.addView(new PixelMenuHeader(c,null),logoLp);

    String[] labels={"PARTIDA LOCAL","JOGAR CONTRA BOT","MULTIPLAYER BLUETOOTH","SKINS DO TABULEIRO","CONFIGURAÇÕES"};
    String[] symbols={"▶","♟","ϟ","▦","⚙"};
    Runnable[] actionsByIndex={actions::playLocal,actions::playBot,actions::playBluetooth,actions::chooseSkin,actions::openSettings};
    int width=Math.min(getResources().getDisplayMetrics().widthPixels-dp(38),dp(386));
    for(int i=0;i<cards.length;i++){
      final int index=i;
      LinearLayout panel=new LinearLayout(c);panel.setOrientation(LinearLayout.HORIZONTAL);
      panel.setGravity(Gravity.CENTER_VERTICAL);panel.setPadding(dp(18),0,dp(12),0);
      panel.setBackground(RoyalUi.panel(c,false));
      panel.setClickable(true);panel.setFocusable(true);panel.setSelected(i==active);
      panel.setContentDescription(labels[i]);
      TextView icon=new TextView(c);icon.setText(symbols[i]);RoyalUi.text(icon,23,true);
      icon.setTextColor(i==0?RoyalUi.RED:RoyalUi.CREAM);
      icon.setGravity(Gravity.CENTER);icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
      panel.addView(icon,new LinearLayout.LayoutParams(dp(39),-1));
      TextView label=new TextView(c);label.setText(labels[i]);RoyalUi.text(label,labels[i].length()>19?14:16,true);
      label.setTextColor(RoyalUi.CREAM);label.setGravity(Gravity.CENTER_VERTICAL);
      label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);
      LinearLayout.LayoutParams nameLp=new LinearLayout.LayoutParams(0,-1,1);
      nameLp.leftMargin=dp(10);panel.addView(label,nameLp);
      LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(width,dp(57));
      rowLp.bottomMargin=dp(9);root.addView(panel,rowLp);cards[i]=panel;
      panel.setOnFocusChangeListener((v,hasFocus)->{if(hasFocus)select(index);});
      panel.setOnClickListener(v->{select(index);actionsByIndex[index].run();});
    }
    TextView themeName=new TextView(c);themeName.setText(theme.name);
    themeName.setGravity(Gravity.CENTER);RoyalUi.text(themeName,12,false);
    LinearLayout.LayoutParams tLp=new LinearLayout.LayoutParams(-1,dp(29));tLp.topMargin=dp(5);root.addView(themeName,tLp);

    TextView licenses=new TextView(c);licenses.setText("LICENÇAS E CRÉDITOS • GPL v3");
    licenses.setGravity(Gravity.CENTER);RoyalUi.text(licenses,11,false);
    licenses.setClickable(true);licenses.setFocusable(true);
    licenses.setOnClickListener(v->actions.openLicenses());
    root.addView(licenses,new LinearLayout.LayoutParams(-1,dp(44)));
    TextView footer=new TextView(c);footer.setText("v"+version+" • PixelChess");
    RoyalUi.text(footer,11,false);footer.setGravity(Gravity.CENTER);
    root.addView(footer,new LinearLayout.LayoutParams(-1,dp(22)));

    music=new TextView(c);music.setText(musicMuted?"♪̸":"♫");RoyalUi.text(music,25,true);
    music.setGravity(Gravity.CENTER);music.setBackground(RoyalUi.panel(c,false));
    music.setContentDescription(musicMuted?"Ativar música":"Desativar música");
    music.setClickable(true);music.setFocusable(true);music.setOnClickListener(v->actions.toggleMusic());
    LayoutParams musicLp=new LayoutParams(dp(52),dp(52),Gravity.END|Gravity.BOTTOM);
    musicLp.setMargins(0,0,dp(12),dp(13));addView(music,musicLp);
  }
  private int dp(int n){return RoyalUi.dp(getContext(),n);}
  private void select(int index){
    active=index;preferences.lastMenuIndex(index);
    for(int i=0;i<cards.length;i++)cards[i].setSelected(i==index);
  }
  int selectedIndex(){return active;}
  void setMusicMuted(boolean muted){music.setText(muted?"♪̸":"♫");
    music.setContentDescription(muted?"Ativar música":"Desativar música");}
}
