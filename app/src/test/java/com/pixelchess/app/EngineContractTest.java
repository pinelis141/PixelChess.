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
    assertEquals(5,configs.size());assertEquals(0,BotDifficulty.EASY.skill);assertEquals(4,BotDifficulty.NORMAL.skill);assertEquals(0,BotDifficulty.NORMAL.elo);assertEquals(0,BotDifficulty.MAXIMUM.elo);assertEquals(20,BotDifficulty.MAXIMUM.skill);
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
  @Test public void allDifficultiesApplyDifferentUciSettingsToTransport()throws Exception{
    Path dir=Files.createTempDirectory("uci-levels");Path exe=dir.resolve("engine"),log=dir.resolve("commands");
    String script="#!/bin/sh\nwhile IFS= read -r line; do\n echo \"$line\" >> '"+log+"'\n case \"$line\" in\n uci) echo uciok;;\n isready) echo readyok;;\n go*) echo 'bestmove e2e4';;\n esac\ndone\n";
    Files.write(exe,script.getBytes(java.nio.charset.StandardCharsets.UTF_8));assertTrue(exe.toFile().setExecutable(true));
    StockfishEngine engine=new StockfishEngine(exe.toString());
    try{
      for(BotDifficulty d:BotDifficulty.values()){
        Files.write(log,new byte[0]);engine.search(EnginePosition.from(new ChessGame()),d,60000);
        String commands=new String(Files.readAllBytes(log),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(commands.contains("setoption name UCI_LimitStrength value "+(d.elo>0)));
        assertTrue(commands.contains("setoption name Skill Level value "+d.skill));
        if(d.elo>0)assertTrue(commands.contains("setoption name UCI_Elo value "+d.elo));
        assertTrue(commands.contains(d.go(60000)));
      }
    }finally{engine.close();}
  }
  @Test public void deadEngineFailsRatherThanWaitingIndefinitely()throws Exception{
    Path exe=Files.createTempFile("uci-dead","");Files.write(exe,"#!/bin/sh\nexit 0\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));assertTrue(exe.toFile().setExecutable(true));
    StockfishEngine engine=new StockfishEngine(exe.toString());long start=System.nanoTime();
    try{engine.search(EnginePosition.from(new ChessGame()),BotDifficulty.NORMAL,60000);fail();}
    catch(IOException expected){assertTrue(System.nanoTime()-start<java.util.concurrent.TimeUnit.SECONDS.toNanos(3));}
    finally{engine.close();}
  }
  @Test public void closingEngineInterruptsHungHandshakeAndKillsProcessAndReader()throws Exception{
    Path exe=Files.createTempFile("uci-hung","");Files.write(exe,"#!/bin/sh\nwhile IFS= read -r line; do :; done\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));assertTrue(exe.toFile().setExecutable(true));
    StockfishEngine engine=new StockfishEngine(exe.toString());java.util.concurrent.ExecutorService executor=java.util.concurrent.Executors.newSingleThreadExecutor();
    java.lang.reflect.Field field=StockfishEngine.class.getDeclaredField("process");field.setAccessible(true);
    try{
      java.util.concurrent.Future<String> request=executor.submit(()->engine.search(EnginePosition.from(new ChessGame()),BotDifficulty.NORMAL,60000));
      long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(2);Process process;
      do{process=(Process)field.get(engine);if(process==null)Thread.sleep(5);}while(process==null&&System.nanoTime()<deadline);
      assertNotNull(process);engine.close();assertTrue(process.waitFor(2,java.util.concurrent.TimeUnit.SECONDS));
      try{request.get(2,java.util.concurrent.TimeUnit.SECONDS);fail();}catch(java.util.concurrent.ExecutionException expected){}
      java.lang.reflect.Field rf=StockfishEngine.class.getDeclaredField("reader");rf.setAccessible(true);Thread reader=(Thread)rf.get(engine);
      reader.join(2000);assertFalse(reader.isAlive());
      java.lang.reflect.Field wf=StockfishEngine.class.getDeclaredField("watchdog");wf.setAccessible(true);
      java.util.concurrent.ExecutorService watchdog=(java.util.concurrent.ExecutorService)wf.get(engine);
      assertTrue(watchdog.awaitTermination(2,java.util.concurrent.TimeUnit.SECONDS));
    }finally{engine.close();executor.shutdownNow();}
  }
  @Test public void nonResponsiveEngineHasBoundedHandshake()throws Exception{
    Path exe=Files.createTempFile("uci-timeout","");Files.write(exe,"#!/bin/sh\nwhile IFS= read -r line; do :; done\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));assertTrue(exe.toFile().setExecutable(true));
    StockfishEngine engine=new StockfishEngine(exe.toString());long start=System.nanoTime();
    try{engine.search(EnginePosition.from(new ChessGame()),BotDifficulty.NORMAL,60000);fail();}
    catch(IOException expected){assertTrue(System.nanoTime()-start<java.util.concurrent.TimeUnit.SECONDS.toNanos(12));}
    finally{engine.close();}
  }

}
