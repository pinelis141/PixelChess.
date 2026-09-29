package com.pixelchess.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Only finished, available themes belong in this registry. */
public final class BoardThemes {
  private BoardThemes() {}
  public static final BoardTheme CLASSIC = BoardTheme.builder("classic", "Tradicional", R.drawable.stone_board_pixel)
      .overlays(0,0x1812261f)
      .build();
  public static final BoardTheme FOREST = BoardTheme.builder("forest", "Floresta Ancestral", R.drawable.forest_board)
      .frame(R.drawable.forest_stone_frame,14f*1.07f,new BoardTheme.FrameSlices(60,75,133,149,1120,1111,1202,1187))
      .background(R.drawable.forest_floor,0x25000000)
      .overlays(0x0e000000,0)
      .glow(new BoardTheme.Glow(BoardTheme.Effect.FIREFLIES,0xffd6df83,0.65f,1f))
      .clock(R.drawable.forest_clock_plaque,new BoardTheme.ClockAppearance(190f,3f,
          0xffffe7a7,0xffcecec5,0xffffe4af,0x00140d00,0x35e6bc55))
      .build();
  public static final BoardTheme CASTLE = BoardTheme.builder("castle", "Castelo Medieval", R.drawable.castle_board)
      .frame(R.drawable.castle_frame,14f*1.07f,new BoardTheme.FrameSlices(74,76,162,177,1092,1077,1180,1175))
      .background(R.drawable.castle_throne_scene,0x10000000)
      .scene(new BoardTheme.Scene(450f/1774f,1294f/1774f,135f/1774f,
          new BoardTheme.Torch(61f/887f,239f/1774f),new BoardTheme.Torch(825f/887f,239f/1774f),
          new BoardTheme.Torch(27f/887f,1335f/1774f),new BoardTheme.Torch(860f/887f,1335f/1774f)))
      .glow(new BoardTheme.Glow(BoardTheme.Effect.TORCHES,0xffffa342,0.72f,1f))
      .overlays(0x0a000000,0)
      .clock(R.drawable.castle_clock_plaque,new BoardTheme.ClockAppearance(190f,3f,
          0xffffe5b1,0xffcbd2dc,0xffffe8c0,0x00120a00,0x30d9a852))
      .build();
  public static final List<BoardTheme> ALL = Collections.unmodifiableList(Arrays.asList(CLASSIC, FOREST, CASTLE));
  public static BoardTheme find(String id) {
    for (BoardTheme theme : ALL) if (theme.id.equals(id)) return theme;
    return CLASSIC;
  }
  public static BoardTheme fromLegacyIndex(int index) { return index == 1 ? FOREST : CLASSIC; }
}
