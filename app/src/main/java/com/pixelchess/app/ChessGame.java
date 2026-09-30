package com.pixelchess.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/** Pure chess state and rules. No Android, rendering, clock or transport dependencies. */
public final class ChessGame {
  private final String[][] board=new String[8][8];
  private final HashMap<String,Integer> repetitions=new HashMap<>();
  private final ArrayList<String> history=new ArrayList<>();
  private final ArrayList<String> moves=new ArrayList<>();

  private boolean whiteTurn=true;
  private boolean whiteKingMoved,blackKingMoved,whiteRookA,whiteRookH,blackRookA,blackRookH;
  private boolean gameOver;
  private int epRow=-1,epCol=-1,halfmove;
  private String status="BRANCAS JOGAM";

  private static final String BACK="rnbqkbnr";

  public ChessGame(){reset();}

  public void reset(){
    for(int r=0;r<8;r++)Arrays.fill(board[r],null);
    for(int i=0;i<8;i++){
      board[0][i]=""+BACK.charAt(i);
      board[1][i]="p";
      board[6][i]="P";
      board[7][i]=(""+BACK.charAt(i)).toUpperCase();
    }
    whiteTurn=true;
    whiteKingMoved=blackKingMoved=whiteRookA=whiteRookH=blackRookA=blackRookH=false;
    gameOver=false;
    epRow=epCol=-1;
    halfmove=0;
    repetitions.clear();
    history.clear();
    moves.clear();
    status="BRANCAS JOGAM";
    recordPosition();
  }

  public String pieceAt(int row,int col){return inside(row,col)?board[row][col]:null;}
  public boolean whiteTurn(){return whiteTurn;}
  public boolean gameOver(){return gameOver;}
  public String status(){return status;}
  public List<String> history(){return java.util.Collections.unmodifiableList(history);}
  public boolean hasHistory(){return !history.isEmpty();}

  public void forceTurn(boolean white){whiteTurn=white;}
  public void finish(String terminalStatus){gameOver=true;status=terminalStatus;}

  public static boolean isWhitePiece(String piece){
    return piece!=null&&Character.isUpperCase(piece.charAt(0));
  }

  public boolean isLegal(int r1,int c1,int r2,int c2){
    String q=pieceAt(r1,c1);
    if(q==null||!pseudo(r1,c1,r2,c2))return false;
    boolean side=isWhitePiece(q);

    String target=board[r2][c2],from=board[r1][c1];
    String epPawn=null;
    int epPawnRow=-1;
    if(Character.toLowerCase(from.charAt(0))=='p'&&c1!=c2&&target==null){
      epPawnRow=r1;
      epPawn=board[epPawnRow][c2];
      board[epPawnRow][c2]=null;
    }

    board[r2][c2]=from;
    board[r1][c1]=null;
    boolean bad=inCheck(side);
    board[r1][c1]=from;
    board[r2][c2]=target;
    if(epPawnRow>=0)board[epPawnRow][c2]=epPawn;
    return !bad;
  }

