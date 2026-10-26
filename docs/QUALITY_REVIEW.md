# Quality and creative review

## Verdict

Integrated-screen follow-up: the [camera and gameplay composition](INTEGRATED_PLAY_SCREEN.md)
now shows the mirrored local camera behind gameplay, with hide/show control and
blink/jaw avatar rendering. Eighty-four Python tests pass; synthetic camera to
Godot transport, avatar telemetry, invalid metadata, hide/show and disconnect
checks pass. The layout was visually reviewed using an explicitly synthetic
camera image. Real webcam framing/latency and fully in-game setup remain open.

TikTok reference follow-up: [official examples and applied changes](TIKTOK_REFERENCE_REVIEW.md)
now inform live gameplay meters, setup practice counts, mouth response and brief
action feedback. This is an interaction refinement, not an imported TikTok model
or a claim of newly validated recognition.

Connection follow-up: the current facial lab now has opt-in core-control sending
and the game launches it through **Connect Facial Controls**. Jaw drop jumps,
closed-lip smile shields and teeth baring chomps; the eye/brow proxy flies.
The four core events passed a real loopback UDP test from prepared detector
inputs to Godot actions without keyboard input. This is transport/action
verification, not a real webcam accuracy result. Python coverage is now 80 tests.

The implemented prototype is more robust and coherent after this review. It is
not a complete professional release. Creative quality needs player feedback;
an automated check cannot establish that a game is funny, original or comfortable.
The remaining scope in [the implementation audit](IMPLEMENTATION_AUDIT.md) is
still applicable except for the defects explicitly corrected below.

## Corrections completed

- Moved simulation to Godot physics ticks and substeps of at most 1/120 second.
  Frame-hitch collision scenarios that previously skipped damage now hit once.
- Made menu shortcuts and keyboard retry use the three-second countdown. Pause
  freezes that countdown; resume gives a fresh countdown and clears old inputs.
- Restricted chomp to a biscuit in front of the character. Backward overlap no
  longer awards an attack bonus. Only biscuit/star counters consume attack/shield.
- Replaced direct score/unlock saves with validated temporary-file replacement.
  Retain the previous valid file as .bak, recover it after a damaged primary,
  notify the player, and preserve a damaged primary as .corrupt on replacement.
  This is not a claim of tested power-loss recovery or a complete profile system.
- Hardened microphone practice against invalid buffers, stopped input and stop
  errors. It still measures volume only; no sound classifier was invented.
- Corrected report metadata to identify facial detector revision 4. Reconciled
  the main design's eyebrow/frown/old campaign tables with later user decisions.
- Clarified that the optional gameplay bridge is Face + Keyboard and that the
  ninth-level ending completes the intro campaign, not the full planned game.

## Creative and presentation improvements

- Nine named courses, including The First Bounce, Cookie Conveyor and The
  Midnight Dash, give the introduction a readable identity.
- Short missions frame the three worlds as an escaped picnic, snack-factory
  shift and falling-star party. These are presentation hooks, not extra mechanics.
- JUMP, CHOMP and SHIELD labels travel above approaching hazards. Different
  silhouettes and words communicate the response alongside colour.
- Gameplay HUD now prioritizes level title, active controls, feedback, hearts,
  finish time and progress. Diagnostic connection details remain in setup.
- READY/cooldown feedback, three scene palettes, biscuit chips, factory backdrop
  and star shapes were inspected in rendered screenshots. Existing procedural
  art is placeholder quality; naming does not replace animation, audio or variety.

## Verification evidence

| Check | Result and limits |
| --- | --- |
| Python unit tests | 76 pass; includes synthetic facial imagery and mocked audio paths, not live accuracy |
| Godot gameplay regressions | Zero failures |
| Campaign simulations | All nine courses complete without damage using ordinary action inputs |
| Quality regressions | Zero failures: countdown pause/resume/retry, forward/backward chomp, 100/400/900 ms crossing updates, matching jump arcs at 30/60/120 sample rates, save backup/corruption/recovery |
| Hidden Tk UI workflow | Pass; facial lab and Sound Practice open/close/calibration workflows, no camera/mic/network or sound playback |
| Startup | Headless project startup completes; system certificate-store warning remains in this environment |
| Python compilation/dependencies | Compile check and pip check pass |
| Model integrity | Local model SHA256 matches the pinned manifest |
| Presentation inspection | Menu, results, meadow, factory and star-world screenshots reviewed |
| Source snapshot | CRC/file-hash integrity checked by the packaging tool |

The tests use isolated save paths and do not intentionally modify a player's
progress. Render previews do not open camera/microphone or write results.

## Unresolved before calling it a professional finished game

1. Validate actual facial recognition, especially reported tongue/cheek failures
   and the eyebrow proxy for Eye widening. Integrate only accepted controls.
2. Implement and test sound classification; resolve audio/jaw overlap and game
   audio feedback. The current four sound tabs are instructions, not detectors.
3. Deliver the remaining five campaign stages and six other mode loops, or
   explicitly agree a smaller release scope. Current content remains nine courses.
4. Add production character animation, original/licensed music and action sounds,
   stronger stage mechanics, comedy events and a complete onboarding experience.
5. Implement input remapping, controller and assist options, adjustable text,
   reduced motion, capability-aware course selection and full accessibility tests.
6. Test real devices, FPS/latency, prolonged comfort, two-hour stability, clean
   Windows distribution, packaging/licences and supported broadcast workflows.

The creative next gate is a complete new-player playthrough: can the player
understand each hazard, enjoy the stage, finish it, and want to replay without
uncomfortable effort? Record observations rather than assuming the answer.
