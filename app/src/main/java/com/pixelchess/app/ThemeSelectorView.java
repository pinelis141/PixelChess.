package com.pixelchess.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/** Visual, data-driven theme picker. New entries in BoardThemes.ALL appear automatically. */
public final class ThemeSelectorView extends ScrollView {
  public interface Listener {
    void onThemeSelected(BoardTheme theme);
    void onClose();
  }

  private final int cream=Color.rgb(235,221,184);
  private final int bg=Color.rgb(20,24,28);
  private final int panel=Color.rgb(29,34,38);
  private final int border=Color.rgb(72,78,80);
  private final int gold=Color.rgb(210,171,82);

  public ThemeSelectorView(Context context,List<BoardTheme> themes,String selectedId,Listener listener) {
    super(context);
    setFillViewport(true);
    setBackgroundColor(bg);

    float d=getResources().getDisplayMetrics().density;
    int pad=(int)(20*d);

    LinearLayout root=new LinearLayout(context);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(pad,pad,pad,pad);
    addView(root,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

    Button back=button(context,"‹  VOLTAR");
    LinearLayout.LayoutParams backLp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,(int)(46*d));
    root.addView(back,backLp);
    back.setOnClickListener(v->listener.onClose());

    TextView heading=text(context,"ESCOLHA SEU TABULEIRO",24,cream,true);
    heading.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams headingLp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    headingLp.setMargins(0,(int)(18*d),0,(int)(6*d));
    root.addView(heading,headingLp);

    TextView sub=text(context,"A skin escolhida fica salva para as próximas partidas.",13,Color.LTGRAY,false);
    sub.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    subLp.setMargins(0,0,0,(int)(18*d));
    root.addView(sub,subLp);

    for(BoardTheme theme:themes) {
      boolean selected=theme.id.equals(selectedId);
      root.addView(card(context,theme,selected,listener,d),cardLayout(d));
    }

    TextView hint=text(context,"Novas skins aparecerão aqui automaticamente.",11,Color.GRAY,false);
    hint.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams hintLp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    hintLp.setMargins(0,(int)(8*d),0,(int)(10*d));
    root.addView(hint,hintLp);
  }

  private View card(Context context,BoardTheme theme,boolean selected,Listener listener,float d) {
    LinearLayout card=new LinearLayout(context);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding((int)(12*d),(int)(12*d),(int)(14*d),(int)(12*d));
    card.setBackground(cardBackground(selected,d));
    card.setClickable(true);
    card.setFocusable(true);
    card.setContentDescription(theme.name+(selected?", selecionado":""));

    ImageView preview=new ImageView(context);
    preview.setImageResource(theme.boardRes);
    preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
    LinearLayout.LayoutParams imageLp=new LinearLayout.LayoutParams((int)(94*d),(int)(94*d));
    card.addView(preview,imageLp);

    LinearLayout copy=new LinearLayout(context);
    copy.setOrientation(LinearLayout.VERTICAL);
    copy.setPadding((int)(16*d),0,0,0);

    TextView name=text(context,theme.name,18,cream,true);
    copy.addView(name,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

    TextView state=text(context,selected?"✓  SELECIONADO":"TOQUE PARA USAR",12,selected?gold:Color.rgb(145,151,151),true);
    LinearLayout.LayoutParams stateLp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    stateLp.setMargins(0,(int)(8*d),0,0);
    copy.addView(state,stateLp);

    card.addView(copy,new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1f));
    card.setOnClickListener(v->listener.onThemeSelected(theme));
    return card;
  }

  private LinearLayout.LayoutParams cardLayout(float d) {
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    lp.setMargins(0,0,0,(int)(12*d));
    return lp;
  }

  private GradientDrawable cardBackground(boolean selected,float d) {
    GradientDrawable g=new GradientDrawable();
    g.setColor(panel);
    g.setCornerRadius(12*d);
    g.setStroke((int)((selected?3:1)*d),selected?gold:border);
    return g;
  }

  private TextView text(Context context,String value,int sp,int color,boolean bold) {
    TextView v=new TextView(context);
    v.setText(value);
    v.setTextColor(color);
    v.setTextSize(sp);
    v.setTypeface(Typeface.MONOSPACE,bold?Typeface.BOLD:Typeface.NORMAL);
    return v;
  }

  private Button button(Context context,String value) {
    Button b=new Button(context);
    b.setText(value);
    b.setTextSize(14);
    b.setAllCaps(false);
    b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
    return b;
  }
}
