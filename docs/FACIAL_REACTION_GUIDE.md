# Facial reaction names and player instructions

Use these names in the lab and as online search phrases. Return to a relaxed
face between gestures. Left/right always means the player's own side.

| Name / search phrase | Expected movement |
| --- | --- |
| Jaw drop (mouth opening) | Lower the jaw; teeth do not need to show. |
| Quick mouth closure | Start open, then close the mouth quickly and gently. |
| Deliberate eye blink | Close both eyes for about half a second, then reopen. |
| Eye widening | Open both eyes wider, as if surprised. |
| Closed-lip smile | Lift both mouth corners while keeping lips together. |
| Teeth baring | Pull lips back and apart to expose upper and lower teeth comfortably. |
| Cheek puff | Keep lips closed, gently fill both cheeks with air, then release. |
| Tongue protrusion | Extend the tongue past the lips, then retract it. |
| Left eye wink | Close only the player's left eye. |
| Right eye wink | Close only the player's right eye. |
| Face scrunch | Gently wrinkle the nose and draw cheeks and brows inward, then relax. |
| Head tilt | Lean an ear toward the same-side shoulder, then return upright. |
| Head nod | Move the head down and back up, like saying yes. |
| Head shake | Turn a little left and right, then return to centre, like saying no. |

These are descriptive movement names, not emotion labels. Teeth baring here
does not require anger or showing all 32 teeth. A closed-lip smile is the game's
chosen smile variant; smiles in general can expose teeth. Quick mouth closure
and face scrunch are game gesture descriptions, not individual certified FACS
action units. Head movements are listed separately from facial muscle movements.

For deeper searches, FACS describes observable facial actions. Useful related
terms are **jaw drop (AU26)**, **upper lid raiser (AU5)** and **lip corner puller
(AU12)**. These references do not imply that the lab is a certified FACS coder.
See the [Paul Ekman Group FACS introduction](https://www.paulekman.com/facial-action-coding-system/)
and [UC San Diego visual reference](https://facsref.ucsd.edu/).

## Detector limitations

Names describe the intended player movement. Renaming does not improve detection.
Eye widening currently retains the user's earlier eyebrow-based proxy, not a
verified pure upper-lid detector. Tongue protrusion and cheek puff remain
unaccepted after failed user trials. Teeth baring uses experimental visible-teeth
evidence, not a tooth count or semantic recognition of both rows. Calibrated
teeth evidence takes priority over the closed-lip smile command.

Internal keys remain unchanged so compatible calibration files keep working.
