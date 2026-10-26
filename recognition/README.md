# M1 camera setup and calibration

This implementation uses MediaPipe Face Landmarker blendshape coefficients. They are measurements for calibration, not probabilities or measured recognition accuracy. Processing stays on this PC; no camera frames are saved or sent to Godot. Only reaction events, meters, and tracking state travel over localhost.

## Start

Open PowerShell in the project folder. Python 3.10 or 3.11 is recommended for the pinned Windows packages.

```powershell
# One-time installation (downloads packages and the official versioned model):
.\recognition\setup.ps1 -Python 'C:\path\to\python.exe'

# Optional: find available camera indices:
.\recognition\start-camera.ps1 -ListCameras

# Open the local camera preview:
.\recognition\start-camera.ps1 -Camera 0
```

Alternatively, run `.\.venv\Scripts\python.exe recognition\webcam_bridge.py --camera 0` directly. Device probes briefly open each camera; they do not record video. If Windows blocks script execution, the direct Python command works without changing execution policy.

## Calibrate in the camera window

1. Press **C**, relax your face during the preparation and three-second measurement period. Keep the face visible throughout.
2. Press **1**, then **E**. After the preparation prompt, comfortably hold your mouth open during measurement. Return to neutral.
3. Press **2**, then **E**, and comfortably raise your eyebrows. Return to neutral.
4. Press **3**, then **E**, and comfortably smile. Return to neutral.
5. Test each reaction individually. The last detected label and live normalized meters should change. A held expression should trigger once, then require a neutral return.

Each calibrated control is saved individually. Poorly separated samples are rejected. You may leave a control disabled and use its keyboard alternative. This first implementation uses neutral-versus-held-expression sampling; guided repeated trials and measured quality reporting remain M1 follow-up work.

Select **1/2/3**, then **+** for more sensitivity or **-** for less. **D** disables that control. **C** or **N** clears the current thresholds and begins new neutral calibration. **Q** or closing the window releases the camera and exits. Calibration stores derived thresholds only under `recognition/profiles/`; remove the JSON to delete a profile. Camera indices may change when hardware changes: recalibrate after changing devices even if Windows reuses the index.

## Play

Run the Godot project and click **Connect Facial Controls**. In the current lab, keep **Send controls to game** enabled and calibrate Jaw drop, Closed-lip smile and Teeth baring (or load compatible calibration). Return to neutral, switch to Godot and choose **Play with Face + Keyboard**. Jaw drop jumps, smile shields, teeth chomp; the optional Eye widening brow proxy flies. Keyboard alternatives remain active. See [full instructions](../FACIAL_LAB_START_HERE.md). The legacy webcam_bridge.py command above remains available but is not the new menu path.

Show-teeth detection is not implemented or claimed. Mouth/grin geometry does not confirm visible teeth. Attack remains available by keyboard until a separate detector or an approved alternative mapping is validated.

Face mode requires at least one calibrated control. Uncalibrated actions retain their keyboard alternatives. Face loss for 300 ms or a stopped bridge suspends the round. Restore tracking and press **Enter**, or press **K** to continue by keyboard; both recovery paths use a three-second countdown. Keyboard-only mode does not depend on camera availability.

Calibration/preview hotkeys apply only while that window has focus. Gameplay hotkeys apply in the game window. An integrated Godot setup screen is planned; this milestone supplies a separate local setup window.

The preview remains running across Godot restarts. Close it with Q when finished, or before opening another preview so both processes do not compete for one camera.

## Check software

### Black camera image

Do not calibrate until your face is visible. Open the physical privacy shutter,
check lighting, and close OBS/other camera apps. Close the old preview with Q or X,
then reopen Camera Setup in the game. The updated bridge warms up the camera and
tries Media Foundation if DirectShow produces black frames. This cannot open a
physical shutter or fix a disabled camera.

If needed, select Media Foundation explicitly from the project folder:

```powershell
.\.venv\Scripts\python.exe recognition\webcam_bridge.py --camera 0 --backend msmf
```

If the picture remains black, close the preview and test the Windows Camera app.
If that app is also black, check the shutter, camera-disable key, Windows camera
permissions, and device selection before retrying. Close Camera before returning
to Face Chaos. Use `--list-cameras` to diagnose another device index.

```powershell
.\.venv\Scripts\python.exe -m unittest discover -s tests -p 'test_*.py' -v
godot --headless --path . --script tests/game_checks.gd
```

Tests cover calibration rejection, profile checks, smoothing/rearming, tracking loss, event repetition/deduplication, validation, cooldowns, pause/recovery, and score persistence. They do not establish webcam accuracy, false activation rate, or end-to-end latency.

For a real device trial, test each expression separately, then during normal blinking, speech, and head motion. Record correct/missed/accidental actions. Unplug the camera or cover it and confirm safe recovery. Use the full acceptance protocol in `docs/recognition-contract.md` before calling M1 complete.

## Dependencies and model

`requirements.txt` pins the three direct dependencies. `requirements-lock.txt`, when present, records the exact tested Windows/Python 3.10 environment. The model manifest records its official versioned download URL and SHA256; setup verifies that checksum. Model redistribution/licensing remains a release audit task.

Reference: [official Face Landmarker Python guide](https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker/python).
