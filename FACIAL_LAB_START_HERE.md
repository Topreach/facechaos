# Face Chaos — Standalone Facial Reaction Lab

## Why this lab

See the [per-section facial cross-check](docs/FACIAL_CROSSCHECK.md) for audited
signal mappings, corrected conflicts and remaining camera-test limits. Updated
scrunch, wink, quick-close and head-shake controls require recalibration when
loading a profile saved before detector revision 2.

The lab tests 14 facial candidates and can optionally send calibrated core
controls to Godot. **Send controls to game** is off by default for independent
testing. Calibration, prompted trials and neutral tests never send action events.
Calibrated does not mean validated; camera accuracy still needs user tests.

## Play using your face

The game now has an [integrated camera/play screen](docs/INTEGRATED_PLAY_SCREEN.md).
The game launcher enables **Show live camera in game (local only)**. After
calibration use **Minimize setup - play in Godot**. Hide/show the background camera
inside Godot without disabling facial actions. No preview frames are recorded.

1. Close the older lab/preview and game, open this project and press F5.
2. Click **Connect Facial Controls**. The current lab opens with **Send controls
   to game** enabled. If already open, enable that switch in its window.
3. Load compatible calibration or calibrate **Jaw drop**, **Closed-lip smile**,
   and **Teeth baring** individually. Optional Eye widening retains the eyebrow
   proxy for flight. Return to neutral afterward.
4. Switch to Godot. When calibrated tracking is received, click **Play with Face
   + Keyboard** and remain neutral during the countdown.
5. Jaw drop jumps, closed-lip smile shields, teeth baring chomps. Watch the
   character and cooldown feedback; keyboard alternatives remain available.

GAME SEND ON means local sending is enabled, not acknowledged delivery or camera
accuracy. Godot's enabled play button shows actual calibrated packet receipt.
If disabled, check tracking/calibration, the switch and other running games.
On input loss restore stable tracking then Enter, or use K for keyboard.
Closing the lab or unchecking the switch disables facial game input. Advanced
reactions and microphone volume are not gameplay commands.

## Open it

1. Close OBS and the previous Face Chaos camera preview, so this lab can use the webcam alone.
2. In the project folder, double-click **Test-Facial-Reactions.cmd**.
3. **Face Chaos - Facial Controls and Test Lab** opens. Camera 0 connects automatically; game sending is off for this launcher. If needed, choose another camera index/backend and click **Open / reconnect camera**.

You can also launch it from the updated Godot menu using **Facial Lab - 14 Sections**
or the C shortcut. **Connect Facial Controls** opens the same lab with game
sending enabled. The old preview is no longer the menu's connection path.

The project environment and model are already installed in this workspace. A new extracted copy needs the setup in `recognition/README.md` first. The lab uses Tkinter from standard Windows Python and Pillow/Numpy/OpenCV/MediaPipe from the tested runtime; it adds no new downloaded dependency.

Direct command, from the folder with `project.godot`:

```powershell
.\.venv\Scripts\python.exe recognition\facial_lab.py
```

## All facial sections

| Section | Candidate detector | What must be tested |
| --- | --- | --- |
| Open mouth | Jaw opening coefficient | Open mouth without a grin; speaking confounds |
| Close mouth quickly | Recent open-to-closed transition | Requires a previously open mouth; still-closed mouth must not trigger |
| Deliberate blink | Both eyes closed with a 250 ms activation dwell | Ordinary short blinks should not trigger; comfortable half-second closure |
| Widen eye | Existing brow-raise measurement, renamed at the user's request | Same detector and calibration as the former Raise eyebrows row |
| Smile | Bilateral smile coefficients | Closed-lip and open-lip smiles; speech |
| Show teeth | Direct bright/low-chroma evidence strictly inside the inner lips (experimental) | Live without personal appearance samples; separate teeth visibility from jaw opening; check glare, lips covering teeth, tongue and speaking |
| Puff cheeks | Experimental personal bilateral cheek appearance + face-shape ratios | Does not rely on cheekPuff alone; distinguish puffing from smiling, scrunching and speaking |
| Tongue out | Direct connected pink/red protrusion below the lower lip (experimental) | Live without appearance samples; distinguish lips from tongue, including tongue through a small lip gap, with/without visible teeth |
| Left wink | Left closure with opposite eye open, dwell | Actual player's left, not mirrored-preview left; ordinary bilateral blinks |
| Right wink | Right closure with opposite eye open, dwell | Actual player's right; squints and face scrunches |
| Squeeze / scrunch face | Second-strongest activation among brow, nose, cheek, eye-squint and lip-press regions | Multi-region movement even when mouth-opening/smile are zero; ordinary blink alone is insufficient |
| Tilt head | Roll relative to the initial head pose | Left/right comfortable tilt, camera angle and body movement |
| Nod head | Pitch excursion with a direction reversal | Held head-down position must not count as repeated nods |
| Shake head | Yaw excursion with a direction reversal | Held turn must not count as repeated shakes |

