package com.pixelchess.app;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.List;

/** Genuine layered previews: the board and scene come from the registered game assets. */
public final class ThemeSelectorView extends ScrollView {
  public interface Listener {void onThemeSelected(BoardTheme theme);void onClose();}
  private BoardTheme pending;
  private final LinearLayout[] entries;
  private final TextView[] states;

  public ThemeSelectorView(Context c,List<BoardTheme> themes,String selectedId,Listener listener){
    super(c);setFillViewport(true);setVerticalScrollBarEnabled(false);RoyalUi.screen(this);
    LinearLayout body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
    body.setGravity(Gravity.CENTER_HORIZONTAL);
    body.setPadding(dp(c,13),dp(c,12),dp(c,13),dp(c,28));
    addView(body,new LayoutParams(-1,-2));
    Button back=new Button(c);back.setText("‹  VOLTAR");RoyalUi.button(back);
    back.setOnClickListener(v->listener.onClose());back.setTextSize(13);
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(c,104),dp(c,48));bp.gravity=Gravity.START;body.addView(back,bp);
    LinearLayout.LayoutParams hp=width(c,-2,12);body.addView(new PixelMenuHeader(c,"SKINS DO TABULEIRO"),hp);
    entries=new LinearLayout[themes.size()];states=new TextView[themes.size()];
    int selectedIndex=0;for(int i=0;i<themes.size();i++)if(themes.get(i).id.equals(selectedId))selectedIndex=i;
    pending=themes.get(selectedIndex);
    for(int i=0;i<themes.size();i++){
      final BoardTheme theme=themes.get(i);final int index=i;
      LinearLayout row=new LinearLayout(c);entries[i]=row;row.setOrientation(LinearLayout.HORIZONTAL);
      row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(c,9),dp(c,8),dp(c,12),dp(c,8));
      row.setBackground(RoyalUi.panel(c,false));
      row.setSelected(i==selectedIndex);row.setAlpha(theme.locked?.62f:1f);
      row.setClickable(!theme.locked);row.setFocusable(!theme.locked);
      row.setContentDescription(theme.name+(theme.locked?", bloqueada":i==selectedIndex?", em uso":""));
      FrameLayout preview=thumbnail(c,theme);
      row.addView(preview,new LinearLayout.LayoutParams(dp(c,111),dp(c,86)));
      LinearLayout copy=new LinearLayout(c);copy.setOrientation(LinearLayout.VERTICAL);
      copy.setGravity(Gravity.CENTER_VERTICAL);copy.setPadding(dp(c,11),0,0,0);
      TextView name=new TextView(c);name.setText(theme.name);name.setAllCaps(true);
      RoyalUi.text(name,theme.name.length()>16?13:15,true);name.setTextColor(RoyalUi.CREAM);
      copy.addView(name,new LinearLayout.LayoutParams(-1,-2));
      TextView state=new TextView(c);states[i]=state;
      state.setText(theme.locked?"BLOQUEADA":i==selectedIndex?"EM USO":"TOQUE PARA SELECIONAR");
      RoyalUi.text(state,11,false);state.setTextColor(i==selectedIndex?RoyalUi.RED:RoyalUi.MUTED);
      LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(c,5);copy.addView(state,sp);
      row.addView(copy,new LinearLayout.LayoutParams(0,-1,1f));
      if(!theme.locked)row.setOnClickListener(v->{
        pending=theme;
        for(int j=0;j<entries.length;j++){
          boolean pick=j==index;boolean inUse=themes.get(j).id.equals(selectedId);
          entries[j].setSelected(pick);
          states[j].setText(pick?(inUse?"EM USO":"SELECIONADA"):inUse?"EM USO":"TOQUE PARA SELECIONAR");
          states[j].setTextColor(pick?RoyalUi.RED:RoyalUi.MUTED);
        }
      });
      body.addView(row,width(c,106,9));
    }
    Button apply=new Button(c);apply.setText("APLICAR SKIN");RoyalUi.button(apply);apply.setSelected(true);
    apply.setTextSize(17);apply.setOnClickListener(v->{if(!pending.locked)listener.onThemeSelected(pending);});
    LinearLayout.LayoutParams ap=width(c,57,0);ap.topMargin=dp(c,9);body.addView(apply,ap);
    // Piece choice lives on the same screen but is stored independently from board themes.
    LinearLayout.LayoutParams piecesHeader=width(c,-2,12);
    piecesHeader.topMargin=dp(c,24);
    body.addView(new PixelMenuHeader(c,"SKINS DAS PEÇAS"),piecesHeader);
    final PieceSkin[] choice={PieceSkin.load(c)};
    final LinearLayout[] pieceRows=new LinearLayout[PieceSkin.values().length];
    int position=0;
    for(PieceSkin skin:PieceSkin.values()){
      final int index=position++;
      LinearLayout row=new LinearLayout(c);pieceRows[index]=row;
      row.setOrientation(LinearLayout.VERTICAL);
      row.setPadding(dp(c,16),dp(c,12),dp(c,16),dp(c,12));
      row.setBackground(RoyalUi.panel(c,false));row.setSelected(choice[0]==skin);
      TextView name=new TextView(c);name.setText(skin.label);
      RoyalUi.text(name,15,true);name.setTextColor(RoyalUi.CREAM);row.addView(name);
      TextView description=new TextView(c);description.setText(skin.description);
      RoyalUi.text(description,12,false);description.setTextColor(RoyalUi.MUTED);row.addView(description);
      row.setContentDescription(skin.label+(choice[0]==skin?", selecionada":""));
      row.setOnClickListener(v->{
        choice[0]=skin;
        for(int j=0;j<pieceRows.length;j++){
          pieceRows[j].setSelected(j==index);
          pieceRows[j].setContentDescription(PieceSkin.values()[j].label+(j==index?", selecionada":""));
        }
      });
      body.addView(row,width(c,76,9));
    }
    Button applyPieces=new Button(c);applyPieces.setText("APLICAR PEÇAS");
    RoyalUi.button(applyPieces);
    applyPieces.setOnClickListener(v->{choice[0].save(c);listener.onClose();});
    body.addView(applyPieces,width(c,57,0));
  }
  private FrameLayout thumbnail(Context c,BoardTheme theme){
    FrameLayout box=new FrameLayout(c);box.setBackground(RoyalUi.panel(c,false));
    if(theme.backgroundRes!=0){
      ImageView scene=new ImageView(c);scene.setImageResource(theme.backgroundRes);
      scene.setScaleType(ImageView.ScaleType.CENTER_CROP);scene.setAlpha(.8f);
      box.addView(scene,new FrameLayout.LayoutParams(-1,-1));
    }
    if(theme.frameRes!=0){
      ImageView edge=new ImageView(c);edge.setImageResource(theme.frameRes);
      edge.setScaleType(ImageView.ScaleType.FIT_CENTER);
      FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(c,86),dp(c,86),Gravity.CENTER);
      box.addView(edge,fp);
    }
    ImageView board=new ImageView(c);board.setImageResource(theme.boardRes);
    board.setScaleType(ImageView.ScaleType.FIT_CENTER);
    FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(c,62),dp(c,62),Gravity.CENTER);
    box.addView(board,lp);
    return box;
  }
  private int dp(Context c,int n){return RoyalUi.dp(c,n);}
  private LinearLayout.LayoutParams width(Context c,int h,int marginBottom){
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
      Math.min(c.getResources().getDisplayMetrics().widthPixels-dp(c,32),dp(c,400)),
      h<0?h:dp(c,h));
    lp.bottomMargin=dp(c,marginBottom);return lp;
  }
}
