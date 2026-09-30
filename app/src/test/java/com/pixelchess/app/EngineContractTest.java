package com.pixelchess.app;

import com.pixelchess.app.bot.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EngineContractTest {
  @Test public void transcriptConversionKeepsHistoryAndCoordinates(){
    ChessGame g=BotIntegrationTest.position("e2e4","e7e5","g1f3");
    assertEquals("position startpos moves e2e4 e7e5 g1f3",EnginePosition.from(g).command);
  }
  @Test public void emptyPositionAndPromotionConversion(){
    assertEquals("position startpos",EnginePosition.from(new ChessGame()).command);
    ChessGame g=BotIntegrationTest.position("a2a4","b8c6","a4a5","h7h5","a5a6","h5h4","a6b7","c6a5","b7b8n");
    assertTrue(EnginePosition.from(g).command.endsWith("b7b8n"));
  }
  @Test public void wrongPromotionAndWrongSideAreRejected(){
    ChessGame g=new ChessGame();assertFalse(EngineMove.parse("e2e4q").legalIn(g));assertFalse(EngineMove.parse("e7e5").legalIn(g));assertTrue(EngineMove.parse("e2e4").legalIn(g));assertEquals("-",g.transcript());
  }
  @Test public void invalidCoordinatesAndTerminalSentinelsAreRejected(){
    for(String bad:new String[]{"0000","(none)","a0a1","a1i3","a1a8k","a1a8Q","e2e4\nquit",""}){
      try{EngineMove.parse(bad);fail(bad);}catch(IllegalArgumentException expected){}
    }
  }
  @Test public void difficultiesUseDistinctStrengthAndBoundedResources(){
    Set<String> configs=new HashSet<>();
    for(BotDifficulty d:BotDifficulty.values()){
      configs.add(d.skill+":"+d.elo+":"+d.go(60000));assertTrue(d.budget(60000)<=2500);assertEquals(1,d.budget(0));assertTrue(d.budget(100)<=5);
    }
    assertEquals(5,configs.size());assertEquals(0,BotDifficulty.EASY.skill);assertEquals(0,BotDifficulty.MAXIMUM.elo);assertEquals(20,BotDifficulty.MAXIMUM.skill);
  }
  @Test public void realProcessTransportHandshakeStrengthAndTeardown()throws Exception{
    Path dir=Files.createTempDirectory("uci-test");Path exe=dir.resolve("engine"),commands=dir.resolve("commands");
    String script="#!/bin/sh\nwhile IFS= read -r line; do\n echo \"$line\" >> '"+commands+"'\n case \"$line\" in\n uci) echo uciok;;\n isready) echo readyok;;\n go*) echo 'info depth 1'; echo 'bestmove e2e4 ponder e7e5';;\n esac\ndone\n";
    Files.write(exe,script.getBytes(java.nio.charset.StandardCharsets.UTF_8));assertTrue(exe.toFile().setExecutable(true));
    StockfishEngine engine=new StockfishEngine(exe.toString());
    try{
      assertEquals("e2e4",engine.search(EnginePosition.from(new ChessGame()),BotDifficulty.HARD,60000));
      String log=new String(Files.readAllBytes(commands),java.nio.charset.StandardCharsets.UTF_8);assertTrue(log.contains("setoption name UCI_LimitStrength value true"));assertTrue(log.contains("setoption name UCI_Elo value 1800"));assertTrue(log.contains("position startpos"));assertTrue(log.contains("Threads value 1"));
    }finally{engine.close();}
    java.lang.reflect.Field field=StockfishEngine.class.getDeclaredField("process");field.setAccessible(true);Process process=(Process)field.get(engine);
    assertTrue(process.waitFor(2,java.util.concurrent.TimeUnit.SECONDS));assertFalse(process.isAlive());
  }
  @Test public void unavailableExecutableFailsControlled()throws Exception{
    StockfishEngine engine=new StockfishEngine("/does/not/exist");
    try{engine.search(EnginePosition.from(new ChessGame()),BotDifficulty.EASY,60000);fail();}catch(IOException expected){}finally{engine.close();}
  }
}