The lab now has 14 rows: Raise eyebrows was renamed Widen eye, the original separate Widen eyes row was removed, and Frown was removed at the user's request. The retained eye detector and calibration are unchanged. Old profiles skip removed controls. Tests may legitimately show several real co-occurring movements; the lab records cross-activations instead of concealing them by forcing a single winner.

A strong face scrunch suppresses blink/wink **commands** to avoid accidental eye
actions during a squeeze; the raw eye measurements remain visible. Smiling eyes
alone no longer qualify as a scrunch. A head shake requires movement to both
sides, rather than one glance and return. Missing head-pose data shows `--`.

When Tongue out is calibrated/enabled, a tongue reading above its personal
threshold suppresses mouth-open and quick-close **commands**, including a short
250 ms retraction guard. Both raw measurements remain visible because your jaw
may really be open with the tongue protruding. Return to neutral before a new
mouth command. The row displays **Suppressed: tongue**. An uncalibrated tongue
heuristic cannot disable your working mouth input. This conflict rule does not
improve tongue pixels that stay at zero, and cannot retract a mouth event that
already fired before the tongue became visible; these are separate detection
and timing issues to diagnose from real trials.

## Musical feedback

**Musical tones** is enabled by default. Each accepted calibrated reaction plays
a short soft note; simultaneous accepted reactions form a small chord. Holding
an expression does not repeatedly play it. **Test tone** checks playback without
requiring a camera reaction. Uncheck **Musical tones** to mute. Sounds run on a
background worker and are rate-limited so they cannot queue a long stream of
notes. Calibration and neutral/confound measurements do not play reaction tones.
No microphone is opened: this is musical feedback, not sound-input recognition.

## Calibrate one section

Start with **Squeeze / scrunch face**, since that is the missing movement you reported.

1. Select its row.
2. Click **Calibrate selected**.
3. Follow the large prompt beside the camera: neutral face, then three prompted repetitions. Keep your face visible and movements comfortable.
4. For a motion/blink, perform it twice during each three-second capture. For a held expression, hold it comfortably during that capture, then relax during the preparation break.
5. Read the result. **Calibrated / UNTESTED** means the samples separated from neutral; it does not mean accuracy was verified. Overlapping or missing samples leave the section disabled.
6. Return to neutral, then try the movement. Its event count should increase once and remain unchanged while holding the expression.

**Show teeth now has a live image-based reading without personal appearance training.** It is independent of the jaw-opening score. Open your mouth with teeth covered: mouth may rise while teeth stays low. Expose teeth with a nearly closed jaw: teeth may rise while mouth stays low. Both can rise when both movements are visible. Select **Show teeth → Calibrate selected** only to set personal thresholds for event counting. The direct visual method is experimental: bright reflections and lighting can still confuse it. It is a new implementation, not a recovered earlier version.

**Tongue out also has a live image-based reading without appearance samples.** It looks for pink/red tissue connected to the mouth and extending below the lower lip, compared with nearby skin. It does not require a wide-open jaw or visible teeth. Try protruding your tongue gently through a small lip opening. Select **Tongue out → Calibrate selected** for event thresholds, not to train an appearance model. Lipstick, facial hair, skin/tongue colour, pose and lighting can cause errors; the current conservative heuristic primarily targets downward/front-facing protrusion and may miss sideways or very small protrusions. Actual camera trials remain required.

