# Voice Runner — Android 0.5.0

Voice Runner now uses a peach aviator, moving cloud hoops, star collectibles and a low cloud course over the real selfie camera. Its compact prompts stay at the top, leaving the central face area clear. The same sprites illustrate the three instructions: hum to rise, stay quiet to descend, and match the star height as a hoop arrives.

Collect five of eight stars to win, with three hearts. Each star awards one saved in-game coin and Good job! feedback. A short celebration separates encounters, and rewards cannot repeat for a single hoop. Coins share the existing local balance used by Expression Escape.

## Microphone mode

Open Games, choose Voice Runner, leave Use microphone for gentle sound enabled, and press Let's play. Allow Android's microphone permission. Stay quiet for the first second, then make a gentle mmm to lift the aviator. Silence lets it drift down. The live bar shows the measured sound level. Pause and resume recalibrates the quiet level.

This measures sound volume; it does not identify words, notes or hums specifically. Background noise can also activate it. Real-phone sensitivity needs testing in the user's room. No audio is stored, streamed or added to video clips; clips remain silent. Samples are processed on-device and discarded. Microphone access stops on pause, menus, round completion, switching games or leaving the app. No background service is used.

Disable the microphone switch for touch practice: hold the screen to rise and release to descend. Denied/unavailable microphone access falls back to this mode. Other games' Android facial/sound recognition remains pending.

## Verification

Pure Java checks pass all six game win/loss paths, synthetic quiet/sound gating and release, rise/descent physics, one reward per hoop and a full held-input winning round. Native rendering tests exercise rise, descent, collection and compact viewport without camera or microphone access. Physical microphone response requires the user to start Voice Runner and grant access; synthetic tests are not a real microphone accuracy claim.

Implementation references: [AudioRecord](https://developer.android.com/reference/android/media/AudioRecord), [Android while-in-use permission restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start). Artwork and prompt: [Voice Runner atlas](../mobile-design/concepts/voice-atlas-prompt.md).
