package com.pixelchess.app.bot;

import com.pixelchess.app.ChessGame;
import java.util.*;

/** Easy-only limited attention. Uses existing rules; Stockfish still chooses the move. */
final class EasyMovePolicy {
  static List<String> candidates(EnginePosition position,Random random){
    if(random.nextInt(4)!=0)return Collections.emptyList();
    ChessGame game=ChessGame.replay(position.transcript);
    if(game.gameOver()||game.inCheck(game.whiteTurn()))return Collections.emptyList();
    List<String> moves=legalMoves(game);
    if(moves.size()<=3)return Collections.emptyList();
    Collections.shuffle(moves,random);
    return new ArrayList<>(moves.subList(0,3));
  }
  static List<String> legalMoves(ChessGame game){
    List<String> moves=new ArrayList<>();
    if(game.gameOver())return moves;
    for(int r=0;r<8;r++)for(int c=0;c<8;c++){
      String piece=game.pieceAt(r,c);
      if(piece==null||ChessGame.isWhitePiece(piece)!=game.whiteTurn())continue;
      for(int toR=0;toR<8;toR++)for(int toC=0;toC<8;toC++){
        if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException();
        if(!game.isLegal(r,c,toR,toC))continue;
        String move=square(r,c)+square(toR,toC);
        if(Character.toLowerCase(piece.charAt(0))=='p'&&(toR==0||toR==7)){
          for(String promotion:new String[]{"q","r","b","n"})moves.add(move+promotion);
        }else moves.add(move);
      }
    }
    return moves;
  }
  private static String square(int r,int c){return ""+(char)('a'+c)+(8-r);}
}
