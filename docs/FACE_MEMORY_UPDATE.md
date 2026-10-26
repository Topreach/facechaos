# Face Memory presentation and interaction � Android 0.7.1

Face Memory now uses the same clean camera-overlay approach as the other illustrated games. The native answer-button row is removed; three illustrated face cards are part of the lower game scene and accept taps only during the repeat stage.

The sequence is presented one face at a time, with an upper prompt naming the shown expression, an ordinal counter and neutral gaps between faces. Sequence slots show the faces during watching, then hide unanswered items behind question marks. During recall, correctly entered faces fill their slots; the next answer is never revealed in the repeat prompt. The answer cards themselves show all three possible choices.

The sequence lengths remain two, three and four. Restarts use a fresh seed; a mistake replays the same sequence rather than replacing it. Adjacent faces differ. Playback is slower and the recall stage allows ten seconds. Three mistakes ends the round. Individual correct answers show Good job! Face matched; a completed sequence awards exactly one saved coin and Good job! +1 coin.

Tap Games > Face Memory, read the instructions and start. Watch the entire sequence; wait for Your turn; tap the illustrated cards in the remembered order. The main circle pauses/resumes/retries as before.

This is still a touch-controlled memory game. Live facial recognition is not connected. The shared facial-input implementation remains the next milestone.

Pure checks pass watch-stage timing, neutral gaps, ignored early answers, same-sequence retries, three-round progression and one coin per sequence. On the Samsung SM-A176B, camera-free native checks passed watch/repeat/correct states, card hit regions, ignored gaps, paused input and compact portrait layout. Existing twelve-game renders and synthetic 720 x 1560 MP4 encode/decode, pause/resume and orientation checks also passed.
