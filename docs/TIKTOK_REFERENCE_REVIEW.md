# TikTok interaction reference review — 24 October 2026

The user asked to learn from similar TikTok games and improve Face Chaos.
The precise effect has not yet been identified; the examples below are relevant
references, not a claim that they are the exact game the user meant. Research
used creator descriptions and official Effect House documentation, not access
to a creator's private code or hands-on testing of every TikTok effect.

## Examples and mechanisms

| Reference | Verified from source | Implication for Face Chaos |
| --- | --- | --- |
| [Open Mouth Challenge — creator Ollie Tyler](https://ollietyler.com/projects/open-mouth-challenge) | Creator describes a 30-second mouth-open/close counting game built for TikTok. | A single clear movement and immediately visible response are a useful small learning loop. We should avoid requiring rapid, strenuous repetition. |
| [Effect House Facial Movement Detection](https://effecthouse.tiktok.com/learn/guides/editor-panels/visual-scripting/nodes/head-and-face/facial-movement-detection) | Provides separate begin/stay/end/none outputs; listed movements include mouth opening, blinks, pout and eyebrow wiggle. | Use onset for a one-shot action, maintain visual feedback while held, and require release before another event. This document does not establish tongue/teeth/cheek detection on our webcam. |
| [Wobbly Face template](https://effecthouse.tiktok.com/learn/guides/tutorials/template-tutorials/wobbly-face) | Opening the mouth drives a visible face deformation using the movement node. | Let the cartoon mouth visibly respond to received jaw measurements, independently of whether a jump is currently eligible. |
| [Perfect Pitch template](https://effecthouse.tiktok.com/learn/guides/tutorials/template-tutorials/perfect-pitch) | Microphone pitch drives note matching; template exposes note sequences and scoring. | A pitch challenge is a narrower task than identifying arbitrary vocal sound names. A future humming/pitch stage should show measured pitch versus a target. |
| [Volume Detection](https://effecthouse.us.tiktok.com/learn/guides/editor-panels/visual-scripting/nodes/audio/volume-detection) | Measures a sound-level value, with an example mapping it to image scale. | A future voice-level runner can use calibrated sound intensity. Do not label volume as recognition of a particular animal, hum, ah or sh. |
| [Audio From Microphone](https://effecthouse.us.tiktok.com/learn/guides/editor-panels/visual-scripting/nodes/audio/audio-from-microphone) | Routes microphone streams to separate volume, pitch and other processing; speaker output is unnecessary for analysis. | Keep audio acquisition separate from interpretation and avoid microphone-to-speaker feedback. |

These are Effect House authoring nodes/templates, not drop-in Python/Godot
detectors. This review adapts interaction patterns; no TikTok character, effect
branding, detector model or template source was imported into the game.

## Changes applied now

1. Godot setup shows live received jaw, smile and teeth levels before play.
2. Setup counts detected onsets for optional practice without starting a course.
   Teeth wins over simultaneous smile/jaw for this display, matching game routing.
   New bridge sessions reset these counts. They are not accuracy measurements.
3. In facial mode, a compact live-input card remains visible in gameplay. Missing
   meters show -- rather than being invented as zero.
4. The avatar mouth reflects received jaw levels. Actual attack still uses its
   chomp pose. This visual response does not bypass action cooldowns or grounding.
5. Accepted actions get brief BOING / BUBBLE UP / CRUNCH feedback. The last
   detector event is shown separately from action acceptance/rejection.

The existing short course -> results -> replay/next-level flow remains intact.
No existing sound meter was relabeled as a sound classifier.

## Next refinements, not implemented in this change

- Use the user's exact TikTok reference to compare framing, input timing, recovery
  and failure feedback. Do not infer those details from another similarly named filter.
- For voice play, first build a comfortable calibrated volume/pitch interaction
  with explicit live targets. Validate separately before adding it to progression.
- Distinguishing humming, ah and sh needs appropriate class-specific testing;
  pitch or level alone cannot establish those identities. Whistling is optional.
- Portrait composition and a separately enabled camera inset can support the
  creator/player reaction framing. The current game still uses landscape and
  sends no camera pixels to Godot.
- Keep later stages mechanically distinct while limiting simultaneous controls.
  This is a design recommendation, not evidence that it guarantees engagement.

## Verification

Production Python detector -> real local UDP -> Godot ability checks pass,
including menu practice counting, live setup text and detected-event feedback.
Gameplay regressions and nine full course simulations pass. The updated feedback
layout was rendered with synthetic meter values and visually inspected. No real
camera accuracy, microphone classifier or TikTok-device test was performed.
