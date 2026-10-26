# Game sounds and stage progression � Android 0.8.0

Each of the 12 games now has five saved stages. Stage 1 retains the existing rules; later stages gradually increase gate targets, tighten timing/alignment/flight/power tolerances, shorten response waits, lengthen memory sequences or require a slightly longer steady sound. Animal rhythm definitions and comfortable sound volume remain unchanged. All 60 winning paths pass pure rule checks.

A win shows Stage N complete! and Next stage on the large circle. Tap it to begin the next stage. A failure stays on the current stage with Mission failed and Try again. Stage 5 shows All stages complete! and Play again starts a new run at Stage 1. Unlocks and completed-stage progress save independently for each game. Choosing a game resumes its highest unlocked stage; Game options > Stages lets the player replay unlocked stages. Stop/restart never unlocks a stage.

The HUD shows Stage N / 5, and the pre-game brief displays the current stage target. Coins persist across stages. Recorded rounds still finalize at the result card; if recording remains enabled, the next stage starts a new clip.

## Original game audio

Bundled original PCM sounds cover start, countdown, go, correct answers, coins, misses, pause, stage completion, final completion and failure. A quiet original background loop plays during touch-controlled rounds. Game options > Sounds has independent effects/music switches and Preview game sounds. Settings persist, and playback stops when the app leaves the foreground.

Speaker music and effects are suppressed throughout live microphone rounds, including calibration, so they cannot masquerade as the requested sound. Result fanfares play after microphone capture stops. Any preview audio is stopped before microphone capture restarts. Animal Choir guide examples still play only in its paused pre-game brief.

Current camera/game MP4 clips remain silent: live game audio has not been mixed into video exports. Actual speaker loudness and subjective sound quality need a user listening test. No system volume settings are changed.

Assets are generated from original mathematical waveforms by [generate_audio.py](../android-app/audio/generate_audio.py); no downloaded music or sound licences are required. Sound files are under android-app/app/src/main/res/raw. Android [SoundPool](https://developer.android.com/reference/android/media/SoundPool) handles low-latency effects and looping.

## Verification

Pure checks pass all 60 game/stage winning paths, default failure paths, stage completion/next actions, memory growth, reward uniqueness and sound event deduplication. The microphone feedback policy is checked without listening to any actual sound. On the Samsung SM-A176B, native camera-free checks passed stage/finale screens, six-face memory on compact portrait, all twelve packaged audio decodes and synthetic 720 x 1560 MP4 encode/decode, pause/resume and orientation. These tests did not play audio; a user listening test remains necessary.
