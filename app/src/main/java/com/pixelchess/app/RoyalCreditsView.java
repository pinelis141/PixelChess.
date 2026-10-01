package com.pixelchess.app;

import android.content.Context;
import android.view.Gravity;
import android.widget.*;

/** Readable credits landing page; full corresponding licenses remain one tap away. */
final class RoyalCreditsView extends ScrollView {
  interface Actions {void back();void fullLicenses();}
  RoyalCreditsView(Context c,Actions actions){
    super(c);setFillViewport(true);RoyalUi.screen(this);
    LinearLayout body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
    body.setGravity(Gravity.CENTER_HORIZONTAL);
    int space=RoyalUi.dp(c,18);body.setPadding(space,RoyalUi.dp(c,30),space,RoyalUi.dp(c,48));
    addView(body,new LayoutParams(-1,-2));
    Button back=new Button(c);back.setText("‹  VOLTAR");RoyalUi.button(back);
    back.setOnClickListener(v->actions.back());body.addView(back,new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,54)));
    heading(c,body,"♞",43);
    heading(c,body,"CRÉDITOS E LICENÇAS",27);
    heading(c,body,"Projeto open source",16);
    section(c,body,"DESENVOLVIMENTO","Pixel Chess");
    section(c,body,"MOTOR DE XADREZ","Stockfish offline");
    section(c,body,"ARTE E INTERFACE","Biblioteca Real e skins do Pixel Chess");
    section(c,body,"ÁUDIO","The Quiet Gambit e efeitos de partida");
    section(c,body,"LICENÇA","GNU GPL v3 — consulte os avisos e textos completos");
    Button full=new Button(c);full.setText("VER LICENÇAS COMPLETAS");RoyalUi.button(full);
    full.setSelected(true);full.setOnClickListener(v->actions.fullLicenses());
    LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,68));fp.topMargin=RoyalUi.dp(c,18);body.addView(full,fp);
  }
  private void heading(Context c,LinearLayout parent,String value,int size){
    TextView title=new TextView(c);title.setText(value);RoyalUi.text(title,size,true);title.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=RoyalUi.dp(c,17);lp.bottomMargin=RoyalUi.dp(c,12);parent.addView(title,lp);
  }
  private void section(Context c,LinearLayout parent,String name,String detail){
    LinearLayout item=new LinearLayout(c);item.setOrientation(LinearLayout.VERTICAL);
    item.setPadding(RoyalUi.dp(c,21),RoyalUi.dp(c,18),RoyalUi.dp(c,21),RoyalUi.dp(c,18));
    item.setBackground(RoyalUi.panel(c,false));
    TextView nameView=new TextView(c);nameView.setText(name);RoyalUi.text(nameView,18,true);item.addView(nameView);
    TextView detailView=new TextView(c);detailView.setText(detail);RoyalUi.text(detailView,14,false);item.addView(detailView);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=RoyalUi.dp(c,12);parent.addView(item,lp);
  }
}
