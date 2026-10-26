# Voice Runner ? five-world update 0.12.0

## Voice controls

Voice Runner previously inherited the global `microphone` preference, whose default was false. The game therefore started in touch practice unless that shared option was explicitly enabled. It now uses a separate `voice_runner_microphone` preference, defaulting to true, with a clearly labelled ?Use my voice (phone microphone)? switch. Explicit touch selection is remembered only for Voice Runner and no longer changes other games. No external microphone is needed.

Hum or say a comfortable sustained ?aaa? to rise. Quiet lets the aviator descend. This game measures live sound energy; it does not transcribe words or identify the speaker. Word recognition remains separate in Flag World Tour. The live input meter and LIVE VOICE/TOUCH labels make the selected mode visible. Live voice rejects touch flight actions. Music/effects are suppressed during mic play to avoid feedback; optional videos remain silent.

Flight waits for the one-second noise calibration, including after resume. A microphone startup/read failure pauses Voice Runner and offers Retry voice, Use touch or Stay paused. It no longer silently saves touch as the control mode after such a failure. Camera imagery is not required for voice control; local microphone samples are discarded.

## Original adventure sections

Each existing stage is now a distinct world with matching scenery, a stage-specific tutorial image, obstacle prompts and eight encounters. All art is original Canvas scenery around the existing aviator artwork.

| Stage | World | Real obstacle behaviour |
| --- | --- | --- |
| 1 | Cloud Meadow | Learn to align with cloud-hoop stars |
| 2 | Jungle Ruins | Alternate lower gaps under vines and higher gaps over thorns |
| 3 | Clockwork Canyon | Moving piston gaps alternate with wind tunnels that change flight acceleration |
| 4 | Frost Caverns | Fly below ice ceilings, then track drifting ice gates |
| 5 | Ember Keep | Rise over lava jets, then follow moving firebar gaps |

Scenes have distinct colour palettes, scrolling scenery, obstacles and safe-gap brackets. The selfie face remains uncovered in the central camera area. Each safe passage earns one coin; collision is checked against the current actual gap at obstacle arrival. Speed increases from 1.00x to 2.10x; final stage needs all eight clears and has one life. The current game remains a voice-controlled flying course; the Mario reference informs world progression and varied obstacles rather than using Nintendo characters or assets.

## Verification

Plain-Java checks cover all 75 game/stage winning paths and existing face/head/flag/racing/sound regressions. New VoiceCourseCheck drives the actual SoundGate ? MiniGame chain with synthetic PCM at the microphone?s 50 ms cadence, across five timing offsets per world (25 voice-driven paths). It also checks quiet/voice readiness, live-touch isolation, distinct worlds, collision lives, moving gaps and actual wind physics. No microphone recording is performed by automated checks.

Native scene checks render both obstacle types in all five worlds at normal and compact portrait heights, plus five world-specific tutorial cards. Real-device voice sensitivity and noise tolerance still need the owner's live trial.

## Phone test

Games ? Voice Runner ? leave ?Use my voice? enabled ? Start. Allow microphone access. Stay quiet during calibration. The label should show LIVE VOICE. Hum or say ?aaa? to rise, then stop to descend; the meter should move with your voice. Complete a stage and press the circle for the next world. To inspect an unlocked earlier world, use Game options ? Stages. In a noisy room, pause/resume and stay quiet to recalibrate. Touch is an explicit fallback under Game options ? Input method.

## Installed verification ? 25 October 2026

Build and Android lint passed. All 16 pure rule suites passed, including 75 campaign stages and 25 synthetic PCM-driven voice paths. Samsung SM-A176B passed twenty normal/compact world-obstacle renders and five matching tutorial renders; central selfie areas remained clear. Final compact preview was visually reviewed with avatar-sized safe openings. Device package verified as versionCode 27 / 0.12.0-worlds, test package removed and Face Chaos reopened. Artifact: `dist/FaceChaos-Android-0.12.0.apk`. Live user microphone accuracy remains to be tested by pressing Start with Use my voice enabled.
