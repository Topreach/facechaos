# Recording and playback fix — 0.19.2 (49)

Starting a game from the picker could queue its dismissal callback after the new round had started. That callback restored the previous paused state. Starting a round now removes both instruction and picker dismissal listeners before closing them, so the new round owns playback.

VideoEncoder also sent MediaRecorder.resume() when the recorder was already recording, including at startup. Pause and resume requests now do nothing when the requested state is already active.

## Verification

- Debug app and instrumentation APK compile successfully.
- Android lint: 0 errors, 33 existing warnings.
- Samsung SM-A176B: real encoder test passed for startup, repeated resume, repeated pause, continued frames after resuming, finalization and MP4 decoding. Uses only synthetic frames.
- Samsung SM-A176B: actual game picker → Smile Stack instructions → record and play passed. Countdown and game position advance; circle pauses both, game position stays fixed during pause, circle resumes motion while recording remains active, and Stop ends the round and saves a clip. This test uses the camera with touch controls; it does not validate facial or sound recognition accuracy.

Regression checks are in RecordingPlaybackCheck.java and run through SceneCheck:

```text
adb shell am instrument -w -e recordingOnly 1 com.facechaos.mobile.test/com.facechaos.mobile.SceneCheck
adb shell am instrument -w -e playbackOnly 1 com.facechaos.mobile.test/com.facechaos.mobile.SceneCheck
```

The second check records a short local test clip and leaves it available in My recordings. Run it with the app language set to English; it restores the previous recording selection and face-control choice. Existing clips and saved player data are preserved.

Manual user check: select a game, enable “Record my round (silent)”, tap “Let's play!”, confirm the countdown and game move, pause/resume with the circle, then use the square Stop button and review the saved clip. Game progress can still intentionally wait for required face tracking or microphone calibration.
