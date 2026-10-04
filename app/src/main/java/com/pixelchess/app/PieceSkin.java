package com.pixelchess.app;
import android.content.Context;
import android.content.SharedPreferences;
/** Piece art is selected independently of the board theme. */
enum PieceSkin {
  CLASSIC("classic","TRADICIONAL","Peças tradicionais"),
  MEDIEVAL("medieval","MEDIEVAL","Personagens medievais originais, animados"),
  FOREST("floresta","FLORESTA","Personagens da Floresta Ancestral, animados");
  final String id,label,description;
  PieceSkin(String id,String label,String description){this.id=id;this.label=label;this.description=description;}
  static PieceSkin from(String id){
    for(PieceSkin skin:values())if(skin.id.equals(id))return skin;
    return CLASSIC; // Retire obsolete generated Guardian/Obsidian choices.
  }
  static PieceSkin load(Context c){
    SharedPreferences pref=c.getSharedPreferences("pixel_piece_skins",Context.MODE_PRIVATE);
    return from(pref.getString("selected",CLASSIC.id));
  }
  void save(Context c){
    c.getSharedPreferences("pixel_piece_skins",Context.MODE_PRIVATE).edit().putString("selected",id).apply();
  }
}