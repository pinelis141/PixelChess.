#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
export PATH="$ANDROID_HOME/platform-tools:$PATH"
cleanup(){ timeout 5 adb emu kill >/dev/null 2>&1 || true; if [[ -n "${emulator_pid:-}" ]]; then kill "$emulator_pid" 2>/dev/null || true; fi; }
trap cleanup EXIT
"$ANDROID_HOME/emulator/emulator" -avd pixelchess35 -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader -memory 2048 -cores 2 > "$root/build/android-emulator.log" 2>&1 &
emulator_pid=$!
timeout 180 adb wait-for-device
booted=false
for attempt in $(seq 1 60); do
  if [[ $(timeout 5 adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r') == 1 ]]; then booted=true; break; fi
  sleep 3
done
[[ "$booted" == true ]] || { echo 'Android emulator boot timed out'; exit 1; }
timeout 180 adb install -r "$root/app/build/outputs/apk/debug/app-debug.apk"
adb shell am start -n com.pixelchess.app/.MainActivity
python3 "$root/tools/verify_android_stockfish.py"
