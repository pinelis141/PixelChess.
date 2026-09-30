package com.pixelchess.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** Compact accessible settings screen backed by persistent game preferences. */
final class SettingsView extends ScrollView {
  interface Listener{void back();}
  SettingsView(Context context,GamePreferences prefs,Listener listener){
    super(context);setFillViewport(true);setBackgroundColor(Color.rgb(20,24,28));float d=getResources().getDisplayMetrics().density;
    LinearLayout body=new LinearLayout(context);body.setOrientation(LinearLayout.VERTICAL);body.setGravity(Gravity.CENTER_HORIZONTAL);body.setPadding((int)(24*d),(int)(24*d),(int)(24*d),(int)(24*d));addView(body);
    TextView heading=new TextView(context);heading.setText("CONFIGURAÇÕES");heading.setTextColor(0xffebddb8);heading.setTextSize(22);heading.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);heading.setGravity(Gravity.CENTER);body.addView(heading);
    Switch vibration=option(context,"Vibração nos turnos",prefs.vibration());body.addView(vibration);vibration.setOnCheckedChangeListener((b,v)->prefs.vibration(v));
    Switch effects=option(context,"Efeitos visuais",prefs.effects());body.addView(effects);effects.setOnCheckedChangeListener((b,v)->prefs.effects(v));
    Switch orientation=option(context,"Pretas na parte inferior",prefs.blackAtBottom());body.addView(orientation);orientation.setOnCheckedChangeListener((b,v)->prefs.blackAtBottom(v));
    Switch sound=option(context,"Sons de jogadas",prefs.sound());body.addView(sound);sound.setOnCheckedChangeListener((b,v)->prefs.sound(v));
    Button back=new Button(context);back.setText("VOLTAR");back.setAllCaps(false);back.setOnClickListener(v->listener.back());body.addView(back);
  }
  private Switch option(Context c,String label,boolean checked){Switch s=new Switch(c);s.setText(label);s.setTextColor(Color.rgb(235,221,184));s.setTextSize(16);s.setChecked(checked);s.setPadding(0,18,0,18);s.setMinHeight((int)(56*getResources().getDisplayMetrics().density));return s;}
}
