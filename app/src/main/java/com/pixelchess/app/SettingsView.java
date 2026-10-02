package com.pixelchess.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.function.Consumer;

/** Four clear front-page switches; infrequently used match defaults remain under Advanced. */
final class SettingsView extends ScrollView {
  interface Listener {void back();void credits();}
  private final Context context;
  private final GamePreferences prefs;
  private final LinearLayout body;
  SettingsView(Context c,GamePreferences prefs,MenuMusicController music,Listener listener){
    super(c);this.context=c;this.prefs=prefs;
    setFillViewport(true);setVerticalScrollBarEnabled(false);RoyalUi.screen(this);
    body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
    body.setGravity(Gravity.CENTER_HORIZONTAL);body.setPadding(dp(14),dp(14),dp(14),dp(30));
    addView(body,new LayoutParams(-1,-2));
    Button back=new Button(c);back.setText("‹  VOLTAR");RoyalUi.button(back);
    back.setTextSize(13);back.setOnClickListener(v->listener.back());
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(104),dp(48));bp.gravity=Gravity.START;body.addView(back,bp);
    body.addView(new PixelMenuHeader(c,"CONFIGURAÇÕES"),width(-2,13));
    option("♫","MÚSICA",!music.isMuted(),on->{if(on==music.isMuted())music.toggleMuted();});
    option("◖","EFEITOS SONOROS",prefs.sound(),prefs::sound);
    option("⌁","VIBRAÇÃO",prefs.vibration(),prefs::vibration);
    option("✦","EFEITOS VISUAIS",prefs.effects(),prefs::effects);

    LinearLayout advanced=new LinearLayout(c);advanced.setOrientation(LinearLayout.VERTICAL);
    advanced.setVisibility(GONE);
    TextView expand=new TextView(c);expand.setText("PREFERÊNCIAS DA PARTIDA   ▾");
    RoyalUi.text(expand,14,true);expand.setTextColor(RoyalUi.CREAM);
    expand.setGravity(Gravity.CENTER);expand.setBackground(RoyalUi.panel(c,false));
    expand.setClickable(true);expand.setFocusable(true);
    expand.setOnClickListener(v->{
      boolean show=advanced.getVisibility()!=VISIBLE;advanced.setVisibility(show?VISIBLE:GONE);
      expand.setText(show?"PREFERÊNCIAS DA PARTIDA   ▴":"PREFERÊNCIAS DA PARTIDA   ▾");
      expand.setSelected(show);
    });
    body.addView(expand,width(53,8));

    LinearLayout p=new LinearLayout(c);p.setOrientation(LinearLayout.VERTICAL);
    p.setPadding(dp(14),dp(10),dp(14),dp(14));p.setBackground(RoyalUi.panel(c,false));
    TextView label=new TextView(c);label.setText("TEMPO PADRÃO");RoyalUi.text(label,16,true);
    p.addView(label,new LinearLayout.LayoutParams(-1,dp(30)));
    LinearLayout clock=new LinearLayout(c);clock.setOrientation(LinearLayout.HORIZONTAL);
    p.addView(clock,new LinearLayout.LayoutParams(-1,dp(50)));
    TextView[] chips=new TextView[3];int[] values={3,5,10};
    for(int i=0;i<3;i++){
      final int index=i;TextView chip=new TextView(c);chip.setText(values[i]+" MIN");RoyalUi.text(chip,14,true);
      chip.setGravity(Gravity.CENTER);chip.setBackground(RoyalUi.panel(c,false));
      chip.setSelected(prefs.defaultMinutes()==values[i]);chip.setClickable(true);chip.setFocusable(true);
      chip.setOnClickListener(v->{
        prefs.defaultMinutes(values[index]);
        for(int j=0;j<chips.length;j++)chips[j].setSelected(j==index);
      });
      chips[i]=chip;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
      if(i>0)lp.leftMargin=dp(5);clock.addView(chip,lp);
    }
    advanced.addView(p,width(-2,8));
    PixelToggle orientation=new PixelToggle(c,prefs.blackAtBottom());
    LinearLayout perspective=new LinearLayout(c);perspective.setOrientation(LinearLayout.HORIZONTAL);
    perspective.setGravity(Gravity.CENTER_VERTICAL);perspective.setPadding(dp(14),dp(8),dp(14),dp(8));
    perspective.setBackground(RoyalUi.panel(c,false));
    TextView side=new TextView(c);side.setText("PRETAS EMBAIXO");RoyalUi.text(side,15,true);
    perspective.addView(side,new LinearLayout.LayoutParams(0,-2,1));
    perspective.addView(orientation,new LinearLayout.LayoutParams(dp(56),dp(33)));
    orientation.setAction(prefs::blackAtBottom);
    perspective.setClickable(true);perspective.setOnClickListener(v->orientation.performClick());
    advanced.addView(perspective,width(63,0));
    body.addView(advanced,new LinearLayout.LayoutParams(-1,-2));

