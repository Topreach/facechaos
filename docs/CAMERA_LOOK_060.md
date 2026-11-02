# Camera appearance - version 60

Version `0.21.0-setup.6`, package `com.facechaos.mobile`.

## Player controls

The camera screen has a **Look** button below the score display. It is available before starting a game and while paused, and hidden during active play to keep the face and HUD clear.

- **Off** restores the original camera appearance.
- **Natural** applies subtle brightening and gentle softening on supported devices; the default intensity is 35% with neutral warmth.
- **Smooth** applies stronger edge-aware softening.
- **Intensity** controls the strength; **Warmth** adjusts the colour tone.
- **Compare original preview** temporarily shows the unfiltered preview. Closing the panel restores the selected appearance.
- Appearance choices persist on the device. Opening the controls pauses an active game/recording; closing preserves the prior pause state and recording session.

Controls and descriptions are translated into all five interface languages.

## Processing and compatibility

The effect adjusts the camera image without reshaping facial features. It is a bounded image-wide colour/edge-aware smoothing effect, not face-specific segmentation or blemish inpainting. Game graphics and controls are excluded from the effect.

Android 13+ uses a RuntimeShader/AGSL effect on the camera container. Android 8-12, or a device whose shader cannot be compiled, retains brightness and warmth using a colour matrix; smoothing is disabled with an explanation. The minimum Android version remains unchanged. The fallback was forced and tested on the Samsung; no physical Android 8-12 device was available.

Face/head recognition still reads the original TextureView bitmap, not the filtered parent container. No new camera processing occurs on the welcome screen. No new permission or network service is introduced.

Recorded video uses the same bounded filter math in an OpenGL ES shader on the camera texture, followed by a separate premultiplied-alpha game-overlay texture. This preserves game colours and edges. Original comparison is preview-only while the recording is paused in the settings panel. The selected look is restored before recording resumes.

## Verification

- 36/36 Java rule suites, including identity at Off/zero strength, bounded settings, invalid preference handling and older-device fallback.
- Actual GPU preview brightens a known synthetic texture and returns to original under comparison.
- The raw texture bitmap used by recognition stays byte-identical with the preview effect enabled.
- Fine-texture variance decreases under smoothing while strong contrast edges remain distinct.
- Forced basic fallback matches the preview's brightness/warmth.
- Encoded MP4 camera colours match the GPU preview within codec tolerance; opaque and half-transparent game graphics remain correctly composited and unfiltered.
- Actual camera/game test: game over, Retry, Next stage, pause/resume and a look change keep one encoder. Saved clip **17,688 ms** versus **17,525 ms** active time; final frames decode; Gallery export is byte-identical with the same duration.
- Test video/texture checks use synthetic images; the separate game-session check uses the camera in the isolated verification app. The player's Play app and recordings are not replaced.

Final optimized Samsung smoke checks pass: face inference, expression adapter, video pause/resume/decode, 75 game/language renders and 20 welcome layouts. The actual optimized Look panel shows all three modes enabled, Natural selected at 35%, neutral warmth, original comparison and Done. Release lint reports 0 errors and 37 warnings. Bundletool, final manifest/R8/language checks, four native ABI/16 KB layouts and all 133 signed payload entries pass. Monetization remains inactive.

Signed bundle: `dist/FaceChaos-0.21.0-60-camera-look-play.aab`.

SHA-256: `6bf4f43854dfdf4c1daf6999e31504a9be2a67bcc3d65640d93183c03cea4d5c`.

References: [TextureView bitmap/layer behaviour](https://developer.android.com/reference/android/view/TextureView), [RuntimeShader](https://developer.android.com/reference/android/graphics/RuntimeShader), [RenderEffect](https://developer.android.com/reference/android/graphics/RenderEffect).