  public boolean move(int r1,int c1,int r2,int c2,String promotion){
    String source=pieceAt(r1,c1);
    if(gameOver||source==null||isWhitePiece(source)!=whiteTurn||!isLegal(r1,c1,r2,c2))return false;
    boolean promotes=source.equalsIgnoreCase("p")&&(r2==0||r2==7);
    String chosen=promotion==null||promotion.equals("-")?"Q":promotion.toUpperCase(java.util.Locale.ROOT);
    if(promotes&&(chosen.length()!=1||"QRBN".indexOf(chosen.charAt(0))<0))return false;
    if(!promotes&&promotion!=null&&!promotion.equals("-"))return false;

    String q=board[r1][c1];
    boolean side=isWhitePiece(q);
    char type=Character.toLowerCase(q.charAt(0));
    String captured=board[r2][c2];
    boolean enPassantCapture=type=='p'&&c1!=c2&&captured==null&&r2==epRow&&c2==epCol;
    String notation=square(r1,c1)+(captured!=null||enPassantCapture?"x":"-")+square(r2,c2);

    if(enPassantCapture)board[r1][c2]=null;
    if(type=='k'&&Math.abs(c2-c1)==2){
      int rookCol=c2>c1?7:0,newCol=c2>c1?5:3;
      board[r2][newCol]=board[r2][rookCol];
      board[r2][rookCol]=null;
    }

    board[r2][c2]=q;
    board[r1][c1]=null;

    if(type=='p'&&(r2==0||r2==7)){
      String p=side?chosen:chosen.toLowerCase(java.util.Locale.ROOT);
      board[r2][c2]=p;
      notation+="="+Character.toUpperCase(p.charAt(0));
    }

    epRow=epCol=-1;
    if(type=='p'&&Math.abs(r2-r1)==2){
      epRow=(r1+r2)/2;
      epCol=c1;
    }

    if(type=='k'){
      if(side)whiteKingMoved=true; else blackKingMoved=true;
    }
    if(type=='r'){
      if(side&&r1==7&&c1==0)whiteRookA=true;
      if(side&&r1==7&&c1==7)whiteRookH=true;
      if(!side&&r1==0&&c1==0)blackRookA=true;
      if(!side&&r1==0&&c1==7)blackRookH=true;
    }
    if(captured!=null&&Character.toLowerCase(captured.charAt(0))=='r'){
      if(r2==7&&c2==0)whiteRookA=true;
      if(r2==7&&c2==7)whiteRookH=true;
      if(r2==0&&c2==0)blackRookA=true;
      if(r2==0&&c2==7)blackRookH=true;
    }

    moves.add(""+r1+c1+r2+c2+(promotes?chosen:"-"));
    history.add((history.size()/2+1)+(side?".":"...")+notation);
    if(type=='p'||captured!=null)halfmove=0;else halfmove++;
    whiteTurn=!whiteTurn;

    int repeated=recordPosition();
    boolean check=inCheck(whiteTurn),any=false;
    outer:
    for(int a=0;a<8;a++)for(int d=0;d<8;d++){
      String z=board[a][d];
      if(z==null||isWhitePiece(z)!=whiteTurn)continue;
      for(int e=0;e<8;e++)for(int f=0;f<8;f++){
        if(isLegal(a,d,e,f)){any=true;break outer;}
      }
    }

    if(!any){
      gameOver=true;
      status=check?"XEQUE-MATE • "+(whiteTurn?"PRETAS":"BRANCAS")+" VENCEM":"EMPATE • AFOGAMENTO";
    }else if(insufficientMaterial()){
      gameOver=true;status="EMPATE • MATERIAL INSUFICIENTE";
    }else if(halfmove>=100){
      gameOver=true;status="EMPATE • REGRA DOS 50 LANCES";
    }else if(repeated>=3){
      gameOver=true;status="EMPATE • REPETIÇÃO TRIPLA";
    }else{
      status=(whiteTurn?"BRANCAS":"PRETAS")+" JOGAM"+(check?" • XEQUE!":"");
    }
    return true;
  }

  public boolean inCheck(boolean side){
    for(int r=0;r<8;r++)for(int c=0;c<8;c++){
      String q=board[r][c];
      if(q!=null&&Character.toLowerCase(q.charAt(0))=='k'&&isWhitePiece(q)==side)
        return attacked(r,c,!side);
    }
    return false;
  }

  public String historyLine(){
    int from=Math.max(0,history.size()-4);
    StringBuilder z=new StringBuilder("JOGADAS: ");
    if(from>0)z.append("…  ");
    for(int i=from;i<history.size();i++){
      if(i>from)z.append("  ");
      z.append(history.get(i));
    }
    return z.toString();
  }

  private boolean pseudo(int r1,int c1,int r2,int c2){
    if(!inside(r2,c2)||(r1==r2&&c1==c2))return false;
    String q=board[r1][c1];
    if(q==null)return false;
    if(board[r2][c2]!=null&&isWhitePiece(q)==isWhitePiece(board[r2][c2]))return false;
    if(board[r2][c2]!=null&&Character.toLowerCase(board[r2][c2].charAt(0))=='k')return false;

    int dr=r2-r1,dc=c2-c1,ar=Math.abs(dr),ac=Math.abs(dc);
    char type=Character.toLowerCase(q.charAt(0));
    if(type=='n')return ar*ac==2;
    if(type=='k'){
      if(ar<=1&&ac<=1)return true;
      return r1==(isWhitePiece(q)?7:0)&&c1==4&&dr==0&&ac==2&&castlePossible(isWhitePiece(q),dc>0);
    }
    if(type=='p'){
      int d=isWhitePiece(q)?-1:1,start=isWhitePiece(q)?6:1;
      if(dc==0&&board[r2][c2]==null&&(dr==d||(r1==start&&dr==2*d&&board[r1+d][c1]==null)))return true;
      return ac==1&&dr==d&&(board[r2][c2]!=null||(r2==epRow&&c2==epCol));
    }
    if(type=='r')return (dr==0||dc==0)&&clear(r1,c1,r2,c2);
    if(type=='b')return ar==ac&&clear(r1,c1,r2,c2);
    if(type=='q')return (dr==0||dc==0||ar==ac)&&clear(r1,c1,r2,c2);
    return false;
  }

