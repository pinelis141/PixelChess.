package com.pixelchess.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Royal Library skin selector: ornate cards keep the current BoardThemes flow. */
public final class ThemeSelectorView extends ScrollView {
  public interface Listener {void onThemeSelected(BoardTheme theme);void onClose();}
  public ThemeSelectorView(Context context,List<BoardTheme> themes,String selectedId,Listener listener){
    super(context);setFillViewport(true);RoyalUi.screen(this);float d=getResources().getDisplayMetrics().density;
    LinearLayout root=new LinearLayout(context);root.setOrientation(LinearLayout.VERTICAL);root.setPadding((int)(20*d),(int)(20*d),(int)(20*d),(int)(24*d));
    addView(root,new LayoutParams(-1,-2));
    Button back=button(context,"‹  VOLTAR");LinearLayout.LayoutParams backLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(context,54));root.addView(back,backLp);back.setOnClickListener(v->listener.onClose());
    TextView heading=text(context,"SKINS DO TABULEIRO",24,true);heading.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams headingLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(context,44));headingLp.setMargins(0,RoyalUi.dp(context,12),0,0);root.addView(heading,headingLp);
    addDivider(context,root);
    TextView sub=text(context,"Escolha o visual do reino",14,false);sub.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);subLp.setMargins(0,0,0,RoyalUi.dp(context,17));root.addView(sub,subLp);
    for(BoardTheme theme:themes)root.addView(card(context,theme,theme.id.equals(selectedId),listener,d),cardLayout(context,d));
    TextView hint=text(context,"Novas skins aparecerão aqui.",12,false);hint.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams hintLp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(context,44));root.addView(hint,hintLp);
  }
  private View card(Context context,BoardTheme theme,boolean selected,Listener listener,float d){
    LinearLayout card=new LinearLayout(context);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding((int)(13*d),(int)(10*d),(int)(15*d),(int)(10*d));card.setBackground(RoyalUi.panel(context,selected));card.setSelected(selected);
    card.setClickable(!theme.locked);card.setFocusable(!theme.locked);card.setContentDescription(theme.name+(selected?", selecionado":theme.locked?", bloqueado":""));
    ImageView preview=new ImageView(context);preview.setImageResource(theme.boardRes);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
    preview.setBackground(RoyalUi.panel(context,false));preview.setPadding(RoyalUi.dp(context,2),RoyalUi.dp(context,2),RoyalUi.dp(context,2),RoyalUi.dp(context,2));
    card.addView(preview,new LinearLayout.LayoutParams(RoyalUi.dp(context,84),RoyalUi.dp(context,84)));
    LinearLayout copy=new LinearLayout(context);copy.setOrientation(LinearLayout.VERTICAL);copy.setGravity(Gravity.CENTER_VERTICAL);copy.setPadding(RoyalUi.dp(context,14),0,0,0);
    TextView name=text(context,theme.name,17,true);copy.addView(name,new LinearLayout.LayoutParams(-1,-2));
    TextView state=text(context,theme.locked?"🔒  BLOQUEADO":selected?"✓  SELECIONADO":"TOQUE PARA USAR",12,true);state.setTextColor(selected?RoyalUi.GOLD:theme.locked?RoyalUi.MUTED:RoyalUi.CREAM);
    LinearLayout.LayoutParams stateLp=new LinearLayout.LayoutParams(-1,-2);stateLp.setMargins(0,RoyalUi.dp(context,6),0,0);copy.addView(state,stateLp);
    card.addView(copy,new LinearLayout.LayoutParams(0,-1,1f));if(theme.locked)card.setAlpha(.7f);else card.setOnClickListener(v->listener.onThemeSelected(theme));return card;
  }
  private void addDivider(Context c,LinearLayout root){View line=new View(c);line.setBackgroundColor(RoyalUi.GOLD);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(RoyalUi.dp(c,116),RoyalUi.dp(c,2));lp.setMargins(0,0,0,RoyalUi.dp(c,9));root.addView(line,lp);}
  private LinearLayout.LayoutParams cardLayout(Context c,float d){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,RoyalUi.dp(c,114));lp.setMargins(0,0,0,RoyalUi.dp(c,11));return lp;}
  private TextView text(Context c,String value,int size,boolean bold){TextView v=new TextView(c);v.setText(value);RoyalUi.text(v,size,bold);return v;}
  private Button button(Context c,String value){Button b=new Button(c);b.setText(value);RoyalUi.button(b);return b;}
}
