package com.pixelchess.app;
import android.app.Activity; import android.os.Bundle; import android.graphics.*; import android.view.*; import android.content.*;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new BoardView(this));}
 static class BoardView extends View {
  Paint p=new Paint(1); String[][] b=new String[8][8]; int sr=-1,sc=-1; boolean white=true; String back="rnbqkbnr";
  BoardView(Context c){super(c);p.setTypeface(Typeface.MONOSPACE);for(int i=0;i<8;i++){b[0][i]=""+back.charAt(i);b[1][i]="p";b[6][i]="P";b[7][i]=(""+back.charAt(i)).toUpperCase();}}
  protected void onDraw(Canvas c){super.onDraw(c);float s=getWidth()/8f,top=(getHeight()-getWidth())/2f;p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s*.62f);
   for(int r=0;r<8;r++)for(int x=0;x<8;x++){p.setColor(((r+x)&1)==0?Color.rgb(224,214,181):Color.rgb(92,111,72));c.drawRect(x*s,top+r*s,(x+1)*s,top+(r+1)*s,p);if(r==sr&&x==sc){p.setColor(0x66FFFF00);c.drawRect(x*s,top+r*s,(x+1)*s,top+(r+1)*s,p);}String q=b[r][x];if(q!=null){p.setColor(Character.isUpperCase(q.charAt(0))?Color.WHITE:Color.BLACK);c.drawText(sym(q),x*s+s/2,top+r*s+s*.7f,p);}}}
  String sym(String q){String a="kqrbnp";String[] z={"♚","♛","♜","♝","♞","♟"};int i=a.indexOf(Character.toLowerCase(q.charAt(0)));return i<0?q:z[i];}
  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float s=getWidth()/8f,top=(getHeight()-getWidth())/2f;int x=(int)(e.getX()/s),r=(int)((e.getY()-top)/s);if(r<0||r>7||x<0||x>7)return true;if(sr<0){if(b[r][x]!=null&&Character.isUpperCase(b[r][x].charAt(0))==white){sr=r;sc=x;invalidate();}}else{if(valid(sr,sc,r,x)){b[r][x]=b[sr][sc];b[sr][sc]=null;white=!white;}sr=sc=-1;invalidate();}return true;}
  boolean valid(int a,int c,int d,int f){String q=b[a][c];if(q==null)return false;if(b[d][f]!=null&&Character.isUpperCase(q.charAt(0))==Character.isUpperCase(b[d][f].charAt(0)))return false;int dr=d-a,dc=f-c,ar=Math.abs(dr),ac=Math.abs(dc);char t=Character.toLowerCase(q.charAt(0));if(t=='n')return ar*ac==2;if(t=='k')return ar<=1&&ac<=1;if(t=='p'){int dir=Character.isUpperCase(q.charAt(0))?-1:1;return (dc==0&&b[d][f]==null&&dr==dir)||(ac==1&&dr==dir&&b[d][f]!=null);}if(t=='r'&&dr!=0&&dc!=0)return false;if(t=='b'&&ar!=ac)return false;if(t=='q'&&dr!=0&&dc!=0&&ar!=ac)return false;int rr=Integer.signum(dr),cc=Integer.signum(dc),r=a+rr,x=c+cc;while(r!=d||x!=f){if(b[r][x]!=null)return false;r+=rr;x+=cc;}return true;}
 }
}