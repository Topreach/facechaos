# Face Chaos — mobile application design

Design baseline: 24 October 2026. Android first, iPhone second, following the
user's platform choice. This supersedes the Windows-first platform decision;
the working desktop prototype remains a reference and test harness.

**Status: product/interaction design plus an initial Android engineering spike.**
See [the native camera/overlay test](../android-app/README.md). It has native
selfie preview and manual touch practice; it does not yet implement facial
recognition, composited recording or Godot integration. The complete mobile
application, iPhone build, camera benchmarks and TikTok-equivalence remain pending.

## Confirmed presentation: selfie camera with game overlay

Camera-first navigation update: after permissions, open the plain selfie camera
with a circular record control and **Games** entry. Do not load a game, score,
character or instructions automatically. Games opens a bottom selection panel;
selecting a supported game adds its transparent overlay to the same camera.
Close game / Camera only removes the overlay and returns to the clean camera.
The Games/Practice/You navigation below is an earlier exploration, superseded
by this camera-first entry flow.

The developing collection includes Emoji Gates and Expression Escape (face),
Voice Runner (sound), plus proposed Bubble Garden (face), Melody Hop (sound)
and Star Duet (face + sound). These names describe concepts, not completed
games. Every released game must use validated facial-expression and/or sound
input as its primary controls; touch is an accessibility fallback and a clearly
identified development preview. Do not unlock a game on the strength of an
unvalidated detector. Sound controls must work at a comfortable volume.

The player holds or positions the phone as if recording a selfie video. The
front-camera picture fills the entire portrait canvas. Game objects, score,
targets and feedback render as transparent overlays over that picture; there is
no separate game panel and no opaque game background replacing the person.
Keep the eyes and mouth unobstructed. Facial actions drive play without requiring
spoken commentary. A phone stand is optional for longer sessions.

**Play and record** is part of the first-release design. Recording remains an
explicit choice: REC starts a visible timer/indicator, Stop opens local review,
and the player can save, delete or share the combined selfie + game clip.
Capture the same camera crop, orientation, mirroring and game state seen during
play. Exclude settings, permission dialogs and recording controls from the clip.
Microphone audio is optional and off unless separately enabled; silent facial
play and recording must work. Permission denial or encoder failure leaves play
available with a clear recording status. Never upload automatically.

The prototype simulates REC/Stop/review only; it does not capture video. On-device
compositing and simultaneous camera/analysis/encoding remain required engineering
and performance gates before this experience can be called implemented.

## Product

Face Chaos is a portrait camera game collection. The player sees themselves,
an expressive cartoon avatar and the current challenge in one screen. Games
change; the camera composition and simple controls remain familiar. Core play
works offline without TikTok, accounts, Python or a separate recognition window.

First complete release target: Emoji Gates (six-prompt courses) and Expression
Escape (the first three campaign stages), with touch alternatives. Voice Runner
follows a separately tested comfortable-volume input milestone. Existing seven
mode and eight-stage ambitions remain the long-term backlog, not launch claims.
Do not advertise tongue, cheek puff or named sound classification as working
before they pass mobile-device trials. Frown remains removed.

## Visual direction

- Portrait first, responsive to 9:16 and taller phones; safe-area-aware layout.
- Ink/navy surfaces, warm yellow avatar, mint success cues, coral misses. Text,
  silhouettes and icons reinforce colour; camera image stays naturally coloured.
- Camera is the main surface during play, not a tiny thumbnail or blurred backdrop.
- Compact score/hearts at the top; target emoji plus movement name near the
  lower play area; controls above the bottom safe area. Keep eyes/mouth visible.
- Original soft geometric gates and comic movement effects; no reused TikTok
  character or creator branding. Example feedback: BOING, CRUNCH, NICE MATCH.
- Motion restrained and optionally reduced. Camera appearance effects are
  optional and off for recognition; do not distort the measured face.
- Minimum 48 dp interactive targets on Android. Support large text and device
  font scaling without covering essential cues. No essential gesture-only menus.

## Navigation and screens

Bottom navigation outside a round: **Games / Practice / You**. During a round,
hide navigation and retain Pause, Hide camera and the active touch alternative.

| Screen | Content / primary action | Failure or alternate state |
| --- | --- | --- |
| Welcome | Short face-and-voice demonstration; Play as guest | No sign-in wall |
| Games | Available game cards, stage progress, control type | Future games marked Coming later, not playable |
| Game details | Objective, required reactions, short demo, Play | Choose touch alternative for unavailable control |
| Camera setup | Native preview, framing/light guidance, Auto quality, mirror | Denied/unavailable: explain and offer touch play/settings |
| Facial practice | One named gesture at a time, neutral/rest cue, three attempts | Retake, sensitivity tuning, skip/choose alternative |
| Sound setup | Microphone request only for voice game, room baseline and comfortable level | No compulsory loudness; mute/disconnect is not silence success |
| Stage map | Sequential stage/level unlocks, selected control preset | Locked tiles explain their prerequisite |
| Play | Live camera, avatar, target/hazard, progress, concise feedback | Tracking loss or interruption suspends the round |
| Pause | Resume, retry, controls, quit to game details | Resume countdown and cleared old events |
| Results | Completion/misses, earned stars, Next / Retry | Touch practice and recognized-input records kept separate |
| Camera settings | Auto/Detail/Battery modes, mirror, hide camera, supported exposure | Unsupported settings hidden or disabled with explanation |
| You / settings | Local profiles, control alternatives, accessibility, privacy | Delete/reset actions explain effects; no required account |
| Recording review | Explicit record state, preview, trim/delete/share via OS sheet | Recording failure never ends gameplay; no automatic upload |

