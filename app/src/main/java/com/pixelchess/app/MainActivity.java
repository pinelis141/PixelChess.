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
  BluetoothAdapter adapter; BluetoothSocket socket; BluetoothServerSocket serverSocket; ChessView game; boolean bluetoothGame=false, myWhite=true; OutputStream btOut;
  @Override public void onCreate(Bundle b){super.onCreate(b); showMenu();}

  TextView title(String s,int sp){ TextView v=new TextView(this); v.setText(s); v.setTextColor(cream); v.setTextSize(sp); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return v; }
  Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return b; }

  void showMenu(){
    LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(40,40,40,40); box.setBackgroundColor(bg);
    TextView logo=title("♜  PIXEL CHESS  ♞",30); box.addView(logo,new LinearLayout.LayoutParams(-1,-2));
    TextView sub=title("\nXADREZ LOCAL\n",14); sub.setTextColor(Color.LTGRAY); box.addView(sub);
    Button local=button("▶ Jogar no mesmo celular"); local.setOnClickListener(v->setContentView(new ChessView(this))); box.addView(local,new LinearLayout.LayoutParams(-1,-2));
    Button bt=button("⌁ Jogar via Bluetooth"); bt.setOnClickListener(v->bluetoothMenu()); box.addView(bt,new LinearLayout.LayoutParams(-1,-2));
    TextView ver=title("\nMVP 0.6 • Bluetooth beta",12); ver.setTextColor(Color.GRAY); box.addView(ver);
    setContentView(box);
  }
  @Override public void onBackPressed(){ closeBluetooth(); showMenu(); }

  boolean btPermission(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission("android.permission.BLUETOOTH_CONNECT")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT","android.permission.BLUETOOTH_SCAN"},42);return false;}return true; }
  void bluetoothMenu(){
    if(!btPermission())return; adapter=BluetoothAdapter.getDefaultAdapter(); if(adapter==null){toast("Este aparelho não possui Bluetooth");return;} if(!adapter.isEnabled()){toast("Ative o Bluetooth e tente novamente");return;}
    new AlertDialog.Builder(this).setTitle("Jogar via Bluetooth").setMessage("Os dois celulares precisam estar pareados nas configurações do Android.").setPositiveButton("CRIAR PARTIDA",(d,w)->hostGame()).setNegativeButton("ENTRAR",(d,w)->chooseDevice()).setNeutralButton("CANCELAR",null).show();
  }
  void hostGame(){toast("Aguardando o outro jogador…"); new Thread(()->{try{serverSocket=adapter.listenUsingRfcommWithServiceRecord("PixelChess",GAME_UUID);socket=serverSocket.accept();serverSocket.close();serverSocket=null;startBtGame(true);}catch(Exception e){if(serverSocket!=null)runOnUiThread(()->toast("Falha ao criar partida: "+e.getMessage()));}}).start();}
  void chooseDevice(){Set<BluetoothDevice> ds=adapter.getBondedDevices();if(ds.isEmpty()){toast("Nenhum aparelho pareado. Pareie os celulares primeiro.");return;} final ArrayList<BluetoothDevice> list=new ArrayList<>(ds);String[] names=new String[list.size()];for(int i=0;i<list.size();i++){String n=list.get(i).getName();names[i]=n==null?list.get(i).getAddress():n;}new AlertDialog.Builder(this).setTitle("Escolha o celular").setItems(names,(d,i)->connectGame(list.get(i))).show();}
  void connectGame(BluetoothDevice dev){toast("Conectando…");new Thread(()->{try{socket=dev.createRfcommSocketToServiceRecord(GAME_UUID);adapter.cancelDiscovery();socket.connect();startBtGame(false);}catch(Exception e){runOnUiThread(()->toast("Não conectou: "+e.getMessage()));}}).start();}
  void startBtGame(boolean host)throws Exception{bluetoothGame=true;myWhite=host;btOut=socket.getOutputStream();runOnUiThread(()->{game=new ChessView(this);game.status=host?"VOCÊ É BRANCAS":"VOCÊ É PRETAS";setContentView(game);});InputStream in=socket.getInputStream();BufferedReader br=new BufferedReader(new InputStreamReader(in));String line;while((line=br.readLine())!=null){String[] a=line.split(",");if(a.length>=4){int r1=Integer.parseInt(a[0]),c1=Integer.parseInt(a[1]),r2=Integer.parseInt(a[2]),c2=Integer.parseInt(a[3]);String promo=a.length>=5?a[4]:"-";runOnUiThread(()->game.remoteMove(r1,c1,r2,c2,promo));}}}
  void sendMove(int r1,int c1,int r2,int c2){sendMove(r1,c1,r2,c2,"-");}
  void sendMove(int r1,int c1,int r2,int c2,String promo){if(!bluetoothGame||btOut==null)return;new Thread(()->{try{btOut.write((r1+","+c1+","+r2+","+c2+","+promo+"\n").getBytes("UTF-8"));btOut.flush();}catch(Exception e){runOnUiThread(()->toast("Conexão Bluetooth perdida"));}}).start();}
  void closeBluetooth(){bluetoothGame=false;try{if(serverSocket!=null)serverSocket.close();}catch(Exception ignored){}try{if(socket!=null)socket.close();}catch(Exception ignored){}serverSocket=null;socket=null;btOut=null;}
  void toast(String x){Toast.makeText(this,x,Toast.LENGTH_LONG).show();}

  class ChessView extends View {
    Paint p=new Paint(3); String[][] b=new String[8][8]; int sr=-1,sc=-1; boolean white=true; String status="BRANCAS JOGAM";
    boolean wKm=false,bKm=false,wRa=false,wRh=false,bRa=false,bRh=false,gameOver=false; int epR=-1,epC=-1,halfmove=0; HashMap<String,Integer> repetitions=new HashMap<>();
    final String back="rnbqkbnr";
    ChessView(Context c){super(c); p.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD)); reset();}
    void reset(){for(int r=0;r<8;r++)Arrays.fill(b[r],null);for(int i=0;i<8;i++){b[0][i]=""+back.charAt(i);b[1][i]="p";b[6][i]="P";b[7][i]=(""+back.charAt(i)).toUpperCase();}white=true;gameOver=false;halfmove=0;repetitions.clear();status="BRANCAS JOGAM";recordPosition();invalidate();}
    protected void onDraw(Canvas c){
      super.onDraw(c);c.drawColor(bg);float w=getWidth(),s=w/8f,top=(getHeight()-w)/2f;
      p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s*.62f);
      boolean flip=bluetoothGame&&!myWhite;
      for(int vr=0;vr<8;vr++)for(int vx=0;vx<8;vx++){int r=flip?7-vr:vr,x=flip?7-vx:vx;p.setColor(((r+x)&1)==0?cream:green);c.drawRect(vx*s,top+vr*s,(vx+1)*s,top+(vr+1)*s,p);
        if(r==sr&&x==sc){p.setColor(0x88FFD54F);c.drawRect(vx*s,top+vr*s,(vx+1)*s,top+(vr+1)*s,p);}
        if(sr>=0&&legal(sr,sc,r,x,true)){p.setColor(0x66000000);c.drawCircle(vx*s+s/2,top+vr*s+s/2,s*.12f,p);}
        String q=b[r][x];if(q!=null){p.setColor(Character.isUpperCase(q.charAt(0))?Color.WHITE:Color.BLACK);c.drawText(sym(q),vx*s+s/2,top+vr*s+s*.72f,p);}
      }
      p.setTextSize(18*getResources().getDisplayMetrics().scaledDensity);p.setColor(cream);c.drawText(status,w/2,Math.max(50,top-35),p);
      p.setTextSize(12*getResources().getDisplayMetrics().scaledDensity);p.setColor(Color.GRAY);c.drawText("PIXEL CHESS • toque numa peça e depois no destino",w/2,Math.min(getHeight()-25,top+w+45),p);
    }
    String sym(String q){String a="kqrbnp";String[] z={"♚","♛","♜","♝","♞","♟"};int i=a.indexOf(Character.toLowerCase(q.charAt(0)));return i<0?q:z[i];}
    public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float s=getWidth()/8f,top=(getHeight()-getWidth())/2f;int vx=(int)(e.getX()/s),vr=(int)((e.getY()-top)/s);if(vr<0||vr>7||vx<0||vx>7)return true;boolean flip=bluetoothGame&&!myWhite;int x=flip?7-vx:vx,r=flip?7-vr:vr;if(gameOver){toast("A partida terminou");return true;}
      if(bluetoothGame && white!=myWhite){toast("Aguarde a jogada do adversário");return true;} if(sr<0){select(r,x);}else if(sr==r&&sc==x){sr=sc=-1;invalidate();}else if(b[r][x]!=null&&isWhite(b[r][x])==white){select(r,x);}else if(legal(sr,sc,r,x,false)){int a=sr,d=sc;String moving=b[a][d];boolean promotes=moving!=null&&Character.toLowerCase(moving.charAt(0))=='p'&&(r==0||r==7);if(promotes)moveWithPromotionChoice(a,d,r,x);else{move(a,d,r,x,"-");sendMove(a,d,r,x,"-");}sr=sc=-1;invalidate();}return true;}
    void remoteMove(int r1,int c1,int r2,int c2,String promo){if(bluetoothGame&&white!=myWhite&&legal(r1,c1,r2,c2,false)){move(r1,c1,r2,c2,promo);sr=sc=-1;invalidate();}}
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
      String q=b[r1][c1];boolean side=isWhite(q);char t=Character.toLowerCase(q.charAt(0));String captured=b[r2][c2];
      if(t=='p'&&c1!=c2&&captured==null&&r2==epR&&c2==epC)b[r1][c2]=null;
      if(t=='k'&&Math.abs(c2-c1)==2){int rc=c2>c1?7:0,nc=c2>c1?5:3;b[r2][nc]=b[r2][rc];b[r2][rc]=null;}
      b[r2][c2]=q;b[r1][c1]=null;
      if(t=='p'&&(r2==0||r2==7)){String z=(promotion==null||promotion.equals("-"))?(side?"Q":"q"):promotion;b[r2][c2]=z;}
      epR=epC=-1;if(t=='p'&&Math.abs(r2-r1)==2){epR=(r1+r2)/2;epC=c1;}
      if(t=='k'){if(side)wKm=true;else bKm=true;} if(t=='r'){if(side&&r1==7&&c1==0)wRa=true;if(side&&r1==7&&c1==7)wRh=true;if(!side&&r1==0&&c1==0)bRa=true;if(!side&&r1==0&&c1==7)bRh=true;} if(captured!=null&&Character.toLowerCase(captured.charAt(0))=='r'){if(r2==7&&c2==0)wRa=true;if(r2==7&&c2==7)wRh=true;if(r2==0&&c2==0)bRa=true;if(r2==0&&c2==7)bRh=true;}
      if(t=='p'||captured!=null)halfmove=0;else halfmove++;white=!white;int repeated=recordPosition();boolean check=inCheck(white),any=false;
      outer:for(int a=0;a<8;a++)for(int d=0;d<8;d++){String z=b[a][d];if(z==null||isWhite(z)!=white)continue;for(int e=0;e<8;e++)for(int f=0;f<8;f++)if(legal(a,d,e,f,false)){any=true;break outer;}}
      if(!any){gameOver=true;status=check?"XEQUE-MATE • "+(white?"PRETAS":"BRANCAS")+" VENCEM":"EMPATE • AFOGAMENTO";}else if(insufficientMaterial()){gameOver=true;status="EMPATE • MATERIAL INSUFICIENTE";}else if(halfmove>=100){gameOver=true;status="EMPATE • REGRA DOS 50 LANCES";}else if(repeated>=3){gameOver=true;status="EMPATE • REPETIÇÃO TRIPLA";}else status=(white?"BRANCAS":"PRETAS")+" JOGAM"+(check?" • XEQUE!":"");
    }
  }
}
