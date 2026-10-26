# Facial and sound control audit — 0.19.5 (52)

## Release decision

**Not yet signed off as production-ready recognition.** Automated game/control tests and Samsung hardware checks pass. They do not establish real-player accuracy across faces, tooth colours, lighting, microphone processing, accents or devices. Teeth detection remains an experimental pixel heuristic, and the reported real-player Emoji Gates failure needs a repeat test with this fix before publishing the update.

**Real-player retest outcome:** the user reports that the teeth counter still does not increase. This is an unresolved release blocker. Exact live tooth-evidence/threshold values and calibration status have been requested; synthetic tests are not a substitute for this failed user test. That report concerned the earlier test behavior. Version 52 now follows the user's clarified requirement: any visible front tooth row can count; both rows are not required. Its real-player retest is pending. This remains a test APK, not a production-approved release.

## Corrections

1. A teeth display can begin with jaw opening. Previously the mouth event could consume the one-shot detector's rearm before tooth evidence appeared. The detector now uses the game's requested expression; unrelated reactions do not consume that cue. Tooth evidence is still required: jaw opening or smiling alone cannot clear a teeth gate.
2. The sensitivity setting previously affected jaw, smile and head thresholds but not teeth. It now adjusts the teeth evidence threshold within bounded limits.
3. Show teeth now accepts a coherent visible upper or lower tooth band. It no longer requires both rows. A tooth-bearing smile counts as Show teeth; Smile requires closed lips. Band measurement follows the mouth axis, tolerates asymmetric jaw opening, rejects tiny highlights and uses bounded exposure adaptation for dim ivory teeth.
4. Insufficient mouth pixel detail now makes a required teeth cue temporarily unavailable, pausing its game movement and asking the player to move closer. It is not silently treated as a valid zero-teeth observation.
5. Face calibration now exposes its measurements, including tooth evidence and the active threshold. A reaction selector permits testing one expression at a time. Closing this diagnostic restarts the gameplay recognition session so pending diagnostic events cannot score in the game.

The main game/recording controls, game artwork and game-switch Yes/No confirmation are unchanged.

## One-by-one controller coverage

| Input | Expected player action | Verified in code/tests | Still needs real-player evidence |
|---|---|---|---|
| Open mouth | Drop jaw, without deliberately baring teeth | Neutral calibration, dwell, held-input rejection, routing and gate completion | Faces, distances and ordinary lighting |
| Closed-lip smile | Smile while keeping lips together | Separate jaw/smile thresholds, one-shot/rearm and correct game routing | Comfortable small smiles and avoidance of false activations |
| Show teeth | Make your front teeth visible comfortably; either row is enough | Mouth-to-teeth transitions; sensitivity; one-row/two-row positives and pink/dark negatives; five synthetic head tilts; complete six-gate stage | Actual teeth vs lips, braces, shadows and naturally different tooth colours; reported failure retest |
| Blink | Close both eyes briefly, then relax | Bilateral blendshape threshold, blink-tolerant calibration and Bubble Garden mapping | Deliberate vs ordinary blinking, glasses and eye shapes |
| Head movement | Calibrate centred, move toward a lane, return to centre | Independent head model, hysteresis, tracking loss/recovery, five-stage rule tests | Physical left/right direction and comfortable range on multiple phones |
| Sound level | Quiet baseline, then soft/strong comfortable hum or aaa | PCM RMS gate, hysteresis, strength, quiet release, volume-to-motion rules | Real soft/strong separation, background noise and device gain processing |
| Sound rhythm | Copy short/long calls with quiet gaps | PCM-to-rhythm durations/counts, brief-dropout tolerance, all 12 animal patterns | Users' actual rhythms under ordinary room noise |
| Spoken country | Say the country's English name | Answer aliases, provider fallback, errors, cancellation and stale callbacks | Real accents, recognition errors, offline/network behaviour |

Animal Choir recognises rhythm, not the biological identity of an animal sound. Melody Hop uses sound level and timing, not musical pitch. Flag World Tour currently checks English country names, regardless of interface language.

## Game-by-game audit

