# Melody Hop 0.16.3

Make a short soft sound for a low hop, or a stronger comfortable sound for a high hop. Act during the gold signal, then go quiet. Volume controls jump height; musical pitch is not measured. No shouting is needed.

Changes:
- Dedicated microphone preference, enabled by default independently of other games. An explicit touch-practice choice remains available.
- Microphone calibration freezes the countdown and course; permission and capture failures pause instead of silently switching to touch.
- The target ring now shares the character's horizontal position; moving note platforms use the same height and encounter time as the jump rules.
- A visible floor, volume meter, low/high cue and per-game coin wallet explain the mechanic. Game area dimensions and face-aware instruction placement are retained.

Validation: Android build and lint; PCM volume separation, calibration freeze, actual jump trajectories, ten microphone-stage playthroughs, quiet rearm, pause, touch isolation, all 75 game-stage win paths and 25 microphone update-cadence jump paths. These use synthetic inputs; live microphone comfort still needs player testing.

Samsung SM-A176B verification: versionCode 41 / 0.16.3-melody installed. Sixty synthetic native scenes passed at two sizes, along with independent sound-preference checks. Cue screenshot visually reviewed. Temporary test package removed; app opened for live player testing.
