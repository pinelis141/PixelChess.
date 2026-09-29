package com.pixelchess.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Only finished, available themes belong in this registry. */
public final class BoardThemes {
  private BoardThemes() {}
  public static final BoardTheme CLASSIC = new BoardTheme("classic", "Tradicional",
      R.drawable.stone_board_pixel, 0, 0f, 0, 0x1812261f,
      new BoardTheme.Glow(BoardTheme.Effect.NONE, 0, 0f, 0f));
  public static final BoardTheme FOREST = new BoardTheme("forest", "Floresta Ancestral",
      R.drawable.forest_board, R.drawable.forest_stone_frame, 14f * 1.07f, 0x0e000000, 0,
      new BoardTheme.Glow(BoardTheme.Effect.FIREFLIES, 0xffd6df83, 0.65f, 1f), R.drawable.forest_floor, R.drawable.forest_clock_plaque,
      new BoardTheme.FrameSlices(60,75,133,149,1120,1111,1202,1187));
  public static final List<BoardTheme> ALL = Collections.unmodifiableList(Arrays.asList(CLASSIC, FOREST));
  public static BoardTheme find(String id) {
    for (BoardTheme theme : ALL) if (theme.id.equals(id)) return theme;
    return CLASSIC;
  }
  public static BoardTheme fromLegacyIndex(int index) { return index == 1 ? FOREST : CLASSIC; }
}
