package com.pixelchess.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

/** Shared presentation only: existing dialog adapters and callbacks remain intact. */
final class RoyalUi {
  static final int CREAM=0xffeee4d2,GOLD=0xffe2b96b,MUTED=0xffc5bca9,BACKGROUND=0xff171317;
  private RoyalUi(){}
  static int dp(Context c,int value){return Math.round(value*c.getResources().getDisplayMetrics().density);}
  static RoyalPanelDrawable panel(Context c,boolean selected){return new RoyalPanelDrawable(c.getResources().getDisplayMetrics().density,selected);}
  static void screen(View view){view.setBackground(new RoyalLibraryDrawable(view.getContext()));}
  static void text(TextView v,int sp,boolean heading){v.setTextColor(heading?GOLD:CREAM);v.setTypeface(Typeface.SERIF,heading?Typeface.BOLD:Typeface.NORMAL);v.setTextSize(sp);}
  static void button(Button b){
    b.setBackgroundTintList(null);b.setBackground(panel(b.getContext(),false));b.setTextColor(CREAM);b.setTypeface(Typeface.SERIF,Typeface.BOLD);
    b.setAllCaps(false);b.setTextSize(14);b.setMinHeight(dp(b.getContext(),48));b.setPadding(dp(b.getContext(),18),dp(b.getContext(),12),dp(b.getContext(),18),dp(b.getContext(),12));
  }
  static ColorStateList tint(){return new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{GOLD,0xff675748});}
  static AlertDialog.Builder dialog(Context context){
    return new AlertDialog.Builder(context){
      @Override public AlertDialog create(){AlertDialog dialog=super.create();dialog.setOnShowListener(d->decorate(dialog));return dialog;}
      @Override public AlertDialog show(){AlertDialog dialog=create();dialog.show();return dialog;}
    };
  }
  static void decorate(AlertDialog dialog){
    Window window=dialog.getWindow();if(window==null)return;window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    View content=window.findViewById(android.R.id.content);if(content==null)return;
    View panel=content instanceof ViewGroup&&((ViewGroup)content).getChildCount()>0?((ViewGroup)content).getChildAt(0):content;
    panel.setBackground(RoyalUi.panel(dialog.getContext(),false));panel.setPadding(dp(dialog.getContext(),10),dp(dialog.getContext(),12),dp(dialog.getContext(),10),dp(dialog.getContext(),12));
    decorateTree(panel);
    ListView list=dialog.getListView();if(list!=null){
      list.setDivider(new ColorDrawable(0xff6e4c2e));list.setDividerHeight(dp(dialog.getContext(),5));
      list.setSelector(RoyalUi.panel(dialog.getContext(),true));list.setDrawSelectorOnTop(false);
      list.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener(){
        public void onChildViewAdded(View parent,View child){decorateTree(child);child.setMinimumHeight(dp(child.getContext(),56));}
        public void onChildViewRemoved(View parent,View child){}
      });
      for(int i=0;i<list.getChildCount();i++){View child=list.getChildAt(i);decorateTree(child);child.setMinimumHeight(dp(child.getContext(),56));}
    }
    for(int which:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEGATIVE,AlertDialog.BUTTON_NEUTRAL}){
      Button b=dialog.getButton(which);if(b!=null)button(b);
    }
  }
  private static void decorateTree(View view){
    if(view instanceof Button){button((Button)view);return;}
    if(view instanceof TextView){TextView text=(TextView)view;text.setTextColor(CREAM);text.setTypeface(Typeface.SERIF,Typeface.NORMAL);text.setLinkTextColor(GOLD);
      int title=view.getResources().getIdentifier("alertTitle","id","android");if(view.getId()==title){text.setTextColor(GOLD);text.setTypeface(Typeface.SERIF,Typeface.BOLD);}}
    if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)decorateTree(group.getChildAt(i));}
  }
}
