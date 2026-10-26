# Melody Hop fire gaps - 0.16.4

Cross separate platforms above animated fire. Wait for the gold cue, make one short sound with enough comfortable volume for the platform height, then go quiet. Pitch is not measured.

Successful jumps land visibly on the far platform and award one coin. Weak, missing and mistimed jumps enter a visible fall. One life is removed at impact, after the fall animation; the last life shows Mission failed and retry starts at Stage 1. A fixed real-time recovery interval keeps the fall readable at every stage. Pause freezes the fall, and an impact cannot deduct a second life.

The existing five stage speeds, stage targets, microphone calibration and quiet rearm are retained. Later stages bring faster cues; Stage 5 requires eight successful jumps with one life. Sound thresholds are not increased to force shouting. Recording controls and original game dimensions are unchanged.

Validation: ten missing/weak-jump falls across five stages, visible descent and delayed single penalties, pause/recovery, final-stage retry, successful landing freeze and nonduplicated coins. PCM volume/trajectory checks, ten microphone-stage playthroughs, all 75 game-stage win paths and microphone cadence checks passed. Native Android scenes and live testing are recorded separately.

Android build and lint passed. Samsung SM-A176B: versionCode 42 / 0.16.4-fire installed, seventy synthetic native scenes passed at two sizes. Falling-character screenshot visually reviewed. No camera or microphone accessed by tests. Test package removed and app opened; live voice play remains the player's check.
