# Face Chaos

Android camera-overlay mini-games controlled with facial expressions, head movement and sound, with touch alternatives where supported.

## Current release

**0.20.0 (version code 53)** ? package `com.facechaos.mobile` ? Android 8.0+ ? target API 36.

- 15 games with five increasingly fast stages each.
- Emoji Gates: mouth opening, closed-lip smile, visible teeth, wide eyes and face squeeze.
- Live selfie preview, calibration and sensitivity controls.
- Game-switch confirmation and separate game sessions.
- Optional silent camera/game recordings, local clip library, Gallery export and Android sharing.
- Core interface languages: English, Arabic, French, Spanish and Portuguese.

[Android build instructions](android-app/README.md) ? [Release notes and verification](docs/RELEASE_0200.md) ? [Recognition audit](docs/recognition-readiness-audit.md) ? [Privacy policy](https://topreach.github.io/facechaos/privacy.html)

## Build and test

The quality-fix branch builds version 58 (`0.21.0-setup.4`), with a [welcome homepage before camera access](docs/WELCOME_057.md) explaining how to choose among 15 games and enable recording in a game's instructions. It retains R8 release optimization, bundled translations, updated Fragment and adaptive orientation/resizing support. Billing setup remains inactive; see [quality fixes](docs/PLAY_QUALITY_055.md) and [optimization and language notes](docs/PLAY_OPTIMIZATION_056.md).

Use Android SDK 36 and Java 17 or newer. From `android-app`:

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Release signing uses a private, locally supplied upload identity. No signing keys, passwords, personal clips or device captures are included in this repository. See [release setup](android-app/README.md#release-signing).

The `android-app/checks` Java programs exercise controller algorithms and game rules. Android instrumentation checks additionally verify model loading, rendering, recordings and session transitions. Synthetic observations do not establish real-player recognition accuracy. Newly added wide-eye and face-squeeze gestures still need real-player/device validation.

Animal Choir checks sound rhythm, not animal identity. Melody Hop uses sound level/timing rather than musical pitch. Flag World Tour currently accepts English country names. Clips remain silent; in-app ratings are local feedback rather than public Play reviews. Some secondary diagnostic text remains English.

## Project structure

- `android-app/`: current native Android app, original game artwork/audio and checks.
- `docs/`: design, feature validation, release notes and historical development records.
- `recognition/`, `scripts/`, `project.godot`: earlier desktop/Godot prototype and recognition lab.
- `mobile-design/`: interactive design prototype.
- `web/privacy/`: public privacy-site source; the live site is deployed from `gh-pages`.
- `store-assets/`: asset prompts/provenance; generated export files remain local.

[Project history](docs/PROJECT_HISTORY.md) ? [Earlier Android notes](docs/ANDROID_HISTORY.md)
