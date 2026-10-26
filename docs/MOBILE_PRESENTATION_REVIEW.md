# Presentation review — 0.2.1

24 October 2026. Trigger: the user rejected the visual quality. This is a candid
comparison of the current engineering prototype against published references,
not a claim that automated checks establish professional quality.

## Evidence inspected

- Our actual Android-rendered Emoji Gates scene:
  `dist/mobile-game-scenes/game-0.png`, plus the six game scenes inspected during
  implementation. These use a neutral camera-free background; it is not the
  actual live camera image or a full screenshot of the interactive controls.
- [Official Crossy Road press screenshots](https://www.crossyroad.com/crossy-road-classic-press-kit),
  including [this scene](https://images.squarespace-cdn.com/content/v1/5cedd5e7c6e7df0001bbb67c/1633684008439-KUTS5Y59LE91M22QOAOA/CRPlus_iOS_iPad-Pro_12.9-inch_2732x2048_02.png).
  The downloaded official image was opened and visually inspected. It shows
  consistent dimensional artwork, shadows, recognisable obstacles and sparse
  score/pause UI. This is a reference for craft, not a proposal to replace the
  selfie with an opaque game world or copy its artwork.
- [Alto's Odyssey official press kit](https://altosodyssey.com/press/): documented
  fluid physics, different environments, lighting/weather and original audio.
  This review read the official descriptions; it did not play the game or
  measure its animation/performance.
- [TikTok Effect House Card Matching template](https://effecthouse.us.tiktok.com/learn/guides/tutorials/template-tutorials/card-matching-game)
  documents separate game objects and UI, countdown, configurable transitions,
  victory animation and retry. It is a closer structural reference for camera
  games. The documentation was read; the template was not run locally.
- [TikTok HUD Toolkit](https://effecthouse.tiktok.com/learn/guides/tutorials/apjs-api/2d-ui-hud-toolkit)
  documents a camera-feed-backed HUD, graphical hearts, animated progress and
  explicit game-over state. It is a teaching sample, not a professional-quality
  visual benchmark by itself.

## Comparison and concrete deficiencies

| Area | Reference lesson / target for Face Chaos | Current prototype |
| --- | --- | --- |
| Composition | Selfie is the full-screen canvas; objects feel layered into the camera view; compact score and one clear cue | Large dark header, instruction panel and lower game rectangle make it resemble a test dashboard |
| Art direction | Consistent character proportions, materials, depth and readable silhouettes | Basic paths/circles, generic stars, text labels such as O/TEETH, limited character personality |
| Motion | Anticipation, visible action, consequence and recovery | Mostly static drawings, a few positional changes, instant rewards; no complete action animation sequence |
| Emoji Gates | A gate approaches, player reacts, character responds, gate passes/breaks, next obstacle arrives | Three stationary arches, changing target and instant score/character position change |
| Expression Escape | Jump trajectory clears an obstacle; shield visibly intercepts a projectile; chomp breaks a biscuit | Correct action during a time window awards score; these are not full collision/action implementations |
| Instruction | Brief before Start, then pictorial target plus a short cue integrated with the scene | Start/brief flow exists, but gameplay relies heavily on text panels rather than visual demonstration |
| Feedback | Success/failure is readable through motion, particles, sound and optional haptics | Text, colour and result stars; no game audio or haptic system |
| Input authenticity | Actual validated face/sound controls visibly drive the game | Touch previews only; essential face/sound connection absent |
| Finish quality | Cohesive chooser, brief, play, results and replay; tested on real users | Working engineering flows with default native buttons and limited visual/lifecycle playtesting |

The rules tests verify score, timing and state transitions. The offscreen tests
verify that Android can draw the scenes. Neither demonstrates fun, visual
quality, meaningful physical interactions, facial accuracy or completed games.
Describing the collection as polished overstated the work.

## Correction to the development approach

Preserve the clean camera entry, game selector and explicit Start flow. Treat
the other five game slices as prototypes while bringing **one** game to a
convincing quality bar; do not expand the catalogue with more similar drawings.

For Emoji Gates, the next complete slice should have:

1. A compact score/progress strip and pictorial target, with the player's eyes
   and mouth unobstructed. Remove the large rectangular game backdrop.
2. An original dimensional gate/track and expressive character, with coherent
   highlights, shadows, scale and perspective. The selfie stays visible around
   the objects; an opaque full-screen landscape would violate the product brief.
3. A real sequence: approaching gate -> response opportunity -> character action
   -> visible pass/miss -> recovery -> next gate. A score change is a consequence,
   not the entire game event.
4. A short illustrated instruction before Start, compact in-game cue and an
   input meter only when it helps the player. No technical diagnostics in play.
5. Character squash/stretch, gate motion, celebratory particles, readable misses,
   audio/mute and reduced-motion choices. Avoid face distortion in analysis.
6. A connected, calibrated facial control with neutral reset and false-trigger
   tests on the phone. Until then, keep touch mode explicitly identified.
7. A complete round and results/replay; inspect video of the actual moving game
   and test human playability. Still images and synthetic tests are insufficient.

These are new acceptance requirements, not completed changes. This review does
not modify the installed app, promise award-level quality or assert novelty.
