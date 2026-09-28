package com.pixelchess.app;

/** Stable, asymmetric paths in normalized side strips; no random allocation per frame. */
final class FireflyMotion {
  static final int COUNT = 11;
  static boolean left(int i) { return i < 6; }
  static float height(int i, double seconds) {
    double seed = i * 2.3999632297;
    return (float)(0.12 + ((i * 0.173 + 0.071) % 0.76) + 0.024 * Math.sin(seconds * (0.19 + i * 0.013) + seed));
  }
  static float distance(int i, double seconds) {
    return (float)(0.51 + 0.17 * Math.sin(seconds * (0.23 + i * 0.017) + i * 1.83));
  }
  static float brightness(int i, double seconds) {
    double wave = 0.5 + 0.5 * Math.sin(seconds * (0.62 + i * 0.047) + i * 2.3999632297);
    return (float)(0.12 + 0.88 * wave * wave);
  }
}
