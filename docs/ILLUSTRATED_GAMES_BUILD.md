# Illustrated camera games — Android 0.3.0

The approved concept is now being implemented with separate transparent artwork
over the real selfie camera, not by displaying the flattened concept image.
The concept's fictional people are never included in the app resources.

## Implemented visual changes

- Emoji Gates: illustrated yellow character with three expression poses; pink,
  cyan and lavender hollow gates; a mint perspective runway with gold rails,
  flags, clouds and stars; compact hearts/count; short floating cue. Gates
  approach the character, then pass or miss with a visible transition. The
  character bobs and changes expression after matching; passage produces stars.
  A point is awarded only after the pass animation, not at the initial tap.
- Bubble Garden: illustrated terracotta pots, seed/bud/blossom states, watering
  character, foliage, glass-like bubbles and blossom feedback. Three pots form
  the current group; six flowers complete the round.
- Melody Hop: illustrated headphone character, dimensional stepping stones,
  musical note, luminous timing ring and a curved musical path. The character
  bounces after a successful timed action.
- Those three games remove the old header/game rectangles and button rows.
  The camera strip has Games, a circular record control and game-options icon.
  Gameplay instructions still precede Start; compact cues remain in the scene.
- Expression Escape, Voice Runner and Star Duet retain their earlier prototype
  visuals pending the same artwork/animation treatment.

This is 2.5D sprite artwork plus native Canvas geometry/animation, not a real-time
3D engine or an exact pixel reproduction of the concept. Responsive positioning
and real phone camera framing can change the composition.

## Current input and recording limits

These are touch previews. In the three illustrated games, tapping the game
simulates the requested action. The pause/options menu offers individual named
touch controls, pause/resume, restart and Camera only. The brief and a small
in-game hint identify preview input. No expression or sound recognition is
implied by the character's drawn face or headphones. Recording is still a
non-recording control preview. The camera opens clean before game selection.

Emoji Gates accepts a matching input during a 5.5-second approach, then plays
a 0.95-second pass or miss. Matching ends the gesture requirement immediately;
the player is told to relax while the hero passes. Six passes win, three misses
end the round. The overall safety timeout is 65 seconds to allow retries.

## Artwork provenance and transparency

New artwork was generated with the built-in image tool, without copying another
game's sprites. Raw atlases are included under
`android-app/app/src/main/res/drawable-nodpi/`:

- `emoji_atlas.png`: three hero poses and three arch gates.
- `garden_music_atlas.png`: three growth states, gardener, musician and note/stone.

Generation prompts are saved in
[emoji-atlas-prompt.md](../mobile-design/concepts/emoji-atlas-prompt.md) and
[garden-music-atlas-prompt.md](../mobile-design/concepts/garden-music-atlas-prompt.md).
The renderer uses source rectangles rather than editing/rasterizing the atlases
into fake camera screenshots. Alpha metadata and sample pixels were checked:
outside objects and inside the gate openings are transparent. Cropping regions
were measured from the actual image bounds; the generated rows were not assumed
to be exactly equal in height.

## Validation scope

Pure-Java rules checks cover winning and losing playthroughs of all six modes,
pause/countdown/wrong input, progressive prompts, a matched gate waiting for
passage, deferred single-point reward and animation pause. Native instrumentation
renders the actual game views offscreen, including gate approach/pass/miss/results
and a compact viewport, without reading camera or microphone imagery.

Rendered images are game-only composition checks over a neutral background.
They cannot prove live face visibility, sustained frame rate, camera quality,
human enjoyment, real facial/sound detection, video recording or release quality.
Those require separate real-device playtests and further implementation.

Build evidence on 24 October 2026: the rules checks passed for all six modes,
Android assembly/lint passed, and the native scene check passed on Samsung
SM-A176B for six modes, five gate states and the compact portrait viewport.
The three new illustrated scenes plus pass/miss/results/compact renders were
visually inspected. The result fixture was then corrected to represent actual
finished-round state rather than retaining its initial Get ready text.

APK artifact: `dist/FaceChaos-Android-0.3.0.apk`. Camera-free rendering artifacts:
`dist/illustrated-games/`. These are not live-camera screenshots. The three
illustrated modes are the current visual implementation; the other three modes
and missing input/recording features remain as explicitly listed above.
