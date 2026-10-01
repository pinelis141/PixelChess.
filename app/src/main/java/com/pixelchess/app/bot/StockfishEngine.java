package com.pixelchess.app.bot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.List;
import java.util.Random;

/** Separate official executable, stdin/stdout UCI, bounded protocol waits and output. */
public final class StockfishEngine implements ChessEngine {
  private static final String EOF="<engine-eof>";
  private final String executable;
  private final Object lifecycle=new Object();
  private final BlockingQueue<String> replies=new ArrayBlockingQueue<>(128);
  private final ScheduledExecutorService watchdog=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"stockfish-watchdog");t.setDaemon(true);return t;});
  private volatile boolean closed;
  private Process process;
  private BufferedWriter input;
  private Thread reader;
  private boolean initialized;
  private final Random easyRandom;
  public StockfishEngine(String executable){this(executable,new Random());}
  StockfishEngine(String executable,Random easyRandom){this.executable=executable;this.easyRandom=easyRandom;}
  @Override public String search(EnginePosition position,BotDifficulty level,long remainingMs) throws Exception {
    if(closed)throw new IOException("Engine closed");
    // Also bounds startup and blocked stdin writes, not only stdout polling.
    ScheduledFuture<?> timeout=watchdog.schedule(this::close,25,TimeUnit.SECONDS);
    try{
      if(!initialized)start();
      send("setoption name UCI_LimitStrength value "+(level.elo>0));
      send("setoption name Skill Level value "+level.skill);
      if(level.elo>0)send("setoption name UCI_Elo value "+level.elo);
      send("isready");await("readyok",deadline(3000));
      send(position.command);
      String go=level.go(remainingMs);
      if(level==BotDifficulty.EASY){
        List<String> candidates=EasyMovePolicy.candidates(position,easyRandom);
        if(!candidates.isEmpty())go+=" searchmoves "+String.join(" ",candidates);
      }
      send(go);
      String reply=await("bestmove ",deadline(level.budget(remainingMs)+2000));
      if(closed)throw new IOException("Engine deadline exceeded");
      return reply.split("\\s+")[1];
    }finally{timeout.cancel(false);}
  }
  private void start() throws Exception {
    Process launched=new ProcessBuilder(executable).redirectErrorStream(true).start();
    synchronized(lifecycle){
      if(closed){launched.destroyForcibly();throw new IOException("Engine closed");}
      process=launched;input=new BufferedWriter(new OutputStreamWriter(launched.getOutputStream(),StandardCharsets.UTF_8));
      reader=new Thread(()->readOutput(launched),"stockfish-output");reader.setDaemon(true);reader.start();
    }
    send("uci");await("uciok",deadline(8000));
    send("setoption name Threads value 1");send("setoption name Hash value 32");
    send("setoption name Ponder value false");send("setoption name UCI_Chess960 value false");
    send("ucinewgame");send("isready");await("readyok",deadline(8000));initialized=true;
  }
  private void readOutput(Process running){
    try(Reader output=new InputStreamReader(running.getInputStream(),StandardCharsets.UTF_8)){
      // Do not use unbounded readLine on an unexpected/broken executable.
      StringBuilder line=new StringBuilder();int ch;
      while(!closed&&(ch=output.read())!=-1){
        if(ch=='\n'){
          String reply=line.toString().trim();line.setLength(0);
          if(reply.equals("uciok")||reply.equals("readyok")||reply.startsWith("bestmove "))
            if(!replies.offer(reply))throw new IOException("Protocol overflow");
        }else if(ch!='\r'){
          if(line.length()>=16384)throw new IOException("Oversize UCI line");line.append((char)ch);
        }
      }
    }catch(IOException ignored){/* Search observes EOF and fails without changing the board. */}
    finally{replies.offer(EOF);}
  }
  private void send(String command) throws IOException {
    if(closed||input==null)throw new IOException("Engine unavailable");
    input.write(command);input.newLine();input.flush();
  }
  private static long deadline(long ms){return System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(ms);}
  private String await(String prefix,long until) throws Exception {
    while(!closed){
      long left=until-System.nanoTime();if(left<=0)throw new IOException("Engine timeout");
      String line=replies.poll(left,TimeUnit.NANOSECONDS);
      if(line==null||line.equals(EOF))throw new IOException("Engine stopped or timed out");
      if(prefix.endsWith(" ")?line.startsWith(prefix):line.equals(prefix))return line;
      throw new IOException("Unexpected UCI reply");
    }
    throw new IOException("Engine closed");
  }
  @Override public void close(){
    synchronized(lifecycle){
      if(closed)return;closed=true;
      // No pipe writes/waitFor on UI: forcible teardown also handles an unresponsive engine.
      if(process!=null)process.destroyForcibly();
      if(reader!=null)reader.interrupt();replies.offer(EOF);watchdog.shutdownNow();
    }
  }
}
