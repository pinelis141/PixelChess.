package com.pixelchess.app;

/** Pure geometry shared by scene art, torch anchors, clocks and board hit testing. */
final class SceneGeometry {
  float left, top, size, upperEnd, lowerStart, height;
  void update(float width,float height,float density,boolean scenic) {
    this.height=height;
    float gutter=18*density;
    size=Math.max(1,width-2*gutter);
    if(scenic) size=Math.max(1,Math.min(size,height-300*density));
    left=(width-size)/2;
    top=Math.max(150*density,(height-size)/2-30*density);
    float margin=Math.min(14f*1.07f*density,Math.max(0,left-3*density));
    upperEnd=top-margin;
    lowerStart=top+size+margin;
  }
  float mapY(float y,BoardTheme.Scene scene) {
    if(y<=scene.boardTop) return y/scene.boardTop*upperEnd;
    if(y>=scene.boardBottom) return lowerStart+(y-scene.boardBottom)/(1-scene.boardBottom)*(height-lowerStart);
    return upperEnd+(y-scene.boardTop)/(scene.boardBottom-scene.boardTop)*(lowerStart-upperEnd);
  }
}
