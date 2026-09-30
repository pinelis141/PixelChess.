package com.pixelchess.app.bot;

/** Strength settings, not artificial delays. Elo targets are not calibrated mobile ratings. */
public enum BotDifficulty {
  EASY("Fácil",0,0,1,500,150),
  NORMAL("Normal",4,0,6,10000,350),
  HARD("Difícil",20,1800,14,100000,700),
  EXPERT("Especialista",20,2400,20,500000,1500),
  MAXIMUM("Máximo",20,0,64,2000000,2500);
  public final String label;
  public final int skill,elo,depth,nodes,timeMs;
  BotDifficulty(String label,int skill,int elo,int depth,int nodes,int timeMs){
    this.label=label;this.skill=skill;this.elo=elo;this.depth=depth;this.nodes=nodes;this.timeMs=timeMs;
  }
  public int budget(long remainingMs){return (int)Math.max(1,Math.min(timeMs,remainingMs/20));}
  public String go(long remainingMs){return "go depth "+depth+" nodes "+nodes+" movetime "+budget(remainingMs);}
}
