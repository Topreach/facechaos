# Mobile game collection — first playable rules

These are six original, short gameplay slices over the selfie camera. Version
0.2.0 implements touch previews. Facial-expression detection, sound detection,
microphone capture and combined video recording are not implemented in this
build. Do not present manual actions as recognized face/sound input.

## Research applied before implementation

- [Android app quality](https://developer.android.com/quality) frames quality
  around core value, user experience, technical quality and privacy. A decorated
  chooser alone does not establish a finished game: each slice needs an actual
  interaction, outcome and replay path.
- [Microsoft XAG 107: Input](https://learn.microsoft.com/en-us/gaming/accessibility/xbox-accessibility-guidelines/107)
  addresses timing, complexity, sustained holds and alternative inputs. Here,
  touch previews use the same game action IDs intended for future face/sound
  adapters. There are no simultaneous face/sound combinations, rapid tapping
  requirements or sustained expression holds. Short rounds, pause and broad
  response windows are the starting design; real-player comfort is untested.
- [Apple feedback guidance](https://developer.apple.com/design/human-interface-guidelines/feedback)
  calls for clear, consistent feedback. The collection uses matching visual
  highlights, response text and visible result states; it never treats a colour
  change alone as a necessary instruction.

These sources inform the design; they do not certify professional quality or
prove novelty. The concepts and vector artwork were created for Face Chaos.

## Distinct games

| Game | Play loop | First course / failure | Visual identity |
| --- | --- | --- | --- |
| Emoji Gates | Match the highlighted expression, then relax | Six matches; seven seconds per prompt, three missed prompts ends round | Golden parade arches, expressive yellow hero |
| Expression Escape | Time jump/shield/chomp against three obstacle types | Clear at least six of eight encounters, three misses ends round | Apricot biscuit world, mint trail, falling stars |
| Bubble Garden | Alternate planting and blossoming | Six flowers from twelve alternating actions; nine seconds per step, three misses | Pastel petals, terracotta pots, green stems |
| Voice Runner | Lift a floating hero and let it drift to collect cloud stars | Five of eight cloud checkpoints; three misses | Blue sky jewels and floating clouds |
| Melody Hop | Play a note in a visible timing window | Six of eight notes; three misses | Lavender staff, travelling note, luminous timing ring |
| Star Duet | Alternate face spark and sound star, without simultaneity | Six turns; seven seconds per cue, three misses | Pink starlight joining two characters |

The touch buttons simulate the named action. “Sound”, “hum” and “face” in the
catalogue describe the intended final channel. A microphone-level trigger must
not later be labelled as validated hum or speech recognition.

## Shared presentation and rules

Version 0.2.1 introduces **Select game → brief and instructions → Start →
countdown → progressive action cues**. The brief includes a short description,
how to play, each expected face/sound action and the current touch-input limit.
No round begins until Start is pressed; Back leaves the chooser available.
During play, action wording and step hints follow the actual game state, including
plant/bloom transitions, face/sound turns and wait/act/relax timing states.

Camera opens clean. Games are selected from a coloured bottom panel with a
short description and instructions. The game occupies the lower camera area
with transparent surfaces; the central face area remains open. Gold, apricot,
mint, sky blue, lavender and pink distinguish modes, alongside text and shapes.
Original vector art scales with the viewport without raster enlargement.

Every course starts with a two-second countdown, ignores input during pause,
supports restart/close, freezes after results and saves a best score keyed by
game and touch control type. Wrong action IDs do not progress the course.
Input cooldown limits burst rewards. Timing modes reward each encounter once.
Rendering stalls advance at most 50 ms per frame to avoid instant failures.
Backgrounding the app stops the camera and pauses the game.

The first slices have no recorded soundtrack, microphone input, haptic settings,
motion-reduction option, difficulty selection or screen-reader description of
every live game state yet. The record control is still a non-recording preview.
Those limits prevent calling this a finished accessible/professional release.

## Validation and next gates

`android-app/checks/MiniGameCheck.java` checks all six winning and losing
playthroughs plus countdown, pause, invalid/wrong input, cooldown, rhythm timing
and score lock. These deterministic checks do not prove human playability.
`SceneCheck` renders actual native game views offscreen without accessing a
camera/microphone. Android compilation/lint and on-phone playtesting are separate
checks. Inspect text clipping, face overlap, timing clarity, input effort and
frame pacing on the Samsung phone before expanding courses.

Next gates: human review of each course, screen-size/accessibility refinement,
validated mobile facial controls, calibrated comfortable sound controls and
composited recording. Publish recognition accuracy per control and device,
separately from game rule tests. Tune individual games from playtest evidence.

## Build evidence — 24 October 2026

- Android 0.2.0 (version code 6) compiled; lint: 0 errors, 7 warnings
  (portrait restrictions and untranslated UI strings remain prototype work).
- All six rule-level winning and losing playthroughs passed, with the additional
  input/lifecycle-state checks listed above. A failing Voice Runner playthrough
  led to reduced lift strength before the final passing run.
- All six native views rendered successfully on Samsung SM-A176B through the
  offscreen instrumentation check. No camera/microphone content was captured.
- All six rendered scenes were inspected; hero opacity, expression mouth shapes,
  countdown feedback and missed-obstacle animation were refined. Updated Emoji
  Gates and Voice Runner renders were inspected again after those changes.
- APK installed and app reopened on the connected phone. Full human gameplay,
  face/sound control accuracy, recording and sustained performance remain untested.
- Artifact: `dist/FaceChaos-Android-0.2.0.apk`. Game-only render artifacts:
  `dist/mobile-game-scenes/game-0.png` through `game-5.png`.
