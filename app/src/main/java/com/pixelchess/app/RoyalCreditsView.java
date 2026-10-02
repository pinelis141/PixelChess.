package com.pixelchess.app;

import android.content.Context;
import android.view.Gravity;
import android.widget.*;

/** Credits follow the same steel menu kit; full legal text remains available. */
final class RoyalCreditsView extends ScrollView {
  interface Actions {void back();void fullLicenses();}
  RoyalCreditsView(Context c,Actions actions){
    super(c);setFillViewport(true);setVerticalScrollBarEnabled(false);RoyalUi.screen(this);
    LinearLayout body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
    body.setGravity(Gravity.CENTER_HORIZONTAL);body.setPadding(dp(c,14),dp(c,14),dp(c,14),dp(c,35));
    addView(body,new LayoutParams(-1,-2));
    Button back=new Button(c);back.setText("‹  VOLTAR");RoyalUi.button(back);back.setTextSize(13);
    back.setOnClickListener(v->actions.back());
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(c,104),dp(c,48));bp.gravity=Gravity.START;
    body.addView(back,bp);
    body.addView(new PixelMenuHeader(c,"CRÉDITOS E LICENÇAS"),lp(c,-2,14));
    row(c,body,"DESENVOLVIMENTO","Pixel Chess");
    row(c,body,"MOTOR DE XADREZ","Stockfish offline");
    row(c,body,"ARTE E INTERFACE","Pixel art e aço negro");
    row(c,body,"ÁUDIO","The Quiet Gambit e efeitos de partida");
    row(c,body,"LICENÇA","GPL v3 — leia os avisos completos");
    Button full=new Button(c);full.setText("VER LICENÇAS COMPLETAS");RoyalUi.button(full);
    full.setSelected(true);full.setTextSize(15);full.setOnClickListener(v->actions.fullLicenses());
    body.addView(full,lp(c,58,0));
  }
  private int dp(Context c,int v){return RoyalUi.dp(c,v);}
  private LinearLayout.LayoutParams lp(Context c,int height,int bottom){
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
      Math.min(c.getResources().getDisplayMetrics().widthPixels-dp(c,32),dp(c,400)),
      height<0?height:dp(c,height));lp.bottomMargin=dp(c,bottom);return lp;
  }
  private void row(Context c,LinearLayout body,String title,String description){
    LinearLayout card=new LinearLayout(c);card.setOrientation(LinearLayout.VERTICAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(c,19),dp(c,6),dp(c,15),dp(c,6));
    card.setBackground(RoyalUi.panel(c,false));
    TextView heading=new TextView(c);heading.setText(title);RoyalUi.text(heading,15,true);
    TextView desc=new TextView(c);desc.setText(description);RoyalUi.text(desc,13,false);
    desc.setTextColor(RoyalUi.MUTED);card.addView(heading);card.addView(desc);
    body.addView(card,lp(c,67,8));
  }
}