  private boolean attacked(int r,int c,boolean byWhite){
    for(int a=0;a<8;a++)for(int d=0;d<8;d++){
      String q=board[a][d];
      if(q==null||isWhitePiece(q)!=byWhite)continue;
      char type=Character.toLowerCase(q.charAt(0));
      int dr=r-a,dc=c-d,ar=Math.abs(dr),ac=Math.abs(dc);
      if(type=='p'&&dr==(byWhite?-1:1)&&ac==1)return true;
      if(type=='n'&&ar*ac==2)return true;
      if(type=='k'&&ar<=1&&ac<=1)return true;
      if(type=='r'&&(dr==0||dc==0)&&clear(a,d,r,c))return true;
      if(type=='b'&&ar==ac&&clear(a,d,r,c))return true;
      if(type=='q'&&(dr==0||dc==0||ar==ac)&&clear(a,d,r,c))return true;
    }
    return false;
  }

  private boolean castlePossible(boolean side,boolean kingSide){
    int r=side?7:0;
    if(side?(whiteKingMoved||(kingSide?whiteRookH:whiteRookA))
        :(blackKingMoved||(kingSide?blackRookH:blackRookA)))return false;
    int rook=kingSide?7:0;
    String rq=board[r][rook];
    if(rq==null||Character.toLowerCase(rq.charAt(0))!='r'||isWhitePiece(rq)!=side)return false;
    int step=kingSide?1:-1;
    for(int c=4+step;c!=rook;c+=step)if(board[r][c]!=null)return false;
    return !inCheck(side)&&!attacked(r,4+step,!side)&&!attacked(r,4+2*step,!side);
  }

  private boolean insufficientMaterial(){
    ArrayList<Character> pieces=new ArrayList<>();
    ArrayList<Integer> bishops=new ArrayList<>();
    for(int r=0;r<8;r++)for(int c=0;c<8;c++){
      String q=board[r][c];
      if(q==null)continue;
      char type=Character.toLowerCase(q.charAt(0));
      if(type=='k')continue;
      if(type=='p'||type=='q'||type=='r')return false;
      pieces.add(type);
      if(type=='b')bishops.add((r+c)&1);
    }
    if(pieces.isEmpty())return true;
    if(pieces.size()==1&&(pieces.get(0)=='b'||pieces.get(0)=='n'))return true;
    if(!pieces.isEmpty()&&pieces.size()==bishops.size()){
      int color=bishops.get(0);
      for(int x:bishops)if(x!=color)return false;
      return true;
    }
    return false;
  }

  private boolean clear(int r1,int c1,int r2,int c2){
    int rr=Integer.signum(r2-r1),cc=Integer.signum(c2-c1),r=r1+rr,c=c1+cc;
    while(r!=r2||c!=c2){
      if(board[r][c]!=null)return false;
      r+=rr;c+=cc;
    }
    return true;
  }

  private boolean inside(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
  private String square(int r,int c){return ""+(char)('a'+c)+(8-r);}

  private String positionKey(){
    StringBuilder key=new StringBuilder();
    for(int r=0;r<8;r++)for(int c=0;c<8;c++)key.append(board[r][c]==null?".":board[r][c]);
    key.append(whiteTurn?"w":"b")
      .append(!whiteKingMoved&&!whiteRookH?"K":"-").append(!whiteKingMoved&&!whiteRookA?"Q":"-")
      .append(!blackKingMoved&&!blackRookH?"k":"-").append(!blackKingMoved&&!blackRookA?"q":"-");
    boolean legalEp=false;
    if(epRow>=0){int from=epRow+(whiteTurn?1:-1);for(int c=epCol-1;c<=epCol+1;c+=2){
      String pawn=pieceAt(from,c);
      if(pawn!=null&&pawn.equals(whiteTurn?"P":"p")&&isLegal(from,c,epRow,epCol))legalEp=true;
    }}
    key.append(legalEp?epRow:-1).append(":").append(legalEp?epCol:-1);
    return key.toString();
  }

  /** Legal move transcript rebuilds castling, en passant, repetitions and halfmove state. */
  public String transcript(){return moves.isEmpty()?"-":String.join(";",moves);}
  public static ChessGame replay(String transcript){
    if(transcript==null||transcript.length()>60000)throw new IllegalArgumentException("Invalid move transcript");
    ChessGame restored=new ChessGame();
    if(transcript.equals("-"))return restored;
    for(String move:transcript.split(";",-1)){
      if(!move.matches("[0-7]{4}[-QRBN]"))throw new IllegalArgumentException("Malformed move");
      if(!restored.move(move.charAt(0)-'0',move.charAt(1)-'0',move.charAt(2)-'0',move.charAt(3)-'0',move.substring(4)))
        throw new IllegalArgumentException("Illegal transcript move");
    }
    return restored;
  }

  private int recordPosition(){
    String key=positionKey();
    int n=repetitions.containsKey(key)?repetitions.get(key)+1:1;
    repetitions.put(key,n);
    return n;
  }
}
