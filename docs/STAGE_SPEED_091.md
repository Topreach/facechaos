# Stage speed and final challenge - Android 0.9.1

Every game now advances its simulation and cue clock faster at each unlocked stage:

| Stage | Pace | Role |
|---|---|---|
| 1 | 1.00x | Learn the controls |
| 2 | 1.20x | Quicker reactions |
| 3 | 1.45x | Fast play and tighter precision |
| 4 | 1.75x | Expert timing |
| 5 | 2.10x | Final challenge |

The stage badge and instructions display the active multiplier. Speed affects approaching objects, block motion, jump trajectories, demonstration sequences, cue deadlines and transitions. Existing narrower stage windows and longer memory patterns also apply, so a final stage is substantially harder than doubling a decorative animation.

Example: Emoji Gates gives about 5.5 seconds before a gate arrives in stage 1, then 4.08, 2.97, 2.11 and 1.48 seconds. Its target grows from six to ten gates. The crossing animation also accelerates.

Stage 5 has **one life in every game**. Stages 1-4 retain three lives, so the final challenge demands mastery without changing recognition sensitivity.

## Expert final targets

- Expression Escape / Melody Hop: 8 of 8 obstacles or hops.
- Voice Runner: 8 of 8 cloud stars.
- Snack Parade: all 6 treats; avoid all 3 peppers.
- Smile Stack: 8 of 8 placements.
- Comet Launch: 6 of 6 launches.
- Animal Choir / Firefly Glow: 6 of 6 patterns or lanterns.
- Emoji Gates, Bubble Garden, Star Duet and Face Memory retain their own gate, sequence and response-deadline challenges. Face Memory's final stage contains sequences of 4, 5 and 6 expressions, demonstrated at the faster pace.

Failure still offers Try again on the same stage, and completed stages stay unlocked. Stage 5 is intended for mastery. No claim about the percentage of players who can finish it is possible without playtesting.

## Controls stay fair

The two-second ready countdown, recognition thresholds and neutral calibration do not accelerate. A given real press duration still produces the same jump strength. The microphone's 150 ms strength sample stays real time, and its cue window compensates for sampling latency. Animal pulse/duration requirements remain real seconds, avoiding chirps shorter than the input update interval. Firefly steady-hold progress also uses real time: later stages require a longer actual hold, rather than accidentally shortening the hold when the world accelerates.

Facial sampling requests now run at up to 12.5 fps with the existing single-worker bound, reducing timing granularity for faster face actions. Frames remain local and transient. Face-required timers still freeze if tracking is unavailable. Touch controls remain available.

## Verification

The plain Java suite passes all 60 stage winning paths, all 60 touch-only paths, live microphone jump paths and prior expression/adaptor checks. New checks measure actual clock advancement in all 12 games at every stage, preserve real-time countdown/press/hold behavior, verify that a missed encounter fails the perfect final targets, and complete 25 jump playthroughs at the real 50 ms microphone callback cadence with different callback phases.

Android build and lint checks pass. Samsung SM-A176B checks passed on 25 October 2026: native game/tutorial/stage/face-status rendering, actual model inference on the public portrait and blank fixture, MP4 encode/decode and audio-asset decoding. Visual inspection confirmed the final-stage 2.10x badge and one-life display. No camera/microphone samples were captured by these checks. Human balance testing of this release remains necessary. In particular, recognition timing, fatigue and real player success rates require playtesting; synthetic winning paths establish possibility, not a polished difficulty curve for every player.
