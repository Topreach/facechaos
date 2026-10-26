# M1 implementation status — 23 October 2026

Current lab-to-game integration: **Connect Facial Controls** launches the lab
with explicit local sending enabled. Jaw drop, closed-lip smile and teeth baring
now route to Jump, Shield and Chomp; the optional eye/brow proxy routes to Fly.
Four real UDP integration scenarios pass from prepared detector values to Godot
abilities; calibration/trial output is blocked and tracking loss pauses play.
All 80 Python tests and the hidden UI opt-in/default-off check pass. Actual
camera expression accuracy is still unverified. See [setup](../FACIAL_LAB_START_HERE.md).

Latest comprehensive review: see [quality and creative review](QUALITY_REVIEW.md).
The 76 Python tests, Godot gameplay/campaign/quality checks and hidden lab workflow
pass. Frame-hitch collision, countdown, attack direction and backup-save recovery
were corrected. This does not close the real-camera recognition milestone.

Sound Practice added: four searchable sound instruction tabs, university example
link and optional microphone device/volume check. Audio is not classified or sent
to gameplay. Eight-stage progression is documented, not built. All 73 unit tests
and the hidden UI workflow check pass; no live microphone test was performed.

Latest distinction: Smile now prompts closed lips; Show teeth prompts exposed
upper and lower teeth. Detector revision 4 gives calibrated teeth priority over
Smile with a 250 ms release guard and neutral rearming. Older smile/teeth
thresholds are discarded for recalibration. All 70 unit tests and the hidden
UI check pass. No replacement recognition model or camera validation is implied.

Latest user follow-up: moving mouth overlays removed from the preview. Missing
native cheek signals now remain unavailable rather than defaulting to zero;
untrained cheeks explicitly request personal samples. Puff-cheek recognition
remains unaccepted. The 64 unit tests and hidden UI workflow pass.

Latest camera issue: tongue protrusion still misses for the user. A diagnostic
overlay and rejection reasons were added; pale pink teeth false positives were
addressed in synthetic fixtures. Real-camera resolution is still pending.

Latest audit: [all 14 facial sections cross-checked](FACIAL_CROSSCHECK.md).
Sixty-three unit tests and the hidden UI workflow check pass, along with a
14/14 synthetic channel-isolation matrix. Corrections cover smile/scrunch
confusion, partial bilateral blinks versus winks, scrunch/eye-command conflicts,
slow versus quick mouth closure, one-sided glance versus head shake, invalid
visual input, and missing-pose gaps. Profiles now record detector revision 3
and drop incompatible older thresholds for recalibration. Puff cheeks and the
pixel-based teeth/tongue heuristics remain unvalidated on real user trials.

Latest UI change: Frown was removed, leaving 14 sections after the earlier eye
label consolidation. Old calibration files discard the removed control.
Synthesized per-reaction musical feedback, mute, and Test tone were added to the
lab. Neutral tests stay silent and playback runs off the UI thread. Forty-six
unit tests, the hidden lab workflow check, and Godot regressions pass; a Windows
test chord returned successfully. Actual speaker audibility remains user-verifiable.

Tongue correction: the lab now shows a live protruding-tongue candidate without
personal appearance samples, jaw-open gating, or visible-teeth gating. A local
colour/geometry heuristic checks tissue connected to and extending below the
lower lip; it is not a validated semantic segmentation model. Very small or
sideways protrusions and varied lighting/colour need real-user trials. Old lab
profiles preserve compatible controls while dropping incompatible old tongue
classifier thresholds. Cheek puff remains sample-based. This update changes the
standalone lab only; no new reaction has been wired into gameplay.

Latest correction: Show teeth now uses a direct inner-lip visual candidate,
independent of jaw opening, without personal appearance-model training. It is
an experimental bright/low-chroma pixel heuristic, not a validated semantic
tooth detector or a recovered historical implementation. Thirty-six tests pass,
including covered-teeth/open-mouth, exposed-teeth/nearly-closed-jaw, coactivation,
tongue/skin negatives, and old-profile migration. Actual user trials remain
necessary. Tongue and cheek puff retain personal appearance training.

## Latest direction: standalone full-face validation

