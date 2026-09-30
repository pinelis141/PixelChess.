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

public class MainActivity extends Activity implements ChessView.Actions,BluetoothMatchController.Listener {
  MenuMusicController menuMusic;
  GamePreferences gamePreferences;
  ChessSounds chessSounds;
  int bg=Color.rgb(20,24,28), cream=Color.rgb(235,221,184), green=Color.rgb(75,96,67);
  BluetoothMatchController matchConnection;
  AlertDialog connectionDialog;
  ChessView game;
  boolean foreground,mainMenuVisible;
  int selectedMinutes=10;
  BoardTheme selectedTheme=BoardThemes.CLASSIC;
  ThemePreferences themePreferences;
  @Override public void onCreate(Bundle b){super.onCreate(b);matchConnection=new BluetoothMatchController(this); menuMusic=new MenuMusicController(this);gamePreferences=new GamePreferences(this);chessSounds=new ChessSounds(this);setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC); themePreferences=new ThemePreferences(getPreferences(MODE_PRIVATE)); selectedTheme=themePreferences.load(); showMenu();}

  TextView title(String s,int sp){ TextView v=new TextView(this); v.setText(s); v.setTextColor(cream); v.setTextSize(sp); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return v; }
  Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return b; }

  @Override protected void onResume(){super.onResume();foreground=true;menuMusic.setForeground(true);}
  @Override protected void onPause(){foreground=false;menuMusic.setForeground(false);super.onPause();}
  @Override protected void onDestroy(){matchConnection.cancel();if(connectionDialog!=null)connectionDialog.dismiss();menuMusic.release();chessSounds.release();super.onDestroy();}
  @Override public void setContentView(View view){
    if(menuMusic!=null)menuMusic.setMenuVisible(false);
    super.setContentView(view);
  }
  void showMenu(){
    if(matchConnection!=null)matchConnection.cancel();game=null;mainMenuVisible=true;
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
    FrameLayout.LayoutParams corner=new FrameLayout.LayoutParams((int)(48*den),(int)(48*den),Gravity.BOTTOM|Gravity.END);
    corner.setMargins(0,0,(int)(16*den),(int)(16*den));
    root.addView(mute,corner);
    mute.setOnClickListener(v->{menuMusic.toggleMuted();mute.setMuted(menuMusic.isMuted());});
    setContentView(root);menuMusic.setMenuVisible(true);
  }
  void showSettings(){mainMenuVisible=false;setContentView(new SettingsView(this,gamePreferences,this::showMenu));}
  void chooseSkin(){
    mainMenuVisible=false;
    setContentView(new ThemeSelectorView(this,BoardThemes.ALL,selectedTheme.id,new ThemeSelectorView.Listener(){
      @Override public void onThemeSelected(BoardTheme theme){
        if(theme.locked){toast("Esta skin ainda está bloqueada");return;}selectedTheme=theme;
        themePreferences.save(selectedTheme);
        chooseSkin();
      }
      @Override public void onClose(){showMenu();}
    }));
  }
  @Override public void onBackPressed(){
    if(game!=null&&!game.gameState.gameOver()){
      new AlertDialog.Builder(this).setTitle("Sair da partida?")
        .setMessage("A partida atual será encerrada. Deseja voltar ao menu?")
        .setPositiveButton("SAIR",(d,w)->showMenu())
        .setNegativeButton("CONTINUAR",null).show();
      return;
    }
    if(game!=null){showMenu();return;}
    if(mainMenuVisible){super.onBackPressed();return;}
    showMenu();
  }
  boolean btPermission(){
    if(Build.VERSION.SDK_INT>=31&&(checkSelfPermission("android.permission.BLUETOOTH_CONNECT")!=PackageManager.PERMISSION_GRANTED||checkSelfPermission("android.permission.BLUETOOTH_SCAN")!=PackageManager.PERMISSION_GRANTED)){
      requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT","android.permission.BLUETOOTH_SCAN"},42);return false;
    }return true;
  }
  @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
    super.onRequestPermissionsResult(request,permissions,results);if(request==42){boolean ok=results.length>0;for(int r:results)ok&=r==PackageManager.PERMISSION_GRANTED;if(ok)bluetoothMenu();else toast("Permita o acesso ao Bluetooth para conectar os aparelhos.");}
  }
  void chooseTime(boolean online){
    new AlertDialog.Builder(this).setTitle("Tempo por jogador").setItems(new String[]{"10 minutos","5 minutos","3 minutos"},(d,i)->{
      selectedMinutes=i==0?10:i==1?5:3;
      if(online)connect(true,null);else{mainMenuVisible=false;game=new ChessView(this,selectedTheme,selectedMinutes,false,true,this);setContentView(game);}
    }).setNegativeButton("VOLTAR",null).show();
  }
  void bluetoothMenu(){
    if(!btPermission())return;BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
    if(adapter==null){toast("Este aparelho não possui Bluetooth");return;}
    try{
      if(!adapter.isEnabled()){startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));return;}
    }catch(SecurityException denied){toast("A permissão Bluetooth foi removida. Autorize novamente e tente de novo.");return;}
    new AlertDialog.Builder(this).setTitle("Jogar via Bluetooth").setMessage("Use a mesma versão do PixelChess nos dois aparelhos. Quem cria escolhe o tempo.")
      .setPositiveButton("CRIAR PARTIDA",(d,w)->chooseTime(true)).setNegativeButton("ENTRAR",(d,w)->chooseDevice())
      .setNeutralButton("PAREAR APARELHOS",(d,w)->startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))).show();
  }
  void chooseDevice(){
    if(!btPermission())return;
    BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();if(adapter==null)return;
    try{
      ArrayList<BluetoothDevice> devices=new ArrayList<>(adapter.getBondedDevices());
      if(devices.isEmpty()){new AlertDialog.Builder(this).setTitle("Parear os aparelhos").setMessage("Pareie o outro celular nas configurações de Bluetooth e volte ao jogo.")
        .setPositiveButton("ABRIR BLUETOOTH",(d,w)->startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))).setNegativeButton("VOLTAR",null).show();return;}
      String[] names=new String[devices.size()];for(int i=0;i<names.length;i++){String n=devices.get(i).getName();names[i]=n==null?devices.get(i).getAddress():n;}
      new AlertDialog.Builder(this).setTitle("Escolha o celular").setItems(names,(d,i)->connect(false,devices.get(i))).setNegativeButton("VOLTAR",null).show();
    }catch(SecurityException denied){
      toast("A permissão Bluetooth foi removida. Autorize novamente e tente de novo.");
    }
  }
  void waiting(boolean host){
    if(connectionDialog!=null)connectionDialog.dismiss();
    connectionDialog=new AlertDialog.Builder(this).setTitle(host?"Aguardando o outro jogador":"Conectando")
      .setMessage("Mantenha o PixelChess aberto nos dois aparelhos. Na retomada, toque em reconectar nos dois celulares.")
      .setNegativeButton("CANCELAR",(d,w)->showMenu()).setCancelable(false).create();connectionDialog.show();
  }
  void connect(boolean host,BluetoothDevice peer){waiting(host);matchConnection.start(host,peer,selectedMinutes);}
  @Override public MatchSnapshot capture(String gameId){
    if(game==null||!game.online())throw new IllegalStateException("Partida indisponível");
    return new MatchSnapshot(gameId,selectedMinutes,game.matchClock.whiteMs(),game.matchClock.blackMs(),game.gameState.transcript());
  }
  @Override public void onReady(boolean host,MatchSnapshot snapshot,ChessGame restored){
    if(connectionDialog!=null){connectionDialog.dismiss();connectionDialog=null;}
    selectedMinutes=snapshot.minutes;
    mainMenuVisible=false;
    if(game==null||!game.online()){game=new ChessView(this,selectedTheme,selectedMinutes,true,host,this);game.restore(restored,snapshot.whiteMs,snapshot.blackMs);setContentView(game);}
    else game.restore(restored,snapshot.whiteMs,snapshot.blackMs);
    toast(host?"Conectado • você joga com as brancas":"Conectado • você joga com as pretas");
  }
  @Override public void onPacket(BluetoothGameProtocol.Message message){
    if(game==null||!game.online())return;
    if(matchConnection.isHost()){
      if(message instanceof BluetoothGameProtocol.Play){BluetoothGameProtocol.Play m=(BluetoothGameProtocol.Play)message;game.applyGuestPlay(m.r1,m.c1,m.r2,m.c2,m.promotion);}
    }else if(message instanceof BluetoothGameProtocol.Move)game.applyAuthorityMove((BluetoothGameProtocol.Move)message);
    else if(message instanceof BluetoothGameProtocol.Sync)game.applyAuthoritySync((BluetoothGameProtocol.Sync)message);
    else if(message instanceof BluetoothGameProtocol.Flag)game.applyAuthorityFlag((BluetoothGameProtocol.Flag)message);
    else if(message instanceof BluetoothGameProtocol.Reject)game.applyAuthorityReject((BluetoothGameProtocol.Reject)message);
  }
  @Override public void onInterrupted(String reason){
    if(isFinishing()||isDestroyed())return;
    if(connectionDialog!=null){connectionDialog.dismiss();connectionDialog=null;}
    boolean resume=game!=null&&game.online();if(resume)game.applyConnectionLost();
    new AlertDialog.Builder(this).setTitle(resume?"Partida pausada":"Não foi possível conectar")
      .setMessage(reason+(resume?"\n\nO tabuleiro foi mantido e os relógios estão pausados. Toquem em reconectar nos dois aparelhos.":""))
      .setPositiveButton(resume?"RECONECTAR":"TENTAR NOVAMENTE",(d,w)->{waiting(matchConnection.isHost());matchConnection.retry(resume);})
      .setNegativeButton("VOLTAR AO MENU",(d,w)->showMenu()).setCancelable(false).show();
  }
  @Override public boolean acceptSequence(long sequence){return matchConnection.accept(sequence);}
  @Override public void desynchronized(){matchConnection.desynchronized();}
  @Override public void playSound(boolean capture,boolean terminal){if(foreground)chessSounds.move(capture,terminal);}
  @Override public void requestMove(int r1,int c1,int r2,int c2,String promotion){if(!matchConnection.isHost())matchConnection.send(BluetoothGameProtocol.play(r1,c1,r2,c2,promotion));}
  @Override public void sendAuthorityMove(int r1,int c1,int r2,int c2,String promotion){
    if(!matchConnection.isHost()||game==null)return;
    matchConnection.send(BluetoothGameProtocol.move(r1,c1,r2,c2,promotion,game.matchClock.whiteMs(),game.matchClock.blackMs(),game.gameState.whiteTurn(),matchConnection.next()));
  }
  @Override public void sendClockSync(){if(matchConnection.isHost()&&game!=null)matchConnection.send(BluetoothGameProtocol.sync(game.matchClock.whiteMs(),game.matchClock.blackMs(),game.gameState.whiteTurn(),matchConnection.next()));}
  @Override public void sendFlag(boolean loserWhite){if(matchConnection.isHost())matchConnection.send(BluetoothGameProtocol.flag(loserWhite,matchConnection.next()));}
  @Override public void sendReject(){if(matchConnection.isHost()&&game!=null)matchConnection.send(BluetoothGameProtocol.reject(game.matchClock.whiteMs(),game.matchClock.blackMs(),game.gameState.whiteTurn(),matchConnection.next()));}
  void toast(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
}
