# Historical Android notes

Use android-app/README.md for the current build.

# Current Android update: 0.13.1 - Instruction placement

Section names and action comments now sit immediately above the lower game area, keeping the selfie clear. [Layout and verification](../docs/INSTRUCTION_PANEL_0131.md).

# Current Android update: 0.13.0 ? Per-game sections

Eleven more games now have five named sections each, matching world previews, route lists and themed scenery. Together with Voice Runner: 60 sections across 12 games. Flag World Tour, Face Memory and Animal Choir retain their rounds. Gate orders, hop rhythms, pepper schedules and stack motion now vary by section. [Full journey list and checks](../docs/GAME_SECTIONS_013.md).

# Current Android update: 0.12.0 ? Voice Runner worlds

Voice Runner now defaults to its own live microphone setting, waits for calibration and offers explicit retry/touch choices on failure. Five original worlds add vines, thorns, moving pistons, wind, ice and lava. [Controls, world design and verification](../docs/VOICE_RUNNER_WORLDS_012.md).

# Speech correction: 0.11.1

Uses the phone?s installed English speech model (UK English on the test Samsung), with default-provider fallback and specific service errors. [Diagnosis and checks](../docs/SPEECH_SERVICE_FIX_0111.md).

# Current Android update: 0.11.0 ? Flag World Tour

15 games, 75 stages. New spoken-country flag quiz with 24 flags, five progressively faster stages, one coin per correct answer and typed fallback. See [flag game notes](../docs/FLAG_WORLD_TOUR_011.md) for speech-provider behaviour and phone testing.

# Face Chaos Android 0.10.0

The collection now has **14 games / 70 stages**. New: **Sound Grand Prix**, an analog sound-speed race against a CPU car, and **Head Dodge**, live calibrated left/right head steering through incoming traffic. Both support touch alternatives. See [controls, tests and first phone trials](../docs/RACING_GAMES_010.md).

All games now accelerate through 1.00x, 1.20x, 1.45x, 1.75x and 2.10x pace. Final obstacle, flight, collection and rhythm stages require perfect encounters. Speed is visible on the stage badge; controls retain real-time calibration and strength. See [stage speed and verification](../docs/STAGE_SPEED_091.md).

Actual on-device facial input is now connected to Emoji Gates, Expression Escape, Bubble Garden, Star Duet, Snack Parade, Smile Stack and Face Memory. Face check provides neutral calibration, reaction counters, sensitivity and recalibration. Teeth detection is experimental. All 12 games have reviewed stage difficulty; see [the input and game audit](../docs/FACE_INPUT_AND_GAME_AUDIT.md) for controls, checks and required human trials.

All 12 games now support microphone-free touch play across all five stages. Touch is the default, with remembered optional microphone choice and an Input method menu that preserves round progress. Quick taps/longer presses control jump height; Animal Choir uses short/long press rhythms. See [control methods](../docs/ACTION_MAPPING.md).

Expression Escape and Melody Hop now use live sound strength for actual jump height: soft sounds give low jumps, stronger sounds give higher jumps. The jump must clear the arriving obstacle or reach the note height. Both include quiet rearming, relative volume meters and touch practice. See [all game action mappings and test limits](../docs/ACTION_MAPPING.md). Facial actions remain touch previews.

All twelve games now have five saved stages, stage-completion screens, Next stage, replay of unlocked stages, original sound effects and optional background music. See [stages and sounds](../docs/STAGES_AND_SOUND.md). Music/effects are suppressed during microphone rounds; result jingles still play. Video exports remain silent.

Face Memory now has slower watch/remember/repeat stages, fresh sequences, in-scene illustrated answer cards and per-face feedback. See [the Face Memory update](../docs/FACE_MEMORY_UPDATE.md).

The collection now contains 12 games. Animal Choir adds dog/cat/bird sound-pattern practice and synthetic example buttons; Face Memory adds repeat-the-sequence play; Firefly Glow adds steady gentle sound control. See [current controls and limits](../docs/ANIMAL_AND_MEMORY_GAMES.md). The next priority is [validated facial and sound recognition](../docs/RECOGNITION_IMPLEMENTATION_PLAN.md).

The previous expansion added: Snack Parade (collect/avoid), Smile Stack (alignment timing) and Comet Launch (hum-to-charge, quiet-to-launch). Each has original illustrated artwork, matching instructions, top prompts and coin rewards. See [research, controls and test limits](../docs/NEW_GAME_COLLECTION.md).

Star Duet now has a constellation scene, matching illustrated instructions, alternating smile/sound prompts and one coin per completed pair. Its optional microphone mode accepts a fresh gentle sound after quiet; the smile is still simulated by tapping. See [Star Duet instructions and limits](../docs/STAR_DUET.md).

Voice Runner now has illustrated sky gameplay, matching tutorial cards, progressive top prompts, optional microphone sound-level control, hold-to-fly touch fallback and one saved coin per collected star. See [Voice Runner testing and limits](../docs/VOICE_RUNNER.md).

Choose Games > Voice Runner > Let's play. Allow microphone access, stay quiet for the first second, then hum gently to rise and stay quiet to descend. Switch the microphone option off for hold-to-fly touch practice. Real microphone sensitivity still needs a user test; synthetic sound, gameplay and native rendering tests do not establish real microphone accuracy.

Twelve games are available. Emoji Gates, Expression Escape, Bubble Garden, Voice Runner and Melody Hop use dimensional artwork; Star Duet now has an illustrated constellation scene. Facial detection and sound controls in the other games remain pending.

Recording remains opt-in and silent: enable Record my round in a game brief. The circle pauses/resumes, the square stops, and Clip opens review/save/share. Nothing uploads automatically. App clips are excluded from backup/device transfer; use Save to phone for clips you want to keep. Only the latest clip is accessible in the current review UI.

Build with Android SDK 36, AGP 8.13.0, Gradle 8.14.3 and Java 17+: `gradlew.bat :app:assembleDebug :app:lintDebug`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Camera2/TextureView provide the selfie preview, Canvas draws the transparent game, and MediaRecorder/EGL encode camera plus game into local MP4. The microphone is used only in opt-in Voice Runner, Star Duet, Comet Launch, Animal Choir and Firefly Glow gameplay, in the foreground, for transient volume analysis. It does not add audio to recordings.
