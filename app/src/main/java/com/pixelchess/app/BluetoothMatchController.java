package com.pixelchess.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Owns connection attempts, handshake, cancellation and stale-session guards. UI callbacks run on main. */
final class BluetoothMatchController {
  interface Listener {
    MatchSnapshot capture(String gameId);
    void onReady(boolean host,MatchSnapshot snapshot,ChessGame restored);
    void onPacket(BluetoothGameProtocol.Message message);
    void onInterrupted(String message);
  }
  private static final UUID SERVICE=UUID.fromString("7e57c0de-5049-5845-4c43-484553530001");
  private final Listener listener;
  private final Handler main=new Handler(Looper.getMainLooper());
  private final AtomicLong generation=new AtomicLong();
  private final AuthoritySequence sequence=new AuthoritySequence();
  private volatile BluetoothManager transport;
  private volatile boolean connected;
  private boolean host;
  private BluetoothDevice peer;
  private int minutes;
  private String gameId;
  private Runnable timeout;
  BluetoothMatchController(Listener listener){this.listener=listener;}
  boolean isHost(){return host;}
  boolean connected(){return connected;}
  long next(){return sequence.next();}
  boolean accept(long value){return sequence.accept(value);}

  void start(boolean host,BluetoothDevice peer,int minutes){
    this.host=host;this.peer=peer;this.minutes=minutes;gameId=null;attempt(false);
  }
  void retry(boolean resume){attempt(resume);}
  private void attempt(boolean resume){
    if(resume&&gameId==null){listener.onInterrupted("A partida anterior não está disponível.");return;}
    stopTransport();final long run=generation.incrementAndGet();connected=false;
    final boolean localHost=host;final String expected=resume?gameId:null;
    final BluetoothManager channel=new BluetoothManager();channel.setAdapter(BluetoothAdapter.getDefaultAdapter());transport=channel;
    timeout=()->fail(run,channel,"Tempo de conexão esgotado. Confira os dois aparelhos e tente novamente.");main.postDelayed(timeout,60000);
    new Thread(()->{
      try{
        if(generation.get()!=run)return;
        if(localHost)channel.hostAndAccept("PixelChess",SERVICE);else channel.connect(peer,SERVICE);
        if(generation.get()!=run)return;
        BufferedReader reader=channel.reader();MatchSnapshot snapshot;
        if(localHost){
          String hello=frame(reader);
          String wanted="HELLO,"+MatchSnapshot.VERSION+","+(resume?expected:"NEW");
          if(!wanted.equals(hello))throw new IOException("Versões ou partidas diferentes. Atualize ambos os aparelhos.");
          snapshot=resume?onMain(run,()->listener.capture(expected)):
            new MatchSnapshot(UUID.randomUUID().toString(),minutes,minutes*60000L,minutes*60000L,"-");
          channel.write(snapshot.encode());
          if(!("ACK,"+snapshot.gameId).equals(frame(reader)))throw new IOException("Retomada não confirmada pelo outro jogador.");
          channel.write("START,"+snapshot.gameId);
        }else{
          channel.write("HELLO,"+MatchSnapshot.VERSION+","+(resume?expected:"NEW"));
          snapshot=MatchSnapshot.parse(frame(reader),expected);
          // Validate every move before acknowledging any state received from the peer.
          snapshot.restore();channel.write("ACK,"+snapshot.gameId);
          if(!("START,"+snapshot.gameId).equals(frame(reader)))throw new IOException("Confirmação da partida inválida.");
        }
        ChessGame restored=snapshot.restore();final MatchSnapshot ready=snapshot;
        onMain(run,()->{main.removeCallbacks(timeout);sequence.reset();gameId=ready.gameId;minutes=ready.minutes;connected=true;listener.onReady(localHost,ready,restored);return null;});
        String line;
        while((line=frame(reader))!=null){
          BluetoothGameProtocol.Message message=BluetoothGameProtocol.parse(line);
          if(message==null)throw new IOException("Mensagem da partida inválida.");
          main.post(()->{if(generation.get()==run&&connected)listener.onPacket(message);});
        }
        throw new IOException("A conexão Bluetooth foi interrompida.");
      }catch(Exception error){fail(run,channel,error.getMessage()==null?"Não foi possível conectar.":error.getMessage());}
      finally{if(generation.get()!=run)channel.dispose();}
    },"PixelChess-Bluetooth").start();
  }
  private <T>T onMain(long run,Callable<T> work)throws Exception{
    FutureTask<T> task=new FutureTask<>(()->{if(generation.get()!=run)throw new IOException("Conexão cancelada");return work.call();});
    main.post(task);try{return task.get(10,TimeUnit.SECONDS);}catch(Exception e){task.cancel(false);throw e;}
  }
  static String frame(BufferedReader reader)throws IOException{
    StringBuilder b=new StringBuilder();int c;
    while((c=reader.read())!=-1){if(c=='\n')return b.toString();if(c=='\r')continue;if(b.length()>=61000)throw new IOException("Mensagem extensa demais");b.append((char)c);}
    if(b.length()!=0)throw new IOException("Mensagem interrompida");return null;
  }
  void send(String text){
    BluetoothManager channel=transport;long run=generation.get();if(!connected||channel==null)return;
    channel.writeAsync(text,e->fail(run,channel,"A conexão Bluetooth foi interrompida."));
  }
  void desynchronized(){BluetoothManager channel=transport;if(channel!=null)fail(generation.get(),channel,"Os tabuleiros divergiram. Reconecte para restaurar o estado do anfitrião.");}
  private void fail(long run,BluetoothManager channel,String message){
    main.post(()->{if(generation.get()!=run)return;generation.incrementAndGet();connected=false;if(timeout!=null)main.removeCallbacks(timeout);channel.dispose();listener.onInterrupted(message);});
  }
  private void stopTransport(){generation.incrementAndGet();connected=false;if(timeout!=null)main.removeCallbacks(timeout);if(transport!=null){transport.dispose();transport=null;}}
  void cancel(){stopTransport();gameId=null;}
}
