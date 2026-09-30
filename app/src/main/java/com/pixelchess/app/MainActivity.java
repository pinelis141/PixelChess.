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
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends Activity {
  MenuMusicController menuMusic;
  GamePreferences gamePreferences;
  ChessSounds chessSounds;
  int bg=Color.rgb(20,24,28), cream=Color.rgb(235,221,184), green=Color.rgb(75,96,67);
  static final UUID GAME_UUID=UUID.fromString("7e57c0de-5049-5845-4c43-484553530001");
  final BluetoothManager bluetooth=new BluetoothManager(); final AuthoritySequence authoritySequence=new AuthoritySequence(); final AtomicLong bluetoothSessions=new AtomicLong(); volatile long activeBluetoothSession; ChessView game; boolean bluetoothGame=false, myWhite=true; int selectedMinutes=10; BoardTheme selectedTheme=BoardThemes.CLASSIC; ThemePreferences themePreferences;
  @Override public void onCreate(Bundle b){super.onCreate(b); menuMusic=new MenuMusicController(this);gamePreferences=new GamePreferences(this);chessSounds=new ChessSounds(this);setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC); themePreferences=new ThemePreferences(getPreferences(MODE_PRIVATE)); selectedTheme=themePreferences.load(); showMenu();}

  TextView title(String s,int sp){ TextView v=new TextView(this); v.setText(s); v.setTextColor(cream); v.setTextSize(sp); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return v; }
  Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return b; }

  @Override protected void onResume(){super.onResume();menuMusic.setForeground(true);}
  @Override protected void onPause(){menuMusic.setForeground(false);super.onPause();}
  @Override protected void onDestroy(){menuMusic.release();chessSounds.release();super.onDestroy();}
  @Override public void setContentView(View view){
    if(menuMusic!=null)menuMusic.setMenuVisible(false);
    super.setContentView(view);
  }
  void showMenu(){
    LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(40,40,40,40); box.setBackgroundColor(bg);
    TextView logo=title("♜  PIXEL CHESS  ♞",30); box.addView(logo,new LinearLayout.LayoutParams(-1,-2));
    TextView sub=title("\nXADREZ LOCAL\n",14); sub.setTextColor(Color.LTGRAY); box.addView(sub);
    Button local=button("▶ Jogar no mesmo celular"); local.setOnClickListener(v->chooseTime(false)); box.addView(local,new LinearLayout.LayoutParams(-1,-2));
    Button bt=button("⌁ Jogar via Bluetooth"); bt.setOnClickListener(v->bluetoothMenu()); box.addView(bt,new LinearLayout.LayoutParams(-1,-2));
    Button skin=button("▣ Skin: "+selectedTheme.name); skin.setOnClickListener(v->chooseSkin()); box.addView(skin,new LinearLayout.LayoutParams(-1,-2));
    Button settings=button("⚙ Configurações");settings.setOnClickListener(v->showSettings());box.addView(settings,new LinearLayout.LayoutParams(-1,-2));
    TextView ver=title("\nMVP "+BuildConfig.VERSION_NAME+" • Temas",12); ver.setTextColor(Color.GRAY); box.addView(ver);
    float den=getResources().getDisplayMetrics().density;
    FrameLayout root=new FrameLayout(this);root.setBackgroundColor(bg);
    box.setPadding(40,40,40,(int)(80*den));
    root.addView(box,new FrameLayout.LayoutParams(-1,-1));
    MusicToggleButton mute=new MusicToggleButton(this);mute.setMuted(menuMusic.isMuted());
    FrameLayout.LayoutParams corner=new FrameLayout.LayoutParams((int)(48*den),(int)(48*den),Gravity.BOTTOM|Gravity.RIGHT);
    corner.setMargins(0,0,(int)(16*den),(int)(16*den));
    root.addView(mute,corner);
    mute.setOnClickListener(v->{menuMusic.toggleMuted();mute.setMuted(menuMusic.isMuted());});
    setContentView(root);menuMusic.setMenuVisible(true);
  }
  void showSettings(){setContentView(new SettingsView(this,gamePreferences,this::showMenu));}
  void chooseSkin(){
    setContentView(new ThemeSelectorView(this,BoardThemes.ALL,selectedTheme.id,new ThemeSelectorView.Listener(){
      @Override public void onThemeSelected(BoardTheme theme){
        if(theme.locked){toast("Esta skin ainda está bloqueada");return;}selectedTheme=theme;
        themePreferences.save(selectedTheme);
        chooseSkin();
      }
      @Override public void onClose(){showMenu();}
    }));
  }
  @Override public void onBackPressed(){ closeBluetooth(); showMenu(); }

  boolean btPermission(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission("android.permission.BLUETOOTH_CONNECT")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT","android.permission.BLUETOOTH_SCAN"},42);return false;}return true; }
  void chooseTime(boolean bt){String[] x={"10 minutos","5 minutos","3 minutos"};new AlertDialog.Builder(this).setTitle("Escolha o tempo por jogador").setItems(x,(d,i)->{selectedMinutes=i==0?10:i==1?5:3;if(bt)hostGame();else setContentView(new ChessView(this));}).setNegativeButton("VOLTAR",(d,w)->showMenu()).show();}
  void bluetoothMenu(){
    if(!btPermission())return;
    bluetooth.setAdapter(BluetoothAdapter.getDefaultAdapter());
    if(!bluetooth.available()){toast("Este aparelho não possui Bluetooth");return;}
    if(!bluetooth.enabled()){toast("Ative o Bluetooth e tente novamente");return;}
    new AlertDialog.Builder(this).setTitle("Jogar via Bluetooth").setMessage("Quem criar a sala escolhe o tempo para os dois jogadores.").setPositiveButton("CRIAR PARTIDA",(d,w)->chooseTime(true)).setNegativeButton("ENTRAR",(d,w)->chooseDevice()).setNeutralButton("CANCELAR",null).show();
  }
    void hostGame(){toast("Aguardando o outro jogador…");new Thread(()->{try{bluetooth.hostAndAccept("PixelChess",GAME_UUID);startBtGame(true);}catch(Exception e){bluetooth.close();runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Não foi possível criar a partida").setMessage("Confira se o outro aparelho está pareado e tente novamente.\n\n"+e.getMessage()).setPositiveButton("TENTAR DE NOVO",(d,w)->hostGame()).setNegativeButton("VOLTAR",(d,w)->showMenu()).show());}}).start();}
  void chooseDevice(){Set<BluetoothDevice> ds=bluetooth.bondedDevices();if(ds.isEmpty()){toast("Nenhum aparelho pareado. Pareie os celulares primeiro.");return;}final ArrayList<BluetoothDevice> list=new ArrayList<>(ds);String[] names=new String[list.size()];for(int i=0;i<list.size();i++){String n=list.get(i).getName();names[i]=n==null?list.get(i).getAddress():n;}new AlertDialog.Builder(this).setTitle("Escolha o celular").setItems(names,(d,i)->connectGame(list.get(i))).show();}
  void connectGame(BluetoothDevice dev){toast("Conectando…");new Thread(()->{try{bluetooth.connect(dev,GAME_UUID);startBtGame(false);}catch(Exception e){bluetooth.close();runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Falha na conexão").setMessage("Confirme que o outro aparelho está aguardando uma partida e tente novamente.\n\n"+e.getMessage()).setPositiveButton("ESCOLHER APARELHO",(d,w)->chooseDevice()).setNegativeButton("VOLTAR",(d,w)->showMenu()).show());}}).start();}
  void startBtGame(boolean host)throws Exception{
    final long session=bluetoothSessions.incrementAndGet();activeBluetoothSession=session;
    bluetoothGame=true;myWhite=host;authoritySequence.reset();
    BufferedReader br=bluetooth.reader();
    if(host){
      writeBt(BluetoothGameProtocol.time(selectedMinutes));
    }else{
      BluetoothGameProtocol.Message setup=BluetoothGameProtocol.parse(br.readLine());
      if(!(setup instanceof BluetoothGameProtocol.Time))throw new IOException("Configuração da sala inválida");
      BluetoothGameProtocol.Time time=(BluetoothGameProtocol.Time)setup;
      if(time.version!=BluetoothGameProtocol.VERSION)throw new IOException("Versões incompatíveis. Atualize o PixelChess nos dois aparelhos.");
      selectedMinutes=time.minutes;
    }
    runOnUiThread(()->{game=new ChessView(this);game.status=host?"VOCÊ É BRANCAS":"VOCÊ É PRETAS";setContentView(game);});
    String line;
    while((line=br.readLine())!=null){
      BluetoothGameProtocol.Message msg=BluetoothGameProtocol.parse(line);
      if(host&&msg instanceof BluetoothGameProtocol.Play){
        BluetoothGameProtocol.Play p=(BluetoothGameProtocol.Play)msg;
        runOnUiThread(()->game.applyGuestPlay(p.r1,p.c1,p.r2,p.c2,p.promotion));
      }else if(!host&&msg instanceof BluetoothGameProtocol.Move){
        BluetoothGameProtocol.Move m=(BluetoothGameProtocol.Move)msg;
        runOnUiThread(()->game.applyAuthorityMove(m));
      }else if(!host&&msg instanceof BluetoothGameProtocol.Sync){
        BluetoothGameProtocol.Sync m=(BluetoothGameProtocol.Sync)msg;
        runOnUiThread(()->game.applyAuthoritySync(m));
      }else if(!host&&msg instanceof BluetoothGameProtocol.Flag){
        BluetoothGameProtocol.Flag m=(BluetoothGameProtocol.Flag)msg;
        runOnUiThread(()->game.applyAuthorityFlag(m));
      }else if(!host&&msg instanceof BluetoothGameProtocol.Reject){
        BluetoothGameProtocol.Reject m=(BluetoothGameProtocol.Reject)msg;
        runOnUiThread(()->game.applyAuthorityReject(m));
      }
    }
    if(bluetoothGame&&session==activeBluetoothSession){
      runOnUiThread(()->handleConnectionLost(session));
    }
  }
  void writeBt(String msg)throws IOException{bluetooth.write(msg);}
  void requestMove(int r1,int c1,int r2,int c2,String promo){if(!bluetoothGame||myWhite)return;final long session=activeBluetoothSession;bluetooth.writeAsync(BluetoothGameProtocol.play(r1,c1,r2,c2,promo),e->runOnUiThread(()->{if(session==activeBluetoothSession){toast("Conexão Bluetooth perdida");handleConnectionLost(session);}}));}
  void sendAuthorityMove(int r1,int c1,int r2,int c2,String promo){if(!bluetoothGame||!myWhite||game==null)return;final long session=activeBluetoothSession,seq=authoritySequence.next(),wm=game.matchClock.whiteMs(),bm=game.matchClock.blackMs();final boolean turn=game.gameState.whiteTurn();bluetooth.writeAsync(BluetoothGameProtocol.move(r1,c1,r2,c2,promo,wm,bm,turn,seq),e->runOnUiThread(()->{if(session==activeBluetoothSession){toast("Conexão Bluetooth perdida");handleConnectionLost(session);}}));}
  void sendClockSync(){if(!bluetoothGame||!myWhite||game==null)return;final long session=activeBluetoothSession,seq=authoritySequence.next(),wm=game.matchClock.whiteMs(),bm=game.matchClock.blackMs();final boolean turn=game.gameState.whiteTurn();bluetooth.writeAsync(BluetoothGameProtocol.sync(wm,bm,turn,seq),e->runOnUiThread(()->handleConnectionLost(session)));}
  void sendFlag(boolean loserWhite){if(!bluetoothGame||!myWhite)return;final long session=activeBluetoothSession,seq=authoritySequence.next();bluetooth.writeAsync(BluetoothGameProtocol.flag(loserWhite,seq),e->runOnUiThread(()->handleConnectionLost(session)));}
  void sendReject(){if(!bluetoothGame||!myWhite||game==null)return;final long session=activeBluetoothSession,seq=authoritySequence.next(),wm=game.matchClock.whiteMs(),bm=game.matchClock.blackMs();final boolean turn=game.gameState.whiteTurn();bluetooth.writeAsync(BluetoothGameProtocol.reject(wm,bm,turn,seq),e->runOnUiThread(()->handleConnectionLost(session)));}
  void handleConnectionLost(long session){if(!bluetoothGame||session!=activeBluetoothSession)return;bluetoothGame=false;bluetooth.close();authoritySequence.reset();if(game!=null)game.applyConnectionLost();new AlertDialog.Builder(this).setTitle("Conexão perdida").setMessage("A partida não pode ser retomada automaticamente. Refaça a conexão para iniciar uma nova partida.").setPositiveButton("VOLTAR AO MENU",(d,w)->{closeBluetooth();showMenu();}).setNegativeButton("OK",null).show();}
  void closeBluetooth(){bluetoothGame=false;activeBluetoothSession=bluetoothSessions.incrementAndGet();bluetooth.close();authoritySequence.reset();}
  void toast(String x){Toast.makeText(this,x,Toast.LENGTH_LONG).show();}

  class ChessView extends View {
    Paint p=new Paint(3); int sr=-1,sc=-1; String status="BRANCAS JOGAM";
    final ChessGame gameState=new ChessGame(); final GameClock matchClock;
    HashMap<Character,Bitmap> pieceSprites=new HashMap<>(); final BoardThemeRenderer themeRenderer; final SceneGeometry boardGeometry=new SceneGeometry(); Paint spritePaint=new Paint();
    boolean flagSent=false,awaitingAuthority=false; boolean animating=false; int animR1,animC1,animR2,animC2; String animPiece,capturedPiece; long animStart; final long ANIM_MS=220; Handler clock=new Handler(Looper.getMainLooper()); Runnable ticker;
    ChessView(Context c){
      super(c);
      p.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
      spritePaint.setAntiAlias(false);spritePaint.setFilterBitmap(false);spritePaint.setDither(false);
      themeRenderer=new BoardThemeRenderer(getResources(),selectedTheme,gamePreferences.effects());
      matchClock=new GameClock(selectedMinutes,System.currentTimeMillis());
      loadPieceSprites();reset();
      ticker=()->{
        if(!gameState.gameOver()){
          long now=System.currentTimeMillis();
          if(!bluetoothGame||myWhite){
            GameClock.Tick tick=matchClock.tick(gameState.whiteTurn(),now);
            if(tick.timedOut){
              gameState.finish("TEMPO • "+(tick.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");
              status=gameState.status();
              if(bluetoothGame&&!flagSent){flagSent=true;sendFlag(tick.loserWhite);}
            }else if(bluetoothGame&&matchClock.shouldSync(now,500)){
              sendClockSync();
            }
            invalidate();
          }
          clock.postDelayed(ticker,bluetoothGame&&!myWhite?250:100);
        }
      };
      clock.post(ticker);
    }
    @Override protected void onDetachedFromWindow(){clock.removeCallbacks(ticker);super.onDetachedFromWindow();}
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
          p.setColor(gameState.gameOver()?Color.rgb(198,40,40):Color.rgb(245,124,0));
          c.drawRect(left0+vx*s,top+vr*s,left0+(vx+1)*s,top+(vr+1)*s,p);
        }
        if(sr==r&&sc==x)drawGoldSquare(c,left0+vx*s,top+vr*s,s,true);
        if(sr>=0&&!(r==sr&&x==sc)&&gameState.isLegal(sr,sc,r,x)){
          if(gameState.pieceAt(r,x)!=null)drawGoldSquare(c,left0+vx*s,top+vr*s,s,false);
          else drawGoldMoveMarker(c,left0+vx*s,top+vr*s,s);
        }
        String q=gameState.pieceAt(r,x);if(q!=null&&!(animating&&r==animR2&&x==animC2))drawPieceSprite(c,q,left0+vx*s,top+vr*s,s);
      }
      if(animating){float t=PieceMotion.progress(System.currentTimeMillis()-animStart,ANIM_MS),u=PieceMotion.eased(t);int fr=flip?7-animR1:animR1,fc=flip?7-animC1:animC1,tr=flip?7-animR2:animR2,tc=flip?7-animC2:animC2;float ax=left0+(fc+(tc-fc)*u)*s,ay=top+(fr+(tr-fr)*u)*s-PieceMotion.arc(t,s);drawPieceSprite(c,animPiece,ax,ay,s);if(capturedPiece!=null){p.setColor(Color.argb(Math.round(150*(1-t)),255,196,88));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,s*.035f));float radius=s*(.15f+.38f*t);c.drawCircle(left0+(tc+.5f)*s,top+(tr+.5f)*s,radius,p);p.setStyle(Paint.Style.FILL);}if(t<1f)postInvalidateOnAnimation();else{animating=false;capturedPiece=null;}}
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
      p.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);p.setColor(themeRenderer.hasBackground()?Color.rgb(183,186,174):Color.rgb(118,121,119));String h=!gameState.hasHistory()?"JOGADAS  ·  nenhuma":gameState.historyLine().replace("JOGADAS:","JOGADAS  ·");if(p.measureText(h)>getWidth()*.94f)p.setTextSize(p.getTextSize()*getWidth()*.94f/p.measureText(h));c.drawText(h,getWidth()/2f,(themeRenderer.hasScene()?Math.min(getHeight()-14*den,top+w+125*den):Math.min(getHeight()-48*den,top+w+112*den)),p);
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
    String[] pixelPattern(char t){switch(Character.toLowerCase(t)){case 'p':return new String[]{"...##...","..####..","..####..","...##...","..####..",".######.","########"};case 'r':return new String[]{"##.##.##","########",".######.","..####..","..####..",".######.","########"};case 'n':return new String[]{"...###..","..#####.",".###.##.",".######.","..#####.","..####..",".######.","########"};case 'b':return new String[]{"...##...","..####..","...##...","..####..",".######.","..####..",".######.","########"};case 'q':return new String[]{"#..##..#",".######.","..####..",".######.","..####..",".######.","########","########"};default:return new String[]{"...##...",".#.##.#.",".######.","..####..",".######.","..####..",".######.","########"};}}
    void drawPixelPiece(Canvas c,String q,float left,float top,float size){String[] pat=pixelPattern(q.charAt(0));float cell=size/10f,ox=left+cell,oy=top+(size-pat.length*cell)/2f;boolean whitePiece=Character.isUpperCase(q.charAt(0));p.setStyle(Paint.Style.FILL);if(whitePiece){p.setColor(Color.rgb(35,38,40));for(int r=0;r<pat.length;r++)for(int x=0;x<pat[r].length();x++)if(pat[r].charAt(x)=='#')for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)if(Math.abs(dx)+Math.abs(dy)==1)c.drawRect(ox+(x+dx)*cell,oy+(r+dy)*cell,ox+(x+dx+1)*cell,oy+(r+dy+1)*cell,p);}p.setColor(whitePiece?Color.rgb(248,245,232):Color.rgb(25,28,31));for(int r=0;r<pat.length;r++)for(int x=0;x<pat[r].length();x++)if(pat[r].charAt(x)=='#')c.drawRect(ox+x*cell,oy+r*cell,ox+(x+1)*cell,oy+(r+1)*cell,p);}
    void startMoveAnimation(String q,int r1,int c1,int r2,int c2){animPiece=q;capturedPiece=gameState.pieceAt(r2,c2);if(capturedPiece==null&&Character.toLowerCase(q.charAt(0))=='p'&&c1!=c2)capturedPiece=gameState.pieceAt(r1,c2);animR1=r1;animC1=c1;animR2=r2;animC2=c2;animStart=System.currentTimeMillis();animating=true;postInvalidateOnAnimation();}
    public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float den=getResources().getDisplayMetrics().density;boardGeometry.update(getWidth(),getHeight(),den,themeRenderer.hasScene());float gutter=boardGeometry.left,w=boardGeometry.size,s=w/8f,top=boardGeometry.top;if(e.getX()<gutter||e.getX()>=gutter+w||e.getY()<top||e.getY()>=top+w)return true;int vx=(int)((e.getX()-gutter)/s),vr=(int)((e.getY()-top)/s);if(vr<0||vr>7||vx<0||vx>7)return true;boolean flip=bluetoothGame?!myWhite:gamePreferences.blackAtBottom();int x=flip?7-vx:vx,r=flip?7-vr:vr;if(gameState.gameOver()){toast("A partida terminou");return true;}
      if(awaitingAuthority){toast("Aguardando confirmação da jogada…");return true;}
      if(bluetoothGame && gameState.whiteTurn()!=myWhite){toast("Aguarde a jogada do adversário");return true;} if(sr<0){select(r,x);}else if(sr==r&&sc==x){sr=sc=-1;invalidate();}else if(gameState.pieceAt(r,x)!=null&&ChessGame.isWhitePiece(gameState.pieceAt(r,x))==gameState.whiteTurn()){select(r,x);}else if(gameState.isLegal(sr,sc,r,x)){int a=sr,d=sc;String moving=gameState.pieceAt(a,d);boolean promotes=moving!=null&&Character.toLowerCase(moving.charAt(0))=='p'&&(r==0||r==7);if(promotes)moveWithPromotionChoice(a,d,r,x);else if(bluetoothGame&&!myWhite){awaitingAuthority=true;requestMove(a,d,r,x,"-");sr=sc=-1;invalidate();}else if(move(a,d,r,x,"-")){if(bluetoothGame)sendAuthorityMove(a,d,r,x,"-");sr=sc=-1;invalidate();}}return true;}
    void applyGuestPlay(int r1,int c1,int r2,int c2,String promo){if(!bluetoothGame||!myWhite||gameState.gameOver()||gameState.whiteTurn()==myWhite)return;if(!gameState.isLegal(r1,c1,r2,c2)){sendReject();return;}if(move(r1,c1,r2,c2,promo)){sendAuthorityMove(r1,c1,r2,c2,promo);if(!gameState.gameOver()&&gameState.whiteTurn()==myWhite)vibrateTurn();invalidate();}}
    void applyAuthorityMove(BluetoothGameProtocol.Move m){if(!bluetoothGame||myWhite||!authoritySequence.accept(m.sequence))return;awaitingAuthority=false;if(gameState.isLegal(m.r1,m.c1,m.r2,m.c2)){move(m.r1,m.c1,m.r2,m.c2,m.promotion);}gameState.forceTurn(m.whiteTurn);matchClock.sync(m.whiteMs,m.blackMs,System.currentTimeMillis());status=gameState.status();sr=sc=-1;if(!gameState.gameOver()&&gameState.whiteTurn()==myWhite)vibrateTurn();invalidate();}
    void applyAuthoritySync(BluetoothGameProtocol.Sync m){if(!bluetoothGame||myWhite||!authoritySequence.accept(m.sequence))return;matchClock.sync(m.whiteMs,m.blackMs,System.currentTimeMillis());gameState.forceTurn(m.whiteTurn);invalidate();}
    void applyAuthorityFlag(BluetoothGameProtocol.Flag m){if(!bluetoothGame||myWhite||!authoritySequence.accept(m.sequence)||gameState.gameOver())return;awaitingAuthority=false;matchClock.flag(m.loserWhite);gameState.finish("TEMPO • "+(m.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");status=gameState.status();invalidate();}
    void applyAuthorityReject(BluetoothGameProtocol.Reject m){if(!bluetoothGame||myWhite||!authoritySequence.accept(m.sequence))return;awaitingAuthority=false;matchClock.sync(m.whiteMs,m.blackMs,System.currentTimeMillis());gameState.forceTurn(m.whiteTurn);toast("Jogada não confirmada. Estado sincronizado.");invalidate();}
    void applyConnectionLost(){awaitingAuthority=false;if(!gameState.gameOver()){gameState.finish("CONEXÃO BLUETOOTH PERDIDA");status=gameState.status();invalidate();}}
    void vibrateTurn(){if(!gamePreferences.vibration())return;try{Vibrator v=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);if(v==null||!v.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(70,VibrationEffect.DEFAULT_AMPLITUDE));else v.vibrate(70);}catch(Exception ignored){}}
    void moveWithPromotionChoice(int r1,int c1,int r2,int c2){
      final boolean side=ChessGame.isWhitePiece(gameState.pieceAt(r1,c1));final String[] labels={"DAMA","TORRE","BISPO","CAVALO"},pcs={"Q","R","B","N"};
      LinearLayout box=new LinearLayout(MainActivity.this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(32,16,32,16);
      AlertDialog dialog=new AlertDialog.Builder(MainActivity.this).setTitle("PROMOÇÃO").setMessage("Escolha a peça:").setView(box).setCancelable(false).create();
      for(int i=0;i<4;i++){final int k=i;Button bt=button(labels[i]);bt.setTextColor(Color.BLACK);bt.setBackgroundColor(Color.rgb(238,238,238));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,6,0,6);box.addView(bt,lp);bt.setOnClickListener(v->{String z=pcs[k];if(!side)z=z.toLowerCase();if(bluetoothGame&&!myWhite){awaitingAuthority=true;sr=sc=-1;requestMove(r1,c1,r2,c2,z);}else if(move(r1,c1,r2,c2,z)&&bluetoothGame){sendAuthorityMove(r1,c1,r2,c2,z);}sr=sc=-1;dialog.dismiss();invalidate();});}
      dialog.show();
    }
    void select(int r,int c){String q=gameState.pieceAt(r,c);if(q!=null&&ChessGame.isWhitePiece(q)==gameState.whiteTurn()){sr=r;sc=c;invalidate();}}
    boolean move(int r1,int c1,int r2,int c2,String promotion){
      String piece=gameState.pieceAt(r1,c1);String captured=gameState.pieceAt(r2,c2);if(captured==null&&piece!=null&&Character.toLowerCase(piece.charAt(0))=='p'&&c1!=c2)captured=gameState.pieceAt(r1,c2);
      if(piece==null)return false;
      long now=System.currentTimeMillis();
      if(!bluetoothGame||myWhite){
        GameClock.Tick tick=matchClock.tick(gameState.whiteTurn(),now);
        if(tick.timedOut){
          gameState.finish("TEMPO • "+(tick.loserWhite?"PRETAS":"BRANCAS")+" VENCEM");
          status=gameState.status();
          if(bluetoothGame&&!flagSent){flagSent=true;sendFlag(tick.loserWhite);}
          invalidate();
          return false;
        }
      }
      startMoveAnimation(piece,r1,c1,r2,c2);
      if(!gameState.move(r1,c1,r2,c2,promotion))return false;
      if(gamePreferences.sound())chessSounds.move(captured!=null,gameState.gameOver());
      matchClock.markTurnChanged(now);
      status=gameState.status();
      return true;
    }
  }
}
