# Comet Launch 0.16.1

Comet Launch uses sound only. Hum gently to build power, then go quiet while the meter is in the gold band to launch. There is nothing to hold on the screen. Start, pause and recording remain normal app buttons.

The rocket now visibly flies toward its landing target. Coins are awarded after a successful landing; underpowered and overpowered launches show their misses. Brief sound dropouts are debounced. Calibration freezes the countdown, microphone failure pauses play, and pausing preserves a launched rocket. The power meter stays inside the original game area.

## Validation

- Android debug build, test build and lint passed.
- PCM-driven checks passed 30 landings across all five stages, including quiet debounce, calibration, pause and misses.
- Existing collection, stage, speed and game-path checks passed.
- Samsung SM-A176B: version 0.16.1-comet installed; 35 synthetic native scenes and mandatory-microphone settings checks passed.

These are synthetic input and rendering checks. Live microphone comfort and timing still need the player's trial on the phone. This release converts Comet Launch; it does not remove legacy touch alternatives from every other game.