**Cheek puff still requires personal training.** It shows
**LIVE MODEL / train events** with the upstream `cheekPuff` raw measurement before
personal training succeeds. A value near zero is a weak model response, not proof
that you did not puff your cheeks. This diagnostic reading cannot trigger events.
Click **Capture cheek samples** below the preview for its guided calibration.
After success, the raw reading uses the personal detector. Cheek detection uses
both cheek regions and eye-span-normalized facial distances. Use **Capture teeth /
tongue / cheeks** to run their calibrations in order. Follow both the target and
negative-example prompts for cheeks; teeth/tongue use ordinary threshold calibration. The full sequence takes a few minutes. Calibration
stays in memory unless you click **Save calibration**. After restarting and
connecting the same camera, use **Load calibration** to restore your trained
detectors. No generic or invented samples are supplied as if they were yours.

### Capture and keep your samples

1. Click **Capture teeth / tongue / cheeks** while your face is tracked.
2. Follow the neutral, three positive-repetition, and comparison prompts for each section.
3. When capture finishes, test the readings and click **Save calibration**.
4. Choose a local JSON file. The default folder is `recognition/profiles/`.
5. Next time open the camera, then click **Load calibration** and select that file.

The saved file contains thresholds and derived personal histogram/shape templates,
not photographs, video, or raw frame samples. Loading restores detection, not
previous test results; verify the readings in the current lighting. Files from
a different camera index or unsupported feature version are rejected. Older v2
profiles preserve the other controls but omit the old teeth classifier/thresholds,
because teeth now uses a different direct measurement. Older v2/v3 profiles also
omit the old tongue classifier/thresholds while preserving compatible controls;
calibrate the new tongue event threshold if needed. Camera indices can be
reused after hardware changes, so recalibrate when changing the physical camera.
To delete saved calibration, delete its JSON file.

If a trained row stays at zero, select it and read the diagnostic under the table:
**outside calibrated examples** means its current appearance differs too much
from training; **region missing / face too small** means there is no usable region;
**Training rejected** shows why the detector was left disabled. Exported reports
now include those reasons and the latest sample-distance diagnostics, without
images or appearance descriptors. This is diagnostic information, not proof of
reliable detection. Do not lower thresholds blindly to make every movement fire.

For cheek puff: close and reopen the lab to load the updated detector, select **Puff cheeks**, and click **Calibrate selected**. Keep lips closed and puff both cheeks gently during the three positive prompts. Then perform the requested negative examples without puffing. Return to neutral before testing. If calibration rejects overlapping samples, leave it disabled and export the report rather than increasing strain.

The sensitivity slider is an **activation threshold**: moving it lower makes triggering easier; moving it higher makes triggering harder. Finish calibration/tuning for every desired row before collecting formal test evidence. Any detector change or camera reconnect clears prior test evidence because the tested configuration changed.

If tracking disappears, rows show **--** rather than fake zero measurements and the active task is cancelled. Reconnect if necessary, keep a complete face visible, and retry comfortably. Do not increase facial strain to make a detector pass.

## Diagnose a missed tongue gesture

The tongue search excludes the ring between the tracked outer and inner lip contours. **Lip pixels excluded** reports the removed pixels. Incorrect lip tracking can still reject a real tongue, and colour alone does not identify it reliably.

The moving mouth overlay was removed at the user's request. Restart the lab and select **Tongue out**. Stick your tongue out comfortably and read the diagnostic below the table. It reports candidate-pixel and past-lip counts and the rejection reason. Export the test report to share those diagnostics; no camera images are saved.

The user-reported tongue miss remains unresolved. Command priority cannot help while the tongue score stays at zero. Pale pink pixels are now excluded from the teeth candidate filter, but this correction still needs camera testing. Do not recalibrate mouth opening to compensate for a missed tongue gesture.

## Searchable reaction names

**Sound Practice** opens instructions for **Humming**, **Sh sound**, **Ah vowel**,
and **Whistling**, with online search links. For an input check, click **Refresh
devices**, choose a microphone and press **Start mic**; make a brief sound, then
press **Stop mic**. The meter shows volume only, not recognized sound. No
microphone starts automatically. See [sound controls and stages](docs/SOUND_CONTROLS_AND_STAGES.md)
for the progression design and remaining recognition work.