| Game | Current control | Automated checks |
|---|---|---|
| Emoji Gates | Mouth, closed-lip smile, teeth | ReactionSequenceCheck: timed raw scores through detector, adapter, all three gate types and six awarded coins |
| Expression Escape | Volume jump; smile shield; teeth attack | FaceInputCheck, SoundJumpCheck, stage/speed tests |
| Bubble Garden | Smile to plant; blink to bloom | FaceInputCheck, FaceFlexibilityCheck, MiniGameCheck |
| Voice Runner | Hum to rise; quiet to descend | SoundGateCheck, VoiceCourseCheck, stage/speed tests |
| Melody Hop | Timed sound; greater strength gives higher jumps | SoundJumpCheck, MelodyCourseCheck, MelodyFireCheck |
| Star Duet | Smile, then fresh sound after quiet | FaceInputCheck, StarDuetCheck; held sound cannot carry into the next cue |
| Snack Catch | Mouth for treats; remain neutral for peppers | FaceInputCheck, CollectionCheck, game/section tests |
| Smile Stack | Smile at block alignment | FaceInputCheck, SmileStackCheck, CollectionCheck |
| Comet Launch | Hum to charge; quiet to launch | CometSoundCheck: PCM charging, release, landing rewards and five stages |
| Animal Choir | Short/long sound patterns | AnimalChoirCheck, AnimalVarietyCheck: all 12 animals and five stages |
| Face Memory | Repeat mouth/smile/teeth after the demonstration | FaceInputCheck, MemoryCheck |
| Firefly Glow | Maintain a gentle hum in the target range | FireflySoundCheck: PCM-to-flight and six lanterns across five stages |
| Sound Grand Prix | Sound strength controls acceleration | RaceSoundCheck: quiet calibration, soft/strong speed, coasting and five race wins |
| Head Dodge | Move head left/centre/right | HeadSignalsCheck, RacingCheck, HeadModelCheck |
| Flag World Tour | Say an English country name or type | FlagQuizCheck, CountrySpeechCheck, SpeechSupportCheck |

The full 32-suite host run is in `android-app/recognition-audit.log`. These suites include pause, held/rearmed input, tracking loss, stages, coins and game rules. They simulate observations/PCM rather than asserting that a real player's face or voice was correctly understood.

## Samsung SM-A176B checks

- The actual bundled MediaPipe model detects a public portrait's landmarks/blendshapes and rejects a blank image. The production threaded face adapter returns measurements.
- The head-only adapter calibrates independently of mouth/smile/teeth decisions.
- Face calibration captions remain clear of the face in tested normal and compact layouts.
- The real phone microphone opens and delivers at least 20 analysis callbacks through the production MicrophoneInput adapter. No samples are retained. This proves capture/calibration, not vocal accuracy.
- All 12 packaged music/effect files decode without playback.
- The phone reports an installed on-device `en-GB` speech model. The default provider returns support-query error 14; the adapter's fallback/error paths pass simulated-provider tests. Actual spoken-answer accuracy remains unverified.

## Required real-player checks before release

Start with Emoji Gates in face mode. In Game options → Face/head check, select Show teeth. Relax for calibration, show your front teeth comfortably (either row), relax, and repeat. The teeth counter should increment once per deliberate display; plain jaw opening and closed-lip smiling must not increment it. If it fails, record the displayed **Teeth evidence / threshold** values and whether it reports insufficient detail. Do not solve this by treating every open mouth as teeth.

Then test mouth, closed-lip smile and blink separately using the selector, followed by head steering in Head Dodge. For sound games, keep quiet during the initial baseline and compare soft hum, stronger comfortable hum and quiet. Test the animal examples, quiet gaps, speaker feedback suppression and real country answers.

Repeat with multiple people, a second phone, normal/dim/backlit rooms, glasses, and ordinary background noise. Include permission denial, camera/mic privacy switches, interruptions, pause/resume and game switching. Do not publish “works perfectly” claims based on synthetic tests or one person passing a gate.

Primary references checked: [MediaPipe face blendshape definitions](https://github.com/google-ai-edge/mediapipe/blob/master/mediapipe/tasks/cc/vision/face_landmarker/face_blendshapes_graph.cc), [Android AudioRecord](https://developer.android.com/reference/android/media/AudioRecord), [MediaPipe blendshape model card](https://storage.googleapis.com/mediapipe-assets/Model%20Card%20Blendshape%20V2.pdf).