### First session

Games -> choose Emoji Gates -> camera explanation -> OS permission -> framing
check -> practice the needed reactions -> short countdown -> first course ->
results -> next course. Returning players get a brief framing/neutral check,
not a forced full recalibration on every launch. All setup is inside the app.

### Permission timing and lifecycle

Ask for camera only when starting face setup; ask microphone only for a selected
voice feature. Touch-only play remains possible after denial. Backgrounding,
screen lock, calls and OS interruptions pause play and release camera/microphone
resources. Resume needs a fresh preview, stable tracking and explicit countdown.
Hiding preview is separate from disabling facial controls: label that distinction.

## Game loops

### Emoji Gates

See a clear emoji plus **Jaw drop**, **Closed-lip smile**, or **Teeth baring**;
return to neutral, perform that movement, then watch the gate open when matched.
The avatar continuously mirrors facial appearance; MATCHED is a separate decision.
Initial timing proposal: one-second rest, four-second response, adjustable after
testing. Six prompts form an introductory course. One event scores one gate;
held poses cannot automatically clear repeated prompts. A miss explains the
requested movement; lost tracking cancels the attempt instead of counting a miss.
Teeth gates require a working detector or an explicit touch substitute.

### Expression Escape

Character auto-runs. Jaw drop jumps, closed-lip smile shields, teeth baring chomps.
Use at most two required actions in a teaching level. Three hearts, readable
hazards, finite finish and Next level. Retain nine existing courses as portable
reference content; first adapt portrait camera composition and touch controls.
If teeth/jaw signals co-occur, apply the documented expression priority rather
than producing unintended simultaneous actions.

### Voice Runner

Measure a comfortable calibrated sound envelope: silence/rest stops or slows
movement, moderate sustained input moves, a comfortable short pulse jumps.
These are proposals to tune, not measured thresholds. Show room floor, current
level and target band. Do not require screaming or label level as recognition
of an animal, humming, ah or sh. Specific sound classes/pitch games have separate
acceptance gates. Voice input never accidentally triggers a facial jump in a
voice-only round. Touch alternatives provide the full course.

## Sharp camera requirement

The requirement is a clean, responsive, naturally exposed face preview on tested
phones. **"Like TikTok" is a visual reference, not a universal image-quality
guarantee or access to TikTok's camera processing.** Sensor, lens, illumination,
phone processing and sustained thermal load affect the result.

### Capture/display architecture

Use one native camera session with separate outputs:

1. **Display:** a high-quality native preview surface/texture, cropped once for
   portrait and correctly rotated/mirrored. Avoid CPU JPEG encode/decode and
   avoid enlarging an analysis thumbnail to fill the screen.
2. **Analysis:** bounded, lower-resolution frames for on-device recognition,
   asynchronously processing the newest available frame. Dropping old analysis
   frames is preferable to delaying the displayed camera or queuing stale actions.
3. **Opt-in play-and-record:** a separately configured composition/encoder,
   off by default and enabled only on devices where camera + analysis + recording
   is supported. Export actual achieved resolution, never an upscaled HD claim.

The desktop 640x480/JPEG/loopback preview is a development transport. It must not
be the mobile display pipeline. Reducing analysis size must not reduce preview
detail. Godot game visuals render over the native camera or an imported camera
texture, with correct transparency and face-safe HUD positioning.

### User settings

| Setting | Intended behavior |
| --- | --- |
| Auto, default | Best tested preview/analysis combination for the device; prioritize stable 30 fps |
| Detail | Request a supported high-detail preview, up to 1080p target; fall back clearly if unsupported |
| Battery | Reduce analysis rate/effects first; choose a supported camera mode without accumulating lag |
| Mirror preview | Default selfie mirror; keep left/right prompts in the player's anatomical coordinates |
| Exposure | Small bounded adjustment where supported; retain auto exposure/white balance by default |
| Focus | Use autofocus only if available; never promise tap-to-focus on a fixed-focus front camera |
| Hide camera | Replace displayed face with a neutral background while face control continues |
| Disable face control | Release camera; change to touch controls with clear scoring category |

Do not ship aggressive digital sharpening, smoothing or beautification as a
substitute for detail. Analysis receives the unfiltered frame. Low light gets
actionable guidance; supported low-light features need compatibility tests and
must not silently introduce gameplay-breaking exposure delay.

