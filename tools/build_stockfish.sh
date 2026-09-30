#!/usr/bin/env bash
# Official source only; version, compiler, API and CPU baseline fixed for reproducibility.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
revision=edb0d9db6731067ec50ce619ff372b463bc4dd5d # official sf_19
ndk_version=28.2.13676358
sdk=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
ndk=${STOCKFISH_NDK:-$sdk/ndk/$ndk_version}
[[ -x "$ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/clang++" ]] || { echo "Install NDK $ndk_version or set STOCKFISH_NDK" >&2; exit 1; }
export PATH="$ndk/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH"
source_dir="$root/build/stockfish-source"
if [[ ! -d "$source_dir/.git" ]]; then
  mkdir -p "$source_dir"
  git -C "$source_dir" init
  git -C "$source_dir" remote add origin https://github.com/official-stockfish/Stockfish.git
fi
if ! git -C "$source_dir" cat-file -e "$revision^{commit}" 2>/dev/null; then
  git -C "$source_dir" fetch --depth 1 origin "$revision"
fi
git -C "$source_dir" checkout --detach "$revision"
[[ $(git -C "$source_dir" rev-parse HEAD) == "$revision" ]]
cd "$source_dir/src"
make net
mkdir -p "$root/build/stockfish-distribution"
manifest="$root/build/stockfish-distribution/SHA256SUMS"
sha256sum *.nnue > "$manifest"
"$root/tools/compile_stockfish.sh" "$source_dir" "$root/app/src/main/jniLibs" "$manifest"
# Corresponding source includes the exact networks and our build recipe, alongside APKs.
make clean
cp "$root/tools/compile_stockfish.sh" "$source_dir/PIXELCHESS-compile.sh"
cp "$root/docs/STOCKFISH.md" "$source_dir/PIXELCHESS-integration.md"
cat > "$source_dir/PIXELCHESS-rebuild.txt" <<'REBUILD'
Install NDK 28.2.13676358; set ANDROID_HOME to its SDK root.
From this extracted source directory (NNUE is already included):
  chmod +x PIXELCHESS-compile.sh
  ./PIXELCHESS-compile.sh "$PWD" "$PWD/android-binaries" "$PWD/SHA256SUMS"
This compiles the exact engine offline, without changing upstream sources.
See PIXELCHESS-integration.md for origin, revision, GPL and application packaging.
REBUILD
tar --exclude=.git -czf "$root/build/stockfish-distribution/stockfish-19-corresponding-source.tar.gz" -C "$source_dir" .
echo "Official Stockfish 19 ($revision), NDK $ndk_version, API 26" > "$root/build/stockfish-distribution/BUILD.txt"
