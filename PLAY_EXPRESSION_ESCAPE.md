# Play Expression Escape

For the camera-and-game composition, follow [integrated play setup](docs/INTEGRATED_PLAY_SCREEN.md).
During face play the live camera can appear behind the gameplay, with a hide
control and an avatar that mirrors blinks and mouth movement.

Open this folder's `project.godot` in Godot and press **F5**. Stop any older
running game first. The title should now say **Face Chaos - Expression Escape**.

1. Select **1-1 The First Bounce**, then **Play with Keyboard**.
2. After the countdown, press **Space** to jump over orange obstacles.
3. Reach the finish with at least one of your three hearts.
4. Click **Next level** to continue. Complete three levels to enter the next stage.

| Stage | Main challenge | Controls |
| --- | --- | --- |
| First Steps | Jump over orange obstacles | Space: jump |
| Biscuit Factory | Bite biscuits; later levels also add jump obstacles | A: chomp, Space: jump |
| Falling Stars | Shield purple stars; later levels also add jump obstacles | S: shield, Space: jump |

Nine authored courses run for 28, 34 or 40 seconds. Follow the approaching
hazards and time actions near the character; the spawn cue announces what is
coming, not an instruction to activate the ability immediately. The first
obstacle takes about two seconds to reach you. Chomp only destroys biscuits;
shield only blocks stars. Jump can avoid hazards. Surviving hearts award a
completion bonus. There are no gap/platform hazards yet.

**Escape** pauses, **R** retries the current course. Results allow replay or
selection of unlocked courses. Highest unlocked course and best score save in
Godot's local user-data directory. Failed runs do not unlock new levels. Writes
use a validated temporary file and retain a previous-valid backup; unreadable
saves recover the backup when available and show a notice. Damaged originals
are retained when replacing them after recovery.

The **Facial Lab - 14 Sections** button still opens the independent test lab;
its **Sound Practice** button opens sound instructions and the microphone meter.
Click **Connect Facial Controls**, calibrate jaw drop, closed-lip smile and teeth
baring in the current lab, return to neutral, then select **Play with Face +
Keyboard**. They trigger jump, shield and chomp. The send switch must be on.
Before play, the menu shows received levels and practice detection counts. During
face play, the LIVE FACE INPUT card and cartoon mouth reflect the input. The
last detection is separate from action feedback: cooldowns can reject an action.
These readings and counts are not recognition accuracy scores.
Optional Eye widening retains the brow proxy for flight. A remains a keyboard
alternative for experimental teeth detection. See [facial setup](FACIAL_LAB_START_HERE.md).
No sound classification or audio game action has been added. Humming Hills,
Windy Valley, Echo Caves, Whistling Heights and the combined
finale remain future content after recognition validation.

This is a playable campaign prototype with procedural artwork, not the finished
professional release. Fixed-step/substep physics, frame-hitch, attack direction,
countdown/pause and save-recovery checks pass, alongside nine keyboard playthroughs.
Visible playability on your computer and facial accuracy still need user tests.
