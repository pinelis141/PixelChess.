package com.pixelchess.app.bot;

import com.pixelchess.app.ChessGame;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class EasyMovePolicyTest {
  private static Random roll(int value){return new Random(){@Override public int nextInt(int bound){return value%bound;}};}
  private static ChessGame position(String... moves){
    ChessGame game=new ChessGame();
    for(String uci:moves){EngineMove m=EngineMove.parse(uci);assertTrue(game.move(m.fromRow,m.fromCol,m.toRow,m.toCol,m.promotion));}
    return game;
  }
  @Test public void initialOptionsMatchExistingRules(){
    ChessGame game=new ChessGame();List<String> moves=EasyMovePolicy.legalMoves(game);
    assertEquals(20,moves.size());assertEquals(20,new HashSet<>(moves).size());
    for(String move:moves)assertTrue(EngineMove.parse(move).legalIn(game));
    assertEquals("-",game.transcript());
  }
  @Test public void onlyOneOfFourRollsRestrictsToThreeLegalOptions(){
    EnginePosition position=EnginePosition.from(new ChessGame());
    for(int roll=1;roll<4;roll++)assertTrue(EasyMovePolicy.candidates(position,roll(roll)).isEmpty());
    List<String> chosen=EasyMovePolicy.candidates(position,roll(0));
    assertEquals(3,chosen.size());assertEquals(3,new HashSet<>(chosen).size());
    for(String move:chosen)assertTrue(EngineMove.parse(move).legalIn(position));
  }
  @Test public void checkDoesNotRestrictAvailableDefenses(){
    ChessGame game=position("e2e4","f7f6","d1h5");assertTrue(game.inCheck(game.whiteTurn()));
    assertTrue(EasyMovePolicy.candidates(EnginePosition.from(game),roll(0)).isEmpty());
  }
  @Test public void optionsPreserveCastlingAndEnPassant(){
    assertTrue(EasyMovePolicy.legalMoves(position("e2e4","e7e5","g1f3","b8c6","f1e2","g8f6")).contains("e1g1"));
    assertTrue(EasyMovePolicy.legalMoves(position("e2e4","a7a6","e4e5","d7d5")).contains("e5d6"));
  }
  @Test public void allFourPromotionOptionsRemainLegal(){
    ChessGame game=position("a2a4","b8c6","a4a5","h7h5","a5a6","h5h4","a6b7","c6a5");
    List<String> moves=EasyMovePolicy.legalMoves(game);
    for(String suffix:new String[]{"q","r","b","n"}){
      assertTrue(moves.contains("b7b8"+suffix));assertTrue(EngineMove.parse("b7b8"+suffix).legalIn(game));
    }
  }
  @Test public void terminalPositionsHaveNoCandidates(){
    assertTrue(EasyMovePolicy.legalMoves(position("f2f3","e7e5","g2g4","d8h4")).isEmpty());
  }
  @Test public void optionEnumerationRespondsToCancellation(){
    Thread.currentThread().interrupt();
    try{EasyMovePolicy.legalMoves(new ChessGame());fail();}
    catch(java.util.concurrent.CancellationException expected){}
    finally{Thread.interrupted();}
  }
  @Test public void sampledOptionsVaryWithoutMutatingLiveGame(){
    ChessGame game=new ChessGame();EnginePosition position=EnginePosition.from(game);Set<String> options=new HashSet<>();
    Random random=new Random(141);int restricted=0;
    for(int i=0;i<200;i++){
      List<String> moves=EasyMovePolicy.candidates(position,random);
      if(!moves.isEmpty()){restricted++;options.addAll(moves);}
    }
    assertTrue(restricted>25&&restricted<75);assertTrue(options.size()>3);assertEquals("-",game.transcript());
  }
  @Test public void actualTransportSendsRestrictionOnlyForEasy()throws Exception{
    Path dir=Files.createTempDirectory("easy-uci");Path executable=dir.resolve("engine"),log=dir.resolve("commands");
    String script="#!/bin/sh\nwhile IFS= read -r line; do\n echo \"$line\" >> '"+log+"'\n case \"$line\" in\n uci) echo uciok;;\n isready) echo readyok;;\n go*) echo 'bestmove e2e4';;\n esac\ndone\n";
    Files.write(executable,script.getBytes(StandardCharsets.UTF_8));assertTrue(executable.toFile().setExecutable(true));
    StockfishEngine engine=new StockfishEngine(executable.toString(),roll(0));
    try{
      EnginePosition position=EnginePosition.from(new ChessGame());engine.search(position,BotDifficulty.EASY,60000);
      String commands=new String(Files.readAllBytes(log),StandardCharsets.UTF_8);
      assertTrue(commands.contains(BotDifficulty.EASY.go(60000)+" searchmoves "));
      Files.write(log,new byte[0]);engine.search(position,BotDifficulty.NORMAL,60000);
      assertFalse(new String(Files.readAllBytes(log),StandardCharsets.UTF_8).contains("searchmoves"));
    }finally{engine.close();}
  }
}
