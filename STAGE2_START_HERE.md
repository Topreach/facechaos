# Face Chaos — Stage 2 webcam prototype

Experimental source package for Windows and Godot 4.7.2. Mouth opening, eyebrow raising, and smile controls are implemented with personal calibration. Show-teeth recognition is not implemented; A remains the attack button. Webcam accuracy and response time have not passed the recognition acceptance gate.

## Installation in a separate folder

1. Extract the ZIP to a new folder. Do not copy it over an earlier working project.
2. Install 64-bit Python 3.11, or use Python 3.10 if already installed.
3. Open Command Prompt in the folder containing `project.godot` (not inside `recognition`). Run:

```bat
py -3.11 -m venv .venv
.venv\Scripts\python.exe -m pip install -r recognition\requirements-lock.txt
.venv\Scripts\python.exe recognition\download_model.py
```

If using Python 3.10, replace `py -3.11` with `py -3.10`. Setup needs internet access for dependencies and the official model. The environment must be at the project root so the game's camera button can find it. The model is downloaded separately and checked against the included SHA256 manifest; it is not bundled in the ZIP.

4. Import this extracted folder's `project.godot` into Godot. Stop an old running game before starting this one, so only one game uses the recognition port. Press F5.
5. The window title should say **Face Chaos - Facial Controls**. You should see **Game Preview - 3 Controls**, **Play with Face Controls**, and **Play with Keyboard** buttons.
6. Keep OBS and other camera apps closed for the initial test. Click **Game Preview - 3 Controls** once. The preview opens in a separate window; wait for it to appear.

If the preview does not open, run the following command in the project folder to see an error:

```bat
.venv\Scripts\python.exe recognition\webcam_bridge.py --camera 0
```

## First test: mouth opens once, character jumps once

1. Give the camera window focus. Sit facing the camera with your entire face visible.
2. Press **C**. Stay relaxed during the preparation and three-second neutral measurement. Wait for the neutral-captured message.
3. Press **1**, then **E**. Follow the preparation prompt, then comfortably hold your mouth open during the three-second measurement. Wait for the saved-mouth message.
4. Close your mouth and relax briefly. Open it again. The preview's last-detected label should show `mouth_open`. Holding it open must not generate repeated reactions; close it before another attempt.
5. Return focus to Godot. Click **Play with Face Controls**; it enables when calibrated tracking is ready. After the countdown, open your mouth without pressing Space. The character should jump once. Return to neutral and wait until grounded before trying again.

Calibration is partial by design: testing just mouth input is enough to enable this first test. Uncalibrated actions keep their keyboard alternatives.

## Add the other available expressions

In the camera window, press **2**, then **E** for eyebrows, and **3**, then **E** for smile. Follow each prompt and return to neutral between expressions. Saved controls are enabled independently. Do not press C between expressions: C clears the current calibration and restarts neutral capture.

| Expression | Action | Expected test |
| --- | --- | --- |
| Open mouth | Jump | One grounded jump per neutral-to-open movement |
| Raise eyebrows | Fly | Brief upward lift, bounded on screen |
| Smile | Shield | Brief shield; one eligible collision absorbed |
| Show teeth | Not implemented | Use keyboard A to chomp |

Select **1/2/3**, then **+** to increase sensitivity or **-** to decrease it. **D** disables the selected expression. **Q** closes the preview and releases the camera. More details: [recognition guide](recognition/README.md).

## Safety and fallback tests

- In face mode, cover your face briefly: the game should enter recovery after sustained tracking loss. Restore tracking, then press Enter to resume through a countdown.
- Close the preview: the game should stop facial play and offer recovery. **K** switches to keyboard with a recovery countdown.
- **Esc** pauses. **Space**, **Up**, **S**, and **A** are the keyboard action alternatives.
- Test normal speech, blinking, and head movement: note any accidental actions. This is experimental; use comfortable expressions and disable any unreliable input.

The preview stays local. Only reaction events, tracking/calibration state, and activation meters go to Godot over localhost. Video and microphone audio are not recorded. This package does not capture the microphone or connect to OBS.

## Evidence and limits

The development computer read camera 0 at 640×480 and ran model inference. No face was present during that probe. Automated tests exercise calibration, event behavior, protocol handling, cooldowns, and recovery; they do not prove that a person can reliably control the game. The next milestone evidence is your actual mouth/eyebrow/smile trials and measured false activations/latency.

This ZIP contains source, tests, design documents, dependency pins, and setup scripts. It excludes the development environment, generated Godot caches, private calibration profiles, logs, and downloaded model. It is not a packaged Windows installer. Creating this ZIP does not reconstruct or restore an earlier Stage 1 backup.
