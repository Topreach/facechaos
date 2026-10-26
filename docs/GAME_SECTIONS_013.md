# Per-game journeys ? Android 0.13.0

Eleven more games now have five named sections, matching stage previews, a route list in the instruction sheet and original themed scenery below the selfie. Together with Voice Runner?s five existing worlds this gives 60 sections across 12 games. The app still has 15 games and 75 stages; this update develops the existing stages rather than adding extra game-menu entries.

| Game | Five-section journey |
| --- | --- |
| Emoji Gates | Petal Parade ? Dune Doorways ? Crystal Crossing ? Neon Portals ? Ember Arches |
| Expression Escape | Cookie Trail ? Bramble Bakery ? Frosted Citadel ? Clockwork Kitchen ? Dragon Oven |
| Bubble Garden | Daisy Courtyard ? Rainforest Nursery ? Oasis Conservatory ? Snowdrop Glasshouse ? Moonflower Terrace |
| Melody Hop | Spring Keys ? River Drums ? Canyon Chimes ? Ice Bell Waltz ? Neon Encore |
| Star Duet | Moonlight Duet ? Aurora Dance ? Sunset Serenade ? Nebula Waltz ? Starlight Crown |
| Snack Parade | Berry Picnic ? Forest Feast ? Sunset Boardwalk ? Frost Fair ? Midnight Market |
| Smile Stack | Garden Workshop ? Harbour Heights ? Canyon Columns ? Crystal Spire ? Skyline Summit |
| Comet Launch | Moon Dock ? Coral Orbit ? Amber Belt ? Ice Satellite ? Solar Beacon |
| Firefly Glow | Twilight Pond ? Fern Hollow ? Desert Lanterns ? Winter Lights ? Aurora Sanctuary |
| Sound Grand Prix | Palm Coast Circuit ? Dune Sprint ? Alpine Circuit ? Neon City Cup ? Volcanic Grand Prix |
| Head Dodge | Palm Boulevard ? Desert Freightway ? Alpine Pass ? Neon Metro ? Ember Expressway |

## Changes in play

- Emoji Gates changes the expression order in each section. All three expression types remain available, and the shown gate always supplies the actual requested action.
- Expression Escape uses candy hurdles, thorn bundles, ice hurdles, gear hurdles and ember blocks, plus matching shield projectiles. Hurdle height is drawn from the actual jump requirement; the smile/shield and teeth/chomp controls keep their established meaning. Main section and action-specific previews use the same objects as play.
- Bubble Garden changes garden environment and flowers around its existing six-flower objective. It remains a calm plant/bloom game without forced hazards.
- Melody Hop has five distinct low/high note schedules, each retaining four low and four high notes. Short/stronger sound and touch-duration mappings are unchanged.
- Star Duet has five constellation paths. The six smile/sound cues light the actual points in sequence, and the existing progressively shorter cue windows apply.
- Snack Parade changes pepper placement between sections while preserving six berries, three peppers, and the correct open-mouth/closed-mouth cue for every item.
- Smile Stack adds a secondary sway from the canyon section onward. Alignment checks use the same actual block position shown by the renderer.
- Comet Launch has different destination art and the existing stage/shot-dependent gold power target. Destination language replaces the old assumption that every landing is on the moon.
- Firefly Glow changes lantern environments while its real steady-hold duration and allowed sound band become more demanding. No new hazardous or strained expression is required.
- Sound Grand Prix and Head Dodge gain different course environments, road tones and section previews. Their existing CPU pace, traffic speed, blocked lanes and late-stage targets remain the actual racing challenge; decoration is not represented as a new collision hazard.

Country/flag identification (Flag World Tour), Face Memory and Animal Choir keep their quiz, memory and rhythm rounds. They do not receive obstacle-course scenery. Voice Runner?s own five-world implementation is preserved.

## Consistency

Section names appear in stage selection, the game?s section label above the selfie, the instruction sheet, its route list and its stage-specific illustrated preview. The original control semantics, saved coins and unlocked stage keys remain compatible. Passing a stage uses the established next-stage circle action to move into the next named section. Stage five remains the final challenge with the existing one-life/perfect-target rules as appropriate to each game.

Scenery uses original Canvas primitives behind existing character sprites. Camera pixels at the central face positions must remain unobscured in normal and compact layouts. Tutorial art preserves its aspect ratio. Control calibration thresholds are not raised with stage speed.

## Verification scope

GameSectionsCheck checks all 55 entries, exclusions, names in actual stage rules, unique gate/note orders, balanced berry/pepper schedules and real stack sway. Campaign and touch checks cover all 75 game/stage winning paths. Existing face, speech, sound, jump, voice-course, racing and head-steering components remain independently testable. Device render checks cover 110 full/compact section views, 55 section tutorial images and 15 Expression Escape action images, plus five compact high-stack views. Synthetic render tests never open the camera or microphone.

## Phone check

Choose one of the listed games, read its current named-section preview, and Start. Complete the objective and press the circle to move to the next section. Use Game options ? Stages to revisit unlocked sections. Quiz/memory/rhythm games continue to use rounds. Real human recognition accuracy still needs user play; new scenery and synthetic playthroughs do not independently validate a player?s facial or voice detection.

## Installed verification - 25 October 2026

Android build and lint passed. All 17 pure rule suites passed, including 75 campaign paths and the new section-pattern checks. Samsung SM-A176B passed 110 normal/compact game renders, 55 section tutorials, five high-stack compact renders and 15 themed Escape action renders. Selected scenes and tutorials were visually reviewed. Final track/feet alignment was rechecked across every Expression Escape section and action. Installed package verified as versionCode 28 / 0.13.0-journeys. Final APK: `dist/FaceChaos-Android-0.13.0.apk`. Test package removed and Face Chaos reopened. Live facial/voice play remains the next user check.
