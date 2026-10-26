# Options sheet - 0.18.1

The top-right three-dot menu now opens a custom bottom sheet with cream and teal styling, rounded cards, original vector icons, descriptive subtitles and a Back to camera button. The four routes are Game options, My recordings, Rate Face Chaos and Video storage. Video storage replaces the long menu label Where videos are saved; its information remains available after tapping the card.

The sheet dims the camera behind it and pauses the current round. Closing or tapping outside restores the previous pause state. Selecting a route dismisses the sheet before opening that route. The panel has at least 48 dp close/button targets, flexible text, pressed/ripple states and a scrollable height capped to 86 percent of the screen. Width is capped on larger displays. Game dimensions and recording behavior are unchanged.

Validation uses native offscreen rendering of the actual menu component at two phone widths, without camera or microphone access, plus Android build and lint. Visual review and installation are recorded below.

Android build and lint passed. Samsung SM-A176B installed versionCode 45 / 0.18.1-menu. Native options panels rendered at 900 and 1080 pixel widths; compact screenshot visually reviewed. Temporary test package removed and app opened. No camera or microphone used by the rendering checks.
