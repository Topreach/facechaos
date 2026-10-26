# Face Chaos 0.20.0 — Google Play build

Prepared 26 October 2026. Version code **53**, package **com.facechaos.mobile**. Minimum Android 8.0/API 26; target/compile API 36.

## Files

Upload **FaceChaos-0.20.0-53-play.aab** to Google Play Console. It is the signed Android App Bundle, not the APK, source ZIP or certificate.

| File | Bytes | Purpose |
|---|---:|---|
| FaceChaos-0.20.0-53-play.aab | 35,176,058 | Google Play upload |
| FaceChaos-Android-0.20.0-release.apk | 60,920,271 | Optional signed sideload build |

Files and checksums are provided on the GitHub release and in the local `dist` directory. Source and documentation are in the matching `v0.20.0` tag.

## Changes

- Emoji Gates now includes eyes wide open and face squeeze alongside mouth opening, closed-lip smile and visible teeth.
- Matching golden characters, gate artwork, illustrated instructions and calibration counters for the new gestures.
- Visible teeth can include either tooth row; a closed-lip smile remains a separate control.
- Confirmation before switching from an active game. No preserves the current session; Yes closes it and finishes its recording before another game is selected.
- Fresh sessions prevent previous-game progress and late callbacks affecting a new game.
- Recording startup/pause handling fixes, preserving the existing circular play/pause/record controls.
- Core interface in English, Arabic, French, Spanish and Portuguese.

## Validation

- Signed release AAB/APK and release lint completed successfully: **0 errors, 33 warnings**.
- **33/33** plain-Java controller/game suites passed. Run `python android-app/tools/check-rules.py` to reproduce them.
- Android bundletool validation passed. Final manifest has package/version 53/0.20.0, target 36 and a non-debuggable application.
- Every one of the **56** bundle payload entries was cryptographically verified against the existing public upload certificate.
- Release APK signature verification and 16 KB ZIP alignment passed.
- The bundle requests 16 KB native packaging; all four native ABI LOAD/RELRO layouts pass the included alignment checker.
- Packaged model and original artwork/audio are present; private signing files are absent.
- Same-code Samsung SM-A176B development checks confirmed the bundled model features and all 25 gate/stage renders with two matching tutorials. Earlier checks cover recording, session isolation, speech-provider handling, microphone acquisition and audio decoding.

This is a signed production build, not a claim of perfect facial recognition. The user confirmed the teeth gate passed in the prior build. The newly added wide-eye and face-squeeze gestures still require real-player validation; automated/synthetic checks and static portrait tests cannot establish accuracy for all faces or lighting. A release APK was not installed over the differently signed debug phone app, preserving its data. No runtime test on a 16 KB device was performed. Release minification remains disabled to preserve the tested runtime behaviour.

## Google Play handoff

Create a new release in the appropriate Play Console track, upload the AAB, paste the release notes, review the pre-launch results and submit for review. This preparation does not upload or publish the app to Google Play. Store setup, account-specific testing/review requirements and Data safety answers remain managed in Play Console.

Privacy policy: https://topreach.github.io/facechaos/privacy.html

Recordings are silent. Animal Choir recognises rhythm, not animal identity; Melody Hop uses level/timing rather than pitch. Country answers remain English and may use the phone speech provider online. In-app ratings are local feedback, not public Play reviews. Secondary diagnostic text is not fully localised.

Keep `android-app/release-signing/` and its passwords private and backed up. The existing upload identity was reused. Do not uninstall the debug phone app merely to sideload the release APK without considering app-local recordings, settings and records.

## SHA-256

```text
35be2d5839bd6e7ef82515b55324201485a55eb88205501c5700bd259db4422f  FaceChaos-0.20.0-53-play.aab
b5d6c6f1b268d16fab8f8bfa9c34117feffc54c961a8ef14a4022f659ddf7e26  FaceChaos-Android-0.20.0-release.apk
```

Official requirement references checked for this handoff: [Google Play target API](https://support.google.com/googleplay/android-developer/answer/11926878), [Android 16 KB page-size guidance](https://developer.android.com/guide/practices/page-sizes).