The lab now uses descriptive movement names such as **Closed-lip smile**,
**Teeth baring**, **Tongue protrusion**, **Cheek puff**, and **Eye widening**.
Selecting a row shows its name as an online search phrase and explains the
expected movement. See the [complete name and instruction guide](docs/FACIAL_REACTION_GUIDE.md).
Earlier instructions saying Smile, Show teeth, Tongue out or Puff cheeks refer
to those same rows. Names changed; detection algorithms did not.

## Test all sections

### Smile versus Show teeth

Restart the lab. Calibrate **Smile** with lips closed and mouth corners lifted.
Calibrate **Show teeth** by pulling lips back and apart to expose both upper and
lower teeth comfortably; do not strain or try to show every back tooth. Older
profiles discard these two thresholds so the new poses can be calibrated.

When the calibrated teeth signal crosses its threshold, **Smile** shows
**Suppressed: show teeth** and cannot issue an event. Raw measurements remain
visible. Return to neutral between gestures; covering teeth while still smiling
does not issue a second action. An earlier smile event cannot be undone if the
player smiles first and exposes teeth later.

Run five selected trials each for closed-lip Smile and Show teeth. During teeth
trials there should be no Smile events. Then open your mouth with teeth covered:
the teeth reading should stay low. Export the report if this does not happen.
The existing experimental teeth detector measures bright mouth pixels; it does
not count teeth or independently certify that both rows are visible. This update
changes prompts and command priority, not the underlying recognition model.

1. Calibrate the sections you can perform. Leave failed sections disabled; record them as unavailable, not passed.
2. Use **5** attempts initially. Click **Prompted trials: selected** for a quick check, or **Test all calibrated** to cycle through every calibrated reaction automatically.
3. During each two-second rest, return to neutral. During the three-second action prompt, perform the named reaction once. Watch the prompt as the suite moves between sections.
4. The lab records detected attempts, misses, duplicate events, and other reactions that triggered during that target's prompt. Five attempts are only a smoke test.
5. For formal screening, choose **50** attempts per section. This takes several minutes per section; take breaks and run smaller blocks if preferred.
6. Run a **60-second neutral/confound test** first, then **600 seconds** for the design's ten-minute false-activation screening. Blink, speak, and move your head naturally without intentionally performing commands. False events from every enabled section are recorded.
7. Click **Export test report** and save the JSON. It contains derived scores and counts only. Send that report back for analysis before adding these reactions to gameplay.

Stopping or losing tracking cancels an incomplete trial; it is not scored as a successful reaction. Valid completed trials and tracked neutral exposure remain in the report. Long processing gaps invalidate an unfinished task.

## Reading results honestly

- Prompt-window detection rate: successful prompted attempts divided by valid attempts. It includes the user's response timing and is **not** frame-labelled detector recall or end-to-end latency.
- Duplicate events: extra target events within one prompt. These suggest rearming/threshold problems unless the user deliberately repeated the movement.
- Cross-activations: other labels during a target prompt. Review whether they reflect actual simultaneous movements or detector confusion.
- False events per ten minutes: neutral events normalized by measured tracked neutral exposure. Do not trust a short neutral run as if it were a full ten-minute sample.
- Initial screening requires at least 50 attempts per section, at least 600 neutral seconds, at least 95% prompt-window detection, fewer than two false events per ten minutes, and zero duplicate target events. A candidate still needs cross-activation review, independent labelled precision/recall, latency measurement, and broader user/hardware testing. Screening never automatically enables game controls.

## Privacy and limitations

Camera processing stays local. Standalone testing opens no game connection by default. Opt-in game sending uses loopback for events and optional in-memory camera preview; nothing is sent to an Internet service. Preview frames/video are not recorded. Sound Practice accesses the microphone only after Start mic and discards its buffers. Frame-level calibration samples stay in memory and disappear when the lab closes. **Save calibration** explicitly stores derived personal histogram/shape templates and thresholds in local JSON. **Export test report** stores thresholds, timestamps, camera index, diagnostics and test statistics, without appearance templates or frame samples. Personal profiles/reports are excluded from the source ZIP. See the [integrated preview description](docs/INTEGRATED_PLAY_SCREEN.md) for hide versus stop-sharing behavior.

This lab is a validation tool, not proof that every proposed expression can become a reliable game control. Some expressions may need a different detector or an alternative input. We will make that decision from trials, not from an animated meter.

Technical reference: [MediaPipe Face Landmarker outputs and options](https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/python).
