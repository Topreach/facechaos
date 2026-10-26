# Melody Hop scrolling course - 0.17.0

Five authored difficulty tiers: Simple, Difficult, Hard, Extreme and Finale. The terrain approaches from the right while the runner stays at the take-off marker. Each stage has eight authored obstacles with changing landing heights, rather than eight identical stationary hops.

## Actual rules

- Ember gaps, raised steps, twin fire pits, tall towers and drifting ledges enter progressively. The ledge drift changes the landing-height rule as well as its drawing.
- Average required height increases at every tier. World speed and stage pace both increase; sound timing windows narrow.
- Later raised steps have low overhangs. Too little height falls short; too much height hits the overhang and falls. Maximum volume cannot solve every obstacle.
- The power marker is an approximate guide for a well-timed launch. Volume and timing both affect clearance. The microphone still calibrates against ambient noise; it does not measure musical pitch or an individual's maximum comfortable volume.
- Successful landings earn one coin. A fall removes one life at impact. Stage 5 requires eight successful obstacles with one life. Failure restarts the run at Stage 1 while keeping the per-game best run.
- Camera recovery follows a successful climb, then joins the next moving platform. The game area dimensions, face-aware captions and recording controls are retained.

## Validation

Forty controlled-power authored obstacle landings passed. Checks verify increasing real heights and speeds at all five tiers, approaching terrain, weak final-stage jumps, actual excessive-power overhang failures, drifting collision height and paused movement. Existing PCM sound-to-jump tests, ten microphone-stage paths, twenty-five microphone cadence paths, all 75 game-stage winning paths, touch-practice paths and fire-impact checks passed.

Live voice comfort and difficulty feel require player testing. Synthetic success does not prove that every person or microphone finds the course comfortable.

Android build and lint passed. Samsung SM-A176B: 80 authored obstacle scenes at two sizes, 15 approaching-course frames, five matching tutorials and 70 lifecycle scenes passed. Corrected overhang screenshot visually reviewed; duplicate course label removed to keep hazards unobstructed. Final release is versionCode 43 / 0.17.0-course. Tests used synthetic input without camera or microphone access. Live player testing remains necessary for comfort and difficulty feel.
