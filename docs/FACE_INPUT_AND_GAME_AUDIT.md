# Android 0.9.0 - facial input and game audit

Latest release: [Android 0.10.0 racing games](RACING_GAMES_010.md) adds analog sound racing and an independent live head-steering adapter.

Earlier update: [Android 0.9.1 stage speed](STAGE_SPEED_091.md) adds a shared accelerating clock and perfect final-stage targets. The timing table below describes the original 0.9.0 curve.

## Root cause and change

Android 0.8.2 displayed the camera but had no face-analysis connection. A working preview and touch-scene checks did not establish facial recognition. This release adds actual local frame inference, independent expression decisions and adapters for all seven face-enabled games.

The sharp Camera2 preview stays unchanged. A downsampled copy of the already-oriented preview is analysed by a single background worker, at most eight requests per second with no frame backlog. No second camera session or additional rotation/mirror is applied. Frames are discarded after inference; they are not saved or transmitted. The bundled model works offline; the final merged manifest excludes Internet and network-state permissions. Face mode is optional; touch and microphone preferences remain independent.

Runtime: `com.google.mediapipe:tasks-vision:0.10.32`. Model: official version-1 float16 Face Landmarker bundle. URLs and SHA256 checksums are recorded in `android-app/face-model-manifest.json`.

Sources: [Google Android Face Landmarker guide](https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/android) and [official portrait-based model tests](https://github.com/google-ai-edge/mediapipe/blob/master/mediapipe/tasks/python/test/vision/face_landmarker_test.py).

## Expression contract

- Open mouth: calibrated jaw opening, with insufficient direct teeth evidence to classify full teeth baring.
- Closed-lip smile: smile blendshape score with low jaw opening and low visible-teeth evidence.
- Teeth baring: bright low-chroma image evidence inside the inner-lip polygon, including upper and lower bands. Pink lip/tongue colours are rejected. This is an experimental image heuristic, not a trained tooth classifier or tooth counter; glare, dentures, teeth colour and lighting still need human trials.
- Blink: both eyes' blink scores above the threshold. Eye widening/eyebrow raising is not used as a substitute.

Neutral calibration requires 12 relaxed samples. Mouth, smile and teeth require a stable onset; blink uses a shorter onset. Each reaction fires once, then requires neutral release. Loss of tracking requires neutral before another reaction. Thresholds remain the same at every stage; difficulty does not demand more facial strain. The live Face check screen provides raw scores, reaction counts, sensitivity and recalibration. No tongue, puff-cheek, frown or animal-identity accuracy is claimed.

Face-required gameplay freezes its timer when no calibrated face is available. Tracking loss cannot deduct a heart. If the detector cannot load or camera permission is missing, touch remains available. In mixed games, sound/touch portions do not require a face until the next face cue. Switching input method preserves stage, score, hearts and coins, while clearing unfinished gestures.

## Each game's controls and difficulty

| Game | Integrated controls | Stage 1 to Stage 5 |
|---|---|---|
| Emoji Gates | Mouth / closed-lip smile / teeth events match the gate; neutral rearm | 6 to 10 gates; arrival 5.5 to 3.1 seconds |
| Expression Escape | Sound-volume or press-duration jumps; smile shield; teeth chomp | Narrower face/jump timing; required clears 6 to 7 of 8 |
| Bubble Garden | Smile plants; blink blooms; optional touch | Cue deadline 9.0 to 7.2 seconds |
| Voice Runner | Sound holds thrust; silence drifts; touch hold/release | Cloud arrival 4.0 to 3.2 seconds; height tolerance .25 to .17 |
| Melody Hop | Sound strength or press duration sets real trajectory | Narrower launch window; required clears 6 to 7 of 8 |
| Star Duet | Smile spark, then fresh hum or touch star | Cue deadline 7.0 to 5.6 seconds |
| Snack Parade | Mouth catches treat; closed mouth avoids pepper; optional touch | Catch window 1.50 to 1.02 seconds |
| Smile Stack | Smile places aligned block; optional touch | Alignment tolerance .30 to .20; block speed 1.8 to 2.2 |
| Comet Launch | Hum/hold charges; quiet/release launches | Power tolerance .12 to .08; no louder voice needed |
| Animal Choir | Sound or touch pulse-duration patterns | Deadline 9.0 to 7.6 seconds; still not animal identity classification |
| Face Memory | Watch, then repeat mouth/smile/teeth events; touch cards optional | Stage sequences grow from 2/3/4 to 4/5/6 faces |
| Firefly Glow | Steady sound or touch hold | Hold 1.5 to 2.1 seconds; sound range .22-.78 to .30-.70 |

## Checks and limits

Automated checks cover all 60 stage winning paths and all 60 touch-only winning paths, staged difficulty, all seven expression adapters, wrong-expression rejection, neutral calibration, held/release/lost-face behavior, mouth/teeth/smile synthetic distinctions, pixel negatives for dark mouths and pink lips, and sound-jump physics. These checks validate logic and wiring, not accuracy for real people.

Android instrumentation renders fourteen face-ready/lost states, prior game/tutorial/stage scenes and touch/sound states. The bundled native model is checked on Google's public test portrait (landmarks and blendshapes) and a blank image (no face), followed by the actual threaded frame adapter. These tests never open the camera/microphone or play audio. MP4 and audio decoding checks remain included.

## Human acceptance sequence - still required

1. Open Emoji Gates, enable facial expressions, leave sound off and start. Stay relaxed for calibration.
2. Open Game options -> Face check. Repeat each reaction five times, relaxing between attempts. Check that only the intended reaction counter increases.
3. For mouth opening, try lips covering teeth. For smile, keep lips closed. For teeth, comfortably expose upper and lower teeth. Compare all three, including neutral and normal speech as negative trials. Blink separately.
4. Return to Emoji Gates and clear a mouth gate without touching the screen. Hold the expression to verify only one event, then relax before the next gate.
5. Move out of view: the face-required timer should stop without losing a heart. Return, relax and resume.
6. Test Garden smile/blink, Snack open/closed mouth, Stack timed smile, Duet face then sound, Escape smile/teeth and Memory ordered faces separately.
7. Repeat with stage advancement, varied comfortable lighting and distances, glasses, background/resume and optional recording. Test touch mode with camera/microphone permissions off.

Do not describe all games as professionally validated or recognition as perfect until these person/device trials have been completed and recorded. Animal identity, pitch recognition and audio in exported MP4 clips remain outside this release.

## Device verification - 25 October 2026

Samsung SM-A176B: native face model loaded successfully; Google's public portrait produced landmarks and blendshapes; a blank image produced no face; the production threaded frame adapter returned real expression measurements. Fourteen face-status scenes and the existing camera-free render, encoder and audio-asset checks passed. No user camera samples were captured for these checks. Human expression accuracy and face-to-game usability remain pending the acceptance sequence above.
