# Current status: Android 0.9.0

Face controls are now connected; the current controls, harder stages and validation limits are documented in [the face/game audit](FACE_INPUT_AND_GAME_AUDIT.md). The sections below describe earlier 0.8.x behavior.

# Action mapping -> 0.8.2

Visible motion, instructions and input must agree. Volume means loudness, not musical pitch.

| Game | Player action -> game action | Current input status |
|---|---|---|
| Emoji Gates | Match expression -> pass matching gate | Touch expression preview |
| Expression Escape | Soft sound -> low jump; stronger sound -> higher jump. Smile -> shield; teeth -> chomp | Live volume jumps; smile/teeth touch previews |
| Bubble Garden | Smile -> plant; blink -> blossom | Touch expression preview |
| Voice Runner | Hum -> upward thrust; silence -> downward drift | Live sound gate; flight rather than jumping |
| Melody Hop | Soft sound -> low hop; stronger sound -> higher hop toward higher notes | Live volume jumps |
| Star Duet | Smile -> spark; then hum -> light star | Smile touch preview + live sound onset |
| Snack Parade | Open mouth -> catch food; closed mouth -> avoid pepper | Touch expression preview |
| Smile Stack | Smile at alignment -> place block | Touch expression preview |
| Comet Launch | Hum duration -> launch power; silence -> launch | Live sound duration |
| Animal Choir | Short/long sound patterns -> animal rhythm response | Live timing; no animal identity classification |
| Face Memory | Repeat expression order -> complete sequence | Illustrated touch answers |
| Firefly Glow | Steady volume -> steady lantern glow | Live sound-level range |

## Sound jumps

A one-second quiet baseline sets the noise threshold. A separate logarithmic strength measure spans 24 dB above that threshold, preserving differences beyond the binary gate meter. The first 150 ms of a fresh sound determine launch impulse. Gravity, height and landing are simulated. Clearance is evaluated when the obstacle arrives, rather than awarding a coin at sound onset. Low and high obstacles alternate. Timing and volume both matter. Quiet must rearm each jump. Touch cannot launch a live microphone jump. Pausing cancels pending sound sampling; speaker effects/music stay muted during microphone rounds.

The volume bar is relative to the noise baseline, not absolute sound pressure. Phone, room and voice tests remain necessary; individual soft/strong voice calibration is not implemented. Comfortable stronger sounds should suffice; no shouting.

## Checks

Synthetic checks cover all 60 stage winning paths, PCM volume separation, trajectory differences, tall-obstacle rejection for soft jumps, ten live-signal jumping-stage paths, quiet rearm, pause and live touch rejection. These do not establish accuracy on a person's voice or face.

## Microphone-free play -> 0.8.2

Touch is the default for new installations; a player's optional microphone preference is remembered. No external microphone/headset is required for sound mode: it uses the phone microphone.

Every game can complete all five stages without microphone input:

- Expression Escape / Melody Hop: quick tap for low jump, hold briefly then release during the gold window for high jump. Press duration sets launch impulse; it does not automatically select the correct height.
- Voice Runner: hold to rise; release to descend.
- Comet Launch: hold to charge; release in gold.
- Firefly Glow: hold to glow; release to rest.
- Animal Choir: two short presses, one longer press, or three short presses. The same rhythm/duration rules apply; a single tap no longer automatically completes the target.
- Star Duet: tap the smile cue, then tap the star cue.
- Other face games retain illustrated touch actions; Face Memory retains its three answer cards. Automatic facial recognition is still not connected.

Game options -> Input method switches between touch and optional microphone without resetting score, stage, hearts or coins. Pending touches, charge and unfinished sound patterns are cleared to prevent unintended actions. Permission refusal and microphone errors keep touch available. A noisy room can be handled by switching to touch; automatic noise classification is not implemented. Touch prompts and strength meters replace microphone prompts where applicable. Cancelled gestures and pauses do not launch a jump.

Validation: all 60 stages completed through the touch adapter, including timed press/release jumps and animal rhythms. Existing live-signal, stage, audio and game-rule checks also pass. Synthetic checks cannot establish usability for every person or device.
