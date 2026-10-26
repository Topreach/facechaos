# Sound controls and stage progression

Approved direction: introduce facial movements and mouth/vocal sounds gradually.
This is a progression design, not implemented campaign levels.

## Sound instructions

Open **Sound Practice** from the Facial Reaction Lab. Each tab has a browser
search button. The examples button opens the university
[SpeechSTAR IPA charts](https://seeingspeech.ac.uk/speechstar/ipa-charts/).
The consonants m and sh (/ʃ/) demonstrate related articulation; the vowel /ɑ/
is one ah reference. Accents vary; exact imitation of a reference is not required.

| Control | Search phrase | Expected sound |
| --- | --- | --- |
| Humming | humming sound mmm | Lips together; gentle steady mmm for about one second. |
| Sh sound | sh sound pronunciation voiceless postalveolar fricative | Brief shhh, as at the beginning of shoe; do not say the word. |
| Ah vowel | open back unrounded vowel ah sound pronunciation | Brief steady ah with a comfortable open mouth; not the letter A. |
| Whistling | pursed lip whistling sound tutorial | Brief comfortable whistle; optional with keyboard alternative. |

## Stage sequence

| Stage | New control | Ability |
| --- | --- | --- |
| 1. First Steps | Jaw drop | Jump over gaps |
| 2. Biscuit Factory | Teeth baring | Chomp edible walls |
| 3. Falling Stars | Closed-lip smile | Shield falling hazards |
| 4. Humming Hills | Humming | Power a platform |
| 5. Windy Valley | Sh sound | Move objects with wind |
| 6. Echo Caves | Ah vowel | Send a sonic pulse |
| 7. Whistling Heights | Whistling, optional | Call a flying companion |
| 8. Chaos Castle | Previously learned controls | Final combined challenge |

Each stage has three finite levels: introduction, practice, final challenge.
Reach the exit objective to unlock the next level; the third unlocks the next
stage. Before a new control, demonstrate it and prompt three successful attempts
or offer the keyboard alternative. Practice completion is not detector validation.
Whistling alternatives must allow the entire campaign to be completed.

Use at most two or three active controls per level. Require neutral/silence
between cues. During ah prompts, audio owns the action: jaw-open must not also
trigger Jump. Request humming and closed-lip facial cues separately. Game audio
must not count as player input. Use brief sounds, not shouting or prolonged
holds. Scenery, puzzles and enemies also provide progression.

## Current implementation

Implemented: four instruction tabs, explicit browser links, opt-in local mic
selection, volume/clipping feedback, Stop mic and cleanup on close. Audio stays
in temporary memory and is discarded after computing level measurements.

Not implemented: sound classification, recorded demonstrations, recognition
confidence, sound game commands, finite campaign levels or persistent unlocking.
The meter checks microphone volume only. It cannot identify a sound.

Next recognition work: silence/noise calibration, positive and confounding
examples, held-out user tests, speaker/game audio rejection, cross-input priority,
and fallback mappings. Validate these before connecting sound actions to gameplay.
