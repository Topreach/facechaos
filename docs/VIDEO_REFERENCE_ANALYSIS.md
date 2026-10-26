# Uploaded emoji-matching game reference

Source supplied by the user: `Try and match the facial expression of the emoji
to win.mp4`, 64.7 seconds, 576x1024, 30 fps. Visual frames were inspected across
the clip and more closely between 24 and 64 seconds. Audio was not assessed.
Reference contact sheets are local analysis outputs under dist/video-reference,
excluded from the source package. No video frames were uploaded elsewhere.

## Observed on screen

- Portrait camera composition: the player remains visible behind an AR-like road.
- A yellow spherical avatar displays moving eyes and mouth resembling the
  player's visible facial features. It responds visibly as the player changes face.
- Brick wall gates have circular openings and a target emoji above each one.
- Targets include angry-looking, sad-looking, grinning, surprised and frightened
  emoji artwork. These describe the target icons, not the player's mental state.
- Gates appear in succession along the road. Multiple later gates are visible.
- Around 40 seconds, an open-mouth surprised target and an open-mouth avatar are
  visible; around 44 seconds the avatar has exposed teeth and a grin-like target
  is visible among the gates. At 64 seconds a checkered finish marker is visible.

The exact success condition, threshold, matching tolerance, scoring and failure
rules cannot be recovered from these frames alone. Some apparent mismatches still
coincide with forward progress. Do not infer that every gate is strictly checked,
or that this clip proves perfect emotion recognition. No visible sound meter or
sound instruction establishes voice control in this reference.

## Main lesson for Face Chaos

This reference is an **emoji expression-matching course**, rather than evidence
for the current jaw-jump / smile-shield / teeth-chomp mechanic. Its central loop
is: see a target face -> imitate its appearance -> see your avatar respond ->
reach the next gate -> reach a finish.

Our existing live meters, mirrored cartoon mouth, finite course and calibrated
event connection are useful building blocks. They do not yet implement that
matching course. The source was not changed into a clone during this inspection.

## Concrete proposed adaptation: Emoji Gates

Add an expression-matching challenge while retaining the existing action runner.
The first course uses three explicit movements that already have game transport:

| Gate artwork | Caption | Candidate measurement |
| --- | --- | --- |
| Open-mouth surprised face | Jaw drop / open mouth | Calibrated jaw opening |
| Closed-lip smiling face | Closed-lip smile | Smile with visible-teeth exclusion |
| Bared upper/lower teeth face | Teeth baring | Existing experimental teeth candidate |

Use original gate/face artwork and captions so Smile and Teeth baring are not
visually ambiguous. Do not ask players to guess a vague emotion. Do not restore
Frown just because the reference uses sad/angry icons: the user removed it.
Eye widening, tongue protrusion and cheek puff still need their own validated
signals before they can become mandatory gates.

### Interaction specification for implementation

1. Show the target emoji, searchable movement name and a simple animated example.
2. Give a neutral/rest cue, then a comfortably timed matching window. Prototype
   proposal: 1 second preparation and 4 seconds to respond, adjustable after trials.
3. Mirror measured eye/mouth appearance continuously; show a separate MATCHED
   indicator only when the target-specific predicate passes.
4. Accept one match per gate. Require return to neutral before the next prompt.
   A held pose must not clear consecutive gates automatically.
5. A match opens the gate and advances the avatar. A miss explains the target
   and retries or costs one declared heart; do not silently advance as success.
6. Begin with six prompts and a visible finish. Count valid matches, misses,
   cross-activations and tracking interruptions separately.
7. Tracking loss suspends the challenge. Manual keyboard practice is explicitly
   labeled and must not be scored as recognized facial matching.

Keep voice-controlled runner play as a separate control variant. This particular
video does not determine how voice controls should work.

## Acceptance before treating this reference as implemented

- A complete playable gate loop with target, detection feedback, pass/miss,
  advance, finish/results and retry.
- Distinct smile/teeth prompts and trials showing they are distinguished on the
  user's webcam. Software fixtures alone are not enough.
- No success from an unsupported detector, a stale event or a held expression
  carried over from a previous gate.
- Test target readability, response timing and comfort with a new player.
- Camera inset/portrait sharing requires its own explicit UI and testing; current
  UDP packets carry measurements/events only, not images.
