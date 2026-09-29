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
  public static final List<BoardTheme> ALL = Collections.unmodifiableList(Arrays.asList(CLASSIC, FOREST));
  public static BoardTheme find(String id) {
    for (BoardTheme theme : ALL) if (theme.id.equals(id)) return theme;
    return CLASSIC;
  }
  public static BoardTheme fromLegacyIndex(int index) { return index == 1 ? FOREST : CLASSIC; }
}
