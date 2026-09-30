package com.pixelchess.app;

import android.app.AlertDialog;
import android.os.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.content.Context;
import java.util.*;

public final class ChessView extends View {
  interface Actions {
    void requestMove(int r1,int c1,int r2,int c2,String promotion);
    void sendAuthorityMove(int r1,int c1,int r2,int c2,String promotion);
    void sendClockSync(); void sendFlag(boolean loserWhite); void sendReject();
    boolean acceptSequence(long sequence); void playSound(boolean capture,boolean terminal);
    void desynchronized();
  }
  private final Actions actions;
  private final boolean bluetoothGame,myWhite;
  private final int selectedMinutes;
  private final GamePreferences gamePreferences;
  private boolean suspended;
  private AlertDialog promotionDialog;
  private static final int bg=0xff14181c;
  Paint p=new Paint(3); int sr=-1,sc=-1; String status="BRANCAS JOGAM";
  ChessGame gameState=new ChessGame(); final GameClock matchClock;
  HashMap<Character,Bitmap> pieceSprites=new HashMap<>(); final BoardThemeRenderer themeRenderer; final SceneGeometry boardGeometry=new SceneGeometry(); Paint spritePaint=new Paint();
  boolean flagSent=false,awaitingAuthority=false; boolean animating=false; int animR1,animC1,animR2,animC2; String animPiece,capturedPiece; long animStart; final long ANIM_MS=220,KNIGHT_ANIM_MS=360; Handler clock=new Handler(Looper.getMainLooper()); Runnable ticker;
  ChessView(Context c,BoardTheme selectedTheme,int minutes,boolean online,boolean white,Actions actions){
    super(c);this.actions=actions;selectedMinutes=minutes;bluetoothGame=online;myWhite=white;gamePreferences=new GamePreferences(c);
    p.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
    spritePaint.setAntiAlias(false);spritePaint.setFilterBitmap(false);spritePaint.setDither(false);
    themeRenderer=new BoardThemeRenderer(getResources(),selectedTheme,gamePreferences.effects());
    matchClock=new GameClock(selectedMinutes,System.currentTimeMillis());
    loadPieceSprites();reset();
    ticker=()->{
      if(!suspended&&!gameState.gameOver()){
        long now=System.currentTimeMillis();
        if(!bluetoothGame||myWhite){
          GameClock.Tick tick=matchClock.tick(gameState.whiteTurn(),now);
          if(tick.timedOut){
            gameState.finish("TEMPO • "+(tick.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");
            status=gameState.status();
            if(bluetoothGame&&!flagSent){flagSent=true;actions.sendFlag(tick.loserWhite);}
          }else if(bluetoothGame&&matchClock.shouldSync(now,500)){
            actions.sendClockSync();
          }
          invalidate();
        }
        }
      clock.postDelayed(ticker,bluetoothGame&&!myWhite?250:100);
    };
  }
  @Override protected void onAttachedToWindow(){super.onAttachedToWindow();clock.removeCallbacks(ticker);clock.post(ticker);}
  @Override protected void onDetachedFromWindow(){clock.removeCallbacks(ticker);if(promotionDialog!=null)promotionDialog.dismiss();super.onDetachedFromWindow();}
  void reset(){gameState.reset();flagSent=false;awaitingAuthority=false;matchClock.reset(selectedMinutes,System.currentTimeMillis());status=gameState.status();invalidate();}
  protected void onDraw(Canvas c){
    super.onDraw(c);float den0=getResources().getDisplayMetrics().density;boardGeometry.update(getWidth(),getHeight(),den0,themeRenderer.hasScene());themeRenderer.drawBackground(c,getWidth(),getHeight(),bg,den0);float w=boardGeometry.size,s=w/8f,left0=boardGeometry.left,top=boardGeometry.top;
    p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s*.62f);
    boolean flip=bluetoothGame?!myWhite:gamePreferences.blackAtBottom();
    themeRenderer.draw(c,left0,top,w,den0);
    if(themeRenderer.animated() && isShown())postInvalidateDelayed(50);
    for(int vr=0;vr<8;vr++)for(int vx=0;vx<8;vx++){int r=flip?7-vr:vr,x=flip?7-vx:vx;
      String squarePiece=gameState.pieceAt(r,x);
      if(squarePiece!=null&&Character.toLowerCase(squarePiece.charAt(0))=='k'&&gameState.inCheck(ChessGame.isWhitePiece(squarePiece))){
        p.setColor(gameState.status().startsWith("XEQUE-MATE")?Color.rgb(198,40,40):Color.rgb(245,124,0));
        c.drawRect(left0+vx*s,top+vr*s,left0+(vx+1)*s,top+(vr+1)*s,p);
      }
      if(sr>=0&&!(r==sr&&x==sc)&&gameState.isLegal(sr,sc,r,x)){
        if(gameState.pieceAt(r,x)!=null)drawCaptureTarget(c,left0+vx*s,top+vr*s,s);
        else drawGoldMoveMarker(c,left0+vx*s,top+vr*s,s);
      }
      String q=gameState.pieceAt(r,x);if(q!=null&&!(animating&&r==animR2&&x==animC2))drawPieceSprite(c,q,left0+vx*s,top+vr*s,s);
    }
    if(animating){
      int fr=flip?7-animR1:animR1,fc=flip?7-animC1:animC1,tr=flip?7-animR2:animR2,tc=flip?7-animC2:animC2;
      boolean knight=animPiece!=null&&Character.toLowerCase(animPiece.charAt(0))=='n'&&PieceMotion.isKnightMove(fr,fc,tr,tc);
      long duration=knight?KNIGHT_ANIM_MS:ANIM_MS;
      float t=PieceMotion.progress(System.currentTimeMillis()-animStart,duration);
      float ax,ay;
      if(knight){
        ax=left0+PieceMotion.knightColumn(fr,fc,tr,tc,t)*s;
        ay=top+PieceMotion.knightRow(fr,fc,tr,tc,t)*s;
      }else{
        float u=PieceMotion.eased(t);
        ax=left0+(fc+(tc-fc)*u)*s;
        ay=top+(fr+(tr-fr)*u)*s-PieceMotion.arc(t,s);
      }
      drawPieceSprite(c,animPiece,ax,ay,s);
      if(capturedPiece!=null){
        p.setColor(Color.argb(Math.round(150*(1-t)),255,196,88));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,s*.035f));
        float radius=s*(.15f+.38f*t);c.drawCircle(left0+(tc+.5f)*s,top+(tr+.5f)*s,radius,p);p.setStyle(Paint.Style.FILL);
      }
      if(t<1f)postInvalidateOnAnimation();else{animating=false;capturedPiece=null;}
    }
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,getResources().getDisplayMetrics().density));p.setColor(Color.argb(95,20,24,28));for(int i=0;i<=8;i++){c.drawLine(left0+i*s,top,left0+i*s,top+w,p);c.drawLine(left0,top+i*s,left0+w,top+i*s,p);}p.setStyle(Paint.Style.FILL);
    float den=getResources().getDisplayMetrics().density;
    if(themeRenderer.hasBackground())p.setShadowLayer(3*den,0,den,Color.BLACK);
    boolean bottomWhite=bluetoothGame?myWhite:!gamePreferences.blackAtBottom();String topName=bottomWhite?"PRETAS":"BRANCAS";String bottomName=bottomWhite?"BRANCAS":"PRETAS";long topMs=bottomWhite?matchClock.blackMs():matchClock.whiteMs(),bottomMs=bottomWhite?matchClock.whiteMs():matchClock.blackMs();
    boolean topActive=bottomWhite?!gameState.whiteTurn():gameState.whiteTurn(),bottomActive=!topActive;
    float clockScale=themeRenderer.topClockScale(den);
    themeRenderer.drawClock(c,themeRenderer.topClockX(getWidth()),themeRenderer.topClockY(top-96*den),topName,clockText(topMs),topActive,den*clockScale,getResources().getDisplayMetrics().scaledDensity*clockScale,getWidth());
    p.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);p.setColor(gameState.gameOver()?Color.rgb(211,87,76):Color.rgb(235,205,132));if(p.measureText(status)>getWidth()*.9f)p.setTextSize(p.getTextSize()*getWidth()*.9f/p.measureText(status));c.drawText(status,getWidth()/2f,themeRenderer.hasScene()?top+w+103*den:top-38*den,p);
    p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.argb(210,235,221,184));
    for(int i=0;i<8;i++){int file=flip?7-i:i;int rank=flip?i:7-i;c.drawText(""+(char)('A'+file),left0+i*s+s/2,top+w+14*den,p);p.setTextAlign(Paint.Align.CENTER);c.drawText(""+(rank+1),left0/2f,top+i*s+s*.58f,p);p.setTextAlign(Paint.Align.CENTER);}
    themeRenderer.drawClock(c,getWidth()/2f,top+w+58*den,bottomName,clockText(bottomMs),bottomActive,den,getResources().getDisplayMetrics().scaledDensity,getWidth());
    p.setTextSize(11.5f*getResources().getDisplayMetrics().scaledDensity);p.setColor(themeRenderer.hasBackground()?Color.rgb(214,211,196):Color.rgb(142,146,143));String h=!gameState.hasHistory()?"JOGADAS  ·  nenhuma":gameState.historyLine().replace("JOGADAS:","JOGADAS  ·");if(p.measureText(h)>getWidth()*.94f)p.setTextSize(p.getTextSize()*getWidth()*.94f/p.measureText(h));c.drawText(h,getWidth()/2f,(themeRenderer.hasScene()?Math.min(getHeight()-14*den,top+w+125*den):Math.min(getHeight()-48*den,top+w+112*den)),p);
    p.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.rgb(55,59,61));if(!themeRenderer.hasScene())c.drawText("◆  PIXEL CHESS  ◆",getWidth()/2f,Math.min(getHeight()-20*den,top+w+140*den),p);p.clearShadowLayer();
  }
  void drawCaptureTarget(Canvas c,float left,float top,float size){
    p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(52,235,151,35));c.drawRect(left,top,left+size,top+size,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,size*.04f));p.setColor(Color.rgb(246,178,54));
    float in=Math.max(2f,size*.04f);c.drawRect(left+in,top+in,left+size-in,top+size-in,p);p.setStyle(Paint.Style.FILL);
  }
  void drawGoldMoveMarker(Canvas c,float left,float top,float size){
    float cx=left+size/2f,cy=top+size/2f,d=size*.115f;
    Path diamond=new Path();diamond.moveTo(cx,cy-d);diamond.lineTo(cx+d,cy);diamond.lineTo(cx,cy+d);diamond.lineTo(cx-d,cy);diamond.close();

    p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(238,218,174,75));c.drawPath(diamond,p);

    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(Math.max(2f,size*.034f));p.setColor(Color.argb(230,48,35,18));c.drawPath(diamond,p);
    p.setStrokeWidth(Math.max(1f,size*.014f));p.setColor(Color.argb(245,255,226,143));c.drawPath(diamond,p);
    p.setStyle(Paint.Style.FILL);
  }
  void loadPieceSprites(){
    pieceSprites.put('P',outlinedWhiteSprite(R.drawable.w_pawn));
    pieceSprites.put('R',outlinedWhiteSprite(R.drawable.w_rook));
    pieceSprites.put('N',outlinedWhiteSprite(R.drawable.w_knight));
    pieceSprites.put('B',outlinedWhiteSprite(R.drawable.w_bishop));
    pieceSprites.put('Q',outlinedWhiteSprite(R.drawable.w_queen));
    pieceSprites.put('K',outlinedWhiteSprite(R.drawable.w_king));
    pieceSprites.put('p',outlinedBlackSprite(R.drawable.b_pawn));
    pieceSprites.put('r',outlinedBlackSprite(R.drawable.b_rook));
    pieceSprites.put('n',outlinedBlackSprite(R.drawable.b_knight));
    pieceSprites.put('b',outlinedBlackSprite(R.drawable.b_bishop));
    pieceSprites.put('q',outlinedBlackSprite(R.drawable.b_queen));
    pieceSprites.put('k',outlinedBlackSprite(R.drawable.b_king));
  }
  Bitmap outlinedWhiteSprite(int resId){return SpriteOutline.thinDark(cleanDisconnectedSprite(resId));}
  Bitmap outlinedBlackSprite(int resId){return SpriteOutline.thinLight(cleanDisconnectedSprite(resId));}
  Bitmap cleanDisconnectedSprite(int resId){
    Bitmap src=BitmapFactory.decodeResource(getResources(),resId);
    if(src==null)return null;
    Bitmap out=src.copy(Bitmap.Config.ARGB_8888,true);
    int w=out.getWidth(),h=out.getHeight(),n=w*h;
    int[] px=new int[n];out.getPixels(px,0,w,0,0,w,h);
    int[] label=new int[n];int next=0,best=0,bestSize=0;
    ArrayDeque<Integer> q=new ArrayDeque<>();
    for(int start=0;start<n;start++){
      if(label[start]!=0||Color.alpha(px[start])<=10)continue;
      next++;int count=0;label[start]=next;q.add(start);
      while(!q.isEmpty()){
        int i=q.removeFirst();count++;int x=i%w,y=i/w,j;
        if(x>0){j=i-1;if(label[j]==0&&Color.alpha(px[j])>10){label[j]=next;q.add(j);}}
        if(x<w-1){j=i+1;if(label[j]==0&&Color.alpha(px[j])>10){label[j]=next;q.add(j);}}
        if(y>0){j=i-w;if(label[j]==0&&Color.alpha(px[j])>10){label[j]=next;q.add(j);}}
        if(y<h-1){j=i+w;if(label[j]==0&&Color.alpha(px[j])>10){label[j]=next;q.add(j);}}
      }
      if(count>bestSize){bestSize=count;best=next;}
    }
    for(int i=0;i<n;i++)if(label[i]!=0&&label[i]!=best)px[i]=Color.TRANSPARENT;
    out.setPixels(px,0,w,0,0,w,h);return out;
  }
  void drawPieceSprite(Canvas c,String q,float left,float top,float size){
    Bitmap bmp=pieceSprites.get(q.charAt(0));
    if(bmp==null){drawPixelPiece(c,q,left,top,size);return;}
    float pad=size*.035f;
    float scale=Character.toLowerCase(q.charAt(0))=='n'?.90f:1f;
    float full=size-pad*2f,draw=full*scale;
    float dx=(full-draw)/2f;
    RectF dst=new RectF(left+pad+dx,top+pad+(full-draw),left+size-pad-dx,top+size-pad);
    c.drawBitmap(bmp,null,dst,spritePaint);
  }
  String clockText(long ms){ms=Math.max(0,ms);long sec=ms/1000;return String.format(Locale.US,"%02d:%02d",sec/60,sec%60);}
  String square(int r,int c){return ""+(char)('a'+c)+(8-r);}
  String[] pixelPattern(char t){switch(Character.toLowerCase(t)){case 'p':return new String[]{"...##...","..####..","..####..","...##...","..####..",".######.","########"};case 'r':return new String[]{"##.##.##","########",".######.","..####..","..####..",".######.","########"};case 'n':return new String[]{"...###..","..#####.",".###.##.",".######.","..#####.","..####..",".######.","########"};case 'b':return new String[]{"...##...","..####..","...##...","..####..",".######.","..####..",".######.","########"};case 'q':return new String[]{"#..##..#",".######.","..####..",".######.","..####..",".######.","########","########"};default:return new String[]{"...##...",".#.##.#.",".######.","..####..",".######.","..####..",".######.","########"};}}
  void drawPixelPiece(Canvas c,String q,float left,float top,float size){String[] pat=pixelPattern(q.charAt(0));float cell=size/10f,ox=left+cell,oy=top+(size-pat.length*cell)/2f;boolean whitePiece=Character.isUpperCase(q.charAt(0));p.setStyle(Paint.Style.FILL);if(whitePiece){p.setColor(Color.rgb(35,38,40));for(int r=0;r<pat.length;r++)for(int x=0;x<pat[r].length();x++)if(pat[r].charAt(x)=='#')for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)if(Math.abs(dx)+Math.abs(dy)==1)c.drawRect(ox+(x+dx)*cell,oy+(r+dy)*cell,ox+(x+dx+1)*cell,oy+(r+dy+1)*cell,p);}p.setColor(whitePiece?Color.rgb(248,245,232):Color.rgb(25,28,31));for(int r=0;r<pat.length;r++)for(int x=0;x<pat[r].length();x++)if(pat[r].charAt(x)=='#')c.drawRect(ox+x*cell,oy+r*cell,ox+(x+1)*cell,oy+(r+1)*cell,p);}
  void startMoveAnimation(String q,int r1,int c1,int r2,int c2){animPiece=q;capturedPiece=gameState.pieceAt(r2,c2);if(capturedPiece==null&&Character.toLowerCase(q.charAt(0))=='p'&&c1!=c2)capturedPiece=gameState.pieceAt(r1,c2);animR1=r1;animC1=c1;animR2=r2;animC2=c2;animStart=System.currentTimeMillis();animating=true;postInvalidateOnAnimation();}
  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;if(suspended){toast("Partida pausada. Reconecte para continuar.");return true;}if(animating)return true;float den=getResources().getDisplayMetrics().density;boardGeometry.update(getWidth(),getHeight(),den,themeRenderer.hasScene());float gutter=boardGeometry.left,w=boardGeometry.size,s=w/8f,top=boardGeometry.top;if(e.getX()<gutter||e.getX()>=gutter+w||e.getY()<top||e.getY()>=top+w)return true;int vx=(int)((e.getX()-gutter)/s),vr=(int)((e.getY()-top)/s);if(vr<0||vr>7||vx<0||vx>7)return true;boolean flip=bluetoothGame?!myWhite:gamePreferences.blackAtBottom();int x=flip?7-vx:vx,r=flip?7-vr:vr;if(gameState.gameOver()){toast("A partida terminou");return true;}
    if(awaitingAuthority){toast("Aguardando confirmação da jogada…");return true;}
    if(bluetoothGame && gameState.whiteTurn()!=myWhite){toast("Aguarde a jogada do adversário");return true;} if(sr<0){select(r,x);}else if(sr==r&&sc==x){sr=sc=-1;invalidate();}else if(gameState.pieceAt(r,x)!=null&&ChessGame.isWhitePiece(gameState.pieceAt(r,x))==gameState.whiteTurn()){select(r,x);}else if(gameState.isLegal(sr,sc,r,x)){int a=sr,d=sc;String moving=gameState.pieceAt(a,d);boolean promotes=moving!=null&&Character.toLowerCase(moving.charAt(0))=='p'&&(r==0||r==7);if(promotes)moveWithPromotionChoice(a,d,r,x);else if(bluetoothGame&&!myWhite){awaitingAuthority=true;actions.requestMove(a,d,r,x,"-");sr=sc=-1;invalidate();}else if(move(a,d,r,x,"-")){if(bluetoothGame)actions.sendAuthorityMove(a,d,r,x,"-");sr=sc=-1;invalidate();}}return true;}
  void applyGuestPlay(int r1,int c1,int r2,int c2,String promo){if(suspended||!bluetoothGame||!myWhite||gameState.gameOver()||gameState.whiteTurn()==myWhite)return;if(!gameState.isLegal(r1,c1,r2,c2)||ChessGame.isWhitePiece(gameState.pieceAt(r1,c1))!=gameState.whiteTurn()){actions.sendReject();return;}if(move(r1,c1,r2,c2,promo)){actions.sendAuthorityMove(r1,c1,r2,c2,promo);if(!gameState.gameOver()&&gameState.whiteTurn()==myWhite)vibrateTurn();invalidate();}else if(!gameState.gameOver())actions.sendReject();}
  void applyAuthorityMove(BluetoothGameProtocol.Move m){if(!bluetoothGame||myWhite||!actions.acceptSequence(m.sequence))return;awaitingAuthority=false;if(!move(m.r1,m.c1,m.r2,m.c2,m.promotion)||gameState.whiteTurn()!=m.whiteTurn){actions.desynchronized();return;}matchClock.sync(m.whiteMs,m.blackMs,System.currentTimeMillis());status=gameState.status();sr=sc=-1;if(!gameState.gameOver()&&gameState.whiteTurn()==myWhite)vibrateTurn();invalidate();}
  void applyAuthoritySync(BluetoothGameProtocol.Sync m){if(!bluetoothGame||myWhite||!actions.acceptSequence(m.sequence))return;if(gameState.whiteTurn()!=m.whiteTurn){actions.desynchronized();return;}matchClock.sync(m.whiteMs,m.blackMs,System.currentTimeMillis());invalidate();}
  void applyAuthorityFlag(BluetoothGameProtocol.Flag m){if(!bluetoothGame||myWhite||!actions.acceptSequence(m.sequence)||gameState.gameOver())return;awaitingAuthority=false;matchClock.flag(m.loserWhite);gameState.finish("TEMPO • "+(m.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");status=gameState.status();invalidate();}
  void applyAuthorityReject(BluetoothGameProtocol.Reject m){if(!bluetoothGame||myWhite||!actions.acceptSequence(m.sequence))return;awaitingAuthority=false;if(gameState.whiteTurn()!=m.whiteTurn){actions.desynchronized();return;}matchClock.sync(m.whiteMs,m.blackMs,System.currentTimeMillis());toast("Jogada não confirmada. Estado sincronizado.");invalidate();}
  void applyConnectionLost(){
    if(!suspended&&myWhite&&!gameState.gameOver()){
      GameClock.Tick tick=matchClock.tick(gameState.whiteTurn(),System.currentTimeMillis());
      if(tick.timedOut)gameState.finish("TEMPO • "+(tick.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");
    }
    suspended=true;awaitingAuthority=false;if(promotionDialog!=null){promotionDialog.dismiss();promotionDialog=null;}
    status="CONEXÃO PERDIDA • PARTIDA PAUSADA";invalidate();
  }
  boolean online(){return bluetoothGame;}
  boolean paused(){return suspended;}
  void restore(ChessGame state,long whiteMs,long blackMs){if(promotionDialog!=null){promotionDialog.dismiss();promotionDialog=null;}gameState=state;matchClock.sync(whiteMs,blackMs,System.currentTimeMillis());suspended=false;flagSent=state.gameOver();awaitingAuthority=false;animating=false;sr=sc=-1;status=state.status();invalidate();}
  void toast(String text){Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();}
  Button button(String text){Button b=new Button(getContext());b.setText(text);return b;}

  void vibrateTurn(){if(!gamePreferences.vibration())return;try{Vibrator v=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);if(v==null||!v.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(70,VibrationEffect.DEFAULT_AMPLITUDE));else v.vibrate(70);}catch(Exception ignored){}}
  void moveWithPromotionChoice(int r1,int c1,int r2,int c2){
    final boolean side=ChessGame.isWhitePiece(gameState.pieceAt(r1,c1));final String[] labels={"DAMA","TORRE","BISPO","CAVALO"},pcs={"Q","R","B","N"};
    LinearLayout box=new LinearLayout(getContext());box.setOrientation(LinearLayout.VERTICAL);box.setPadding(32,16,32,16);
    AlertDialog dialog=new AlertDialog.Builder(getContext()).setTitle("PROMOÇÃO").setMessage("Escolha a peça:").setView(box).setCancelable(false).create();
    for(int i=0;i<4;i++){final int k=i;Button bt=button(labels[i]);bt.setTextColor(Color.BLACK);bt.setBackgroundColor(Color.rgb(238,238,238));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,6,0,6);box.addView(bt,lp);bt.setOnClickListener(v->{if(suspended){dialog.dismiss();return;}String z=pcs[k];if(!side)z=z.toLowerCase();if(bluetoothGame&&!myWhite){awaitingAuthority=true;sr=sc=-1;actions.requestMove(r1,c1,r2,c2,z);}else if(move(r1,c1,r2,c2,z)&&bluetoothGame){actions.sendAuthorityMove(r1,c1,r2,c2,z);}sr=sc=-1;dialog.dismiss();invalidate();});}
    promotionDialog=dialog;dialog.show();
  }
  void select(int r,int c){String q=gameState.pieceAt(r,c);if(q!=null&&ChessGame.isWhitePiece(q)==gameState.whiteTurn()){sr=r;sc=c;invalidate();}}
  boolean move(int r1,int c1,int r2,int c2,String promotion){
    if(suspended)return false;
    String piece=gameState.pieceAt(r1,c1);String captured=gameState.pieceAt(r2,c2);if(captured==null&&piece!=null&&Character.toLowerCase(piece.charAt(0))=='p'&&c1!=c2)captured=gameState.pieceAt(r1,c2);
    if(piece==null)return false;
    long now=System.currentTimeMillis();
    if(!bluetoothGame||myWhite){
      GameClock.Tick tick=matchClock.tick(gameState.whiteTurn(),now);
      if(tick.timedOut){
        gameState.finish("TEMPO • "+(tick.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");
        status=gameState.status();
        if(bluetoothGame&&!flagSent){flagSent=true;actions.sendFlag(tick.loserWhite);}
        invalidate();
        return false;
      }
    }
    if(!gameState.isLegal(r1,c1,r2,c2)||ChessGame.isWhitePiece(piece)!=gameState.whiteTurn())return false;
    startMoveAnimation(piece,r1,c1,r2,c2);
    if(!gameState.move(r1,c1,r2,c2,promotion)){animating=false;capturedPiece=null;invalidate();return false;}
    if(gamePreferences.sound())actions.playSound(captured!=null,gameState.gameOver());
    matchClock.markTurnChanged(now);
    status=gameState.status();
    return true;
  }
}
