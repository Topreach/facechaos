# Optimization and language delivery - version 56

Version `0.21.0-setup.2`, package `com.facechaos.mobile`. Built on the existing quality-fix/billing-setup branch. Purchases, premium locks, ads and watermark removal remain inactive.

## Release optimization

Previous releases explicitly disabled minification. Release builds now enable R8 shrinking, optimization and obfuscation using `proguard-android-optimize.txt`, plus resource shrinking. Mapping data is embedded in the AAB for Google Play crash deobfuscation.

Compatibility rules preserve the MediaPipe native bridge, task callbacks, protobuf field access and Flogger stack-based logging. Only three absent optional library types have individual warning exceptions. No blanket warning suppression is used. The optimized verification run caught a Flogger initialization failure; its classes are retained to protect the native recognition path.

Google Play evaluates the new uploaded artifact. Updating source code does not change the optimization scores for version 54.

The final release embeds R8 8.13.6 metadata confirming shrinking, optimization and obfuscation enabled, with debug mode disabled. Its `noObfuscationPercentage`, `noOptimizationPercentage` and `noShrinkingPercentage` values are 55.27%, 55.46% and 55.39% respectively (corresponding enabled coverage about 44.5-44.7%). These are local embedded values; the Play Console result must be checked after upload. Do not claim 100% coverage: JNI and logging compatibility require preserved code.

Uncompressed DEX size decreased from **12,217,292 bytes** in version 55 to **1,447,172 bytes** in version 56, an **88.2% reduction**. This measures compiled-code size, not total memory consumption or a measured gameplay frame-rate improvement.

## Languages

The default remains **Use phone language**. An English phone correctly displays English. Supported interfaces are English, Arabic, French, Spanish and Portuguese; an explicit app-language selection overrides the phone preference.

Language splitting is disabled, so all supported translations are available immediately and offline. On the Play-installed version 55, Arabic resources were installed but French was initially absent; changing to French caused Play to download a new language split. The first rendered frame still displayed English before that download completed.

Fixed the untranslated secondary Games/Clip button and Face/head controls label. The translation source now includes later-added gate instructions and game-switch confirmation strings, preventing regeneration from deleting them. Brand/game names and some secondary diagnostics remain English; Flag World Tour speech answers still use English.

## Play Games Sidekick

Sidekick is a separate Play Console release option, not a DEX optimization or game-control defect. To test it, create an internal or closed testing release and select **Sidekick is on by default**. On eligible test devices, enable Play Games Sidekick in Play Store developer options. This setting was not changed in the user's Play Console.

Reference: [Sidekick setup and requirements](https://developer.android.com/games/pgs/play-games-sidekick), [DEX optimization metrics](https://developer.android.com/google/play/vitals/code-optimization), [app languages](https://developer.android.com/guide/topics/resources/app-languages).

## Isolated optimized verification

Build `:app:assembleVerification -PoptimizedCheck`. This optional variant uses the release optimization configuration, a debug signing key and the separate package `com.facechaos.mobile.verify`. It does not replace or read the Play-installed app's data. Its synthetic fixture, test activity and temporary video are excluded from the production AAB.

The verification activity exercises the optimized face-recognition adapter with a public portrait, synthetic MP4 recording with duplicate pause/resume and decoding, and 75 game/language renders. No camera or microphone is used by this activity.

Final Samsung result: **PASS** for optimized model loading/inference, expression adapter, recording startup/pause/resume/finalization/decoding, and 75 game/language renders. The temporary verification copy was removed after testing; the user's Play-installed app and records were preserved.

Other checks: 35/35 rule suites; release lint 0 errors and 32 warnings; bundletool validation; bundled languages with no language splitting; no verification fixture or activity in production; upload signature verified for all 133 payload entries; all four native ABIs meet the existing 16 KB alignment checks. MainActivity has no orientation restriction and remains resizable; Fragment 1.9.1 remains selected.

Local signed bundle: `dist/FaceChaos-0.21.0-56-optimized-play.aab`.

SHA-256: `7d872fba550fb656387cf1293c37df70c9cd376e6b7cbda612a061c68020a90a`.

Upload version 56 to the appropriate Play testing track, then install through its testing link. The Play-installed version 55 cannot be replaced in place by the locally debug-signed verification build. Internal App Sharing alone does not replace a release on a testing/production track.
