# Continuous game recording - version 59

Version `0.21.0-setup.5`, package `com.facechaos.mobile`.

## Report and evidence

The player reported a saved video lasting about one second. Metadata inspection found the latest private clip was **1.788 seconds**, with a Gallery copy having the same duration and byte count. The preceding private clip was **55.7833 seconds** and remained intact. No footage was viewed, uploaded or deleted from the player's app or Gallery.

The recording lifecycle automatically finalized one second after a round ended. Retry and Next stage each started another recording, replacing `lastClip`, so a brief new attempt became the default clip offered for saving. The export operation copied the selected short file, rather than truncating the preceding long recording.

## Changes

- One active recording continues through a game's result screen, retries and stage transitions until Stop, a game switch, an app/window lifecycle interruption or the existing two-minute limit.
- Stage/retry transitions preserve the encoder and recording clock while replacing the game session and resetting input ownership.
- Retry after Stop or automatic completion does not silently start a replacement recording. A fresh recording is requested from the game's instructions.
- Switching games still ends the old recording and uses an independent game/recording session.
- The recording library loads video durations off the UI thread. The review screen also shows the loaded video's duration.
- Game instructions explain Stop, Clip, recording continuity and the two-minute limit in all five interface languages.
- The original recording is copied unchanged to Gallery; recordings remain silent and sharing remains explicit.

## Verification

Isolated Samsung test using the actual camera, game overlay and recording/export code:

- Game over, Retry, Next stage and pause/resume kept the same encoder.
- Saved clip: **16,657 ms**, compared with **16,515 ms** active recording time.
- Final frames decoded successfully.
- Gallery export: **16,657 ms**, SHA-256 identical to the private clip.
- Retry after Stop did not replace the selected full recording with a new short take.
- Test Gallery export and its local test clip were removed; the player's recordings were preserved.
- Existing isolation suite passed all 225 game transitions, close-game confirmation behavior, rapid recording switches and stale permission callbacks.
- 35/35 Java rule suites passed.

The optional `-PrecordingCheck` build creates a debug-signed `recordingCheck` variant under `com.facechaos.mobile.verify`, allowing instrumentation against the actual app without replacing the Play-installed package. Run the new check with `-e continuousRecordingOnly 1` through SceneCheck. It uses a real camera; it does not publish recordings.

Final optimized Samsung smoke checks pass: native face model/expression adapter, recording pause/resume/decode, 75 game/language renders and 20 welcome layouts. The optimized verification copy is installed separately from the player's Play app. Release lint reports 0 errors and 34 warnings. Bundletool, final manifest/R8/language checks, four native ABI/16 KB layouts and all 133 signed payload entries pass. Monetization remains inactive.

Signed bundle: `dist/FaceChaos-0.21.0-59-recording-fix-play.aab`.

SHA-256: `a4010ea0e7f47ad3cd3ff17c8f8242412a2103b8b6fb188a3d69d84972af647b`.

The local metadata-inspection copies of the player's two recordings were removed after inspection; their phone originals were left untouched. Use the Play testing track to update the main app while preserving those recordings.
