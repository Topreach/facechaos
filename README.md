## Latest mobile build: 0.17.0 - Melody Hop scrolling course

Simple, Difficult, Hard, Extreme and Finale: approaching terrain, forty authored obstacles, gradually higher sound-powered jumps and low overhangs that require controlled power. [Rules and validation](docs/MELODY_COURSE_0170.md).

## Latest mobile build: 0.16.4 - Melody Hop fire gaps

Jump between platforms above animated fire. Failed jumps visibly fall before losing a life; successful jumps land and earn a coin. [Rules and validation](docs/MELODY_FIRE_0164.md).

## Latest mobile build: 0.16.3 - Melody Hop sound and target alignment

Independent sound-on default, calibration pause, aligned jump targets and clearer volume cues. [Changes and validation](docs/MELODY_HOP_0163.md).

## Latest mobile build: 0.16.2 - Smile Stack landing update

Consistent blocks, visible drops, coins after landing, and smile timing that allows for detector dwell. [Controls and validation](docs/SMILE_STACK_0162.md).

## Latest mobile build: 0.16.1 - Sound-only Comet Launch

Hum to charge, go quiet in gold to launch, and earn coins after landing. [Controls and validation](docs/COMET_SOUND_0161.md).

## Latest mobile build: 0.16.0 - Twelve-animal choir

Animal Choir now includes twelve illustrated animals, twelve example rhythms, mixed short/long calls and a larger stage roster. [Roster and validation](docs/ANIMAL_VARIETY_0160.md).

## Latest mobile build: 0.15.1 - Animal Choir rhythm play

Independent microphone default, visible beat progress, reliable short/long call matching and an in-game Hear pattern button that pauses listening and the round. [Rules and validation](docs/ANIMAL_CHOIR_0151.md).

## Latest mobile build: 0.15.0 - Clearer face calibration

Compact bottom Face Check panel, hidden check sticker, face-aware gameplay captions and more responsive personalized expression handling. Game dimensions remain unchanged. [Changes and validation](docs/FACE_CALIBRATION_0150.md).

## Latest mobile build: 0.14.3 - Sound-only Firefly Glow

Hum to guide the firefly to lanterns. Touch no longer substitutes for sound; the phone microphone is required for this game. [Rules and validation](docs/FIREFLY_SOUND_0143.md).

## Latest mobile build: 0.14.2 - Sound Grand Prix microphone fix

Sound Grand Prix now defaults to its own microphone setting, independent of other games. A visible microphone/power/speed readout and paused microphone recovery make the input state clear. [Validation notes](docs/RACE_SOUND_0142.md).

## Latest mobile build: 0.14.1 - Increasing stage speed

Stage speeds: 1.00x, 1.25x, 1.50x, 1.80x, 2.10x. The countdown and stage result show the current/next speed. [Validation notes](docs/STAGE_SPEED_0141.md).

## Latest mobile build: 0.14.0 - Fresh runs and best coins

New runs and retries start at Stage 1. Coins carry across successful stages within a run; each game stores its highest single-run coin total. [Rules and validation](docs/RUN_RECORDS_0140.md).

## Latest mobile build: 0.13.3 - Camera quality

Uses the highest advertised video-shaped preview stream at a supported frame duration, supported fast detail/noise processing, and higher-detail encoder-checked composite recordings. Device verification and limits: [camera quality notes](docs/CAMERA_QUALITY_0133.md).

## Latest mobile build: 0.13.2 - Original game size restored

Restores the original game dimensions and position. Only the comment panel is positioned immediately above the game landscape.

## Latest mobile build: 0.13.1 - Instruction placement

Names and action comments immediately above the game, with a clear selfie area. [Layout notes](docs/INSTRUCTION_PANEL_0131.md).

## Latest mobile build: 0.13.0 - Per-game sections

Eleven more games gain five named sections each, matching previews and themed environments. [Journey list and validation](docs/GAME_SECTIONS_013.md).

## Latest mobile build: 0.12.0 - Voice Runner worlds

Voice Runner uses a separate live-voice preference and five original adventure worlds. [Update and phone test](docs/VOICE_RUNNER_WORLDS_012.md).

## Speech fix: Android 0.11.1

Selects the installed English model (UK English on the Samsung) and adds default speech-service fallback. [Fix and checks](docs/SPEECH_SERVICE_FIX_0111.md).

## Latest mobile build: 0.11.0 - Flag World Tour

15 games and 75 stages. New country-flag quiz: say the country, earn a coin, then see the next flag. Spoken English and typed fallback. See [implementation and test notes](docs/FLAG_WORLD_TOUR_011.md).

# Face Chaos — Stage 1 Playable Prototype

## Mobile application direction

Current Android build: **0.10.0** connects actual local facial recognition to the seven face games, adds live Face check, and reviews difficulty across all twelve games. Teeth detection remains experimental and person-level tests are required. See [the facial input and game audit](docs/FACE_INPUT_AND_GAME_AUDIT.md). All games now accelerate up to 2.10x with expert final-stage targets; see [stage speed](docs/STAGE_SPEED_091.md). The collection now contains 14 games, including [Sound Grand Prix and Head Dodge](docs/RACING_GAMES_010.md), with analog sound speed and independent head steering. Older release notes follow.

Android 0.7.0 expands the collection to 12 games with [Animal Choir, Face Memory and Firefly Glow](docs/ANIMAL_AND_MEMORY_GAMES.md). Animal patterns and volume controls are distinct from animal identity recognition. The next focus is [validated facial and sound recognition](docs/RECOGNITION_IMPLEMENTATION_PLAN.md).


