# Smile Stack 0.16.2

Smile with closed lips when the moving block reaches the gold centre line. Relax between drops so the face detector can recognize the next smile. Watch the block fall and land: a successful landing earns one coin; an off-centre landing costs one life. Each stage offers eight blocks, with targets of 6, 6, 7, 7 and 8 successful placements.

## Changes

- Consistent 112-unit block widths and identical movement/landing coordinates; no sprite resizing or compressed offsets on placement.
- Solid illustrated blocks, shadows, highlights, a gold alignment corridor and an explicit platform.
- Visible drop and missed-block fall; coin awarded after landing rather than on smile input.
- A fixed real-time landing and recovery interval preserves time to relax between smiles. Stage movement still accelerates and alignment tolerance narrows.
- Motion eases through the centre to accommodate expression detection dwell. Global expression thresholds remain unchanged.
- Existing game dimensions and face-aware instruction placement are retained.

## Validation

Pure Java checks passed: five full stages through FaceSignals at a simulated 60 ms detector cadence, delayed and nonduplicated rewards, exact landing coordinates, pause, misses, all face adapters, 75 game/stage win paths, touch alternatives, stage speed and section checks.

Real-device rendering and live smile testing are reported separately; simulated signals do not prove recognition accuracy on a person's face.

Device verification: Samsung SM-A176B installed versionCode 40 / 0.16.2-smile. All 50 native moving, drop, landing, miss and tall-tower scenes passed at two sizes. Tall-tower screenshot visually reviewed. No camera or microphone was accessed by these tests. Temporary test package removed after verification.
