# Face visibility and calibration - 0.15.0

The detector reads the camera TextureView bitmap before game artwork and instruction stickers are drawn. A sticker can obscure the player's view without entering the detector image. This release addresses both visibility and recognition responsiveness.

## UI

Face Check is now a bottom dialog bounded to 32 percent of screen height with scrollable content, reaction counters, sensitivity and recalibration controls. The upper camera remains undimmed. The instruction sticker is hidden while Face Check is open. During gameplay calibration the large sticker collapses into a single-line, 28-design-unit caption at the game edge. Detected face landmarks provide a lower face boundary: instruction captions move below it with an eight-unit gap. If no safe space remains above the native controls the caption is suppressed instead of covering the face. Game rendering scale, coordinates and dimensions remain unchanged. A close-up face can cause captions to overlap the game area; step back to restore space above the game. The bottom Face Check panel is fixed, so players should keep their face in the upper camera area.

## Calibration and responsiveness

Neutral calibration uses at least six valid samples over at least 650 ms and accepts a wider range of natural resting jaw/smile levels. A natural blink pauses sampling without discarding a stable baseline. Open mouth, strong smiles and tooth displays cannot be learned as neutral. Reaction thresholds adapt to the personal baseline; slow adaptation is restricted to confirmed neutral periods. Mouth/smile confirmation is 90 ms, tooth confirmation 130 ms, and event cooldown 260 ms. A stable neutral return rearms reactions; a held expression cannot repeat. Tracking recovery preserves the personal expression baseline and requires brief neutral recovery before unfreezing face-controlled actions. Recalibrate resets the existing model's signals without reloading its weights. Finishing Face Check while returning to active face gameplay keeps its loaded detector/baseline.

Head calibration uses eight stable samples, a wider comfortable starting position, distance-aware face-width normalization and light offset smoothing while retaining lane hysteresis. It still rejects extreme edge positions and invalid landmarks.

Camera analysis targets a 60 ms submission cadence with a single busy worker and no frame backlog. Analysis frames preserve the preview aspect ratio within 480x1040 rather than stretching the selfie. Tooth image analysis copies only the mouth region instead of allocating a full-frame pixel array. If the mouth image is too small, the other expression measurements continue instead of losing all face readiness; tooth classification remains experimental, and insufficient tooth detail can limit expression separation. End-to-end response depends on device load, lighting and the player's face. Detector confidence thresholds remain unchanged.

MediaPipe video-mode tracking is retained; its documented purpose is to avoid running face detection on every tracked frame and reduce latency: https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/android .

## Validation

Pure checks passed for four frame cadences, personalized resting baselines, timed calibration, natural blinks, deliberate mouth/smile response, held-input rejection, neutral rearming, tracking recovery, reset and comfortable off-centre steering. Existing four-expression/teeth-evidence tests and all seven face-game adapters passed. Existing head-control tests passed across all five stages at an 80 ms sample cadence. Layout checks preserved original game geometry across 180 phone/control/game combinations; face-aware caption position checks passed.

Offline Android debug/test builds and lint passed. On the Samsung SM-A176B, the actual bundled model detected landmarks/blendshapes on the public test portrait and rejected a blank image. The threaded expression adapter and head-only calibration adapter passed. Six synthetic normal/compact calibration/caption/check scenes passed face-clear placement and unchanged game geometry checks; a calibration scene was visually reviewed. No player's face or microphone was recorded by these tests. Installed versionCode 36 / 0.15.0-face. Real-user response and bottom-panel comfort still need a live phone check.