    Button credits=new Button(c);credits.setText("CRÉDITOS E LICENÇAS   ›");RoyalUi.button(credits);
    credits.setOnClickListener(v->listener.credits());credits.setTextSize(15);
    body.addView(credits,width(58,0));
  }
  private int dp(int n){return RoyalUi.dp(context,n);}
  private LinearLayout.LayoutParams width(int height,int bottom){
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
      Math.min(context.getResources().getDisplayMetrics().widthPixels-dp(32),dp(400)),
      height<0?height:dp(height));lp.bottomMargin=dp(bottom);return lp;
  }
  private void option(String glyph,String name,boolean checked,Consumer<Boolean> save){
    LinearLayout row=new LinearLayout(context);row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(13),dp(8),dp(13),dp(8));
    row.setBackground(RoyalUi.panel(context,false));
    LinearLayout line=new LinearLayout(context);line.setOrientation(LinearLayout.HORIZONTAL);
    line.setGravity(Gravity.CENTER_VERTICAL);row.addView(line,new LinearLayout.LayoutParams(-1,-1));
    TextView symbol=new TextView(context);symbol.setText(glyph);RoyalUi.text(symbol,24,true);
    symbol.setTextColor(RoyalUi.CREAM);symbol.setGravity(Gravity.CENTER);
    symbol.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    line.addView(symbol,new LinearLayout.LayoutParams(dp(43),dp(44)));
    LinearLayout copy=new LinearLayout(context);copy.setOrientation(LinearLayout.VERTICAL);
    TextView heading=new TextView(context);heading.setText(name);RoyalUi.text(heading,16,true);
    copy.addView(heading);
    LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.leftMargin=dp(8);
    line.addView(copy,cp);
    PixelToggle toggle=new PixelToggle(context,checked);
    toggle.setContentDescription(name);toggle.setAction(save);
    line.addView(toggle,new LinearLayout.LayoutParams(dp(56),dp(33)));
    row.setClickable(true);row.setFocusable(true);
    row.setContentDescription(name);
    row.setOnClickListener(v->toggle.performClick());
    body.addView(row,width(62,9));
  }
  /** Angular, canvas-drawn ON/OFF slider: avoids Android system rounded switches. */
  private static final class PixelToggle extends View {
    private final Paint p=new Paint();
    private boolean checked;
    private Consumer<Boolean> action;
    PixelToggle(Context c,boolean checked){super(c);this.checked=checked;
      setClickable(true);setFocusable(true);setContentDescription(checked?"Ligado":"Desligado");}
    void setAction(Consumer<Boolean> action){this.action=action;}
    @Override public boolean performClick(){
      super.performClick();checked=!checked;
      if(action!=null)action.accept(checked);
      setContentDescription(checked?"Ligado":"Desligado");invalidate();return true;
    }
    @Override protected void onDraw(Canvas c){
      float w=getWidth(),h=getHeight(),u=Math.max(1f,Math.min(w/56f,h/33f));
      p.setAntiAlias(false);p.setColor(0xff06070a);
      c.drawRect(0,0,w,h,p);
      p.setColor(0xff858c94);c.drawRect(2*u,3*u,w-2*u,h-3*u,p);
      p.setColor(checked?0xffa21d24:0xff181b21);
      c.drawRect(4*u,5*u,w-4*u,h-5*u,p);
      p.setColor(checked?0xfff3443d:0xff48505a);
      float left=checked?w-23*u:5*u;
      c.drawRect(left,5*u,left+18*u,h-5*u,p);
      p.setColor(0xffdee3e8);c.drawRect(left+3*u,7*u,left+15*u,h-7*u,p);
      p.setColor(0xff898f9b);c.drawRect(left+11*u,8*u,left+15*u,h-8*u,p);
    }
  }
}
