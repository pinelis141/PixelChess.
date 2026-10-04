package com.pixelchess.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Visual-only piece choice: never enters the move or Bluetooth protocol. */
enum PieceSkin {
  CLASSIC("classic","TRADICIONAL","Peças originais, sem animação"),
  GUARDIANS("guardians","GUARDIÕES","Personagens com respiração discreta"),
  OBSIDIAN("obsidian","OBSIDIANA","Peças escuras de aparência mineral");

  final String id,label,description;
  PieceSkin(String id,String label,String description){this.id=id;this.label=label;this.description=description;}
  static PieceSkin from(String id){
    for(PieceSkin skin:values())if(skin.id.equals(id))return skin;
    return CLASSIC;
  }
  static PieceSkin load(Context context){
    SharedPreferences preferences=context.getSharedPreferences("pixel_piece_skins",Context.MODE_PRIVATE);
    return from(preferences.getString("selected",CLASSIC.id));
  }
  void save(Context context){
    context.getSharedPreferences("pixel_piece_skins",Context.MODE_PRIVATE).edit().putString("selected",id).apply();
  }
}
