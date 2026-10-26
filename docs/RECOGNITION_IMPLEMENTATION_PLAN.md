# Facial and sound recognition - remaining validation

Update: Android 0.9.0 now implements local Face Landmarker inference, mouth/smile/blink decisions, experimental direct teeth evidence, a live face-check screen and seven face-game adapters. See [the current audit](FACE_INPUT_AND_GAME_AUDIT.md). Person-level accuracy, cross-expression trials and animal identity remain pending. The original plan below provides the remaining validation criteria.

The 12-game catalogue is now sufficient for the next implementation stage. Prioritize a reliable shared input system before another game-expansion batch. Current tap previews and sound patterns must remain labelled as such until their replacement passes device tests.

## 1. Build the Android input lab

Use a camera analysis stream alongside the clean selfie preview. Keep detection off the UI thread, reuse buffers and measure camera smoothness, frame latency, recording performance and battery impact on the Samsung test phone. Expose per-expression confidence, neutral calibration, sensitivity, face-found/lost state, event history and an explicit touch fallback in the lab. The playing screen should retain the clean presentation.

[MediaPipe Face Landmarker for Android](https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/android) supports facial landmarks and optional blendshape outputs. Evaluate it as the shared base, with a pinned library/model version and recorded provenance. Its outputs do not establish our game's expression accuracy by themselves.

Start with open mouth, closed-lip smile and blink. Test one at a time, then test them against one another. Eye widening must stay distinct from the eyebrow action. Teeth baring must be distinguished from a smile and an open mouth. Tongue, puffed cheeks and face squeezing require their own validated evidence; never rename a mouth or eye proxy to claim those controls work. Do not restore frown as a required gesture.

Use onset/release thresholds, a neutral return and cooldown so holding an expression triggers once. Pause safely on tracking loss or camera interruption instead of deducting lives for missing input. Collect labelled lab trials only with explicit user participation; do not save camera footage implicitly.

## 2. Validate sound controls and animal classification separately

The current volume gate, pulse counter and duration matching are useful game inputs, but do not identify an animal. Calibrate quiet and comfortable sound levels; test speech, TV, music, claps and the app's own guide playback as negative examples. Verify pause, background, permission denial and camera/video coexistence. Keep recognition samples transient unless the user explicitly opts to save examples.

[MediaPipe Audio Classifier for Android](https://developers.google.com/edge/mediapipe/solutions/audio/audio_classifier/android) is a candidate runtime for a suitable trained model. Evaluate model labels and licensing before selecting it. Human imitations are a different input distribution from recordings of actual pets. Build and test representative woof/meow/chirp imitation examples, with an unknown/other category and confidence-based rejection. A high score on a prerecorded animal sample does not validate a person imitating that animal.

Publish confusion results for dog vs cat vs bird vs unknown, plus latency and false activations. Leave unsupported labels disabled until validated; never allow every loud sound to count as the requested animal.

## 3. Connect validated events to each game

Use one input event contract: action identifier, confidence, timestamp and source (face, sound, or manual). The same game rules must accept manual and detected inputs. Replace preview adapters one control at a time, keeping visible input state and a reliable fallback. Face Memory should accept only validated, distinct expression events in the correct order. Animal Choir should switch from pattern practice to identity recognition only after the animal classifier passes its checks.

## 4. Release validation

Test different people, lighting, skin tones, glasses, camera distances and quiet/noisy rooms. Include neutral/relaxed trials and competing expressions, not only positive demonstrations. Record per-control precision, recall, false activations per minute and event latency; do not advertise an accuracy percentage before measuring it. Repeat with game recording active, background/resume, denied permissions and interrupted devices. Use the findings to decide which controls ship enabled by default.

Completion means real-device evidence for the enabled controls and each game's adapters, not only a build, synthetic test or working camera preview.
