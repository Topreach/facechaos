# Recording verification - version 68

Verified on Samsung SM-A176B on 4 November 2026 using the isolated `com.facechaos.mobile.verify` app and the production recording controller.

The real camera/game recording check passed game over, retry, next stage, pause/resume and a camera-look change while retaining one encoder. Saved clip duration was **17,455 ms**, against **17,313 ms** of active session time. End-of-clip frames decoded successfully. Save to Gallery completed; its copy had the same 17,455 ms duration and a matching SHA-256 digest over every byte. Retry after Stop preserved the full clip instead of replacing it with a new short recording.

The temporary private test clip and its Gallery copy were removed by the check. Original last-clip selection, recording/face/look settings and existing player recordings were preserved. Gameplay persistence was disabled during this isolated test. No recording was uploaded or shared.

Recording is automatically saved in My Recordings after Stop. To create a Gallery copy, open Clip or My Recordings, select the clip, then Save to Gallery. On this Samsung the export destination is Movies / Face Chaos. Clips remain silent as designed.

No production code changed during this verification. The signed 0.21.2/version 68 bundle remains the current upload artifact.
