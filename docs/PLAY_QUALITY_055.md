# Play Console quality fixes - version 55

Package `com.facechaos.mobile`; version `0.21.0-setup.1` (55).
Built on the internal billing setup branch. Purchases, premium locks, ads and watermark removal remain inactive. This is not the completed monetized release.

## Changes

- Explicit Fragment 1.9.1 dependency replaces the 1.1.0 dependency brought in by Play Services through Billing 9.1.0. The release dependency report confirms 1.9.1 is selected.
- Removed the main activity's portrait lock and explicitly enabled resizing. The final bundle manifest has no orientation or aspect-ratio restriction.
- Preserve the existing portrait-phone game width. Wide windows and tablets center a bounded tall game canvas over the full camera preview, with separately bounded controls.
- Compose recordings in camera coordinates so a centered game overlay retains its placement and proportions.
- Handle window/orientation changes without replacing the game session. Pause inputs, reset the frame clock, finish an active recording, and reopen the camera after rotation. The player resumes with the circular control.
- Release verification now rejects a main-activity orientation lock or missing resizability declaration.

## Verification

- 35/35 Java rule suites, including phone/tablet/landscape/split-window sizing.
- Debug APK, instrumentation APK, signed release AAB and release lint: successful; lint reports 0 errors and 32 warnings.
- Samsung SM-A176B: 90 native game renders across six window sizes; rotation in both directions preserves the same game, score, coins and hearts and pauses safely.
- Samsung: an active recording finishes to a decodable clip when rotated; fresh camera frames resume after rotation.
- Bundled face model, blank-frame rejection, asynchronous expression adapter, head adapter and six face-aware caption/calibration scenes pass after the dependency upgrade. This does not establish accuracy for every real player's expressions.
- Bundletool validation, final manifest inspection, all 196 signed payload entries and four ABI native-library/16 KB alignment checks pass.

Tablet layouts were rendered on the Samsung; no physical tablet or foldable was available. Live fold/unfold, density-change recreation and every vendor's multi-window behavior remain device-matrix checks.

Local bundle: `dist/FaceChaos-0.21.0-55-quality-check.aab`.

SHA-256: `2f6ca431d42f66876b82c56cb66f0b1832476d395ec92851f3737b8fa185bda6`.

Upload a new bundle to the appropriate Play testing track to replace the affected version. An already-published artifact does not change when source code is fixed. Check Play's processing results for the uploaded version before promotion.

References: [Fragment releases](https://developer.android.com/jetpack/androidx/releases/fragment), [large-screen game resizability](https://developer.android.com/games/develop/multiplatform/support-large-screen-resizability).
