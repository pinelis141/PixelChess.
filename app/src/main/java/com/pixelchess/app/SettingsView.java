package com.pixelchess.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** Game preferences on the shared, animated Royal Library surface. */
final class SettingsView extends ScrollView {
  interface Listener{void back();}
  SettingsView(Context context,GamePreferences prefs,Listener listener){
    super(context);setFillViewport(true);RoyalUi.screen(this);float d=getResources().getDisplayMetrics().density;
    LinearLayout body=new LinearLayout(context);body.setOrientation(LinearLayout.VERTICAL);body.setGravity(Gravity.CENTER_HORIZONTAL);body.setPadding((int)(24*d),(int)(34*d),(int)(24*d),(int)(28*d));addView(body,new LayoutParams(-1,-2));
    TextView heading=new TextView(context);heading.setText("CONFIGURAÇÕES");RoyalUi.text(heading,24,true);heading.setGravity(Gravity.CENTER);
    body.addView(heading,new LinearLayout.LayoutParams(-1,RoyalUi.dp(context,50)));
    addDivider(context,body);
    addOption(context,body,option(context,"Vibração nos turnos",prefs.vibration()),d);
    addOption(context,body,option(context,"Efeitos visuais",prefs.effects()),d);
    addOption(context,body,option(context,"Pretas na parte inferior",prefs.blackAtBottom()),d);
    addOption(context,body,option(context,"Sons de jogadas",prefs.sound()),d);
    Button back=new Button(context);back.setText("VOLTAR");RoyalUi.button(back);back.setOnClickListener(v->listener.back());
    LinearLayout.LayoutParams backLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(context,58));backLp.setMargins(0,RoyalUi.dp(context,16),0,0);body.addView(back,backLp);
  }
  private void addDivider(Context c,LinearLayout body){
    android.view.View line=new android.view.View(c);line.setBackgroundColor(RoyalUi.GOLD);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(RoyalUi.dp(c,96),RoyalUi.dp(c,2));lp.setMargins(0,0,0,RoyalUi.dp(c,22));body.addView(line,lp);
  }
  private void addOption(Context c,LinearLayout body,Switch option,float d){
    option.setBackground(RoyalUi.panel(c,false));option.setPadding((int)(18*d),(int)(8*d),(int)(18*d),(int)(8*d));
    option.setButtonTintList(RoyalUi.tint());option.setTrackTintList(RoyalUi.tint());
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,66));lp.setMargins(0,0,0,RoyalUi.dp(c,10));body.addView(option,lp);
  }
  private Switch option(Context c,String label,boolean checked){
    Switch s=new Switch(c);s.setText(label);s.setTextColor(RoyalUi.CREAM);s.setTextSize(16);s.setTypeface(Typeface.SERIF,Typeface.BOLD);s.setChecked(checked);s.setMinHeight(RoyalUi.dp(c,56));return s;
  }
}
