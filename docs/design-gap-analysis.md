# Face Chaos — Design v1.0 versus current prototype

This assessment is based on source inspection and a Python syntax check. Godot gameplay, device access, dependencies, and recognition performance have not been verified on this computer.

## Current position

The project is an early endless-runner scaffold. It overlaps development phases 1 and 2, but neither phase's acceptance gate has been demonstrated. The specification's statement that no playable prototype exists should be updated to distinguish existing implementation from verified playability.

| Area | Existing implementation | Gap against specification |
| --- | --- | --- |
| Engine and presentation | Godot 4, one scene, code-drawn 2D character and obstacles | Character animation, authored comedy assets, sound, polished screens |
| Obstacle race | Jump, flight, shield, attack, collision, score, retry | Five mechanically distinct obstacles, level objective and finish, facial-only completion |
| Face recognition | Experimental mouth, eyebrow, smile threshold events | Calibration, blink, validated accuracy, smoothing, robust lifecycle |
| Input interpretation | Boolean event accumulation | Shared cooldowns, confidence semantics, deduplication, conflict rules, stale-input clearing |
| Audio | A placeholder `loud_sound` game input | Microphone acquisition, calibration, loudness detection; classification later |
| Tracking technology | Legacy MediaPipe FaceMesh interface | Migration to the specified Face Landmarker and reproducible dependency setup |
| Progression | Session score and in-memory best | Persistent scores, results, rewards, unlocks |
| Comedy Director | None | Warned events, event history, fair difficulty and control changes |
| Modes | One endless runner | Six additional modes and complete goals/end conditions |
| Streaming | Ordinary game window | Portrait layout, OBS verification, host controls; platform voting separately |
| Accessibility and setup | Keyboard fallback and text instructions | Device selection, sensitivity, remapping, pause, calibration and setup views |
| Release | Godot source project and Python script | Windows packaging, dependency distribution, licensing review, acceptance testing |

## Immediate defects and limitations

1. Holding shield or attack provides uninterrupted protection. Holding flight can move the player above the screen.
2. Reactions accumulate outside active play and can trigger when a run begins.
3. Bridge connection status never expires after packets stop; tracking loss is not communicated to the game.
4. Facial edges and held keyboard inputs have different action semantics.
5. Current mappings differ from the design: smile activates shield rather than bonus collection, and loud sound is mapped to attack rather than super jump.
6. Obstacle variants differ in color and radius, without distinct behavior. There is no level completion condition.
7. Best score is not persistent. Scoring continues for the remainder of the collision frame after game over is set, which can slightly disagree with the recorded best.
8. Recognition thresholds are fixed, and landmark distances mix normalized x/y coordinates without image-aspect correction. Camera aspect ratio can therefore affect measurements.

## Recommended sequence

1. Implement and validate the [Reaction Recognition Contract](recognition-contract.md): four core reactions, calibration, transport lifecycle, diagnostics, and shared action rules.
2. Complete one obstacle-race level with one character, five distinct obstacles, bounded abilities, persistent scoring, and a clear results screen. Demonstrate facial-only completion.
3. Add a small Comedy Director with warnings and a limited set of fair, tested events.
4. Expand controls and modes only after their recognition and gameplay gates pass.
5. Validate OBS layouts before implementing audience-platform connections, then package and run the release acceptance suite.

The full specification is a multi-phase product scope. Twenty screens and all proposed expressions should not be treated as requirements for the first playable milestone.
