# Game failure audit - version 68

Face Chaos 0.21.2, version code 68. Policy requested by the user: the first physical collision ends the round with Mission failed / Try again.

## Rules for all 15 games

| Game | Failure rule |
|---|---|
| Emoji Gates | An unmatched gate reaching the hero immediately ends the round. |
| Expression Escape | Failed jump, shield or attack ends the round at the authored contact point. The shield/attack input window cannot extend beyond contact. Clear all eight obstacles. |
| Bubble Garden | Expired plant/bloom cues cost a life. Zero lives ends the round. |
| Voice Runner | Colliding outside the safe gap ends the round. Clear all eight obstacles. |
| Melody Hop | Overhang collision ends the round immediately. A weak/missing jump shows its fall and ends the round on fire impact. Clear all eight hops. |
| Star Duet | Expired smile/sound cues cost a life. Zero lives ends the round. |
| Snack Parade | Eating a pepper immediately ends the round. Missed treats cost a life. An unreachable treat target ends the round. |
| Smile Stack | An off-centre block landing or missed block ends the round. Place all eight blocks. |
| Comet Launch | An unsuccessful landing or expired launch window ends the round. Land all six rockets. |
| Animal Choir | Incorrect/expired rhythms cost a life. Zero lives or an unreachable pattern target ends the round. |
| Face Memory | Incorrect sequence actions and response timeouts cost a life. Zero lives ends the round. |
| Firefly Glow | Expired lantern attempts cost a life. Zero lives or an unreachable lantern target ends the round. |
| Sound Grand Prix | Computer crossing the finish first ends the round. Checkpoint coins already earned remain part of that run. |
| Head Dodge | First car collision ends the round, keeping the impact wave visible. Clear every traffic wave. |
| Flag World Tour | Wrong countries and flag timeouts cost a life. Zero lives ends the round. Speech processing retains its existing timer pause. |

Crash-based games start with one life on every stage. Other games retain three cue lives on stages 1-4 and one on stage 5. Stage speed and facial recognition thresholds are unchanged. Missing face tracking or pending sound calibration continues to pause the relevant game rather than punish the player.

## State and display corrections

- Terminal results are immutable: subsequent finish requests cannot overwrite failure with success. Step/input/sound updates are rejected after a result.
- Late face/head and microphone error callbacks cannot alter a finished round.
- Failure offers Try again, resets progression to Stage 1 and clears carried run coins on the next run. Existing best-record persistence remains in place.
- A recoverable cue miss displays a lives-left notice. Instructions and HUD use the same failure policy; crash games show one heart.
- Escape contact is aligned at 2.65 model seconds; Head Dodge cannot generate a new traffic wave after impact.
- Comet planet drawing resets its paint colour/alpha so repeated failure frames remain identical.
- Existing recording controls and continuous recording across retries are retained.

## Verification

37/37 plain-Java suites passed, including all 75 existing winning game/stage paths, all 75 losing game/stage paths, ten additional Escape shield/attack contacts, first-collision checks, immutable terminal state, late input rejection, recoverable misses, unreachable collection target, and Stage 1 retry/carry behaviour. The rule runner now bounds each suite at 45 seconds to detect stale loops that assume movement continues after failure.

Samsung native checks passed all 75 game/stage failure scenes. Repeated frames were pixel-identical after extra steps, inputs and sound updates; each round selected one failure sound event and the localized Mission failed result. Representative Melody Hop and Head Dodge failure renders were visually inspected. No player clips or camera/microphone samples were used by these scene checks.

Optimized version 68 was installed as a separate verification app on Samsung on 4 November 2026. Final optimized checks passed face-model loading, video start/pause/resume/decode, 75 game/language renders and 20 welcome layouts/navigation callbacks. The existing Play app and saved player recordings were preserved.

Release build and bundletool validation passed. Lint: 0 errors, 48 warnings. Release payload checks verified version identity, non-debuggable manifest, target API 36, R8 optimization/mapping, bundled translations, model, ZIP integrity, private-key exclusion and 16 KB packaging/four native ABI layouts. 133 bundle entries verified against the existing upload certificate. These checks and synthetic scenes do not establish real-player facial/sound recognition accuracy.


## Google Play upload

Prepared 4 November 2026. Use `dist/FaceChaos-0.21.2-68-play.aab` (34,744,303 bytes), superseding version 67. No Play Console upload was performed by this task. Monetization remains inactive.

SHA-256: `9df8c96d66999a1e4165dc7cf24f11e3bfaf1b1732b6ef2bf3db976070f057d8`

## Release notes

```text
<en-US>
Games now stop on the first physical collision, fire impact or traffic crash. Clear failure screens and Stage 1 retries make every result easier to follow. Updated instructions and life counters match each game, with clearer missed-cue feedback and fixes for input arriving after a round ends.
</en-US>
```