## Proposed mobile architecture

- **Shared gameplay/UI:** adapt the current Godot 4 project to portrait scenes,
  touch controls, mobile lifecycle and data-defined games.
- **Android native plugin:** Kotlin + CameraX for preview/capture/lifecycle;
  MediaPipe Face Landmarker live-stream callbacks for supported facial signals;
  AudioRecord or equivalent native capture for a separately tested voice path.
- **iOS after Android acceptance:** Swift/AVFoundation capture and on-device
  MediaPipe, with a Godot native bridge and equivalent permission/lifecycle rules.
- **Internal interfaces:** CameraSession, FaceSignalProvider, AudioSignalProvider,
  InputRouter, GameMode, SaveStore and optional Recorder. Timestamped signals,
  not camera frames, enter the gameplay action router.
- No Python installer, local HTTP server, UDP setup screen or external runtime
  app in the delivered mobile application. No cloud AI service required.

**Architecture gate:** first prove supported camera resolution, native preview
plus transparent gameplay/texture interop, asynchronous face inference and pause
recovery on a physical Android device. Native-plugin support does not itself
prove efficient texture interop. Select the rendering bridge from that result
before scaling screens/content; retain the desktop reference until it passes.
Android API floor, exact dependency versions and supported device list are
provisional until this spike. iOS export requires its Apple build/signing workflow.

## Data, privacy and access

Core progress/settings/templates stay on device; raw camera and microphone
buffers are not retained by default. Recording is separate explicit opt-in with
a visible indicator and local review/delete before an OS share action. Do not
upload faces/audio for analytics. No face identity recognition or emotion claims.
The app must explain camera-hidden versus camera-off and provide delete-profile
controls. Publish accurate store disclosures after actual SDK/data-flow review.
If aimed at children, complete the applicable platform/privacy review before
distribution; no age-policy compliance is claimed by this design.

## Acceptance targets — to measure, not claim as achieved

| Area | Initial gate |
| --- | --- |
| Camera detail | Document negotiated preview/recording size, crop, rotation and analysis size on each supported phone |
| Sharpness/exposure | Compare the phone camera app and our app on the same front camera, position and lighting; assess fine detail, motion blur, noise and skin tone |
| Smoothness | Stable 30 fps preview target during a 15-minute game; record dropped frames, thermal state and game frame times |
| Interaction | Measure threshold-to-visible-action latency; initial p95 goal below 150 ms on declared test devices |
| Recognition | At least 50 prompted attempts/control plus 10-minute neutral/confound exposure; initial precision/recall target 95%, fewer than 2 unintended actions/10 minutes |
| Composition | Face eyes/mouth and active target remain readable across supported aspect ratios, notches and large-text settings |
| Lifecycle | Denial, revoke, interruption, background/resume, camera unavailable and thermal downgrade recover without stale actions |
| Reach | Entry/mid/high-tier Android test matrix, varied lighting/appearance, glasses and motion; then iPhone matrix |
| Offline/install | Fresh install, permission flow, offline round, save, force-stop, relaunch and update migration on real devices |
| Privacy | No raw recording until opt-in; stop capture on background; deletion/share behavior verified |

Cross-app comparison is a practical quality benchmark, not proof of equal ISP
processing or a guarantee on unsupported phones. Native camera sharpness and
expression-recognition accuracy must be evaluated separately.

## Delivery sequence

1. Review the mobile screens, interaction model and Android-first choice.
2. Build the Android native camera/inference/rendering spike on the user's phone.
3. Build integrated onboarding, touch fallback and one complete Emoji Gates course.
   Add composited selfie + game recording, visible state, and local review/save/delete;
   validate simultaneous preview, analysis and encoding on supported Android phones.
4. Port the first Expression Escape stages; harden records and failure recovery.
5. Build/validate comfortable-volume Voice Runner before named sound classifiers.
6. Test quality, performance, privacy, accessibility and Android distribution.
7. Port the verified design to iPhone; expand the broader game collection afterward.

## Primary technical references

- [CameraX preview surface](https://developer.android.com/media/camera/camerax/preview)
  and [device-aware configuration](https://developer.android.com/media/camera/camerax/configuration)
  support separate native preview/analysis use cases with negotiated resolutions.
- [CameraX analysis/backpressure](https://developer.android.com/media/camera/camerax/analyze)
  documents latest-frame analysis to avoid a queue blocking the preview.
- [MediaPipe Android](https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/android)
  and [iOS](https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/ios)
  document on-device face landmark/blendshape workflows; they do not validate
  our custom teeth, tongue or cheek detectors.
- [Godot Android native plugins](https://docs.godotengine.org/en/stable/tutorials/platform/android/android_plugin.html)
  provide platform/library integration; efficient camera composition remains a spike.
- [AVFoundation capture preview](https://developer.apple.com/documentation/avfoundation/setting-up-a-capture-session)
  is the iPhone camera design reference.
