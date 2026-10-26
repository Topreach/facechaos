# Sound Grand Prix and Head Dodge - Android 0.10.0

The mobile collection now contains 14 games and 70 stages. Both racing games use original vector sports cars, a miniature road below the central selfie area, matching illustrated instructions, top prompts, shared coins and the existing play/pause/retry/optional-recording flow.

## Sound Grand Prix

Race a local computer-controlled opponent across a 450-metre sprint. Live microphone strength controls the accelerator continuously: soft sound gives less speed, a stronger comfortable sound gives more speed, and quiet lets the car coast. This uses sound loudness, not words or pitch. Speed changes smoothly rather than acting as an on/off boost. The CPU has its own changing pace and can win; its base pace increases at each stage. First across the finish wins, including interpolated finish timing if both cross in one simulation step. No online connection or other computer is required.

The header shows current rank, player/CPU progress, sound power and the speedometer. Every quarter of the track awards one coin, once. The computer waits for microphone readiness. Music/effects remain suppressed while a microphone round is active, avoiding feedback into the accelerator.

Touch alternative: switch microphone mode off, hold to accelerate and release to coast. Holding longer builds more throttle. All five stages are playable by either input path.

## Head Dodge

Steer between three lanes by moving your head left/right; return to centre for the middle lane. Head steering has independent centred-head calibration and does not require smiling, teeth baring or mouth opening. It uses nose displacement relative to the calibrated face width, with a dead zone and hysteresis to avoid lane jitter. The front-camera preview remains unchanged. Reverse head steering is available in Game options -> Input method if the direction does not match the player's expectation. Sensitivity and recalibration are available in Face / head check.

Traffic always leaves at least one lane open. Later stages introduce two blocked lanes. The lane generator covers all lanes over each three-wave group, so staying still cannot win through a lucky sequence. Stage 1 contains 12 waves, increasing to 20 in stage 5. Stages 1-4 permit two missed waves within the three-life budget; stage 5 requires all 20 with one life. Collision checks use the car's actual interpolated lane position, including unsafe lane crossings at impact.

Camera/head tracking loss freezes traffic and does not deduct lives. When tracking returns, a 0.6-second real-time recovery period allows steering before traffic moves again. Losing the tracker requires centred-head calibration again. Backgrounding and pausing stop analysis. Frames stay local and transient; no new network permissions are introduced.

Touch alternative: switch head steering off, then tap or drag in the left, middle or right third of the screen to select that lane. Touch cannot masquerade as head input while live head mode is enabled.

## Stage progression

Both games inherit the 1.00x, 1.20x, 1.45x, 1.75x and 2.10x game clock. The sound-race CPU also gets stronger. Incoming traffic approach time decreases from 3.0 to 2.2 simulation seconds, about 3.0 to 1.05 real seconds after the speed multiplier. Head thresholds and calibration do not increase with stage speed. The existing two-second ready countdown and head recovery use real time.

## Verification

- All 70 stage winning paths and all 70 touch-only paths pass.
- Sound power produces distinct actual speeds; quiet decelerates; low power loses to the CPU and stronger power wins across all five stages.
- All five live-signal sound races finish at 100% with four unique checkpoint coins.
- Traffic checks cover 100 stage/seed combinations, safe-lane wins, collision outcomes and guaranteed stationary-player loss.
- Head checks cover left/centre/right, dead-zone hysteresis, invalid/edge calibration, tracking loss, sensitivity and all stages at an 80-ms steering cadence.
- Readiness, pause, tracking recovery and live-input/touch isolation are checked.
- Android instrumentation includes twelve racing scenes (play, result and compact layouts), fourteen game/tutorial scenes and the prior regression scenes. The actual production head-only adapter calibrates from twelve frames of Google's public portrait without depending on mouth/smile calibration. These checks do not capture a user camera/microphone or play audio.

Build/static checks pass. Samsung SM-A176B verification passed on 25 October 2026: native racing/tutorial/compact/result rendering, both actual model adapters on the public fixture, MP4 encode/decode and audio decoding. Visual review confirmed the car artwork, player/CPU progress and clear-lane prompts. The two racing games are featured first in Games, marked NEW. Person-level microphone response and physical left/right steering remain acceptance tests, not proven by synthetic/model-fixture checks. Teeth recognition remains experimental in the other games, animal controls remain rhythm patterns rather than identity classification, and exported videos remain silent.

## First phone test

1. Games -> Sound Grand Prix. Enable the optional microphone, stay quiet for its initial baseline, then compare soft and stronger comfortable sounds. Confirm the speedometer and progress change and quiet slows the car. Try beating the coral CPU car.
2. Games -> Head Dodge. Enable head steering, hold centred for calibration, then move left/right and return to centre. Confirm the car follows. If necessary, use Game options -> Input method -> Reverse head steering.
3. Use Face / head check to inspect lane readings and recalibrate. Move out of view and return: traffic should freeze, recalibrate, then give recovery time before resuming.
4. Repeat with touch modes, pause/resume, later stages and optional recording. Tune real-player comfort and difficulty from these trials.
