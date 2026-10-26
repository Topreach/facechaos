# Face Chaos implementation audit — 23 October 2026

**Verdict: incomplete campaign prototype; not a release candidate.**

**Follow-up quality review:** the three reproduced gameplay defects below are
now corrected and covered by `tests/quality_checks.gd`. Validated temporary-save
replacement, backup recovery and damaged-file preservation were added. Python
coverage is now 76 tests; all nine course simulations and gameplay checks pass.
See [quality and creative review](QUALITY_REVIEW.md) for the current evidence.

Scope comes from the approved game design and later user decisions, not a generic
online game checklist. External sources below cross-check engineering practices;
they do not certify this game. No completion percentage or release date is assigned.

## Evidence collected

- Current source inspected: game.gd, campaign.gd, reaction_bridge.gd, recognition
  lab/sound code, project configuration, content inventory and design/backlog.
- Python unit suite: 73 passed on this audit run. Includes synthetic image/control
  tests, not a human-camera accuracy measurement.
- Godot gameplay regression: zero failures.
- Godot campaign regression: zero failures, all nine full keyboard courses
  simulated, including no-damage completion and unlock/reload checks.
- Additional runtime probes reproduced the three open defects below.
- Earlier rendered menu/game/results layouts were inspected; no new manual
  human playthrough, microphone classifier trial, camera trial or stability run
  was performed during this audit.
- Godot printed a system root-certificate-store warning during headless runs;
  the local checks themselves completed.

## Checks in order

Pass below means only the listed test passed, not that a whole product area is
finished. Partial means implemented with missing requirements. Missing means no
implemented system found. Unverified means real-device/player evidence is absent.

| # | Check | Status | Finding / remaining acceptance |
| --- | --- | --- | --- |
| 1 | Startup and menu | Partial | Project loads; buttons and keyboard shortcuts use countdowns. Countdown pause/resume and retry checks pass. No complete profile/mode/setup flow. |
| 2 | Keyboard abilities | Partial | Jump, fly, shield, chomp and cooldown regression checks pass. No key remapping/controller mapping, coyote time or jump buffer. |
| 3 | Movement and collision | Corrected / bounded tests pass | Physics ticks and substeps replace variable-frame movement. Crossing tests at 100/400/900 ms and equal jump arcs at 30/60/120 sample rates pass. No claim of general continuous collision for future hazards. |
| 4 | Hazard responses | Partial | Three distinct hazards; schedules and nine bot runs pass. Chomp direction is corrected and tested. Five slice hazards and expanded interactions remain absent. |
| 5 | Health and recovery | Automated pass / partial | Three hits end a run; grace rejects repeated hits; retry restores health. No pit/checkpoint recovery, impact explanation or full failure presentation. |
| 6 | Course finish and scoring | Automated pass / partial | Finite finish, heart bonus, results and repeat-completion guard pass. Coins, timing/clean-play scoring and separate control/assist records absent. |
| 7 | Level progression | Automated pass / partial | Nine courses, locked selection, next-level countdown and saved highest unlock pass. Only three of the latest eight-stage plan exist. |
| 8 | Save durability | Improved / tested | Validated temporary file replacement, previous-valid backup, corrupt-primary recovery, player notice and damaged-file preservation tested. Full version migration, deletion UI and power-loss stress tests remain. |
| 9 | Facial controls in game | Core connection implemented / camera accuracy unverified | Current lab optionally sends calibrated mouth_open, brows_up, smile and teeth. Real UDP detector-to-ability test passes, including chomp. Calibration/trials are blocked. Other advanced reactions are not routed. |
| 10 | Each facial detector | Unverified / reported failures | Software fixtures pass but real precision/recall, false triggers and latency are unmeasured. Tongue and cheek puff have user-reported failures. Eye widening still uses an eyebrow proxy. |
| 11 | Mouth/vocal sounds | Partial guide; recognition missing | Four searchable instruction tabs and opt-in volume meter exist. No hum/sh/ah/whistle classifier or corresponding gameplay action. A level meter is not recognition. |
| 12 | Face/device recovery | Automated pass / partial | Legacy packet validation, deduplication, timeout, tracking pause and keyboard countdown checked. Actual unplug/reconnect, OBS contention and process crash trials remain. |
| 13 | Onboarding | Partial | Written instructions, cue banners and button countdowns exist. Missing demonstrations, mandatory practice checks, capability-aware course selection and consistent keyboard-shortcut countdown. |
| 14 | Full campaign | Missing content | Latest approved extension is eight stages / 24 levels; current build is three stages / nine short courses. Sound worlds and finale missing. |
| 15 | Seven game modes | Missing content | One partial obstacle-race mode. Try Not to Laugh, Don't Scream, Mouth Power, Face Battle, Sound Madness and Chaos Survival are not implemented. |
| 16 | Characters/art/animation/audio | Prototype | One procedural face and simple shapes/palettes. No four-character roster, production animation set, licensed music/action audio or full asset inventory. Lab reaction notes are not game audio. |
| 17 | Comedy Director/rewards | Missing | No authored Director event system, eligibility/history, safe-window selection, cosmetic economy or character unlocks. |
| 18 | Accessibility | Partial | Keyboard-only play and visual hazard differences exist. Missing remapping, controller-only completion, text scaling, reduced motion, adjustable timing and assist presets. |
| 19 | Studio/highlights/voting | Missing | No in-game studio, portrait composition, replay/highlight pipeline or audience vote adapter. OBS installed on the PC does not implement these systems. |
| 20 | Performance/stability | Unverified | No representative hardware benchmark, p95 response measurement, two-hour play/reconnect run or broad participant trial. Passing short headless runs is insufficient. |
| 21 | Windows distribution | Missing | Source ZIP exists; no export preset, distributable game/runtime/model bundle or clean-machine install/launch/device/save test. |

