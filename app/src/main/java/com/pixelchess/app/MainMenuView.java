package com.pixelchess.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Approved Royal Library menu with real, accessible Android controls. */
public final class MainMenuView extends FrameLayout {
  public interface Actions {
    void playLocal();void playBot();void playBluetooth();void chooseSkin();void openSettings();void toggleMusic();
    default void openLicenses(){}
  }
  private final float density;
  private final int cream=0xffeee4d2;
  private final SceneContent content;
  private final TextView music;

  public MainMenuView(Context context,String version,BoardTheme theme,boolean musicMuted,Actions actions){
    super(context);density=getResources().getDisplayMetrics().density;setBackgroundColor(0xff100a08);
    ScrollView scroll=new ScrollView(context);scroll.setFillViewport(true);scroll.setClipToPadding(false);
    content=new SceneContent(context,version,theme,actions);
    scroll.addView(content,new ScrollView.LayoutParams(-1,-2));addView(scroll,new LayoutParams(-1,-1));
    music=label(musicMuted?"♪̸":"♫",24,cream,Typeface.BOLD);music.setGravity(Gravity.CENTER);
    music.setContentDescription(musicMuted?"Ativar música":"Desativar música");
    GradientDrawable musicFrame=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff653119,0xff25140f});
    musicFrame.setCornerRadius(dp(10));musicFrame.setStroke(dp(2),0xffe2b96b);music.setBackground(musicFrame);
    music.setClickable(true);music.setFocusable(true);music.setOnClickListener(v->actions.toggleMusic());
    LayoutParams musicLp=new LayoutParams(dp(50),dp(50),Gravity.BOTTOM|Gravity.END);
    musicLp.setMargins(0,0,dp(18),dp(18));addView(music,musicLp);
  }
  /** Updates the real audio toggle without resetting the selected card or candle animation. */
  void setMusicMuted(boolean muted){music.setText(muted?"♪̸":"♫");music.setContentDescription(muted?"Ativar música":"Desativar música");}
  int selectedIndex(){return content.scene.selectedIndex();}

  private final class SceneContent extends FrameLayout {
    final RoyalMenuScene scene;
    final LinearLayout[] cards=new LinearLayout[5];
    final TextView[] titles=new TextView[5],subtitles=new TextView[5],icons=new TextView[5];
    final TextView footer,licenses;
    SceneContent(Context context,String version,BoardTheme theme,Actions actions){
      super(context);scene=new RoyalMenuScene(context);addView(scene,new LayoutParams(-1,-1));
      String[] labels={"PARTIDA LOCAL","JOGAR CONTRA BOT","MULTIPLAYER BLUETOOTH","SKINS DO TABULEIRO","CONFIGURAÇÕES"};
      String[] detail={"Duas pessoas no mesmo aparelho","Stockfish offline • Cinco dificuldades","Jogue offline com outro celular",theme.name,"Som, vibração e preferências"};
      String[] symbols={"▶","♟","ϟ","▦","⚙"};
      Runnable[] routes={actions::playLocal,actions::playBot,actions::playBluetooth,actions::chooseSkin,actions::openSettings};
      for(int i=0;i<cards.length;i++){
        final int index=i;LinearLayout card=new LinearLayout(context);cards[i]=card;card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);card.setClickable(true);card.setFocusable(true);card.setSelected(i==0);
        card.setContentDescription(labels[i]+". "+detail[i]);
        titles[i]=label(labels[i],15,cream,Typeface.BOLD);titles[i].setGravity(Gravity.START);titles[i].setSingleLine(true);
        subtitles[i]=label(detail[i],11,0xffd8ccba,Typeface.NORMAL);subtitles[i].setGravity(Gravity.START);subtitles[i].setSingleLine(true);
        subtitles[i].setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(titles[i],new LinearLayout.LayoutParams(-1,-2));card.addView(subtitles[i],new LinearLayout.LayoutParams(-1,-2));
        icons[i]=label(symbols[i],27,0xfff6d591,Typeface.BOLD);icons[i].setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        icons[i].setGravity(Gravity.CENTER);addView(icons[i],new LayoutParams(-2,-2));addView(card,new LayoutParams(-1,-2));
        card.setOnFocusChangeListener((v,hasFocus)->{if(hasFocus)selectCard(index);});
        card.setOnClickListener(v->{selectCard(index);routes[index].run();});
      }
      footer=label("v"+version+"  •  PixelChess",11,0xffddc79f,Typeface.NORMAL);addView(footer,new LayoutParams(-1,-2));
      licenses=label("LICENÇAS E CRÉDITOS • GPL v3",11,0xffddc79f,Typeface.NORMAL);
      licenses.setClickable(true);licenses.setFocusable(true);licenses.setOnClickListener(v->actions.openLicenses());
      addView(licenses,new LayoutParams(-1,dp(48)));
    }
    void selectCard(int index){for(int i=0;i<cards.length;i++)cards[i].setSelected(i==index);scene.select(index);}
    @Override protected void onMeasure(int widthSpec,int heightSpec){
      int width=MeasureSpec.getSize(widthSpec);
      float fontScale=getResources().getConfiguration().fontScale;
      int desired=Math.max(Math.round(width*16f/9f),Math.round(dp(760)*Math.max(1,fontScale)));
      int height=MeasureSpec.getMode(heightSpec)==MeasureSpec.EXACTLY?MeasureSpec.getSize(heightSpec):desired;
      // FillViewport can request a larger exact height after the initial scroll measurement.
      super.onMeasure(widthSpec,MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
      scene.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
      int cardWidth=Math.round(width*.656f),cardHeight=Math.round(height*.0765f);
      float fontPx=width*.033f*Math.max(1,fontScale);
      for(int i=0;i<cards.length;i++){
        cards[i].setPadding(Math.round(cardWidth*.225f),0,Math.round(cardWidth*.045f),0);
        titles[i].setSingleLine(fontScale<=1.05f);titles[i].setMaxLines(fontScale>1.05f?2:1);
        titles[i].setTextSize(TypedValue.COMPLEX_UNIT_PX,fontPx);subtitles[i].setTextSize(TypedValue.COMPLEX_UNIT_PX,width*.025f*Math.max(1,fontScale));
        icons[i].setTextSize(TypedValue.COMPLEX_UNIT_PX,width*.065f);
        cards[i].measure(MeasureSpec.makeMeasureSpec(cardWidth,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(cardHeight,MeasureSpec.EXACTLY));
        icons[i].measure(MeasureSpec.makeMeasureSpec(Math.round(cardWidth*.15f),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(cardHeight,MeasureSpec.EXACTLY));
      }
      footer.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(dp(24),MeasureSpec.EXACTLY));
      licenses.measure(MeasureSpec.makeMeasureSpec(Math.round(width*.65f),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(dp(48),MeasureSpec.EXACTLY));
      setMeasuredDimension(width,height);
    }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
      int width=r-l,height=b-t;scene.layout(0,0,width,height);
      for(int i=0;i<cards.length;i++){
        int left=Math.round(width*.172f),top=Math.round(height*RoyalMenuScene.ROW_TOPS[i]);
        cards[i].layout(left,top,left+cards[i].getMeasuredWidth(),top+cards[i].getMeasuredHeight());
        int iconLeft=left+Math.round(cards[i].getMeasuredWidth()*.035f);
        icons[i].layout(iconLeft,top,iconLeft+icons[i].getMeasuredWidth(),top+icons[i].getMeasuredHeight());
      }
      int footerTop=height-dp(74);footer.layout(0,footerTop,width,footerTop+footer.getMeasuredHeight());
      int licenseLeft=Math.round(width*.175f),licenseTop=height-dp(48);
      licenses.layout(licenseLeft,licenseTop,licenseLeft+licenses.getMeasuredWidth(),height);
    }
  }
  private TextView label(String text,int sp,int color,int style){
    TextView v=new TextView(getContext());v.setText(text);v.setTextSize(sp);v.setTextColor(color);
    v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.SERIF,style);v.setIncludeFontPadding(false);return v;
  }
  private int dp(int value){return Math.round(value*density);}
}
