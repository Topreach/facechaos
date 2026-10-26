# Animal Choir, Face Memory and Firefly Glow � Android 0.7.0

The collection now contains 12 games. This completes the current game-expansion batch; the next priority is validated facial and sound recognition across the collection.

| New game | How it works | Current input |
| --- | --- | --- |
| Animal Choir | Follow dog, cat and bird cues: two short woofs, one longer meow, three short chirps. Complete 5 of 6 cues; each match earns one coin. | Optional microphone timing/duration matching. Touch practice simulates the response. It does not recognize animal identity. |
| Face Memory | Watch and repeat sequences of two, three and four expressions. Wrong answers replay the sequence and cost a heart. Each complete sequence earns one coin. | Three expression buttons; actual facial detection is pending. |
| Firefly Glow | Hold a gentle steady sound in the gold meter range for 1.5 seconds. Light 5 of 6 lanterns; each earns one coin. | Optional microphone level control or hold-to-glow touch practice. |

All three use original dimensional artwork shared with their illustrated instructions. Prompts stay in the upper banner. Existing camera overlay, silent recording, pause/resume/retry, saved coin balance and result cards are retained.

## Animal sound guides

Animal Choir's brief includes Hear dog, Hear cat and Hear bird buttons. These play original synthetic cartoon examples demonstrating two short sounds, one sustained sound and three chirps. They are not real animal recordings. Guides play only on request; playback stops before gameplay, when the brief closes or when the app leaves the foreground. The microphone is paused during the brief, so guide playback cannot earn game coins.

The live matcher checks pulses and durations after a short quiet interval. Other sounds with the same rhythm can pass. This limitation is stated in the brief and game prompt. Distinguishing genuine or human-imitated dog/cat/bird sounds requires a separately validated classifier; a volume threshold is not that classifier.

## Verification and limits

Pure Java checks pass all 12 winning/losing playthroughs, pulse timing, wrong-duration rejection, synthetic guide compatibility, memory replay, input blocking while showing, over-range sound rejection and lantern rewards. On the Samsung SM-A176B, camera-free native checks passed all twelve game/tutorial renders, twelve new animal/memory/lantern states and compact portrait layouts. Native 720 x 1560 MP4 encode/decode, pause/resume and orientation checks passed using synthetic frames. Actual microphone sensitivity, speaker playback and human usability need user testing; synthetic checks are not real-world recognition accuracy claims.

Art and saved prompt: [animal/lantern atlas](../mobile-design/concepts/animal-atlas-prompt.md). Next milestone: [professional recognition implementation](RECOGNITION_IMPLEMENTATION_PLAN.md).