## Baseline defects — corrected in follow-up

Run `godot --headless --path . --script res://tools/audit_gameplay.gd`.
This probe prints observations; exit code zero means the probe ran, not that
the reported behaviors are accepted. It does not save progress or scores.

1. **F-01 — Collision tunneling under a frame hitch (high).** Ground obstacle
   starts at x=260 and moves to x=128 in one 0.4 s update, crossing player x=180.
   Hearts remain 3 because only the final overlap is tested. Movement/collision
   belong on a controlled physics step with appropriate crossing checks.
2. **F-02 — Shortcut skips start countdown (medium).** Space from the menu
   produces running=true, countdown=0.0. Clickable Play uses three seconds.
3. **F-03 — Backward chomp (medium).** A biscuit at player_x-35 is removed by
   attack and awards 35 points; absolute-distance overlap ignores facing. The
   design specifies a forward attack.

These were reproduced in the initial audit and then corrected. The quality
regression explicitly checks each failure scenario; no failed assertion was
removed to claim success. The probe now prints the corrected expected behavior.

## Online engineering cross-check

- Godot distinguishes variable-rate `_process` from fixed-rate physics updates,
  recommending physics processing for collision movement. This supports making
  F-01 the first correction; fixed stepping alone is not proof that all crossing
  cases are solved. [Official Godot processing documentation](https://raw.githubusercontent.com/godotengine/godot-docs/master/tutorials/scripting/idle_and_physics_processing.rst)
- Accessibility guidance includes remapping, adjustable interfaces, alternative
  digital inputs and visual equivalents for important sounds. Current keyboard
  fallback is useful but does not satisfy the full planned accessibility scope.
  [Game Accessibility Guidelines](https://gameaccessibilityguidelines.com/full-list/)
- Godot's Windows export flow produces an optimized runtime and packaged project
  data. The current source snapshot is not that exported distribution.
  [Official Windows export documentation](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_windows.html)

## Ordered correction and acceptance queue

1. Completed: F-01 physics/substeps and bounded frame-rate/stall checks.
2. Completed: F-02 shortcut/retry countdown; pause/countdown/resume checks.
3. Completed: F-03 forward chomp checks; broaden counter coverage as hazards expand.
4. Save backup/corruption recovery added; full release durability testing remains.
5. Validate each facial reaction separately before lab-to-game integration.
6. Implement and validate each sound independently, including confounds and game
   audio; retain alternatives and resolve audio/face action conflicts.
7. Finish one polished level and conduct a new-player setup-to-finish trial.
8. Expand campaign, modes, characters, comedy and accessibility against the
   approved scope; then complete release/export/stability/broadcast checks.

## Design reconciliation

Later user decisions remove Frown, name Eye widening, distinguish closed-lip
smile from teeth baring and add eight-stage sound progression. Some older design
tables still mention raised eyebrows/frown, 12 race levels and 120-second courses.
The design tables now reflect those latest user decisions and 24-level campaign
target. Current 28/34/40-second courses are explicitly prototype teaching levels,
not evidence that the older full-slice target is delivered. The complete
seven-mode game has not been reduced to the nine-course prototype.
