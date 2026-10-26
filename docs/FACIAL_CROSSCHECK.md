# Facial reaction cross-check — 23 October 2026

**Result:** all 14 current sections were audited. The synthetic channel-isolation
matrix passes 14/14 cases. Code corrections and targeted regressions are listed
below. This is not a claim that all 14 expressions work reliably on a person's
webcam. No new human-camera accuracy, false-trigger or latency trial was performed.

Follow-up: detector revision 3 gives a calibrated tongue gesture command priority
over mouth-open/quick-close while retaining both raw readings. A short retraction
guard and neutral rearming prevent delayed mouth commands after tongue withdrawal.
The synthetic tongue fixture now includes high jaw opening to check this conflict.
This is routing behavior, not proof that the tongue heuristic recognizes a real
camera image. Previously emitted mouth events are not retrospectively undone.

User-camera follow-up: tongue protrusion was reported as mouth opening with no
tongue activation. This remains OPEN. The lab now exposes transient search-region,
colour-mask and lower-lip-boundary overlays plus scalar rejection diagnostics.
Following the user's lip/tongue confusion report, a tracked outer-minus-inner lip
ring is explicitly excluded from the colour mask, including after morphological
closing. The lip-only fixture checks that lip pixels are actually excluded.
This geometric exclusion is not semantic tongue recognition; inaccurate lip
landmarks can still remove real tongue tissue. Camera acceptance remains open.
The teeth filter rejects pale pink candidates in new synthetic fixtures. All 63
unit tests and the hidden UI check pass; neither establishes a fix for this
reported camera failure. Diagnostic image masks are excluded from JSON reports.

## What each row actually reads

Revision 4: closed-lip Smile and broad Show teeth prompts replace the prior poses.
A calibrated teeth signal suppresses Smile regardless of control iteration order.
A 250 ms release guard plus neutral rearming rejects a delayed smile after teeth
withdrawal. Six added tests cover this routing, closed-lip smile coefficients,
covered-teeth jaw opening, unavailable teeth and calibration migration (70 total).
These fixtures do not establish semantic recognition of both tooth rows. Prior
smile events cannot be retrospectively cancelled if teeth appear later.

| Section | Input and intended response | Cross-check / remaining limit |
| --- | --- | --- |
| Open mouth | `jawOpen`, independent of teeth pixels | Isolated jaw opening triggers once; exposed teeth are not required. A naturally open-mouth smile may also be a real mouth opening. |
| Close mouth quickly | A recent open jaw followed by a sufficiently fast drop to closed | Added amplitude/rate checks; slow closure is rejected. A quick return to neutral after mouth-opening is still a real quick close. Do not require both commands in the basic game preset without state/conflict rules. |
| Deliberate blink | Both eyes closed, 250 ms activation dwell, then neutral rearm | Ordinary short closures rejected in fixtures; strong scrunch suppresses the command. A naturally slow blink can still be indistinguishable from an intended one. |
| Widen eye | The existing `browInnerUp`/outer-brow signal renamed at the user's explicit request | Deliberately preserved the working detector. This is a personal gesture alias, not a new pure `eyeWide` detector; widening eyes without brow movement may not raise it. Original separate eye-wide row removed. |
| Smile | Left/right smile coefficients | Smiling cheek/eye squint no longer becomes scrunch without an additional brow/nose/lip-press anchor. Actual teeth visibility and jaw opening may legitimately coexist. |
| Show teeth | Bright, low-chroma connected regions inside the inner lips | Separate from jaw score; pixel fixtures cover mouth open with covered teeth, exposed teeth with small jaw opening, both together, and red tongue/surrounding skin negatives. Glare/colour/lighting remain unresolved camera confounds. This is an experimental heuristic. |
| Puff cheeks | Native `cheekPuff` shown diagnostically; events use a separately trained bilateral cheek appearance/shape model | Native raw value is never allowed to trigger an event without a personal model. User has reported unreliable detection. This remains experimental/unaccepted, even when other synthetic tests pass. |
| Tongue out | Connected reddish/pink protrusion below lower lip, compared with nearby skin | Independent of jaw and teeth; synthetic pixel fixtures reject lips alone, teeth alone and detached red chin patches. Small/sideways protrusions, lipstick, skin/tongue colours and pose remain limitations. This is an experimental heuristic. |
| Left wink | Player's left eye closes while opposite eye remains sufficiently open; 150 ms dwell | Added opposite-eye gate to prevent bilateral partial closures becoming winks. Strong scrunch suppresses command; mirror affects preview only, not left/right identity. |
| Right wink | Player's right eye closes while opposite eye remains sufficiently open; 150 ms dwell | Same checks as left; distinct side fixtures pass. Natural eye asymmetry still needs personal testing. |
| Squeeze / scrunch face | At least two movement regions plus a brow/nose/lip-press anchor | Corrected smile-only cheek/eye squint confusion. Strong scrunch owns eye-closure commands; raw closure is still visible. A person who scrunches only their eyes may need a different mapping/calibration. |
| Tilt head | Roll relative to initial/saved pose | Isolated roll tested; missing/invalid pose now displays unavailable instead of zero. Begin facing straight; camera angle/profile changes require rechecking neutral. |
| Nod head | Pitch excursion followed by a reversal | A held head-down pose does not keep generating nods. Missing-pose gaps no longer become synthetic movement. Genuine diagonal head motion may activate more than one axis. |
| Shake head | Yaw movement to both sides and return near neutral | Corrected single sideways glance being classified as a shake. Bilateral excursion now required; held head turn and pure nod fixtures are negative cases. |

