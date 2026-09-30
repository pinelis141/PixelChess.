# Menu music

The user supplied and approved The Quiet Gambit for the main menu.
The bundled Ogg Vorbis asset is stereo, 44.1 kHz, quality 4, 164 seconds.
It was derived from the uploaded MP3 with 0.5 gain. The original fading
ending is omitted: source seconds 164–170 overlap seconds 0–6 with a
six-second equal-power crossfade; the circular boundary is at source second 8.
The encoded peak is 0.5272; duration and boundary continuity were checked
numerically. Musical phrasing and device playback still require listening.

MenuMusicController prepares asynchronously and loops the bundled resource.
Playback requires the main menu, foreground activity, enabled music and audio
focus. Navigation away from the menu and onPause pause playback. Audio focus
loss pauses instead of mixing over another application. onDestroy releases
the player. Mute persists in SharedPreferences. Music volume is 0.55 of the
prepared asset, controlled by the device media volume.

The lower-right pixel speaker is a 48 dp accessible touch target. The current
layout, board art, rules, clocks and Bluetooth protocol are retained.
Controller tests cover menu/foreground gating and persistence of mute.
Manual device checks: menu loop, mute/relaunch, local match, Bluetooth match,
skin selector, return to menu, background/foreground and audio interruptions.
