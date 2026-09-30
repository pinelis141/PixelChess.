package com.pixelchess.app;

/** Versioned checkpoint; replay restores rules and history before replacing a live game. */
final class MatchSnapshot {
  static final int VERSION=3;
  final String gameId,transcript;
  final int minutes;
  final long whiteMs,blackMs;
  MatchSnapshot(String gameId,int minutes,long whiteMs,long blackMs,String transcript){
    if(gameId==null||!gameId.matches("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}")||minutes<1||minutes>180
        ||whiteMs<0||blackMs<0||whiteMs>minutes*60000L||blackMs>minutes*60000L||(whiteMs==0&&blackMs==0)
        ||transcript==null||transcript.length()>60000||!validTranscript(transcript))
      throw new IllegalArgumentException("Checkpoint inválido");
    this.gameId=gameId;this.minutes=minutes;this.whiteMs=whiteMs;this.blackMs=blackMs;this.transcript=transcript;
  }
  private static boolean validTranscript(String text){
    if(text.equals("-"))return true;
    for(String move:text.split(";",-1))if(!move.matches("[0-7]{4}[-QRBN]"))return false;
    return true;
  }
  String encode(){return "STATE,"+VERSION+","+gameId+","+minutes+","+whiteMs+","+blackMs+","+transcript;}
  static MatchSnapshot parse(String line,String expectedId){
    if(line==null||line.length()>61000)throw new IllegalArgumentException("Checkpoint ausente ou extenso");
    String[] a=line.split(",",-1);
    if(a.length!=7||!a[0].equals("STATE")||!a[1].equals(""+VERSION)||expectedId!=null&&!expectedId.equals(a[2]))
      throw new IllegalArgumentException("Versão ou partida incompatível");
    return new MatchSnapshot(a[2],Integer.parseInt(a[3]),Long.parseLong(a[4]),Long.parseLong(a[5]),a[6]);
  }
  ChessGame restore(){
    ChessGame g=ChessGame.replay(transcript);
    if(!g.gameOver()&&(whiteMs==0||blackMs==0)){
      boolean loserWhite=whiteMs==0,winnerWhite=!loserWhite;
      if(g.canPossiblyMate(winnerWhite))g.finish("TEMPO • "+(loserWhite?"PRETAS":"BRANCAS")+" VENCEM");
      else g.finish("EMPATE • TEMPO SEM MATERIAL PARA MATE");
    }
    return g;
  }
}
