# Instructions immediately above the game - 0.13.1

Section names such as Dune Doorways and the live action/comment now share one compact strip immediately above the lower game area. The upper/central selfie area stays clear. Native play, pause, record and menu controls remain below the game. This follows the corrected request: above the game, rather than below it or near the player's face.

All games use the same ordered slots. Artwork scales uniformly to fit its slot without stretching character proportions. Older top-of-camera reaction cards and the separate section-name badge are suppressed. Hearts, stage and coin HUD remain at the top edge. Gold action-window highlighting, sound calibration state, live sound meters, face-tracking guidance, correct-answer feedback and retry/next-stage wording are provided in the instruction strip. Firefly Glow retains its highlighted steady-sound target band. Word-quiz speech status remains separate from volume meters.

Face Memory taps and Head Dodge touch lanes map back through the exact game transform, so moved artwork still has matching hit coordinates. Rules, saved coins, stage progress, recognition thresholds and speech-provider selection are unchanged.

## Verification - 25 October 2026

Android build and lint passed. OverlayLayoutCheck passed 180 combinations of phone height, control reservation and game type, verifying instruction/game/control ordering and inverse touch coordinates. The native Samsung suite passed normal/compact scenes, memory card/gap hit tests, face-ready/lost scenes, stage results and voice-course views. Pixel checks verify the central selfie from 20% to 47% of screen height remains clear and the instruction strip exists at its calculated position. The synthetic video encoder, bundled sound assets and face/head model checks also passed without using the owner's camera or microphone. Dune Doorways was visually reviewed at 720x1560 and 720x1280.

Installed Android package: versionCode 29 / 0.13.1-layout. APK: `dist/FaceChaos-Android-0.13.1.apk`.
