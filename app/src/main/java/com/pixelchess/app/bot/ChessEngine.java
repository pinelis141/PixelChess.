package com.pixelchess.app.bot;

/** search runs on a worker; close must promptly interrupt any pending search. */
public interface ChessEngine extends AutoCloseable {
  String search(EnginePosition position,BotDifficulty difficulty,long remainingMs) throws Exception;
  @Override void close();
}
