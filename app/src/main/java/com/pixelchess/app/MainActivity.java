package com.pixelchess.app;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.bluetooth.*;
import java.util.*;
import java.io.*;
import java.util.UUID;

public class MainActivity extends Activity {
  int bg=Color.rgb(20,24,28), cream=Color.rgb(235,221,184), green=Color.rgb(75,96,67);
  static final UUID GAME_UUID=UUID.fromString("7e57c0de-5049-5845-4c43-484553530001");
  BluetoothAdapter adapter; BluetoothSocket socket; BluetoothServerSocket serverSocket; ChessView game; boolean bluetoothGame=false, myWhite=true; OutputStream btOut; int selectedMinutes=10; String selectedTheme="classic"; final Object btWriteLock=new Object();
  @Override public void onCreate(Bundle b){super.onCreate(b); selectedTheme=getSharedPreferences("pixelchess",MODE_PRIVATE).getString("theme","classic"); showMenu();}

  TextView title(String s,int sp){ TextView v=new TextView(this); v.setText(s); v.setTextColor(cream); v.setTextSize(sp); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return v; }
  Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return b; }

  void showMenu(){
    LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(40,40,40,40); box.setBackgroundColor(bg);
    TextView logo=title("♜  PIXEL CHESS  ♞",30); box.addView(logo,new LinearLayout.LayoutParams(-1,-2));
    TextView sub=title("\nXADREZ LOCAL\n",14); sub.setTextColor(Color.LTGRAY); box.addView(sub);
    Button local=button("▶ Jogar no mesmo celular"); local.setOnClickListener(v->chooseTime(false)); box.addView(local,new LinearLayout.LayoutParams(-1,-2));
    Button bt=button("⌁ Jogar via Bluetooth"); bt.setOnClickListener(v->bluetoothMenu()); box.addView(bt,new LinearLayout.LayoutParams(-1,-2));
    Button themes=button("◆ Cenário: "+themeName()); themes.setOnClickListener(v->chooseTheme()); box.addView(themes,new LinearLayout.LayoutParams(-1,-2));
    TextView ver=title("\nMVP 0.17 • Pixel Forest",12); ver.setTextColor(Color.GRAY); box.addView(ver);
    setContentView(box);
  }
  @Override public void onBackPressed(){ closeBluetooth(); showMenu(); }
  String themeName(){return selectedTheme.equals("forest")?"FLORESTA":"MÁRMORE";}
  void chooseTheme(){
    String[] names={"Mármore","Floresta"};
    int checked=selectedTheme.equals("forest")?1:0;
    new AlertDialog.Builder(this).setTitle("CENÁRIO").setSingleChoiceItems(names,checked,(d,i)->{
      selectedTheme=i==1?"forest":"classic";
      getSharedPreferences("pixelchess",MODE_PRIVATE).edit().putString("theme",selectedTheme).apply();
      d.dismiss();showMenu();
    }).setNegativeButton("CANCELAR",null).show();
  }

  boolean btPermission(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission("android.permission.BLUETOOTH_CONNECT")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT","android.permission.BLUETOOTH_SCAN"},42);return false;}return true; }
  void chooseTime(boolean bt){String[] x={"10 minutos","5 minutos","3 minutos"};new AlertDialog.Builder(this).setTitle("RELÓGIO").setItems(x,(d,i)->{selectedMinutes=i==0?10:i==1?5:3;if(bt)hostGame();else setContentView(new ChessView(this));}).setNegativeButton("CANCELAR",null).show();}
  void bluetoothMenu(){
    if(!btPermission())return; adapter=BluetoothAdapter.getDefaultAdapter(); if(adapter==null){toast("Este aparelho não possui Bluetooth");return;} if(!adapter.isEnabled()){toast("Ative o Bluetooth e tente novamente");return;}
    new AlertDialog.Builder(this).setTitle("Jogar via Bluetooth").setMessage("Quem criar a sala escolhe o tempo para os dois jogadores.").setPositiveButton("CRIAR PARTIDA",(d,w)->chooseTime(true)).setNegativeButton("ENTRAR",(d,w)->chooseDevice()).setNeutralButton("CANCELAR",null).show();
  }
  void hostGame(){toast("Aguardando o outro jogador…"); new Thread(()->{try{serverSocket=adapter.listenUsingRfcommWithServiceRecord("PixelChess",GAME_UUID);socket=serverSocket.accept();serverSocket.close();serverSocket=null;startBtGame(true);}catch(Exception e){if(serverSocket!=null)runOnUiThread(()->toast("Falha ao criar partida: "+e.getMessage()));}}).start();}
  void chooseDevice(){Set<BluetoothDevice> ds=adapter.getBondedDevices();if(ds.isEmpty()){toast("Nenhum aparelho pareado. Pareie os celulares primeiro.");return;} final ArrayList<BluetoothDevice> list=new ArrayList<>(ds);String[] names=new String[list.size()];for(int i=0;i<list.size();i++){String n=list.get(i).getName();names[i]=n==null?list.get(i).getAddress():n;}new AlertDialog.Builder(this).setTitle("Escolha o celular").setItems(names,(d,i)->connectGame(list.get(i))).show();}
  void connectGame(BluetoothDevice dev){toast("Conectando…");new Thread(()->{try{socket=dev.createRfcommSocketToServiceRecord(GAME_UUID);adapter.cancelDiscovery();socket.connect();startBtGame(false);}catch(Exception e){runOnUiThread(()->toast("Não conectou: "+e.getMessage()));}}).start();}
  void startBtGame(boolean host)throws Exception{bluetoothGame=true;myWhite=host;btOut=socket.getOutputStream();InputStream in=socket.getInputStream();BufferedReader br=new BufferedReader(new InputStreamReader(in));if(host){writeBt("TIME,"+selectedMinutes);}else{String setup=br.readLine();if(setup==null||!setup.startsWith("TIME,"))throw new IOException("Configuração da sala inválida");selectedMinutes=Integer.parseInt(setup.substring(5));}runOnUiThread(()->{game=new ChessView(this);game.status=host?"VOCÊ É BRANCAS":"VOCÊ É PRETAS";setContentView(game);});String line;while((line=br.readLine())!=null){final String msg=line;if(msg.startsWith("MOVE,")){String[] a=msg.split(",");if(a.length>=8){int r1=Integer.parseInt(a[1]),c1=Integer.parseInt(a[2]),r2=Integer.parseInt(a[3]),c2=Integer.parseInt(a[4]);String promo=a[5];long wm=Long.parseLong(a[6]),bm=Long.parseLong(a[7]);runOnUiThread(()->game.remoteMove(r1,c1,r2,c2,promo,wm,bm));}}else if(msg.startsWith("SYNC,")){String[] a=msg.split(",");if(a.length>=4){long wm=Long.parseLong(a[1]),bm=Long.parseLong(a[2]);boolean turn=a[3].equals("W");runOnUiThread(()->game.applyClockSync(wm,bm,turn));}}else if(msg.startsWith("FLAG,")){String[] a=msg.split(",");if(a.length>=2){boolean loserWhite=a[1].equals("W");runOnUiThread(()->game.applyRemoteFlag(loserWhite));}}}}
  void writeBt(String msg)throws IOException{synchronized(btWriteLock){if(btOut==null)throw new IOException("Sem conexão");btOut.write((msg+"\n").getBytes("UTF-8"));btOut.flush();}}
  void sendMove(int r1,int c1,int r2,int c2){sendMove(r1,c1,r2,c2,"-");}
  void sendMove(int r1,int c1,int r2,int c2,String promo){if(!bluetoothGame||btOut==null||game==null)return;final long wm=game.whiteMs,bm=game.blackMs;new Thread(()->{try{writeBt("MOVE,"+r1+","+c1+","+r2+","+c2+","+promo+","+wm+","+bm);}catch(Exception e){runOnUiThread(()->toast("Conexão Bluetooth perdida"));}}).start();}
  void sendClockSync(){if(!bluetoothGame||btOut==null||game==null)return;final long wm=game.whiteMs,bm=game.blackMs;final String turn=game.white?"W":"B";new Thread(()->{try{writeBt("SYNC,"+wm+","+bm+","+turn);}catch(Exception ignored){}}).start();}
  void sendFlag(boolean loserWhite){if(!bluetoothGame||btOut==null)return;new Thread(()->{try{writeBt("FLAG,"+(loserWhite?"W":"B"));}catch(Exception ignored){}}).start();}
  void closeBluetooth(){bluetoothGame=false;try{if(serverSocket!=null)serverSocket.close();}catch(Exception ignored){}try{if(socket!=null)socket.close();}catch(Exception ignored){}serverSocket=null;socket=null;btOut=null;}
  void toast(String x){Toast.makeText(this,x,Toast.LENGTH_LONG).show();}

  static class ThemeAssets {
    final int backgroundRes,groundRes,frameRes,boardRes,clockRes;
    ThemeAssets(int backgroundRes,int groundRes,int frameRes,int boardRes,int clockRes){
      this.backgroundRes=backgroundRes;this.groundRes=groundRes;this.frameRes=frameRes;this.boardRes=boardRes;this.clockRes=clockRes;
    }
  }
  ThemeAssets themeAssets(){
    if(selectedTheme.equals("forest"))return new ThemeAssets(0,0,0,R.drawable.stone_board_pixel,R.drawable.forest_clock_panel);
    return new ThemeAssets(0,0,0,R.drawable.stone_board_pixel,0);
  }

  class ChessView extends View {
    Paint p=new Paint(3); String[][] b=new String[8][8]; int sr=-1,sc=-1; boolean white=true; String status="BRANCAS JOGAM";
    HashMap<Character,Bitmap> pieceSprites=new HashMap<>(); ThemeAssets activeTheme; Bitmap themeBackgroundBitmap,themeGroundBitmap,themeFrameBitmap,boardBitmap,themeClockBitmap; Paint spritePaint=new Paint(),boardPaint=new Paint(),themePaint=new Paint();
    boolean wKm=false,bKm=false,wRa=false,wRh=false,bRa=false,bRh=false,gameOver=false,flagSent=false; int epR=-1,epC=-1,halfmove=0; boolean animating=false; int animR1,animC1,animR2,animC2; String animPiece; long animStart; final long ANIM_MS=200; HashMap<String,Integer> repetitions=new HashMap<>(); long whiteMs,blackMs,lastTick,lastSyncSent; ArrayList<String> history=new ArrayList<>(); Handler clock=new Handler(Looper.getMainLooper()); Runnable ticker;
    final String back="rnbqkbnr";
    ChessView(Context c){super(c); p.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD)); spritePaint.setAntiAlias(false); spritePaint.setFilterBitmap(false); spritePaint.setDither(false); boardPaint.setAntiAlias(false); boardPaint.setFilterBitmap(false); boardPaint.setDither(false); themePaint.setAntiAlias(false); themePaint.setFilterBitmap(false); themePaint.setDither(false); loadPieceSprites(); loadThemeBitmaps(); reset(); ticker=()->{if(!gameOver){long now=System.currentTimeMillis(),dt=now-lastTick;lastTick=now;if(white)whiteMs-=dt;else blackMs-=dt;if(whiteMs<=0||blackMs<=0){boolean loser=white;if(loser)whiteMs=0;else blackMs=0;gameOver=true;status="TEMPO • "+(loser?"PRETAS":"BRANCAS")+" VENCEM";if(bluetoothGame&&!flagSent){flagSent=true;sendFlag(loser);}}else if(bluetoothGame&&myWhite&&now-lastSyncSent>=1000){lastSyncSent=now;sendClockSync();}invalidate();clock.postDelayed(ticker,100);}};lastTick=System.currentTimeMillis();lastSyncSent=lastTick;clock.post(ticker);}
    void reset(){for(int r=0;r<8;r++)Arrays.fill(b[r],null);for(int i=0;i<8;i++){b[0][i]=""+back.charAt(i);b[1][i]="p";b[6][i]="P";b[7][i]=(""+back.charAt(i)).toUpperCase();}white=true;gameOver=false;flagSent=false;halfmove=0;repetitions.clear();history.clear();whiteMs=blackMs=selectedMinutes*60000L;lastTick=System.currentTimeMillis();lastSyncSent=lastTick;status="BRANCAS JOGAM";recordPosition();invalidate();}
    protected void onDraw(Canvas c){
      super.onDraw(c);float den0=getResources().getDisplayMetrics().density,gutter=(selectedTheme.equals("forest")?42f:18f)*den0,w=getWidth()-gutter*2,s=w/8f,left0=gutter;float top=Math.max(150*den0,(getHeight()-w)/2f-30*den0);drawThemeBackdrop(c,left0,top,w,den0);
      p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s*.62f);
      boolean flip=bluetoothGame&&!myWhite;
      if(selectedTheme.equals("forest")){drawThemeGround(c,left0,top,w,den0);drawThemeFrame(c,left0,top,w,den0);}
      if(boardBitmap!=null)c.drawBitmap(boardBitmap,null,new RectF(left0,top,left0+w,top+w),boardPaint);
      else{p.setColor(Color.rgb(48,67,59));c.drawRect(left0,top,left0+w,top+w,p);}
      // Subtle glaze on dark marble squares: calms the bright veins without flattening the stone volume.
      p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(24,18,38,31));
      for(int vr=0;vr<8;vr++)for(int vx=0;vx<8;vx++){int rr=flip?7-vr:vr,xx=flip?7-vx:vx;if(((rr+xx)&1)==1)c.drawRect(left0+vx*s,top+vr*s,left0+(vx+1)*s,top+(vr+1)*s,p);}
      for(int vr=0;vr<8;vr++)for(int vx=0;vx<8;vx++){int r=flip?7-vr:vr,x=flip?7-vx:vx;
        String squarePiece=b[r][x];
        if(squarePiece!=null&&Character.toLowerCase(squarePiece.charAt(0))=='k'&&inCheck(isWhite(squarePiece))){
          p.setColor(gameOver?Color.rgb(198,40,40):Color.rgb(245,124,0));
          c.drawRect(left0+vx*s,top+vr*s,left0+(vx+1)*s,top+(vr+1)*s,p);
        }
        if(sr>=0&&!(r==sr&&x==sc)&&legal(sr,sc,r,x,true)){
          if(b[r][x]!=null)drawGoldSquare(c,left0+vx*s,top+vr*s,s,false);
          else drawGoldMoveMarker(c,left0+vx*s,top+vr*s,s);
        }
        String q=b[r][x];if(q!=null&&!(animating&&r==animR2&&x==animC2))drawPieceSprite(c,q,left0+vx*s,top+vr*s,s);
      }
      if(animating){float t=Math.min(1f,(System.currentTimeMillis()-animStart)/(float)ANIM_MS);float u=1f-(1f-t)*(1f-t);int fr=flip?7-animR1:animR1,fc=flip?7-animC1:animC1,tr=flip?7-animR2:animR2,tc=flip?7-animC2:animC2;float ax=left0+(fc+(tc-fc)*u)*s,ay=top+(fr+(tr-fr)*u)*s;drawPieceSprite(c,animPiece,ax,ay,s);if(t<1f)postInvalidateOnAnimation();else animating=false;}
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,getResources().getDisplayMetrics().density));p.setColor(Color.argb(95,20,24,28));for(int i=0;i<=8;i++){c.drawLine(left0+i*s,top,left0+i*s,top+w,p);c.drawLine(left0,top+i*s,left0+w,top+i*s,p);}p.setStyle(Paint.Style.FILL);
      float den=getResources().getDisplayMetrics().density;
      boolean bottomWhite=!bluetoothGame||myWhite;String topName=bottomWhite?"PRETAS":"BRANCAS";String bottomName=bottomWhite?"BRANCAS":"PRETAS";long topMs=bottomWhite?blackMs:whiteMs,bottomMs=bottomWhite?whiteMs:blackMs;
      boolean topActive=bottomWhite?!white:white,bottomActive=!topActive;
      drawClockPanel(c,getWidth()/2f,top-100*den,topName,clockText(topMs),topActive,den);
      p.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);p.setColor(gameOver?Color.rgb(211,87,76):Color.rgb(235,205,132));c.drawText(status,getWidth()/2f,top-38*den,p);
      if(selectedTheme.equals("forest"))drawStoneCoordinates(c,left0,top,w,s,flip,den);
      else{
        p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.argb(210,235,221,184));
        for(int i=0;i<8;i++){int file=flip?7-i:i;int rank=flip?i:7-i;c.drawText(""+(char)('A'+file),left0+i*s+s/2,top+w+14*den,p);p.setTextAlign(Paint.Align.CENTER);c.drawText(""+(rank+1),left0/2f,top+i*s+s*.58f,p);p.setTextAlign(Paint.Align.CENTER);}
      }
      drawClockPanel(c,getWidth()/2f,top+w+64*den,bottomName,clockText(bottomMs),bottomActive,den);
      p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.rgb(118,121,119));String h=history.isEmpty()?"JOGADAS  ·  nenhuma":historyLine().replace("JOGADAS:","JOGADAS  ·");c.drawText(h,getWidth()/2f,Math.min(getHeight()-48*den,top+w+112*den),p);
      p.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.rgb(55,59,61));c.drawText("◆  PIXEL CHESS  ◆",getWidth()/2f,Math.min(getHeight()-20*den,top+w+140*den),p);
    }
    void drawThemeBackdrop(Canvas c,float left,float top,float board,float den){
      if(!selectedTheme.equals("forest")){
        if(themeBackgroundBitmap==null)c.drawColor(bg);else drawCenterCrop(c,themeBackgroundBitmap,new RectF(0,0,getWidth(),getHeight()),themePaint);
        return;
      }
      if(themeBackgroundBitmap!=null){drawCenterCrop(c,themeBackgroundBitmap,new RectF(0,0,getWidth(),getHeight()),themePaint);return;}
      drawPixelForest(c,left,top,board,den);
    }
    void drawPixelForest(Canvas c,float left,float top,float board,float den){
      c.drawColor(Color.rgb(13,39,27));
      int px=Math.max(3,Math.round(3*den));
      p.setStyle(Paint.Style.FILL);
      // pixel-art ground path / clearing
      p.setColor(Color.rgb(65,77,43));
      c.drawRect(0,0,getWidth(),getHeight(),p);
      p.setColor(Color.rgb(91,82,48));
      float pathW=Math.max(board*.34f,90*den),cx=getWidth()/2f;
      c.drawRect(cx-pathW/2f,0,cx+pathW/2f,getHeight(),p);
      // deterministic pine silhouettes along both sides; blocky on purpose
      int rows=Math.max(8,(int)(getHeight()/(82*den)));
      for(int side=0;side<2;side++)for(int i=0;i<rows;i++){
        float y=i*82*den-18*den;
        float x=side==0?(18+(i%3)*17)*den:getWidth()-(18+(i%3)*17)*den;
        drawPixelPine(c,x,y,(i%2==0?1f:.84f)*den,px);
      }
      // small rocks / flowers in the clearing
      for(int i=0;i<24;i++){
        float x=((i*83)%Math.max(1,getWidth()-20*px))+10*px;
        float y=((i*137)%Math.max(1,getHeight()-20*px))+10*px;
        if(x>left-26*den&&x<left+board+26*den&&y>top-26*den&&y<top+board+26*den)continue;
        p.setColor(i%4==0?Color.rgb(151,154,126):Color.rgb(45,67,43));
        c.drawRect(x,y,x+2*px,y+2*px,p);
      }
    }
    void drawPixelPine(Canvas c,float cx,float cy,float scale,int px){
      p.setStyle(Paint.Style.FILL);
      int u=Math.max(px,Math.round(4*scale));
      p.setColor(Color.rgb(71,53,35));c.drawRect(cx-u,cy+13*u,cx+u,cy+18*u,p);
      p.setColor(Color.rgb(10,44,31));
      c.drawRect(cx-4*u,cy+7*u,cx+4*u,cy+15*u,p);
      c.drawRect(cx-5*u,cy+9*u,cx+5*u,cy+13*u,p);
      p.setColor(Color.rgb(19,68,43));
      c.drawRect(cx-3*u,cy+3*u,cx+3*u,cy+10*u,p);
      c.drawRect(cx-4*u,cy+6*u,cx+4*u,cy+9*u,p);
      p.setColor(Color.rgb(38,91,53));c.drawRect(cx-2*u,cy,cx+2*u,cy+6*u,p);
    }
    void drawCenterCrop(Canvas c,Bitmap bmp,RectF dst,Paint paint){
      float srcRatio=bmp.getWidth()/(float)bmp.getHeight(),dstRatio=dst.width()/dst.height();
      Rect src;
      if(srcRatio>dstRatio){
        int sw=Math.round(bmp.getHeight()*dstRatio),x=(bmp.getWidth()-sw)/2;src=new Rect(x,0,x+sw,bmp.getHeight());
      }else{
        int sh=Math.round(bmp.getWidth()/dstRatio),y=(bmp.getHeight()-sh)/2;src=new Rect(0,y,bmp.getWidth(),y+sh);
      }
      c.drawBitmap(bmp,src,dst,paint);
    }
    void drawThemeGround(Canvas c,float left,float top,float board,float den){
      float framePad=board*.045f,groundPad=board*.105f;
      RectF ground=new RectF(left-groundPad,top-groundPad,left+board+groundPad,top+board+groundPad);
      if(themeGroundBitmap!=null){themePaint.setAlpha(255);c.drawBitmap(themeGroundBitmap,null,ground,themePaint);return;}
      // Real ring: draw only the strips OUTSIDE the stone frame, never a solid rectangle.
      p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(190,67,79,43));
      float il=left-framePad,it=top-framePad,ir=left+board+framePad,ib=top+board+framePad;
      c.drawRect(ground.left,ground.top,ground.right,it,p);
      c.drawRect(ground.left,ib,ground.right,ground.bottom,p);
      c.drawRect(ground.left,it,il,ib,p);
      c.drawRect(ir,it,ground.right,ib,p);
      p.setColor(Color.rgb(42,66,39));
      int dot=Math.max(2,Math.round(2*den));
      for(int i=0;i<30;i++){
        float gx=ground.left+((i*53)%Math.max(1,(int)ground.width()));
        float gy=ground.top+((i*79)%Math.max(1,(int)ground.height()));
        if(gx>il&&gx<ir&&gy>it&&gy<ib)continue;
        c.drawRect(gx,gy,gx+dot,gy+dot,p);
      }
    }
    void drawThemeFrame(Canvas c,float left,float top,float board,float den){
      float pad=board*.045f,outer=board+pad*2f;
      RectF dst=new RectF(left-pad,top-pad,left+board+pad,top+board+pad);
      if(themeFrameBitmap!=null){themePaint.setAlpha(255);c.drawBitmap(themeFrameBitmap,null,dst,themePaint);return;}
      p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(43,48,43));c.drawRoundRect(dst,5*den,5*den,p);
      p.setColor(Color.rgb(65,69,61));float block=board/8f;
      for(int i=0;i<8;i++){
        float x=left+i*block,y=top+i*block;
        c.drawRect(x,top-pad,x+block-1*den,top-2*den,p);c.drawRect(x,top+board+2*den,x+block-1*den,top+board+pad,p);
        c.drawRect(left-pad,y,left-2*den,y+block-1*den,p);c.drawRect(left+board+2*den,y,left+board+pad,y+block-1*den,p);
      }
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,den));p.setColor(Color.rgb(25,30,27));c.drawRoundRect(dst,5*den,5*den,p);
      p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(48,73,43));
      for(int i=0;i<12;i++){float x=left-pad+(i*37%(int)Math.max(1,outer-10*den))+5*den;float y=(i%2==0)?top-pad+3*den:top+board+pad-5*den;c.drawCircle(x,y,2.1f*den,p);}
    }
    void drawStoneCoordinates(Canvas c,float left,float top,float board,float square,boolean flip,float den){
      float outer=board*(512f/428f),pad=(outer-board)/2f;
      p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);
      p.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
      p.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);
      p.setColor(Color.rgb(218,207,177));
      for(int i=0;i<8;i++){
        int file=flip?7-i:i,rank=flip?i:7-i;
        c.drawText(""+(char)('A'+file),left+i*square+square/2f,top+board+pad*.63f,p);
        c.drawText(""+(rank+1),left-pad*.56f,top+i*square+square*.62f,p);
      }
    }
    void drawClockPanel(Canvas c,float cx,float cy,String name,String time,boolean active,float den){
      if(selectedTheme.equals("forest")&&themeClockBitmap!=null){
        float pw=176*den,ph=52*den,l=cx-pw/2f,t=cy-ph/2f;
        themePaint.setAlpha(active?255:205);c.drawBitmap(themeClockBitmap,null,new RectF(l,t,l+pw,t+ph),themePaint);themePaint.setAlpha(255);
        p.setTextAlign(Paint.Align.CENTER);p.setStyle(Paint.Style.FILL);
        p.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);p.setColor(active?Color.rgb(245,222,162):Color.rgb(190,181,151));c.drawText(name,cx,t+17*den,p);
        p.setTextSize(22*getResources().getDisplayMetrics().scaledDensity);p.setColor(active?Color.rgb(255,239,194):Color.rgb(218,207,177));c.drawText(time,cx,t+43*den,p);
        if(active){p.setColor(Color.rgb(223,177,76));c.drawCircle(cx,t+48*den,1.5f*den,p);}
        return;
      }
      float pw=156*den,ph=58*den,l=cx-pw/2f,t=cy-ph/2f;
      p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(25,30,33));c.drawRect(l,t,l+pw,t+ph,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,den));p.setColor(active?Color.rgb(184,148,70):Color.rgb(55,62,63));c.drawRect(l+.5f*den,t+.5f*den,l+pw-.5f*den,t+ph-.5f*den,p);
      p.setStyle(Paint.Style.FILL);p.setColor(active?Color.rgb(210,171,82):Color.rgb(73,79,79));c.drawRect(l,t,l+3*den,t+ph,p);
      p.setTextAlign(Paint.Align.CENTER);p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(active?Color.rgb(226,211,173):Color.rgb(154,158,156));c.drawText(name,cx,t+18*den,p);
      p.setTextSize(24*getResources().getDisplayMetrics().scaledDensity);p.setColor(active?cream:Color.rgb(196,194,184));c.drawText(time,cx,t+47*den,p);
    }
    Bitmap decodeDrawable(int resId){
      if(resId==0)return null;BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;return BitmapFactory.decodeResource(getResources(),resId,o);
    }
    void loadThemeBitmaps(){
      activeTheme=themeAssets();
      themeBackgroundBitmap=decodeDrawable(activeTheme.backgroundRes);
      themeGroundBitmap=decodeDrawable(activeTheme.groundRes);
      themeFrameBitmap=decodeDrawable(activeTheme.frameRes);
      boardBitmap=decodeDrawable(activeTheme.boardRes);
      themeClockBitmap=decodeDrawable(activeTheme.clockRes);
    }
    void drawGoldSquare(Canvas c,float left,float top,float size,boolean selected){
      p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(112,222,158,35));c.drawRect(left,top,left+size,top+size,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,size*.035f));p.setColor(Color.rgb(238,184,58));
      float in=Math.max(2f,size*.035f);c.drawRect(left+in,top+in,left+size-in,top+size-in,p);p.setStyle(Paint.Style.FILL);
    }
    void drawGoldMoveMarker(Canvas c,float left,float top,float size){
      float cx=left+size/2f,cy=top+size/2f,d=size*.105f;
      Path diamond=new Path();diamond.moveTo(cx,cy-d);diamond.lineTo(cx+d,cy);diamond.lineTo(cx,cy+d);diamond.lineTo(cx-d,cy);diamond.close();
      p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(218,174,75));c.drawPath(diamond,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,size*.018f));p.setColor(Color.rgb(247,214,132));c.drawPath(diamond,p);p.setStyle(Paint.Style.FILL);
    }
    void loadPieceSprites(){
      pieceSprites.put('P',cleanDisconnectedSprite(R.drawable.w_pawn));
      pieceSprites.put('R',cleanDisconnectedSprite(R.drawable.w_rook));
      pieceSprites.put('N',cleanDisconnectedSprite(R.drawable.w_knight));
      pieceSprites.put('B',cleanDisconnectedSprite(R.drawable.w_bishop));
      pieceSprites.put('Q',cleanDisconnectedSprite(R.drawable.w_queen));
      pieceSprites.put('K',cleanDisconnectedSprite(R.drawable.w_king));
      pieceSprites.put('p',cleanDisconnectedSprite(R.drawable.b_pawn));
      pieceSprites.put('r',cleanDisconnectedSprite(R.drawable.b_rook));
      pieceSprites.put('n',cleanDisconnectedSprite(R.drawable.b_knight));
      pieceSprites.put('b',cleanDisconnectedSprite(R.drawable.b_bishop));
      pieceSprites.put('q',cleanDisconnectedSprite(R.drawable.b_queen));
      pieceSprites.put('k',cleanDisconnectedSprite(R.drawable.b_king));
    }
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
    String historyLine(){int from=Math.max(0,history.size()-4);StringBuilder z=new StringBuilder("JOGADAS: ");for(int i=from;i<history.size();i++){if(i>from)z.append("  ");z.append(history.get(i));}return z.toString();}
    String[] pixelPattern(char t){switch(Character.toLowerCase(t)){case 'p':return new String[]{"...##...","..####..","..####..","...##...","..####..",".######.","########"};case 'r':return new String[]{"##.##.##","########",".######.","..####..","..####..",".######.","########"};case 'n':return new String[]{"...###..","..#####.",".###.##.",".######.","..#####.","..####..",".######.","########"};case 'b':return new String[]{"...##...","..####..","...##...","..####..",".######.","..####..",".######.","########"};case 'q':return new String[]{"#..##..#",".######.","..####..",".######.","..####..",".######.","########","########"};default:return new String[]{"...##...",".#.##.#.",".######.","..####..",".######.","..####..",".######.","########"};}}
    void drawPixelPiece(Canvas c,String q,float left,float top,float size){String[] pat=pixelPattern(q.charAt(0));float cell=size/10f,ox=left+cell,oy=top+(size-pat.length*cell)/2f;boolean whitePiece=Character.isUpperCase(q.charAt(0));p.setStyle(Paint.Style.FILL);if(whitePiece){p.setColor(Color.rgb(35,38,40));for(int r=0;r<pat.length;r++)for(int x=0;x<pat[r].length();x++)if(pat[r].charAt(x)=='#')for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)if(Math.abs(dx)+Math.abs(dy)==1)c.drawRect(ox+(x+dx)*cell,oy+(r+dy)*cell,ox+(x+dx+1)*cell,oy+(r+dy+1)*cell,p);}p.setColor(whitePiece?Color.rgb(248,245,232):Color.rgb(25,28,31));for(int r=0;r<pat.length;r++)for(int x=0;x<pat[r].length();x++)if(pat[r].charAt(x)=='#')c.drawRect(ox+x*cell,oy+r*cell,ox+(x+1)*cell,oy+(r+1)*cell,p);}
    void startMoveAnimation(String q,int r1,int c1,int r2,int c2){animPiece=q;animR1=r1;animC1=c1;animR2=r2;animC2=c2;animStart=System.currentTimeMillis();animating=true;postInvalidateOnAnimation();}
    public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float den=getResources().getDisplayMetrics().density,gutter=(selectedTheme.equals("forest")?42f:18f)*den,w=getWidth()-gutter*2,s=w/8f,top=Math.max(150*den,(getHeight()-w)/2f-30*den);int vx=(int)((e.getX()-gutter)/s),vr=(int)((e.getY()-top)/s);if(vr<0||vr>7||vx<0||vx>7)return true;boolean flip=bluetoothGame&&!myWhite;int x=flip?7-vx:vx,r=flip?7-vr:vr;if(gameOver){toast("A partida terminou");return true;}
      if(bluetoothGame && white!=myWhite){toast("Aguarde a jogada do adversário");return true;} if(sr<0){select(r,x);}else if(sr==r&&sc==x){sr=sc=-1;invalidate();}else if(b[r][x]!=null&&isWhite(b[r][x])==white){select(r,x);}else if(legal(sr,sc,r,x,false)){int a=sr,d=sc;String moving=b[a][d];boolean promotes=moving!=null&&Character.toLowerCase(moving.charAt(0))=='p'&&(r==0||r==7);if(promotes)moveWithPromotionChoice(a,d,r,x);else{move(a,d,r,x,"-");sendMove(a,d,r,x,"-");}sr=sc=-1;invalidate();}return true;}
    void remoteMove(int r1,int c1,int r2,int c2,String promo,long wm,long bm){if(bluetoothGame&&white!=myWhite&&legal(r1,c1,r2,c2,false)){move(r1,c1,r2,c2,promo);whiteMs=Math.max(0,wm);blackMs=Math.max(0,bm);lastTick=System.currentTimeMillis();sr=sc=-1;if(!gameOver&&white==myWhite)vibrateTurn();invalidate();}}
    void applyClockSync(long wm,long bm,boolean turn){if(!bluetoothGame||myWhite)return;whiteMs=Math.max(0,wm);blackMs=Math.max(0,bm);white=turn;lastTick=System.currentTimeMillis();invalidate();}
    void applyRemoteFlag(boolean loserWhite){if(gameOver)return;if(loserWhite)whiteMs=0;else blackMs=0;gameOver=true;status="TEMPO • "+(loserWhite?"PRETAS":"BRANCAS")+" VENCEM";invalidate();}
    void vibrateTurn(){try{Vibrator v=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);if(v==null||!v.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(70,VibrationEffect.DEFAULT_AMPLITUDE));else v.vibrate(70);}catch(Exception ignored){}}
    void moveWithPromotionChoice(int r1,int c1,int r2,int c2){
      final boolean side=isWhite(b[r1][c1]);final String[] labels={"DAMA","TORRE","BISPO","CAVALO"},pcs={"Q","R","B","N"};
      LinearLayout box=new LinearLayout(MainActivity.this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(32,16,32,16);
      AlertDialog dialog=new AlertDialog.Builder(MainActivity.this).setTitle("PROMOÇÃO").setMessage("Escolha a peça:").setView(box).setCancelable(false).create();
      for(int i=0;i<4;i++){final int k=i;Button bt=button(labels[i]);bt.setTextColor(Color.BLACK);bt.setBackgroundColor(Color.rgb(238,238,238));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,6,0,6);box.addView(bt,lp);bt.setOnClickListener(v->{String z=pcs[k];if(!side)z=z.toLowerCase();move(r1,c1,r2,c2,z);sendMove(r1,c1,r2,c2,z);dialog.dismiss();invalidate();});}
      dialog.show();
    }
    void select(int r,int c){if(b[r][c]!=null&&isWhite(b[r][c])==white){sr=r;sc=c;invalidate();}}
    boolean isWhite(String q){return Character.isUpperCase(q.charAt(0));}
    boolean inside(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
    boolean clear(int r1,int c1,int r2,int c2){int rr=Integer.signum(r2-r1),cc=Integer.signum(c2-c1),r=r1+rr,c=c1+cc;while(r!=r2||c!=c2){if(b[r][c]!=null)return false;r+=rr;c+=cc;}return true;}
    boolean pseudo(int r1,int c1,int r2,int c2){
      if(!inside(r2,c2)||(r1==r2&&c1==c2))return false;String q=b[r1][c1];if(q==null)return false;
      if(b[r2][c2]!=null&&isWhite(q)==isWhite(b[r2][c2]))return false;if(b[r2][c2]!=null&&Character.toLowerCase(b[r2][c2].charAt(0))=='k')return false;int dr=r2-r1,dc=c2-c1,ar=Math.abs(dr),ac=Math.abs(dc);char t=Character.toLowerCase(q.charAt(0));
      if(t=='n')return ar*ac==2;
      if(t=='k'){if(ar<=1&&ac<=1)return true; if(dr==0&&ac==2)return castlePossible(isWhite(q),dc>0); return false;}
      if(t=='p'){int d=isWhite(q)?-1:1,start=isWhite(q)?6:1;if(dc==0&&b[r2][c2]==null&&(dr==d||(r1==start&&dr==2*d&&b[r1+d][c1]==null)))return true;if(ac==1&&dr==d&&(b[r2][c2]!=null||(r2==epR&&c2==epC)))return true;return false;}
      if(t=='r')return (dr==0||dc==0)&&clear(r1,c1,r2,c2);
      if(t=='b')return ar==ac&&clear(r1,c1,r2,c2);
      if(t=='q')return (dr==0||dc==0||ar==ac)&&clear(r1,c1,r2,c2);
      return false;
    }
    boolean legal(int r1,int c1,int r2,int c2,boolean preview){
      String q=b[r1][c1];if(q==null||!pseudo(r1,c1,r2,c2))return false;boolean side=isWhite(q);
      String a=b[r2][c2],from=b[r1][c1];int oldEpR=epR,oldEpC=epC;String epPawn=null;int epPawnR=-1;
      if(Character.toLowerCase(from.charAt(0))=='p'&&c1!=c2&&a==null){epPawnR=r1;epPawn=b[epPawnR][c2];b[epPawnR][c2]=null;}
      b[r2][c2]=from;b[r1][c1]=null;boolean bad=inCheck(side);b[r1][c1]=from;b[r2][c2]=a;if(epPawnR>=0)b[epPawnR][c2]=epPawn;epR=oldEpR;epC=oldEpC;return !bad;
    }
    boolean attacked(int r,int c,boolean byWhite){
      for(int a=0;a<8;a++)for(int d=0;d<8;d++){String q=b[a][d];if(q==null||isWhite(q)!=byWhite)continue;char t=Character.toLowerCase(q.charAt(0));int dr=r-a,dc=c-d,ar=Math.abs(dr),ac=Math.abs(dc);
        if(t=='p'&&dr==(byWhite?-1:1)&&ac==1)return true;if(t=='n'&&ar*ac==2)return true;if(t=='k'&&ar<=1&&ac<=1)return true;
        if(t=='r'&&(dr==0||dc==0)&&clear(a,d,r,c))return true;if(t=='b'&&ar==ac&&clear(a,d,r,c))return true;if(t=='q'&&(dr==0||dc==0||ar==ac)&&clear(a,d,r,c))return true;}
      return false;
    }
    boolean inCheck(boolean side){for(int r=0;r<8;r++)for(int c=0;c<8;c++){String q=b[r][c];if(q!=null&&Character.toLowerCase(q.charAt(0))=='k'&&isWhite(q)==side)return attacked(r,c,!side);}return false;}
    String positionKey(){StringBuilder k=new StringBuilder();for(int r=0;r<8;r++)for(int c=0;c<8;c++)k.append(b[r][c]==null?".":b[r][c]);k.append(white?"w":"b").append(wKm?"1":"0").append(bKm?"1":"0").append(wRa?"1":"0").append(wRh?"1":"0").append(bRa?"1":"0").append(bRh?"1":"0").append(epR).append(":").append(epC);return k.toString();}
    int recordPosition(){String k=positionKey();int n=repetitions.containsKey(k)?repetitions.get(k)+1:1;repetitions.put(k,n);return n;}
    boolean insufficientMaterial(){ArrayList<Character> pcs=new ArrayList<>();ArrayList<Integer> bishops=new ArrayList<>();for(int r=0;r<8;r++)for(int c=0;c<8;c++){String q=b[r][c];if(q==null)continue;char t=Character.toLowerCase(q.charAt(0));if(t=='k')continue;if(t=='p'||t=='q'||t=='r')return false;pcs.add(t);if(t=='b')bishops.add((r+c)&1);}if(pcs.size()==0)return true;if(pcs.size()==1&&(pcs.get(0)=='b'||pcs.get(0)=='n'))return true;if(pcs.size()>0&&pcs.size()==bishops.size()){int color=bishops.get(0);for(int x:bishops)if(x!=color)return false;return true;}return false;}
    boolean castlePossible(boolean side,boolean kingSide){int r=side?7:0;if(side?(wKm||(kingSide?wRh:wRa)):(bKm||(kingSide?bRh:bRa)))return false;int rook=kingSide?7:0;String rq=b[r][rook];if(rq==null||Character.toLowerCase(rq.charAt(0))!='r'||isWhite(rq)!=side)return false;int step=kingSide?1:-1;for(int c=4+step;c!=rook;c+=step)if(b[r][c]!=null)return false;if(inCheck(side)||attacked(r,4+step,!side)||attacked(r,4+2*step,!side))return false;return true;}
    void move(int r1,int c1,int r2,int c2,String promotion){
      String q=b[r1][c1];startMoveAnimation(q,r1,c1,r2,c2);String notation=square(r1,c1)+"-"+square(r2,c2);boolean side=isWhite(q);char t=Character.toLowerCase(q.charAt(0));String captured=b[r2][c2];
      if(t=='p'&&c1!=c2&&captured==null&&r2==epR&&c2==epC)b[r1][c2]=null;
      if(t=='k'&&Math.abs(c2-c1)==2){int rc=c2>c1?7:0,nc=c2>c1?5:3;b[r2][nc]=b[r2][rc];b[r2][rc]=null;}
      b[r2][c2]=q;b[r1][c1]=null;
      if(t=='p'&&(r2==0||r2==7)){String z=(promotion==null||promotion.equals("-"))?(side?"Q":"q"):promotion;b[r2][c2]=z;notation+="="+Character.toUpperCase(z.charAt(0));}
      epR=epC=-1;if(t=='p'&&Math.abs(r2-r1)==2){epR=(r1+r2)/2;epC=c1;}
      if(t=='k'){if(side)wKm=true;else bKm=true;} if(t=='r'){if(side&&r1==7&&c1==0)wRa=true;if(side&&r1==7&&c1==7)wRh=true;if(!side&&r1==0&&c1==0)bRa=true;if(!side&&r1==0&&c1==7)bRh=true;} if(captured!=null&&Character.toLowerCase(captured.charAt(0))=='r'){if(r2==7&&c2==0)wRa=true;if(r2==7&&c2==7)wRh=true;if(r2==0&&c2==0)bRa=true;if(r2==0&&c2==7)bRh=true;}
      history.add((history.size()/2+1)+(side?".":"...")+notation);if(t=='p'||captured!=null)halfmove=0;else halfmove++;white=!white;lastTick=System.currentTimeMillis();int repeated=recordPosition();boolean check=inCheck(white),any=false;
      outer:for(int a=0;a<8;a++)for(int d=0;d<8;d++){String z=b[a][d];if(z==null||isWhite(z)!=white)continue;for(int e=0;e<8;e++)for(int f=0;f<8;f++)if(legal(a,d,e,f,false)){any=true;break outer;}}
      if(!any){gameOver=true;status=check?"XEQUE-MATE • "+(white?"PRETAS":"BRANCAS")+" VENCEM":"EMPATE • AFOGAMENTO";}else if(insufficientMaterial()){gameOver=true;status="EMPATE • MATERIAL INSUFICIENTE";}else if(halfmove>=100){gameOver=true;status="EMPATE • REGRA DOS 50 LANCES";}else if(repeated>=3){gameOver=true;status="EMPATE • REPETIÇÃO TRIPLA";}else status=(white?"BRANCAS":"PRETAS")+" JOGAM"+(check?" • XEQUE!":"");
    }
  }
}
