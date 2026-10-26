# Face Chaos — Implementation backlog

**Mobile direction, 24 October 2026:** follow the
[mobile application design](MOBILE_APP_DESIGN.md) for Android-first delivery,
camera quality targets and the subsequent iPhone port. The milestones below
record the desktop prototype backlog; they do not imply a mobile build exists.
The next mobile engineering gate is a real-device native camera/analysis/rendering
spike, followed by one complete portrait game course.

Follow [GAME_DESIGN.md](GAME_DESIGN.md) as the product baseline. This backlog describes work to do, not completed functionality. Each milestone ends with an evidence report before dependent scope expands.

Campaign implementation update: the [Expression Escape build](../PLAY_EXPRESSION_ESCAPE.md)
now has three playable stages with three short authored courses each, three hearts,
damage recovery, finish/results, saved unlocks and Next level flow. Campaign data
lives in `scripts/campaign.gd`. All nine courses pass scripted keyboard
playthroughs. This advances the playable prototype while recognition validation
continues separately. It does not complete M2's 120-second slice, production
art/audio, five hazard types, accessibility or facial completion acceptance.

## M0 — Design

- [x] Define all seven complete mode loops, campaign, controls, presentation, and progression.
- [x] Define screens, architecture, data ownership, failure behavior, and acceptance gates.
- [x] Align the recognition contract with the user's four core mappings.
- [x] User authorized implementation of the baseline on 23 October 2026.

## M1 — Real facial control

Current user instruction: complete and validate the full facial vocabulary, including
face squeezing, in a standalone test lab before connecting additional actions to the game.
The approved [sound and stage extension](SOUND_CONTROLS_AND_STAGES.md) adds four
sound candidates. Their instruction/search UI and opt-in microphone volume check
are implemented; classification, audio/face priority and real-user validation
remain pending before gameplay integration.
See [Facial Reaction Lab](../FACIAL_LAB_START_HERE.md). Fourteen facial candidate sections,
three-repetition calibration, prompted suites, neutral tests, and derived report export
are implemented. Actual camera/user trials are pending; none of the advanced controls
has been accepted for gameplay.

- [x] Create an isolated Python runtime, tested dependency lock, and checksummed model manifest.
- [x] Implement Face Landmarker blendshape acquisition, camera index probing/selection, and a separate preview window (visual/user testing pending).
- [x] Add basic profile-specific neutral/action calibration, hysteresis, smoothing, and rearming (guided repeated trials pending).
- [ ] Implement and test mouth, eyebrow, and smile reactions individually.
- [ ] Assess show-teeth visibility detection separately; provide labeled alternative mapping if it fails.
- [x] Implement versioned packets, event IDs, deduplication, heartbeat, device/calibration states, and bounded validation.
- [ ] Extract the shared action router with cooldown, priority, and rejection feedback.
- [ ] Add setup, mapping, practice feedback, device recovery, and resume countdown.
- [ ] Collect target-device accuracy, false-activation, and latency evidence.

See [M1 implementation and validation status](M1_STATUS.md) for measured evidence and remaining work. M1 is not complete until actual expression trials pass.

## M2 — Finished vertical slice

- [ ] Replace monolithic gameplay with state, character, level, and mode components.
- [ ] Implement three hearts, safe recovery, jump buffer/coyote time, and clear collision counters.
- [ ] Author a finite 120-second course with safe sections and a finish gate.
- [ ] Build five distinct obstacle definitions and solvable schedules.
- [ ] Produce Bouncy's core animations and readable action/hazard effects.
- [ ] Add onboarding, countdown, objective HUD, results, retry, and action audio.
- [ ] Implement score idempotency, saved records, and one warned cosmetic comedy event.
- [ ] Run a new-player facial-control completion test and record usability findings.

## M3–M5 — First release content

- [ ] Director eligibility, event history, safe windows, warnings, and eight events.
- [ ] Twelve race levels, four themes, and four character animation sets.
- [ ] Stars, tickets, costumes, unlocks, versioned saves, and local leaderboard categories.
- [ ] Accessibility settings and keyboard/controller completeness.
- [ ] Try Not to Laugh with calibrated smile behavior and authored scene variations.
- [ ] Chaos Survival with capability filtering, perks, banking, and resume checkpoints.
- [ ] First-release playtest, performance report, and acceptance audit.

## M6 — Remaining modes

- [ ] Comfortable audio calibration and dependable loudness/onset detection.
- [ ] Don't Scream: three rooms, suspicion, gate rules, device recovery.
- [ ] Mouth Power: three key puzzles, safe reset, supported-action routing.
- [ ] Sound Madness: three beat maps, latency offset, grading, alternative inputs.
- [ ] Face Battle: player profiles, alternating turns, equal seeds, tie rules.
- [ ] Validate specialist inputs individually before enabling optional content.

## M7 — Broadcast and highlights

- [ ] Clean landscape and portrait composition, privacy switches, avatar, host controls.
- [ ] Local event timeline, deterministic replay feasibility test, optional recording/export.
- [ ] Provider-independent votes, safe outcomes, manual mode, simulated-vote tests.
- [ ] Verify current platform capabilities and permissions before connector implementation.
- [ ] Run real OBS capture tests in both layouts.

## M8 — Release

- [ ] Bundle Windows game, recognition runtime, licensed models, and notices.
- [ ] Test clean-machine install/start/device setup/save/restart/uninstall behavior.
- [ ] Complete two-hour stability, reconnect, corrupt-save, privacy, and accessibility checks.
- [ ] Benchmark target hardware and publish supported device/performance limits.
- [ ] Fix blocking defects and document remaining limitations without promising unverified capabilities.

## Current evidence

Existing prototype: keyboard runner, simple drawn visuals, experimental legacy webcam bridge, action cooldowns, bounded flight, manual pause, persistent best score, and headless regression checks. The user supplied a screenshot of the game running. Actual webcam recognition, all seven mode loops, calibration, and production assets are not yet validated or implemented as complete systems.

This repository's design gap analysis predates some gameplay fixes; use its feature comparison as historical context, and this backlog for current planned work.
