# Face Chaos — Reaction Recognition Contract

Status: engineering draft aligned with [Complete Game Design v1.1](GAME_DESIGN.md). Nothing in this document establishes measured recognition accuracy. The complete design is authoritative for product scope and mappings.

## Initial scope

Validate one player using four core mappings: mouth open → jump, Eye widening (currently an eyebrow proxy) → fly, smile → shield, and show teeth → chomp/attack. Keyboard alternatives must exercise the same game action logic. Microphone loudness is a separate optional proof of concept and must not block facial gameplay.

This preserves the prototype's smile → shield mapping. The current opt-in lab connection includes experimental visual teeth events; the legacy bridge does not. Teeth visibility requires a separately validated visual detector; landmarks alone do not establish visible teeth. If this detector fails acceptance, offer a calibrated distinct alternative expression or button, clearly labeled. Deliberate blink/dodge and smile/bonus magnet belong to advanced presets after validation, not the initial required controls.

Advanced expressions, emotion inference, speech recognition, laughter classification, multiplayer, and platform integration are outside this milestone.

## Event transport

Integrated-preview extension: the event channel may include validated `avatar`
render coefficients and a loopback preview port/token. JPEG frames use a separate
opt-in, token-addressed, memory-only local HTTP path; they are never included in
event packets. See [integrated screen contract and limits](INTEGRATED_PLAY_SCREEN.md).

Keep UDP bound to 127.0.0.1:47777. Video and audio remain in the recognition process. Send a versioned JSON packet at a target rate of 30 Hz, including packets with no events so the game can detect a stopped bridge.

Proposed packet:

```json
{
  "version": 1,
  "session_id": "random-id-per-bridge-start",
  "sequence": 42,
  "tracking": "tracked",
  "calibrated": true,
  "events": [
    {"id": 7, "reaction": "mouth_open"}
  ],
  "enabled": ["mouth_open", "brows_up", "smile", "teeth"],
  "meters": {"mouth_open": 0.8}
}
```

`tracking` is `tracked`, `lost`, or `unavailable`. Confidence must have a documented meaning; an arbitrary landmark threshold score must not be represented as a measured probability. Until confidence is validated, it is diagnostic and must not be claimed as accuracy.

Reject unsupported versions, malformed fields, unknown reactions, non-finite values, and packets larger than 4 KiB. Process each event ID once per session, ignore older sequence numbers, and cap packet processing per frame. A new session clears previous sequence and event state. Use local monotonic receive time for connection health rather than comparing process clocks.

UDP can lose events. For this prototype, repeat each event in the next three packets and deduplicate by event ID. Record losses during testing; if reliability remains inadequate, replace this mechanism with acknowledged delivery or a local reliable transport.

## Detection and action rules

- Require a completed personal calibration and stable face tracking before enabling facial actions.
- Smooth landmark measurements over a short bounded interval. Tune the interval against measured end-to-end latency.
- Use separate activation and release thresholds to prevent flicker. One expression produces one event and must return to neutral before rearming.
- Treat blink duration and bilateral eye closure as calibrated measurements; distinguish ordinary blinks and winks through validation rather than assuming they are distinguishable.
- Apply cooldowns in the game, shared across keyboard and facial sources. Starting values: jump 350 ms, flight 1,800 ms, shield 2,500 ms, attack 1,200 ms. These require playtesting.
- Give flight (450 ms), shield (650 ms or one absorbed hit), and attack (300 ms) fixed durations. Held keys or sustained expressions must not extend them indefinitely. Clamp the player within playable bounds.
- Reject actions incompatible with the current character state. Jump requires grounding; define dodge collision behavior explicitly before enabling it.
- Disable combinations initially. For simultaneous events, jump takes priority over flight; attack takes priority over shield without charging the suppressed shield cooldown. Log suppressed events for diagnosis. Optional combination challenges follow the complete design's explicit timing rules.
- Clear input state on menus, restart, pause, calibration, bridge reconnect, and tracking loss. Do not queue reactions for later gameplay.
- Start and restart through explicit confirmation, with an accessible keyboard alternative.

## Calibration and feedback

Capture neutral measurements followed by prompted samples for each supported expression. Save the derived thresholds, calibration schema version, and camera identifier locally. Do not save raw video or audio by default. Allow users to recalibrate or delete their profile.

Show tracking health, calibration state, last accepted reaction, action cooldown, and suppressed-input reasons. Display confidence only when its interpretation is meaningful. Keep diagnostics outside the primary gameplay area.

## Failure behavior

After 1 second without a valid packet, show the bridge as disconnected and clear reactions. On tracking loss, immediately stop accepting facial actions; after 300 ms of continuous loss, pause a facial-control session and offer reconnect or keyboard continuation. A keyboard-only session continues normally. Resume through explicit confirmation and a three-second countdown so reconnecting does not surprise the player.

Microphone loss disables only audio actions in facial modes, and pauses an audio-required mode. Recognition process errors must release the camera, microphone, and socket and report an actionable local error.

## Acceptance plan

Automated checks should cover packet validation, duplicate and out-of-order delivery, event rearming, shared cooldowns, simultaneous reactions, menu-state clearing, disconnect, and reconnect.

Measure each reaction separately on the target PC with prompted attempts and labeled neutral periods. Report precision, recall, missed actions, false activations, and median/p95 end-to-end latency. Include ordinary blinks, speaking, glasses, lighting changes, head motion, and temporary face occlusion. Record participant count, hardware, sample counts, and conditions with every result.

The design targets are at least 95% core recognition in controlled tests, fewer than 2 false activations per 10 minutes, basic response under 150 ms where hardware permits, 60 FPS rendering, and a 2-hour stability session. Define pass/fail statistics and latency measurement boundaries before collecting acceptance data; report per-reaction results rather than hiding failures in an average.

Only enable a reaction as a required control after it passes the agreed tests. Retain remapping and keyboard alternatives for every action.