Android 0.4.0 adds [Play, Pause, Stop, Retry and optional silent recording](docs/PLAY_RECORD_CONTROLS.md). The native camera and illustrated games can be recorded together and reviewed, saved or shared. Android games still use touch controls; facial and sound recognition remain pending.

See the [Android build and phone test instructions](android-app/README.md).

Android 0.2.0 adds a [six-game collection](docs/MOBILE_GAME_COLLECTION.md) with
distinct touch-controlled gameplay, original vector scenes, countdowns and results.
Sound and facial-expression detection are still separate implementation gates.

**Android first, then iPhone.** The [mobile application design](docs/MOBILE_APP_DESIGN.md)
now defines the platform, camera experience and release sequence. Open the
[interactive screen prototype](mobile-design/index.html) in a browser to explore
the proposed screens. It uses illustrated camera images and simulated matches;
it is not an APK or a tested mobile camera implementation.

## Desktop prototype and development plan

**New playable build: Expression Escape.** Open `project.godot`, press F5, and
choose **Play with Keyboard**. Complete three levels per stage to unlock the next
stage. See [play instructions](PLAY_EXPRESSION_ESCAPE.md).
Face mode now supports a [live camera background in the same game screen](docs/INTEGRATED_PLAY_SCREEN.md),
with hide/show controls and blink/mouth avatar feedback. Calibration setup can
be minimized before play; preview images remain local and are not recorded.
See the [quality and creative review](docs/QUALITY_REVIEW.md) for verified fixes
and the remaining work before a professional release.

**Recognition work remains separate.** Double-click
`Test-Facial-Reactions.cmd` and follow the [Facial Reaction Lab guide](FACIAL_LAB_START_HERE.md).
See the [14-section cross-check and remaining validation limits](docs/FACIAL_CROSSCHECK.md).
The lab covers 14 sections including face scrunching; advanced detectors are experimental
and advanced candidates stay disconnected from gameplay. The current lab now
has opt-in game sending for calibrated jaw, smile, teeth and the eye/brow proxy.

For the separate Stage 2 source package, follow [Stage 2 installation and first webcam test](STAGE2_START_HERE.md).

- [Complete Game Design v1.1](docs/GAME_DESIGN.md) — authoritative product design for all seven modes.
- [Implementation backlog](docs/IMPLEMENTATION_PLAN.md) — milestone order and completion gates.
- [Reaction Recognition Contract](docs/recognition-contract.md) — detection and bridge requirements.

The design describes the intended product; the features below describe the current prototype.

## Current prototype features
- Godot 4 2D campaign prototype: three stages, three finite courses per stage
- Keyboard: **Space** jump, **Up** fly, **S** shield, **A** chomp, **R** restart
- Authored obstacle schedules, three hearts, recovery grace, finish/results/retry
- Saved course unlocks, Next level countdown, stage selection, heart completion bonus
- Distinct meadow, factory and night palettes with jump obstacles, biscuits and stars
- Optional localhost UDP bridge for webcam reaction input
- Face Landmarker camera preview, personal mouth/eyebrow/smile calibration, and live meters
- Fresh-press keyboard input and shared facial/keyboard ability cooldowns
- Bounded flight, Escape pause, cooldown feedback, and persistent best score
- Webcam heartbeat timeout and face-tracking status (keyboard remains available)

Abilities are brief actions: release and press again to retrigger after cooldown.
Current lab mappings: jaw drop jumps, the eye/brow proxy flies, closed-lip smile
shields and teeth baring chomps. Teeth recognition remains experimental; A is
its keyboard alternative. Press F to opt into face mode
after calibration, or K for keyboard mode. See [camera setup](recognition/README.md).

## Regression checks

Run `godot --headless --path . --script tests/game_checks.gd` and
`godot --headless --path . --script tests/campaign_checks.gd` using your Godot executable.
The latter simulates complete no-damage keyboard runs through all nine courses.
Also run `godot --headless --path . --script tests/quality_checks.gd` for frame
hitches, attack direction, countdown/pause transitions and corrupt-save recovery.

## What is NOT yet implemented
- Microphone recognition and laughter/speech classification
- Validated recognition quality, integrated recognition setup screens, full accessibility options
- Sound-powered campaign stages and the final combined challenge
- OBS/TikTok/YouTube integration, all other modes, character customization
- Webcam recognition accuracy/latency validation on your machine

## Run the game
1. Install Godot 4.x (standard editor).
2. Open Godot -> Import -> select `project.godot`.
3. Press F6/F5 to run the project (F5 is the usual run-project shortcut).
4. Press Space to start, then use the keyboard controls above.

The start screen has **Connect Facial Controls**, **Play with Face + Keyboard**, and
**Play with Keyboard** buttons. Face play enables after the camera reports
calibrated tracking. See [facial setup and play steps](FACIAL_LAB_START_HERE.md).
If the old runner is still open, stop it with F8 in Godot
and run this project's `project.godot` again with F5. The window title is
**Face Chaos - Expression Escape**.

## Camera setup

Follow [the camera setup and calibration guide](recognition/README.md).
From the project folder, after one-time setup:

```powershell
.\.venv\Scripts\python.exe recognition\webcam_bridge.py --camera 0
```

The bridge uses MediaPipe's Face Landmarker Tasks interface and versioned localhost
events. Frames remain local; no video is recorded. Actual accuracy and latency must
be measured on your device before the recognition milestone is considered complete.

## Implementation notes
- The main gameplay is independent of camera availability.
- `scripts/reaction_bridge.gd` listens only on localhost.
- `recognition/webcam_bridge.py` is experimental, not a validated production recognizer.
- Before expanding, verify Godot project load, gameplay, collision logic, and webcam behavior on the actual Windows computer.
# facechaos
