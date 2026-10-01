package com.pixelchess.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public final class MainMenuView extends FrameLayout {
  public interface Actions {
    void playLocal();
    void playBluetooth();
    void chooseSkin();
    void openSettings();
    void toggleMusic();
  }

  private final int cream=Color.rgb(239,224,188);
  private final int muted=Color.rgb(166,158,143);
  private final int gold=Color.rgb(190,145,72);
  private final float density;

  public MainMenuView(Context context,String version,BoardTheme theme,boolean musicMuted,Actions actions){
    super(context); density=getResources().getDisplayMetrics().density;
    setBackground(background());

    ScrollView scroll=new ScrollView(context);
    scroll.setFillViewport(true);
    LinearLayout content=new LinearLayout(context);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setGravity(Gravity.CENTER_HORIZONTAL);
    content.setPadding(dp(24),dp(42),dp(24),dp(92));
    scroll.addView(content,new ScrollView.LayoutParams(-1,-2));

    TextView pawn=text("♟",54,cream,Typeface.BOLD);
    content.addView(pawn,new LinearLayout.LayoutParams(-1,dp(70)));

    TextView logo=text("PIXEL CHESS",30,cream,Typeface.BOLD);
    content.addView(logo,new LinearLayout.LayoutParams(-1,-2));

    TextView tagline=text("XADREZ • PIXEL ART • OFFLINE",11,muted,Typeface.BOLD);
    LinearLayout.LayoutParams tagLp=new LinearLayout.LayoutParams(-1,-2); tagLp.setMargins(0,dp(4),0,dp(32));
    content.addView(tagline,tagLp);

    content.addView(section("JOGAR"),full());
    content.addView(menuButton("▶  PARTIDA LOCAL","Duas pessoas no mesmo aparelho",true,v->actions.playLocal()),buttonLp());
    content.addView(menuButton("⌁  MULTIPLAYER BLUETOOTH","Jogue offline com outro celular",false,v->actions.playBluetooth()),buttonLp());

    LinearLayout.LayoutParams sectionLp=full(); sectionLp.setMargins(0,dp(18),0,0);
    content.addView(section("PERSONALIZAR"),sectionLp);
    content.addView(menuButton("▣  SKINS DO TABULEIRO",theme.name,false,v->actions.chooseSkin()),buttonLp());
    content.addView(menuButton("⚙  CONFIGURAÇÕES","Som, vibração e preferências",false,v->actions.openSettings()),buttonLp());

    TextView footer=text("v"+version+"  •  PixelChess",11,Color.rgb(105,105,105),Typeface.NORMAL);
    LinearLayout.LayoutParams footLp=full(); footLp.setMargins(0,dp(26),0,0); content.addView(footer,footLp);

    addView(scroll,new FrameLayout.LayoutParams(-1,-1));

    TextView music=text(musicMuted?"♪̸":"♫",23,cream,Typeface.BOLD);
    music.setContentDescription(musicMuted?"Ativar música":"Desativar música");
    music.setGravity(Gravity.CENTER);
    music.setBackground(roundRect(Color.rgb(38,34,31),Color.rgb(104,78,49),14,1));
    music.setOnClickListener(v->actions.toggleMusic());
    FrameLayout.LayoutParams musicLp=new FrameLayout.LayoutParams(dp(50),dp(50),Gravity.BOTTOM|Gravity.END);
    musicLp.setMargins(0,0,dp(18),dp(18)); addView(music,musicLp);
  }

  private TextView section(String label){
    TextView v=text(label,11,gold,Typeface.BOLD); v.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
    v.setLetterSpacing(.16f); v.setPadding(dp(4),0,0,dp(8)); return v;
  }

  private View menuButton(String title,String subtitle,boolean primary,OnClickListener click){
    LinearLayout card=new LinearLayout(getContext()); card.setOrientation(LinearLayout.VERTICAL);
    card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(20),dp(13),dp(20),dp(13));
    int fill=primary?Color.rgb(74,62,43):Color.rgb(31,34,35);
    int stroke=primary?Color.rgb(191,147,75):Color.rgb(69,72,70);
    card.setBackground(roundRect(fill,stroke,12,1));
    TextView t=text(title,16,cream,Typeface.BOLD); t.setGravity(Gravity.START); card.addView(t,new LinearLayout.LayoutParams(-1,-2));
    TextView s=text(subtitle,11,primary?Color.rgb(211,194,158):muted,Typeface.NORMAL); s.setGravity(Gravity.START);
    LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,-2); slp.setMargins(0,dp(3),0,0); card.addView(s,slp);
    card.setClickable(true); card.setFocusable(true); card.setOnClickListener(click);
    return card;
  }

  private TextView text(String value,int sp,int color,int style){
    TextView v=new TextView(getContext()); v.setText(value); v.setTextSize(sp); v.setTextColor(color);
    v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,style); return v;
  }

  private GradientDrawable background(){
    GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
      new int[]{Color.rgb(12,14,15),Color.rgb(24,23,21),Color.rgb(13,15,16)});
    return g;
  }

  private GradientDrawable roundRect(int fill,int stroke,int radius,int strokeWidth){
    GradientDrawable g=new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(radius)); g.setStroke(dp(strokeWidth),stroke); return g;
  }

  private LinearLayout.LayoutParams full(){return new LinearLayout.LayoutParams(-1,-2);}
  private LinearLayout.LayoutParams buttonLp(){LinearLayout.LayoutParams p=full();p.setMargins(0,0,0,dp(10));p.height=dp(72);return p;}
  private int dp(int value){return Math.round(value*density);}
}