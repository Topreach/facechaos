# Face Chaos Android

Current release: **0.20.0 (53)**. Package: `com.facechaos.mobile`. Minimum SDK 26, target/compile SDK 36. Java 17+, Android Gradle Plugin 8.13.0, Gradle wrapper 8.14.3.

## Development build

Install Android SDK Platform 36 and build tools. Set `ANDROID_HOME` or create a local, untracked `local.properties` containing `sdk.dir` for your SDK path. Set `JAVA_HOME` to a suitable JDK.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Release signing

Reuse the existing upload identity for every update. Supply the following private local files, which are excluded from Git:

```text
release-signing/upload.properties
release-signing/<existing-upload-keystore>
```

The properties file needs `storeFile` (relative to the android-app project), `storePassword`, `keyAlias` and `keyPassword`. Do not place real values in documentation, source control, command output or GitHub releases. The build fails when release signing is missing; do not regenerate an existing upload identity.

```powershell
.\gradlew.bat :app:bundleRelease :app:assembleRelease :app:lintRelease
.\gradlew.bat -I tools/validate-play.gradle validatePlayBundle dumpPlayManifest dumpPlayConfig
```

Play upload: `app/build/outputs/bundle/release/app-release.aab`. The APK is for sideloading; upload the AAB to Play Console. Release builds are non-debuggable. Minification is currently disabled to retain the tested runtime behaviour.

`tools/verify-play-payload.py` verifies version/package identity, SDK, model packaging, ZIP integrity and native 16 KB LOAD/RELRO alignment after manifest/config dumps. `tools/VerifyBundleSignature.java` verifies every payload entry against the public upload certificate.

Do not uninstall a debug phone build to install a differently signed release over it without exporting recordings and considering loss of app-local data. Building a release does not replace the current phone app.

## Checks

`checks/*.java` are plain-Java rule/controller checks. Android tests use `com.facechaos.mobile.SceneCheck` and support focused arguments including `faceOnly`, `eyeGatesOnly`, `recordingOnly`, `playbackOnly`, `isolationOnly`, `speechOnly` and `hardwareOnly`, each set to `1`. Hardware checks may use the phone microphone; game-session checks can save local test clips. Read the check documentation before running them on a player's phone.

The public fixture and model download URLs/hashes are recorded in `face-model-manifest.json`. Original synthesized audio provenance is in [audio/README.md](audio/README.md). Camera frames and microphone samples are transient; clips are explicitly recorded silent video. Speech-provider fallback may process country-name answers online.

[Current release verification](../docs/RELEASE_0200.md) ? [Gesture implementation and artwork provenance](../docs/emoji-gates-eye-controls.md) ? [Recognition audit](../docs/recognition-readiness-audit.md)