Frown is removed. The redundant original Widen eyes row is removed. Feature output
now contains exactly the 14 supported keys, without hidden removed labels.

## Corrections made during the audit

1. Added multi-region scrunch anchoring to reject smile/eye-squint-only input.
2. Added an opposite-eye gate for unilateral winks.
3. Added explicit event conflict handling: a strong scrunch suppresses blink/wink
   commands. The UI explains suppression; raw measurements are not hidden.
4. Added amplitude and rate checks for quick mouth closing.
5. Required both left/right excursions for head shake.
6. Preserved missing pose as unavailable and stopped inferring motion across gaps.
7. Failed closed on invalid/non-finite image-region measurements rather than
   crashing the entire camera session or promoting NaN to full activation.
8. Shared the exact cheek event-readiness guard between UI and tests; an untrained
   live native measurement cannot silently become an accepted cheek event.
9. Added detector revision 2 to profiles/reports. Loading older files preserves
   compatible controls but requires recalibration for scrunch, winks, quick close,
   and head shake because those acceptance rules changed. Old teeth/tongue
   classifier migrations and removed-row filtering remain in place.

## What the automated evidence means

The [matrix result](facial-crosscheck-results.json) is generated by
`tools/crosscheck_facials.py`. It stimulates synthetic coefficients, pose sequences,
and prepared visual/personal-cheek scores. Each target must produce one intended
command with no unexpected command in its action window. The preceding opening
required to test a quick close is intentionally outside that close window.

Separate unit tests exercise synthetic image regions for teeth/tongue, cheek
descriptor geometry, sample separation, ordinary blink timing, head reversals,
profile persistence/migration, invalid data, and reporting. The hidden Tk workflow
check covers the current row list, calibration, test scheduling, evidence reset,
and save/load without accessing a camera or playing sounds.

These tests establish software behavior for the supplied inputs. They do not
establish human expression recognition, statistical independence between facial
muscles, universal tooth/tongue identification, or production readiness.

## Camera acceptance still required

1. Close/reopen the updated lab. Load saved calibration if desired; recalibrate
   controls affected by this revision. Begin looking straight at the camera.
2. Calibrate puff cheeks separately, or leave it disabled if samples overlap.
3. Perform five smoke-test prompts for every desired control. Check the live
   raw rows and suppression reason, not just the event counter.
4. With the final unchanged calibration, collect at least 50 prompted attempts
   per control and a 600-second neutral/confound exposure using the lab buttons.
5. Include normal speech, blinking, smiling with/without exposed teeth, mouth open
   with teeth covered, tongue through a small lip gap, and head glances. Review
   cross-activations against the actual visible movement. Do not mark every
   co-occurring real movement as a detector error.
6. Export the JSON report. Prompt-window rates are preliminary screening, not
   frame-labelled precision/recall or end-to-end latency. Those measurements and
   multiple-user/device tests remain necessary for release acceptance.

No new facial action was connected to gameplay during this cross-check.
