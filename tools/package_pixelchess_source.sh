#!/usr/bin/env bash
# Archive the exact tracked application source; Stockfish source is a separate artifact.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
revision=$(git -C "$root" rev-parse HEAD)
out="$root/build/pixelchess-distribution"
mkdir -p "$out"
git -C "$root" archive --format=tar --prefix=PixelChess/ "$revision" | gzip -n > "$out/pixelchess-$revision-source.tar.gz"
tar -tzf "$out/pixelchess-$revision-source.tar.gz" > "$out/CONTENTS.txt"
for required in LICENSE NOTICE README.md app/build.gradle tools/compile_stockfish.sh; do
  if ! grep -Fx "PixelChess/$required" "$out/CONTENTS.txt" >/dev/null; then
    echo "Missing corresponding-source file: $required" >&2; exit 1
  fi
done
cat > "$out/BUILD.txt" <<INFO
PixelChess source revision: $revision
Rebuild: README.md and docs/LICENSING.md in the archive.
JDK 17, Gradle 8.7, Android SDK/build-tools 35, NDK 28.2.13676358.
Export PIXELCHESS_SOURCE_REVISION=$revision before compiling from this archive.
Stockfish 19 exact source/NNUE/recipe: matching Stockfish-19-corresponding-source artifact.
INFO
sha256sum "$out/pixelchess-$revision-source.tar.gz" > "$out/SHA256SUMS"
