# Integrated play screen ? Android 0.4.1

The app opens with a paused game over the selfie camera. Start opens its illustrated tutorial; the gold gamepad button opens the collection. Instructions use existing game characters, numbered mission/action/cue cards, and a pinned Let's play button. Recording is selected inside this brief. The older separate camera landing screen and its standalone recording switch are no longer shown.

The control table below describes the earlier 0.4.0 behavior; in 0.4.1 the camera-only rows are replaced by the ready-to-start game state. Pause, resume, stop, retry and clip review retain their behavior.

# Play and recording controls — Android 0.4.0

The main circle changes with the current action:

| State | Circle action | Secondary action |
| --- | --- | --- |
| Camera, recording off | Choose a game | Games |
| Camera, recording enabled | Start silent recording | Games |
| Playing or recording | Pause | Square: stop |
| Paused | Resume | Square: stop |
| Mission failed | Try again | Clip if recorded |
| Mission complete or stopped | Play again | Clip if recorded |
| Preparing or finishing video | Wait for encoder | Controls temporarily disabled |

Games show instructions before Start and prompts during play. A failed round says **Mission failed**, with **Try again**. Manual stopping has its own Round stopped result. A recorded round includes the end card before automatic finalization.

Recording is opt-in. The native MP4 contains the selfie and game artwork, not the app controls. It is silent, limited to two minutes, and stops when the app leaves the foreground. Review, Save, Share and Delete are explicit actions. Share opens the system chooser; no automatic publishing occurs.

Verified on the connected Samsung SM-A176B: synthetic 720 x 1560 native MP4 encode/decode, pause/resume, exclusion of paused time, portrait marker orientation. Also verified six offscreen game scenes, gate pass/miss states and compact rendering. The instrumentation test does not access the camera or microphone.

Still requiring a real user test: actual camera plus game recording, crop and orientation during recording, sustained frame rate, and playback quality. Facial and sound detection remain disconnected from the Android games; touch input simulates actions.
