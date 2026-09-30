package com.pixelchess.app;

import android.app.*;
import com.pixelchess.app.bot.*;
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
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    matchConnection=new BluetoothMatchController(this);menuMusic=new MenuMusicController(this);gamePreferences=new GamePreferences(this);chessSounds=new ChessSounds(this);
    setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);themePreferences=new ThemePreferences(getPreferences(MODE_PRIVATE));selectedTheme=themePreferences.load();
    if(!restoreLocalMatch(b))showMenu();
  }

  TextView title(String s,int sp){ TextView v=new TextView(this); v.setText(s); v.setTextColor(cream); v.setTextSize(sp); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return v; }
  Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(18); b.setAllCaps(false); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); return b; }

  @Override protected void onResume(){super.onResume();foreground=true;menuMusic.setForeground(true);chessSounds.setForeground(true);if(game!=null&&game.botGame())game.resumeBot();}
  @Override protected void onPause(){if(game!=null&&game.botGame()){game.settleBotClock();game.pauseBot();}foreground=false;menuMusic.setForeground(false);chessSounds.setForeground(false);super.onPause();}
  @Override protected void onDestroy(){if(game!=null)game.stopBot();matchConnection.cancel();if(connectionDialog!=null)connectionDialog.dismiss();menuMusic.release();chessSounds.release();super.onDestroy();}
  @Override protected void onSaveInstanceState(Bundle out){
    super.onSaveInstanceState(out);
    if(game==null||game.online())return;
    game.settleBotClock();
    out.putBoolean("bot_match",game.botGame());
    if(game.botGame()){
      out.putBoolean("bot_white",game.humanWhite());
      out.putString("bot_difficulty",game.botDifficulty().name());
      out.putLong("bot_saved_at",SystemClock.elapsedRealtime());
    }
    out.putBoolean("local_match",true);
    out.putInt("local_minutes",selectedMinutes);
    out.putLong("local_white_ms",game.matchClock.whiteMs());
    out.putLong("local_black_ms",game.matchClock.blackMs());
    out.putString("local_transcript",game.gameState.transcript());
    out.putBoolean("local_terminal",game.gameState.gameOver());
    out.putString("local_status",game.gameState.status());
  }
  boolean restoreLocalMatch(Bundle state){
    if(state==null||!state.getBoolean("local_match",false))return false;
    try{
      int minutes=state.getInt("local_minutes",10);
      long white=state.getLong("local_white_ms",-1),black=state.getLong("local_black_ms",-1);
      if(minutes<1||minutes>180||white<0||black<0||white>minutes*60000L||black>minutes*60000L)return false;
      ChessGame restored=ChessGame.replay(state.getString("local_transcript","-"));
      if(state.getBoolean("local_terminal",false)&&!restored.gameOver()){
        String terminal=state.getString("local_status","");
        if(terminal.isEmpty())return false;
        restored.finish(terminal);
      }
      selectedMinutes=minutes;mainMenuVisible=false;
      game=new ChessView(this,selectedTheme,minutes,false,true,this);
      game.restore(restored,white,black);
      if(state.getBoolean("bot_match",false)){
        BotDifficulty difficulty=BotDifficulty.valueOf(state.getString("bot_difficulty",BotDifficulty.NORMAL.name()));
        long elapsed=Math.max(0,SystemClock.elapsedRealtime()-state.getLong("bot_saved_at",SystemClock.elapsedRealtime()));
        if(!restored.gameOver()){
          if(restored.whiteTurn())white=Math.max(0,white-elapsed);else black=Math.max(0,black-elapsed);
          game.matchClock.sync(white,black,SystemClock.elapsedRealtime());
        }
        game.configureBot(state.getBoolean("bot_white",true),difficulty,this::createEngine);
      }
      setContentView(game);
      return true;
    }catch(RuntimeException invalidState){return false;}
  }
  @Override public void setContentView(View view){
    if(menuMusic!=null)menuMusic.setMenuVisible(false);
    super.setContentView(view);
  }
  void showMenu(){
    if(game!=null)game.stopBot();if(matchConnection!=null)matchConnection.cancel();game=null;mainMenuVisible=true;
    LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(40,40,40,40); box.setBackgroundColor(bg);
    TextView logo=title("♜  PIXEL CHESS  ♞",30); box.addView(logo,new LinearLayout.LayoutParams(-1,-2));
    TextView sub=title("\nXADREZ LOCAL\n",14); sub.setTextColor(Color.LTGRAY); box.addView(sub);
    Button local=button("▶ Jogar no mesmo celular"); local.setOnClickListener(v->chooseTime(false)); box.addView(local,new LinearLayout.LayoutParams(-1,-2));
    Button bot=button("▶ Jogar contra bot");bot.setOnClickListener(v->chooseBotDifficulty());box.addView(bot,new LinearLayout.LayoutParams(-1,-2));
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
  ChessEngine createEngine(){return new StockfishEngine(new File(getApplicationInfo().nativeLibraryDir,"libstockfish.so").getAbsolutePath());}
  void chooseBotDifficulty(){
    String[] labels=new String[BotDifficulty.values().length];
    for(int i=0;i<labels.length;i++)labels[i]=BotDifficulty.values()[i].label;
    new AlertDialog.Builder(this).setTitle("Dificuldade").setItems(labels,(d,i)->chooseBotColor(BotDifficulty.values()[i]))
      .setNeutralButton("STOCKFISH / LICENÇA",(d,w)->showEngineLicense())
      .setNegativeButton("VOLTAR",null).show();
  }
  void showEngineLicense(){
    try{
      StringBuilder text=new StringBuilder();
      for(String name:new String[]{"NOTICE.txt","COPYING.txt","AUTHORS"}){
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(getAssets().open("stockfish/"+name),java.nio.charset.StandardCharsets.UTF_8))){
          String line;while((line=reader.readLine())!=null)text.append(line).append('\n');
        }
        text.append('\n');
      }
      TextView content=title(text.toString(),12);content.setGravity(Gravity.START);content.setTextIsSelectable(true);content.setPadding(24,24,24,24);
      content.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
      ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(bg);scroll.addView(content);
      new AlertDialog.Builder(this).setTitle("Stockfish 19 • GPL v3").setView(scroll).setPositiveButton("FECHAR",null).show();
    }catch(IOException unavailable){toast("A licença não pôde ser aberta.");}
  }
  void chooseBotColor(BotDifficulty difficulty){
    new AlertDialog.Builder(this).setTitle("Escolher cor").setItems(new String[]{"Brancas","Pretas","Aleatória"},(d,i)->{
      boolean white=i==0||(i==2&&new java.security.SecureRandom().nextBoolean());
      new AlertDialog.Builder(this).setTitle("Tempo por jogador").setItems(new String[]{"10 minutos","5 minutos","3 minutos"},(dialog,t)->{
        selectedMinutes=t==0?10:t==1?5:3;startBotMatch(white,difficulty);
      }).setNegativeButton("VOLTAR",null).show();
    }).setNegativeButton("VOLTAR",null).show();
  }
  void startBotMatch(boolean white,BotDifficulty difficulty){
    if(game!=null)game.stopBot();matchConnection.cancel();mainMenuVisible=false;
    game=new ChessView(this,selectedTheme,selectedMinutes,false,white,this);
    game.configureBot(white,difficulty,this::createEngine);setContentView(game);
    toast("Você joga com as "+(white?"brancas":"pretas")+" • "+difficulty.label);
  }
  @Override public void botFailed(String message){
    if(isFinishing()||isDestroyed())return;
    new AlertDialog.Builder(this).setTitle("Motor indisponível").setMessage(message)
      .setPositiveButton("VOLTAR AO MENU",(d,w)->showMenu()).setCancelable(false).show();
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
    boolean onlineGame=game!=null&&game.online();
    if(onlineGame)game.applyConnectionLost();
    boolean resume=onlineGame;
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
