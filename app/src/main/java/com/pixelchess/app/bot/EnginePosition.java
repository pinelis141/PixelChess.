package com.pixelchess.app.bot;

import com.pixelchess.app.ChessGame;
import java.util.Locale;

/** Immutable UCI request. Replay is the game's sole rules authority. */
public final class EnginePosition {
  public final String transcript,command;
  private EnginePosition(String transcript,String command){this.transcript=transcript;this.command=command;}
  public static EnginePosition from(ChessGame game){
    if(game.gameOver())throw new IllegalStateException("Terminal position");
    String transcript=game.transcript();
    StringBuilder command=new StringBuilder("position startpos");
    if(!transcript.equals("-")){
      command.append(" moves");
      for(String move:transcript.split(";")){
        if(!move.matches("[0-7]{4}[-QRBN]"))throw new IllegalArgumentException("Malformed transcript");
        command.append(' ').append(square(move.charAt(0)-'0',move.charAt(1)-'0'))
          .append(square(move.charAt(2)-'0',move.charAt(3)-'0'));
        if(move.charAt(4)!='-')command.append(move.substring(4).toLowerCase(Locale.ROOT));
      }
    }
    return new EnginePosition(transcript,command.toString());
  }
  private static String square(int row,int col){return ""+(char)('a'+col)+(8-row);}
}
