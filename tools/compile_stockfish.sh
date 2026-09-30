#!/usr/bin/env bash
# Compile an already-obtained exact Stockfish source tree, including its NNUE file.
set -euo pipefail
source_dir=$(cd "${1:?source directory required}" && pwd)
output_dir=${2:?absolute output directory required}
manifest=${3:?checksum manifest required}
ndk_version=28.2.13676358
sdk=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
ndk=${STOCKFISH_NDK:-$sdk/ndk/$ndk_version}
[[ -x "$ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/clang++" ]] || { echo "Install NDK $ndk_version or set STOCKFISH_NDK" >&2; exit 1; }
export PATH="$ndk/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH"
cd "$source_dir/src"
make net
for abi in arm64-v8a armeabi-v7a x86_64; do
  case "$abi" in
    arm64-v8a) arch=armv8; compiler=aarch64-linux-android26-clang++ ;;
    armeabi-v7a) arch=armv7-neon; compiler=armv7a-linux-androideabi26-clang++ ;;
    x86_64) arch=x86-64; compiler=x86_64-linux-android26-clang++ ;;
  esac
  make clean
  make -j"${STOCKFISH_BUILD_JOBS:-2}" build ARCH="$arch" COMP=ndk CXX="$compiler" \
    EXTRALDFLAGS='-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=4096'
  llvm-strip stockfish
  mkdir -p "$output_dir/$abi"
  cp stockfish "$output_dir/$abi/libstockfish.so"
  sha256sum "$output_dir/$abi/libstockfish.so" >> "$manifest"
  # Executable PIE in nativeLibraryDir, never dlopen/JNI. No shared C++ dependency.
  llvm-readelf -h stockfish | grep -q 'DYN'
  if llvm-readelf -d stockfish | grep -q 'libc++_shared'; then exit 1; fi
done
make clean
