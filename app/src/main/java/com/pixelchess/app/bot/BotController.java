package com.pixelchess.app.bot;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** One calculation at a time. Public methods and callbacks belong to the UI executor. */
public final class BotController implements AutoCloseable {
  public interface Listener {
    void onMove(EnginePosition position,EngineMove move);
    void onFailure(String message);
  }
  private final ChessEngine engine;
  private final Executor callback;
  private final Listener listener;
  private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"pixelchess-bot");t.setDaemon(true);return t;});
  private Future<?> pending;
  private volatile boolean closed;
  private boolean thinking;
  private long generation;
  public BotController(ChessEngine engine,Executor callback,Listener listener){this.engine=engine;this.callback=callback;this.listener=listener;}
  public boolean thinking(){return thinking;}
  public boolean closed(){return closed;}
  public void request(EnginePosition position,BotDifficulty difficulty,long remainingMs){
    if(closed||thinking)return;
    thinking=true;long token=++generation;
    pending=worker.submit(()->{
      try{
        EngineMove move=EngineMove.parse(engine.search(position,difficulty,remainingMs));
        callback.execute(()->{if(closed||token!=generation)return;thinking=false;listener.onMove(position,move);});
      }catch(Exception failure){
        callback.execute(()->{if(closed||token!=generation)return;thinking=false;close();listener.onFailure("O motor de xadrez não respondeu corretamente. Volte ao menu e tente novamente.");});
      }
    });
  }
  @Override public void close(){
    if(closed)return;
    closed=true;thinking=false;generation++;
    engine.close();if(pending!=null)pending.cancel(true);worker.shutdownNow();
  }
}
