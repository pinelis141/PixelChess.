package com.pixelchess.app;

import android.content.Context;
import android.view.Gravity;
import android.widget.*;

/** Settings reuse the same animated Royal Library backdrop and persist real options. */
final class SettingsView extends ScrollView {
  interface Listener {void back();void credits();}
  SettingsView(Context c,GamePreferences prefs,MenuMusicController music,Listener listener){
    super(c);setFillViewport(true);RoyalUi.screen(this);
    LinearLayout root=new LinearLayout(c);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);
    int pad=RoyalUi.dp(c,20);root.setPadding(pad,RoyalUi.dp(c,30),pad,RoyalUi.dp(c,48));
    addView(root,new LayoutParams(-1,-2));
    Button back=new Button(c);back.setText("‹  VOLTAR");RoyalUi.button(back);
    back.setOnClickListener(v->listener.back());root.addView(back,new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,54)));
    TextView emblem=title(c,"⚙",42);emblem.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams emblemLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,81));root.addView(emblem,emblemLp);
    TextView heading=title(c,"CONFIGURAÇÕES",28);heading.setGravity(Gravity.CENTER);root.addView(heading);
    TextView subtitle=label(c,"Som, vibração e preferências",15);
    subtitle.setGravity(Gravity.CENTER);LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,55));root.addView(subtitle,subLp);
    option(c,root,"MÚSICA","Trilha do menu","♫",!music.isMuted(),on->{
      if(on==music.isMuted())music.toggleMuted();
    });
    option(c,root,"EFEITOS SONOROS","Cliques e movimentos","♪",prefs.sound(),prefs::sound);
    option(c,root,"VIBRAÇÃO","Feedback do turno","⌁",prefs.vibration(),prefs::vibration);
    option(c,root,"EFEITOS VISUAIS","Animações durante a partida","✦",prefs.effects(),prefs::effects);
    option(c,root,"PRETAS NA PARTE INFERIOR","Perspectiva das partidas locais","♟",prefs.blackAtBottom(),prefs::blackAtBottom);
    LinearLayout clock=panel(c,root);
    TextView clockTitle=title(c,"◷  TEMPO PADRÃO",17);clock.addView(clockTitle);
    TextView clockSub=label(c,"Utilizado ao abrir uma nova partida",13);clock.addView(clockSub);
    LinearLayout times=new LinearLayout(c);times.setOrientation(LinearLayout.HORIZONTAL);
    LinearLayout.LayoutParams timesLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,55));timesLp.topMargin=RoyalUi.dp(c,9);clock.addView(times,timesLp);
    TextView[] choices=new TextView[3];int[] presets={3,5,10};
    for(int i=0;i<presets.length;i++){
      final int index=i;TextView chip=title(c,presets[i]+" min",15);chip.setGravity(Gravity.CENTER);
      chip.setBackground(RoyalUi.panel(c,false));chip.setSelected(prefs.defaultMinutes()==presets[i]);
      chip.setClickable(true);chip.setFocusable(true);chip.setOnClickListener(v->{
        prefs.defaultMinutes(presets[index]);for(int j=0;j<choices.length;j++)choices[j].setSelected(j==index);
      });
      choices[i]=chip;LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1f);
      if(i>0)cp.leftMargin=RoyalUi.dp(c,5);times.addView(chip,cp);
    }
    Button credits=new Button(c);credits.setText("CRÉDITOS E LICENÇAS   ›");RoyalUi.button(credits);
    credits.setOnClickListener(v->listener.credits());
    LinearLayout.LayoutParams creditsLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,66));creditsLp.topMargin=RoyalUi.dp(c,6);root.addView(credits,creditsLp);
  }
  private TextView title(Context c,String value,int size){TextView t=new TextView(c);t.setText(value);RoyalUi.text(t,size,true);return t;}
  private TextView label(Context c,String value,int size){TextView t=new TextView(c);t.setText(value);RoyalUi.text(t,size,false);return t;}
  private LinearLayout panel(Context c,LinearLayout root){
    LinearLayout row=new LinearLayout(c);row.setOrientation(LinearLayout.VERTICAL);
    row.setPadding(RoyalUi.dp(c,18),RoyalUi.dp(c,13),RoyalUi.dp(c,18),RoyalUi.dp(c,13));
    row.setBackground(RoyalUi.panel(c,false));
    LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=RoyalUi.dp(c,12);root.addView(row,rp);return row;
  }
  private void option(Context c,LinearLayout root,String name,String description,String symbol,boolean enabled,java.util.function.Consumer<Boolean> save){
    LinearLayout row=panel(c,root);LinearLayout line=new LinearLayout(c);line.setGravity(Gravity.CENTER_VERTICAL);row.addView(line);
    TextView symbolView=title(c,symbol,25);symbolView.setGravity(Gravity.CENTER);
    line.addView(symbolView,new LinearLayout.LayoutParams(RoyalUi.dp(c,39),RoyalUi.dp(c,50)));
    LinearLayout copy=new LinearLayout(c);copy.setOrientation(LinearLayout.VERTICAL);
    copy.addView(title(c,name,15));copy.addView(label(c,description,12));
    LinearLayout.LayoutParams copyLp=new LinearLayout.LayoutParams(0,-2,1);copyLp.leftMargin=RoyalUi.dp(c,6);line.addView(copy,copyLp);
    Switch toggle=new Switch(c);toggle.setChecked(enabled);toggle.setThumbTintList(RoyalUi.tint());
    toggle.setTrackTintList(RoyalUi.tint());toggle.setContentDescription(name);toggle.setOnCheckedChangeListener((button,isChecked)->save.accept(isChecked));
    line.addView(toggle,new LinearLayout.LayoutParams(RoyalUi.dp(c,57),RoyalUi.dp(c,50)));
    row.setOnClickListener(v->toggle.setChecked(!toggle.isChecked()));row.setClickable(true);
  }
}
