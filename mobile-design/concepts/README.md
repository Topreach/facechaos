# Face Chaos — original art direction v1

Open [the visual concept board](face-chaos-art-direction-v1.png).

Created with the built-in image generation tool after online image research.
This is an AI-generated design proposal containing fictional adult selfie
portraits, not screenshots of implemented games or an example of actual phone
camera quality. The installed Android build has not changed to match it.

Three directions share one app composition: full-screen selfie, compact score
and hearts, a short pictorial action cue, a dimensional game over the lower
camera area, and circular record control. Emoji Gates uses tactile arches and
an expressive mascot; Bubble Garden uses pots, petals and dew; Melody Hop uses
luminous stepping stones and notes. The other three modes still need comparable
art development. Do not imply facial controls or recording work based on this image.

## Image research

- [Crossy Road official artwork/screenshots](https://www.crossyroad.com/crossy-road-classic-press-kit):
  readable silhouettes, dimensional objects, sparse gameplay UI.
- [Cut the Rope image examples](https://www.mobygames.com/game/49034/cut-the-rope/screenshots/android/547078/):
  expressive character, clear goal and tactile playful objects.
- [Stumble Guys official site](https://www.stumbleguys.com/): bold obstacle shapes
  and bright, legible party-game presentation.
- [TikTok camera-game template](https://effecthouse.us.tiktok.com/learn/guides/tutorials/template-tutorials/card-matching-game):
  camera-backed game composition and distinct countdown/play/results states.

These are design references, not assets imported into Face Chaos. Their artwork
and characters are not included in the app. Broad visual principles informed
the new illustration; this does not establish legal uniqueness or game novelty.

## Implementation implications

The board is one flattened image. It cannot be used directly as a live game
overlay because it contains fictional camera photographs and baked text/UI.
Implementation needs separate transparent character/gate/plant/note assets or
equivalent 3D models, animation states, responsive native UI, actual game-world
motion, recognition and a tested camera/game composition path. Keep the real
player's image behind those separate objects.

Use this board as the visual quality target for one complete Emoji Gates slice
before expanding the catalogue. Keep the explicit brief/Start flow. In the
first real implementation, preserve face visibility and reduce any lower-scene
decoration that competes with gameplay or makes the camera seem replaced.

Generation mode: built-in tool, new image, opaque concept board. Prompt:
[image-generation-prompt.md](image-generation-prompt.md).
