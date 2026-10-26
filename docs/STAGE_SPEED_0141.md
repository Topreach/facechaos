# Stage speed - 0.14.1

The shared stage clock already accelerated across stages; this update increases the intermediate stage multipliers from 1.20/1.45/1.75 to 1.25/1.50/1.80. Stages 1 and 5 remain 1.00 and 2.10. Thus Stage 2 is 25 percent faster than Stage 1. Later stages retain their additional obstacle patterns, narrower action windows and tougher objectives. Individual encounter arrival times can shorten by more than the clock multiplier because of those additional difficulty rules.

The HUD, countdown hint, briefing and next-stage result derive labels from the same speed settings that drive simulation. The ready countdown, touch strength sampling, sound duration recognition and face calibration retain their real-time timing. No camera settings or game-area geometry changed. The Stage 1 failure reset and per-game best coin records remain in place.

Verification includes all 15 stage clocks and measured real elapsed time until gates, Voice Runner hazards, Melody Hop judgments, Head Dodge traffic and country answer timeouts: each arrives sooner at every successive stage. All 75 winning stage paths and 75 touch-only paths passed. Voice Runner's 25 synthetic PCM-driven stage paths and Melody Hop's 25 microphone-cadence paths passed. Run restart/carry checks also passed. These rule checks do not claim real-user face or voice accuracy.

Offline debug/test build and Android lint passed. Native phone checks confirmed preserved run records, Stage 1 retries and result rendering. Installed versionCode 33 / 0.14.1-speed on Samsung SM-A176B. Temporary test package removed.
