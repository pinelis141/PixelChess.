package com.pixelchess.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import com.pixelchess.app.bot.BotDifficulty;
import java.util.ArrayList;
import java.util.List;

/** Shared Royal Library presentation for every match-setup flow; no game logic duplicated. */
final class RoyalSetupView extends ScrollView {
  enum Mode { LOCAL, BOT, BLUETOOTH }
  interface Actions {
    void back();
    void local(int minutes, boolean blackAtBottom);
    void bot(int minutes, BotDifficulty difficulty, int color);
    void host(int minutes);
    void join();
    void pairDevices();
  }
  private final Context context;
  private final Mode mode;
  private final Actions actions;
  private final GamePreferences preferences;
  private final LinearLayout body;
  private int minutes, color=0, level=2;
  private boolean blackAtBottom;

  RoyalSetupView(Context c, Mode mode, GamePreferences preferences, Actions actions) {
    super(c);
    this.context=c;this.mode=mode;this.preferences=preferences;this.actions=actions;
    this.minutes=preferences.defaultMinutes();this.blackAtBottom=preferences.blackAtBottom();
    setFillViewport(true);setClipToPadding(false);RoyalUi.screen(this);
    body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
    body.setGravity(Gravity.CENTER_HORIZONTAL);body.setPadding(dp(18),dp(34),dp(18),dp(52));
    addView(body,new LayoutParams(-1,-2));
    header();
    if(mode==Mode.LOCAL)localOptions();
    else if(mode==Mode.BOT)botOptions();
    else bluetoothOptions();
  }
  private int dp(int n){return RoyalUi.dp(context,n);}
  private TextView text(String value,int size,boolean heading){
    TextView t=new TextView(context);t.setText(value);RoyalUi.text(t,size,heading);
    t.setGravity(Gravity.CENTER_VERTICAL);t.setIncludeFontPadding(true);return t;
  }
  private void header(){
    Button back=new Button(context);back.setText("‹  VOLTAR");RoyalUi.button(back);
    back.setContentDescription("Voltar ao menu principal");back.setOnClickListener(v->actions.back());
    body.addView(back,new LinearLayout.LayoutParams(-1,dp(54)));
    TextView crest=text("♞",45,true);crest.setGravity(Gravity.CENTER);crest.setContentDescription("Emblema Pixel Chess");
    LinearLayout.LayoutParams crestLp=new LinearLayout.LayoutParams(-1,dp(82));crestLp.topMargin=dp(14);body.addView(crest,crestLp);
    String title=mode==Mode.LOCAL?"PARTIDA LOCAL":mode==Mode.BOT?"JOGAR CONTRA BOT":"MULTIPLAYER BLUETOOTH";
    TextView heading=text(title,mode==Mode.BLUETOOTH?27:31,true);heading.setGravity(Gravity.CENTER);
    body.addView(heading,new LinearLayout.LayoutParams(-1,-2));
    TextView sub=text(mode==Mode.LOCAL?"Jogo no mesmo aparelho":mode==Mode.BOT?"Stockfish offline":"Jogue offline com outro celular",15,false);
    sub.setGravity(Gravity.CENTER);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(40));sp.bottomMargin=dp(17);body.addView(sub,sp);
  }
  private LinearLayout card(String heading,String subtitle,String icon){
    LinearLayout panel=new LinearLayout(context);panel.setOrientation(LinearLayout.VERTICAL);
    panel.setBackground(RoyalUi.panel(context,false));panel.setPadding(dp(16),dp(13),dp(16),dp(15));
    LinearLayout caption=new LinearLayout(context);caption.setGravity(Gravity.CENTER_VERTICAL);
    TextView symbol=text(icon,27,true);symbol.setGravity(Gravity.CENTER);
    caption.addView(symbol,new LinearLayout.LayoutParams(dp(46),dp(46)));
    LinearLayout copy=new LinearLayout(context);copy.setOrientation(LinearLayout.VERTICAL);
    TextView title=text(heading,17,true);copy.addView(title);
    TextView description=text(subtitle,13,false);copy.addView(description);
    caption.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
    panel.addView(caption);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(13);body.addView(panel,lp);
    return panel;
  }
  /** Selected states are native drawables and move to the newly tapped option. */
  private void choices(LinearLayout parent,String[] labels,int initial,java.util.function.IntConsumer changed) {
    LinearLayout group=new LinearLayout(context);group.setOrientation(LinearLayout.HORIZONTAL);
    LinearLayout.LayoutParams gLp=new LinearLayout.LayoutParams(-1,dp(56));gLp.topMargin=dp(8);parent.addView(group,gLp);
    List<TextView> chips=new ArrayList<>();
    for(int i=0;i<labels.length;i++){
      final int selected=i;TextView chip=text(labels[i],labels.length==5?18:14,true);
      chip.setGravity(Gravity.CENTER);chip.setBackground(RoyalUi.panel(context,false));
      chip.setSelected(i==initial);chip.setClickable(true);chip.setFocusable(true);
      chip.setContentDescription(labels[i]);chip.setOnClickListener(v->{
        for(int j=0;j<chips.size();j++)chips.get(j).setSelected(j==selected);
        changed.accept(selected);
      });
      LinearLayout.LayoutParams chipLp=new LinearLayout.LayoutParams(0,-1,1f);if(i>0)chipLp.leftMargin=dp(4);
      group.addView(chip,chipLp);chips.add(chip);
    }
  }
  private void timeOptions(boolean host) {
    LinearLayout clock=card("TEMPO DE PARTIDA",host?"Definido pelo criador da sala":"Relógio para ambos","◷");
    choices(clock,new String[]{"3 MIN","5 MIN","10 MIN"},minutes==3?0:minutes==5?1:2,i->{
      minutes=new int[]{3,5,10}[i];preferences.defaultMinutes(minutes);
    });
  }
  private void bigButton(String title,Runnable action){
    Button go=new Button(context);go.setText(title);RoyalUi.button(go);go.setSelected(true);
    go.setTextSize(20);go.setMinHeight(dp(66));go.setOnClickListener(v->action.run());
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(70));lp.topMargin=dp(15);body.addView(go,lp);
  }
  private void localOptions(){
    // White always moves first; this option sets the fixed perspective only.
    LinearLayout side=card("ORIENTAÇÃO DO TABULEIRO","Escolha o lado exibido na parte inferior","♟");
    choices(side,new String[]{"BRANCAS","PRETAS"},blackAtBottom?1:0,i->{blackAtBottom=i==1;});
    timeOptions(false);
    LinearLayout info=card("PROMOÇÃO DE PEÃO","Ao chegar ao fim, escolha a peça manualmente","♛");
    TextView note=text("Rainha, torre, bispo ou cavalo",12,false);note.setGravity(Gravity.CENTER);info.addView(note);
    bigButton("INICIAR PARTIDA",()->actions.local(minutes,blackAtBottom));
  }
  private void botOptions(){
    LinearLayout challenge=card("DIFICULDADE","Cinco níveis de desafio","▥");
    choices(challenge,new String[]{"1","2","3","4","5"},level,i->level=i);
    LinearLayout side=card("LADO DAS PEÇAS","Defina sua cor","♟");
    choices(side,new String[]{"BRANCAS","PRETAS","ALEATÓRIO"},color,i->color=i);
    timeOptions(false);
    LinearLayout tip=card("DICAS VISUAIS","Check, mate e casas destacadas","✦");
    TextView note=text("As indicações seguem as regras da partida.",12,false);note.setGravity(Gravity.CENTER);tip.addView(note);
    bigButton("JOGAR AGORA",()->actions.bot(minutes,BotDifficulty.values()[level],color));
  }
  private void bluetoothOptions(){
    option("CRIAR SALA","Hospede a partida no seu aparelho","♜",()->actions.host(minutes),true);
    option("ENTRAR EM SALA","Conecte-se a um anfitrião","⇄",actions::join,false);
    option("DISPOSITIVOS PAREADOS","Gerencie conexões Bluetooth","⌁",actions::pairDevices,false);
    timeOptions(true);
    TextView note=text("Somente o criador escolhe o tempo. A vibração pode ser alterada nas Configurações.",13,false);
    note.setGravity(Gravity.CENTER);body.addView(note,new LinearLayout.LayoutParams(-1,dp(70)));
  }
  private void option(String title,String subtitle,String symbol,Runnable action,boolean selected){
    LinearLayout item=card(title,subtitle,symbol);item.setSelected(selected);
    item.setClickable(true);item.setFocusable(true);item.setOnClickListener(v->action.run());
    item.setContentDescription(title+". "+subtitle);
    item.setMinimumHeight(dp(88));
  }
}
