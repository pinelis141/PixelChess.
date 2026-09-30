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
  BluetoothAdapter adapter; BluetoothSocket socket; BluetoothServerSocket serverSocket; ChessView game; boolean bluetoothGame=false, myWhite=true; OutputStream btOut; int selectedMinutes=10; BoardTheme selectedTheme=BoardThemes.CLASSIC; ThemePreferences themePreferences; final Object btWriteLock=new Object();
  @Override public void onCreate(Bundle b){super.onCreate(b); themePreferences=new ThemePreferences(getPreferences(MODE_PRIVATE)); selectedTheme=themePreferences.load(); showMenu();}

  TextView title(String s,int sp){ TextView v=new TextView(this); v.setText(s); v.setTextColor(cream); v.setTextSize(sp); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return v; }
  Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return b; }

  void showMenu(){
    LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(40,40,40,40); box.setBackgroundColor(bg);
    TextView logo=title("♜  PIXEL CHESS  ♞",30); box.addView(logo,new LinearLayout.LayoutParams(-1,-2));
    TextView sub=title("\nXADREZ LOCAL\n",14); sub.setTextColor(Color.LTGRAY); box.addView(sub);
    Button local=button("▶ Jogar no mesmo celular"); local.setOnClickListener(v->chooseTime(false)); box.addView(local,new LinearLayout.LayoutParams(-1,-2));
    Button bt=button("⌁ Jogar via Bluetooth"); bt.setOnClickListener(v->bluetoothMenu()); box.addView(bt,new LinearLayout.LayoutParams(-1,-2));
    Button skin=button("▣ Skin: "+selectedTheme.name); skin.setOnClickListener(v->chooseSkin()); box.addView(skin,new LinearLayout.LayoutParams(-1,-2));
    TextView ver=title("\nMVP "+BuildConfig.VERSION_NAME+" • Temas",12); ver.setTextColor(Color.GRAY); box.addView(ver);
    setContentView(box);
  }
  void chooseSkin(){
    String[] names=new String[BoardThemes.ALL.size()]; int checked=0;
    for(int i=0;i<names.length;i++){BoardTheme theme=BoardThemes.ALL.get(i);names[i]=theme.name;if(theme.id.equals(selectedTheme.id))checked=i;}
    new AlertDialog.Builder(this).setTitle("SKIN DO TABULEIRO").setSingleChoiceItems(names,checked,(d,i)->{
      selectedTheme=BoardThemes.ALL.get(i);
      themePreferences.save(selectedTheme);
      d.dismiss();showMenu();
    }).setNegativeButton("CANCELAR",null).show();
  }
  @Override public void onBackPressed(){ closeBluetooth(); showMenu(); }

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

  class ChessView extends View {
    Paint p=new Paint(3); String[][] b=new String[8][8]; int sr=-1,sc=-1; boolean white=true; String status="BRANCAS JOGAM";
    HashMap<Character,Bitmap> pieceSprites=new HashMap<>(); final BoardThemeRenderer themeRenderer; final SceneGeometry boardGeometry=new SceneGeometry(); Paint spritePaint=new Paint();
    boolean wKm=false,bKm=false,wRa=false,wRh=false,bRa=false,bRh=false,gameOver=false,flagSent=false; int epR=-1,epC=-1,halfmove=0; boolean animating=false; int animR1,animC1,animR2,animC2; String animPiece; long animStart; final long ANIM_MS=200; HashMap<String,Integer> repetitions=new HashMap<>(); long whiteMs,blackMs,lastTick,lastSyncSent; ArrayList<String> history=new ArrayList<>(); Handler clock=new Handler(Looper.getMainLooper()); Runnable ticker;
    final String back="rnbqkbnr";
    ChessView(Context c){super(c); p.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD)); spritePaint.setAntiAlias(false); spritePaint.setFilterBitmap(false); spritePaint.setDither(false); themeRenderer=new BoardThemeRenderer(getResources(),selectedTheme); loadPieceSprites(); reset(); ticker=()->{if(!gameOver){long now=System.currentTimeMillis(),dt=now-lastTick;lastTick=now;if(white)whiteMs-=dt;else blackMs-=dt;if(whiteMs<=0||blackMs<=0){boolean loser=white;if(loser)whiteMs=0;else blackMs=0;gameOver=true;status="TEMPO • "+(loser?"PRETAS":"BRANCAS")+" VENCEM";if(bluetoothGame&&!flagSent){flagSent=true;sendFlag(loser);}}else if(bluetoothGame&&myWhite&&now-lastSyncSent>=1000){lastSyncSent=now;sendClockSync();}invalidate();clock.postDelayed(ticker,100);}};lastTick=System.currentTimeMillis();lastSyncSent=lastTick;clock.post(ticker);}
    @Override protected void onDetachedFromWindow(){clock.removeCallbacks(ticker);super.onDetachedFromWindow();}
    void reset(){for(int r=0;r<8;r++)Arrays.fill(b[r],null);for(int i=0;i<8;i++){b[0][i]=""+back.charAt(i);b[1][i]="p";b[6][i]="P";b[7][i]=(""+back.charAt(i)).toUpperCase();}white=true;gameOver=false;flagSent=false;halfmove=0;repetitions.clear();history.clear();whiteMs=blackMs=selectedMinutes*60000L;lastTick=System.currentTimeMillis();lastSyncSent=lastTick;status="BRANCAS JOGAM";recordPosition();invalidate();}
    protected void onDraw(Canvas c){
      super.onDraw(c);float den0=getResources().getDisplayMetrics().density;boardGeometry.update(getWidth(),getHeight(),den0,themeRenderer.hasScene());themeRenderer.drawBackground(c,getWidth(),getHeight(),bg,den0);float w=boardGeometry.size,s=w/8f,left0=boardGeometry.left,top=boardGeometry.top;
      p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s*.62f);
      boolean flip=bluetoothGame&&!myWhite;
      themeRenderer.draw(c,left0,top,w,den0);
      if(themeRenderer.animated() && isShown())postInvalidateDelayed(50);
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
      if(themeRenderer.hasBackground())p.setShadowLayer(3*den,0,den,Color.BLACK);
      boolean bottomWhite=!bluetoothGame||myWhite;String topName=bottomWhite?"PRETAS":"BRANCAS";String bottomName=bottomWhite?"BRANCAS":"PRETAS";long topMs=bottomWhite?blackMs:whiteMs,bottomMs=bottomWhite?whiteMs:blackMs;
      boolean topActive=bottomWhite?!white:white,bottomActive=!topActive;
      float clockScale=themeRenderer.topClockScale(den);
      themeRenderer.drawClock(c,themeRenderer.topClockX(getWidth()),themeRenderer.topClockY(top-96*den),topName,clockText(topMs),topActive,den*clockScale,getResources().getDisplayMetrics().scaledDensity*clockScale,getWidth());
      p.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);p.setColor(gameOver?Color.rgb(211,87,76):Color.rgb(235,205,132));if(themeRenderer.hasScene() && p.measureText(status)>getWidth()*.9f)p.setTextSize(p.getTextSize()*getWidth()*.9f/p.measureText(status));c.drawText(status,getWidth()/2f,themeRenderer.hasScene()?top+w+103*den:top-38*den,p);
      p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.argb(210,235,221,184));
      for(int i=0;i<8;i++){int file=flip?7-i:i;int rank=flip?i:7-i;c.drawText(""+(char)('A'+file),left0+i*s+s/2,top+w+14*den,p);p.setTextAlign(Paint.Align.CENTER);c.drawText(""+(rank+1),left0/2f,top+i*s+s*.58f,p);p.setTextAlign(Paint.Align.CENTER);}
      themeRenderer.drawClock(c,getWidth()/2f,top+w+58*den,bottomName,clockText(bottomMs),bottomActive,den,getResources().getDisplayMetrics().scaledDensity,getWidth());
      p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(themeRenderer.hasBackground()?Color.rgb(183,186,174):Color.rgb(118,121,119));String h=history.isEmpty()?"JOGADAS  ·  nenhuma":historyLine().replace("JOGADAS:","JOGADAS  ·");c.drawText(h,getWidth()/2f,(themeRenderer.hasScene()?Math.min(getHeight()-14*den,top+w+125*den):Math.min(getHeight()-48*den,top+w+112*den)),p);
      p.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.rgb(55,59,61));if(!themeRenderer.hasScene())c.drawText("◆  PIXEL CHESS  ◆",getWidth()/2f,Math.min(getHeight()-20*den,top+w+140*den),p);p.clearShadowLayer();
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
    public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float den=getResources().getDisplayMetrics().density;boardGeometry.update(getWidth(),getHeight(),den,themeRenderer.hasScene());float gutter=boardGeometry.left,w=boardGeometry.size,s=w/8f,top=boardGeometry.top;if(e.getX()<gutter||e.getX()>=gutter+w||e.getY()<top||e.getY()>=top+w)return true;int vx=(int)((e.getX()-gutter)/s),vr=(int)((e.getY()-top)/s);if(vr<0||vr>7||vx<0||vx>7)return true;boolean flip=bluetoothGame&&!myWhite;int x=flip?7-vx:vx,r=flip?7-vr:vr;if(gameOver){toast("A partida terminou");return true;}
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

