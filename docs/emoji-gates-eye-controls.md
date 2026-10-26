# Emoji Gates: five expressions — 0.20.0 (53)

Emoji Gates now uses these five cues in every stage, with a different five-cue order per stage:

1. Open mouth.
2. Closed-lip smile.
3. Show teeth (the user confirmed the previous version's teeth gate passed).
4. Eyes wide open: open both eyes wide with a relaxed mouth for a brief moment, then relax.
5. Face squeeze: gently scrunch both eyes shut with the cheeks, brow or nose for about half a second, then relax. Ordinary blinking and eye closure alone are not sufficient.

The new controls are added to Emoji Gates only. Blink remains a separate detector action used by Bubble Garden; neither new action substitutes for that blink or for the existing three Face Memory expressions. Normal game switching, recording controls and game size remain unchanged.

## Recognition and calibration

The bundled MediaPipe model supplies bilateral eyeWide, eyeBlink, eyeSquint, cheekSquint, browDown and noseSneer scores. Both eyes must widen for the wide-eyes signal. Face squeeze combines bilateral eye closure with a facial scrunch signal. It requires 350 ms stability; wide eyes require 160 ms stability. One-shot/rearm and tracking-loss rules still apply. Neutral calibration includes a personal eye-wide baseline, and sensitivity applies to both new gestures.

Face/head check has separate Eyes wide open and Face squeeze selections, counters and live measurement values. Keep the movement comfortable; return to neutral between attempts. Real-player accuracy is still to be verified on the user's face; synthetic checks do not prove universal accuracy.

## Artwork

Matching golden 3D characters and turquoise/coral gates were generated using the built-in image_gen tool, with the original emoji atlas as a style reference. The new characters and tutorial illustrations demonstrate the requested expressions. Original mouth/smile/teeth sprites remain intact.

- Saved atlas: `android-app/app/src/main/res/drawable-nodpi/emoji_extra_atlas.png`
- Consuming sprite rectangles: `GateExtraArt.java`.
- Final generation prompt: [emoji-extra-prompt.txt](../store-assets/emoji-extra-prompt.txt).

## Verification

- All 33 host check suites pass, including the full five-expression Emoji Gates detector-to-coins playthrough.
- EyeReactionsCheck covers four input cadences, held gesture/rearm/loss, short strong blinks, eye closure without scrunch, wrong-expression rejection and isolation from Bubble Garden's blink.
- GameSectionsCheck verifies all five expressions occur in every stage.
- On Samsung SM-A176B the actual bundled model exposes all required feature names, and its production threaded adapter returns measurements.
- All 25 combinations of stage and gate/actor pose, plus two matching illustrated tutorials, render on Android without a camera or microphone. Saved synthetic render samples were visually inspected.
- Android debug and instrumentation builds pass; lint has no errors.

The new reactions still need the user to try wide eyes and a comfortable face squeeze in actual gameplay. They are not claimed production-validated merely because model and synthetic tests pass.

Primary feature-name reference: [MediaPipe blendshape definitions](https://github.com/google-ai-edge/mediapipe/blob/master/mediapipe/tasks/cc/vision/face_landmarker/face_blendshapes_graph.cc).
