package com.pixelchess.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.pixelchess.app.bot.BotDifficulty;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/** One shared, compact pixel interface for local chess and Stockfish, with real Android controls. */
final class RoyalSetupView extends ScrollView {
  enum Mode { LOCAL, BOT, BLUETOOTH }
  interface Actions {
    void back();
    void local(int minutes,boolean blackAtBottom);
    void bot(int minutes,BotDifficulty difficulty,int color);
    void host(int minutes);
    void join();
    void pairDevices();
  }
  private final Context context;
  private final GamePreferences prefs;
  private final Actions actions;
  private final Mode mode;
  private final LinearLayout body;
  private int minutes,level,color;
  private boolean blackAtBottom;

  RoyalSetupView(Context c,Mode mode,GamePreferences prefs,Actions actions){
    super(c);this.context=c;this.mode=mode;this.prefs=prefs;this.actions=actions;
    minutes=mode==Mode.BOT?prefs.botMinutes():prefs.defaultMinutes();
    level=prefs.botLevel();color=prefs.botColor();blackAtBottom=prefs.blackAtBottom();
    RoyalUi.screen(this);setFillViewport(true);setVerticalScrollBarEnabled(false);
    body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
    body.setGravity(Gravity.CENTER_HORIZONTAL);
    body.setPadding(dp(14),dp(14),dp(14),dp(32));
    addView(body,new LayoutParams(-1,-2));
    header();
    if(mode==Mode.LOCAL)localOptions();
    else if(mode==Mode.BOT)botOptions();
    else bluetoothOptions();
  }
  private int dp(int x){return RoyalUi.dp(context,x);}
  private int maxWidth(){return Math.min(context.getResources().getDisplayMetrics().widthPixels-dp(32),dp(400));}
  private TextView label(String value,int size,boolean title){
    TextView text=new TextView(context);text.setText(value);RoyalUi.text(text,size,title);
    text.setTextColor(title?RoyalUi.CREAM:RoyalUi.MUTED);text.setGravity(Gravity.CENTER);return text;
  }
  private LinearLayout.LayoutParams centered(int height,int bottom){
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(maxWidth(),height<0?height:dp(height));
    lp.bottomMargin=dp(bottom);return lp;
  }
  private void header(){
    // The arrow has a 48dp touch target, but does not occupy a full-width banner.
    Button back=new Button(context);back.setText("‹  VOLTAR");RoyalUi.button(back);
    back.setContentDescription("Voltar ao menu principal");
    back.setOnClickListener(v->actions.back());back.setTextSize(13);
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(104),dp(48));bp.gravity=Gravity.START;
    body.addView(back,bp);
    String heading=mode==Mode.LOCAL?"PARTIDA LOCAL":mode==Mode.BOT?"JOGAR CONTRA BOT":"MULTIPLAYER BLUETOOTH";
    LinearLayout.LayoutParams hp=centered(-2,12); // 12 dp below section banner
    hp.topMargin=0;body.addView(new PixelMenuHeader(context,heading),hp);
  }
  private LinearLayout section(String title){
    LinearLayout card=new LinearLayout(context);card.setOrientation(LinearLayout.VERTICAL);
    card.setGravity(Gravity.CENTER_HORIZONTAL);
    card.setBackground(RoyalUi.panel(context,false));
    card.setPadding(dp(13),dp(8),dp(13),dp(11));
    TextView heading=label(title,17,true);card.addView(heading,new LinearLayout.LayoutParams(-1,dp(27)));
    body.addView(card,centered(-2,11));
    return card;
  }
  private void choices(LinearLayout parent,String[] labels,int initial,int height,IntConsumer save,boolean clock,boolean piece){
    LinearLayout row=new LinearLayout(context);row.setOrientation(LinearLayout.HORIZONTAL);
    LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(height));rp.topMargin=dp(4);parent.addView(row,rp);
    List<LinearLayout> tiles=new ArrayList<>();
    for(int i=0;i<labels.length;i++){
      final int chosen=i;
      LinearLayout tile=new LinearLayout(context);tile.setOrientation(LinearLayout.VERTICAL);
      tile.setGravity(Gravity.CENTER);tile.setPadding(dp(2),dp(2),dp(2),dp(2));
      tile.setBackground(RoyalUi.panel(context,false));tile.setSelected(i==initial);
      tile.setClickable(true);tile.setFocusable(true);
      tile.setContentDescription(labels[i]+(i==initial?", selecionado":""));
      if(piece && labels.length==2){
        View preview=new OrientationPreview(context,i==1);
        tile.addView(preview,new LinearLayout.LayoutParams(dp(44),dp(37)));
      }else if(clock){
        TextView icon=label("◷",21,true);icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon,new LinearLayout.LayoutParams(-1,dp(26)));
      }else if(piece){
        TextView icon=label(i==0?"♙":i==1?"♟":"⚄",20,true);
        icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon,new LinearLayout.LayoutParams(-1,dp(25)));
      }
      TextView value=label(labels[i],labels.length==5?19:labels[i].length()>10?12:15,true);
      value.setMaxLines(2);value.setClickable(true);value.setFocusable(true);
      value.setOnClickListener(v->tile.performClick());
      tile.addView(value,new LinearLayout.LayoutParams(-1,-2));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1f);
      if(i>0)lp.leftMargin=dp(5);row.addView(tile,lp);tiles.add(tile);
      tile.setOnClickListener(v->{
        for(int j=0;j<tiles.size();j++){
          tiles.get(j).setSelected(j==chosen);
          tiles.get(j).setContentDescription(labels[j]+(j==chosen?", selecionado":""));
        }
        save.accept(chosen);
      });
    }
  }
  private void timeOptions(boolean host){
    LinearLayout clock=section("TEMPO"+(host?" DA PARTIDA":""));
    choices(clock,new String[]{"3 MIN","5 MIN","10 MIN"},minutes==3?0:minutes==5?1:2,61,i->{
      minutes=new int[]{3,5,10}[i];
      if(mode==Mode.BOT)prefs.botMinutes(minutes);else prefs.defaultMinutes(minutes);
    },true,false);
    if(host){
      TextView note=label("O criador da sala define o tempo",12,false);
      clock.addView(note,new LinearLayout.LayoutParams(-1,dp(26)));
    }
  }
  private void action(String value,Runnable job){
    Button button=new Button(context);button.setText("▶   "+value);RoyalUi.button(button);
    button.setSelected(true);button.setTextSize(18);button.setOnClickListener(v->job.run());
    body.addView(button,centered(58,0));
  }
  private void localOptions(){
    // This changes perspective, not the chess rule that white always moves first.
    LinearLayout orientation=section("ORIENTAÇÃO");
    choices(orientation,new String[]{"BRANCAS\nEMBAIXO","PRETAS\nEMBAIXO"},blackAtBottom?1:0,88,
      i->blackAtBottom=i==1,false,true);
    timeOptions(false);
    action("INICIAR PARTIDA",()->{prefs.blackAtBottom(blackAtBottom);actions.local(minutes,blackAtBottom);});
  }
  private void botOptions(){
    LinearLayout difficulty=section("DIFICULDADE");
    choices(difficulty,new String[]{"1","2","3","4","5"},level,51,i->{level=i;prefs.botLevel(i);},false,false);
    LinearLayout pieces=section("COR");
    choices(pieces,new String[]{"BRANCAS","PRETAS","ALEATÓRIO"},color,65,i->{color=i;prefs.botColor(i);},false,true);
    timeOptions(false);
    action("JOGAR AGORA",()->actions.bot(minutes,BotDifficulty.values()[level],color));
  }
  private void bluetoothOptions(){
    route("CRIAR SALA","Hospedar a partida neste aparelho","♙",()->actions.host(minutes));
    route("ENTRAR EM SALA","Conectar a uma sala criada","♟",actions::join);
    route("DISPOSITIVOS PAREADOS","Gerenciar aparelhos Bluetooth","⌁",actions::pairDevices);
    timeOptions(true);
  }
  private void route(String name,String note,String icon,Runnable job){
    LinearLayout line=new LinearLayout(context);line.setOrientation(LinearLayout.HORIZONTAL);
    line.setGravity(Gravity.CENTER_VERTICAL);line.setPadding(dp(10),dp(7),dp(12),dp(7));
    line.setBackground(RoyalUi.panel(context,false));
    TextView symbol=label(icon,27,true);symbol.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    line.addView(symbol,new LinearLayout.LayoutParams(dp(44),dp(48)));
    LinearLayout text=new LinearLayout(context);text.setOrientation(LinearLayout.VERTICAL);
    TextView nameText=label(name,name.length()>18?14:17,true);nameText.setGravity(Gravity.START);
    TextView detail=label(note,12,false);detail.setGravity(Gravity.START);
    text.addView(nameText);text.addView(detail);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=dp(7);line.addView(text,lp);
    TextView arrow=label("›",27,true);line.addView(arrow,new LinearLayout.LayoutParams(dp(20),-2));
    line.setClickable(true);line.setFocusable(true);line.setContentDescription(name+". "+note);
    line.setOnClickListener(v->job.run());
    body.addView(line,centered(72,8));
  }
  /** Two little board diagrams reproduce the approved local-match orientation control. */
  private static final class OrientationPreview extends View {
    private final boolean black;
    private final Paint p=new Paint();
    OrientationPreview(Context c,boolean black){super(c);this.black=black;
      setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas c){
      int n=6;float cell=Math.min(getWidth(),getHeight())/7f;
      float left=(getWidth()-cell*n)/2f,top=(getHeight()-cell*n)/2f;
      p.setAntiAlias(false);
      for(int r=0;r<n;r++)for(int col=0;col<n;col++){
        p.setColor((r+col)%2==0?0xffd5d0c5:0xff49464b);
        c.drawRect(left+col*cell,top+r*cell,left+(col+1)*cell,top+(r+1)*cell,p);
      }
      for(int col=0;col<n;col++){
        p.setColor(black?0xff14171e:0xfff8efdf);
        float x=left+(col+.5f)*cell,y=top+(n-.43f)*cell;
        c.drawRect(x-cell*.19f,y-cell*.28f,x+cell*.19f,y+cell*.13f,p);
        c.drawRect(x-cell*.30f,y+cell*.12f,x+cell*.30f,y+cell*.22f,p);
      }
    }
  }
}
