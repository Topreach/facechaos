# My Recordings and Language - version 67

Face Chaos 0.21.1, version code 67, package `com.facechaos.mobile`. Final bundle prepared 4 November 2026.

## Changes

- Illustrated Canvas recordings sheet, recycled native play cards, dates and sizes, and asynchronously loaded durations. Newest recordings appear first. Metadata work stops when the sheet closes.
- Illustrated empty state with instructions for recording a round.
- Canvas clip preview retaining native video playback, Save to Gallery, Share and confirmed Delete. Game resume only applies to the same game session.
- Language cards with a visible current selection and persistent Cancel/Apply footer. Selecting a card does not immediately change the language. Applying a change keeps the existing restart notice in the game; the welcome screen explains device language instead.
- Same picker on the welcome and camera screens, with translated text in English, Arabic, French, Spanish and Portuguese and native RTL handling.

## Verification

100 native Canvas panel layouts passed on Samsung across five languages, compact/tablet sizes and large text. Interaction checks covered staged language selection/apply callbacks, recording selection, empty state and existing Look/Share callbacks. Real phone flow checks passed language cancellation preserving locale, opening/closing recordings, synthetic MP4 playback in the new review panel, and opening/cancelling Share without posting or deleting clips. Existing player recordings were not used or changed. UI-only renders were visually inspected.

36/36 plain-Java rule suites passed. Release build completed with lint 0 errors and 47 warnings. Bundletool and payload checks passed: identity/version, non-debuggable, target API 36, model, R8 mapping/optimization, bundled translations, 16 KB packaging, four native ABI layouts and private-key exclusion. 133 payload entries verified against the existing upload certificate. The optimized version 67 test app was installed separately on Samsung on 4 November; the Play-installed app was preserved. Final optimized Samsung smoke checks also passed face-model loading, synthetic video pause/resume/decode, 75 game/language renders and 20 welcome layouts/navigation callbacks.

## Upload

Use `dist/FaceChaos-0.21.1-67-play.aab` (34,742,248 bytes), rather than the older version 66 bundle. This task does not upload to Play Console.

SHA-256: `c5ae256b487727f6614ea502f9c1acf38625248f01bfdc9607f7cd0ffd6df4fa`

Latest teeth corrections are included; real-player teeth detection is still awaiting confirmation. Monetization remains inactive. Previously stripped native libraries may still cause Play's debug-symbol warning.

## Release notes

```text
<en-US>
Discover redesigned My Recordings and Language panels. Browse recordings with clearer play cards, dates and durations, and use the refreshed video preview to save or share. Choose a language with a visible selection and Apply button, including right-to-left Arabic support.
</en-US>
```
