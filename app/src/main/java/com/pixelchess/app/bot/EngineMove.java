package com.pixelchess.app.bot;

import com.pixelchess.app.ChessGame;
import java.util.Locale;

/** Strict UCI coordinates. No legality rules live here. */
public final class EngineMove {
  public final int fromRow,fromCol,toRow,toCol;
  public final String promotion;
  private EngineMove(String uci){
    fromCol=uci.charAt(0)-'a';fromRow=8-(uci.charAt(1)-'0');
    toCol=uci.charAt(2)-'a';toRow=8-(uci.charAt(3)-'0');
    promotion=uci.length()==5?uci.substring(4).toUpperCase(Locale.ROOT):"-";
  }
  public static EngineMove parse(String uci){
    if(uci==null||!uci.matches("[a-h][1-8][a-h][1-8][qrbn]?"))throw new IllegalArgumentException("Invalid bestmove");
    return new EngineMove(uci);
  }
  public boolean legalIn(ChessGame game){
    if(game.gameOver())return false;
    // Validate promotion, turn, king safety, and every special move with existing rules.
    ChessGame copy=ChessGame.replay(game.transcript());
    return copy.move(fromRow,fromCol,toRow,toCol,promotion);
  }
}