The user's priority is now testing all proposed facial/head inputs, including
face squeezing, before connecting more actions to the game. A standalone
16-section lab is implemented; see [launch and testing guide](../FACIAL_LAB_START_HERE.md).
It adds three-repetition calibration, experimental personal appearance classifiers
for teeth/tongue, independent scrunch/frown measurements, timed blinks/winks and
head motion, prompted test suites, neutral false-event counting, and JSON report
export. It sends no network packets or game actions. This is candidate detection,
not accepted recognition performance.

Software evidence: 23 unit tests pass, plus a hidden Tk UI/workflow smoke check
covering all 16 rows, scrunch calibration, multi-control trial scheduling, neutral
event counts, and evidence reset after threshold changes. Actual user/camera
trials of these new sections are pending. No real-user recognition accuracy,
precision/recall, or latency claim is established by these tests.

Later lab update: guided capture for teeth/tongue/cheeks now has explicit local
Save/Load Calibration. Saved profiles contain derived templates and thresholds,
not camera images; they are excluded from source packaging. Thirty unit tests
and the hidden UI workflow check passed, including save/load detection parity,
bad-file validation, and preserving the separation between calibration and test
evidence. Actual personal samples must still be captured by following the live
prompts; no fabricated samples are presented as a working detector.

Earlier sections below describe the first three-control game bridge and its
remaining integration work. The lab is a separate tool and does not replace or
silently expand that bridge's accepted control vocabulary.

## Implemented

- Project-isolated Python 3.10 runtime, pinned direct dependencies, complete tested dependency lock, official Face Landmarker model download with SHA256 verification.
- DirectShow camera index selection/probing, mirrored local preview, raw and calibrated reaction meters, last-detected label, and explicit unavailable/not-calibrated states.
- Basic neutral-versus-action calibration for mouth opening, raised eyebrows, and smile; independently adjustable sensitivity, disabled controls, local profile storage, smoothing, hysteresis, dwell, and neutral rearming.
- Versioned localhost packets, session/sequence IDs, three-packet event repetition, event deduplication, packet/field validation, bounded processing, and heartbeat timeout.
- Explicit game face-mode opt-in (F), keyboard fallback (K), setup launch shortcut (C), accepted/cooldown feedback, face-loss recovery pause, stable tracking check, and three-second recovery countdown.
- Attack stays on A. No teeth visibility claim or unvalidated grin substitute is shipped.

## Verification evidence

- Six Python unit tests passed: overlap rejection, neutral rearming and held expressions, tracking loss, noise/invalid measurements, profile persistence/validation, and event retransmission.
- Godot 4.7.2 headless regression script passed with zero failures: action cooldown, bounds, pause, restart, saved score, packet validity/order/deduplication/session restart, invalid meter/reaction rejection, tracking loss, and keyboard recovery.
- Godot headless project startup passed. The environment reports a system root-certificate-store warning; this does not establish whether the visible UI is correct.
- Python dependency consistency check passed.
- Face Landmarker loaded its model and processed a blank frame with zero faces.
- Normal Windows access successfully read camera 0 at 640×480 and ran inference on that frame. No face was present at the instant of the probe. No frame was saved, shown, or uploaded.
- A restricted-process camera probe could not read the camera; the normal-access probe succeeded. Run the camera preview as the normal Windows user.

## Remaining before M1 acceptance

1. User checks the visible preview, positioning, and live reaction meters on this camera.
2. Guided multi-repetition samples, sample-quality checks, and a mixed-expression/neutral trial need implementing beyond the basic held-pose calibration.
3. Measure precision, recall, false accepted actions, and end-to-end latency for each expression. Current passing tests do not provide these results.
4. Assess a true show-teeth detector or agree a distinct alternative expression. Attack is currently keyboard only.
5. Complete integrated Godot device/setup/mapping views; current setup is a separate local window with camera index selection through its launcher.
6. Extract the action router from the prototype, provide full mapping controls, and add target-device trial telemetry.
7. User verifies actual disconnect/reconnect, closing the camera window, multiple-instance/port conflict behavior, and the recovery countdown during visible gameplay.

The existing keyboard runner is still an endless prototype. Finite levels, three-heart damage, production art/audio, Director events, and other mode loops belong to later milestones in the approved design.

## Start testing

See [camera setup guide](../recognition/README.md). Installation is already present in this workspace. Run the game, click Game Preview - 3 Controls to open the old bridge, calibrate the three available expressions in that window, return to the game, and press F to enable face mode. Press Space to begin; A remains attack.
