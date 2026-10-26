# More games — Android 0.6.0

The collection expands from six to nine games with three different interaction patterns. All three use original dimensional artwork shared between gameplay and illustrated instructions. The selfie remains the background; prompts and coin feedback appear in the top banner.

| New game | Mechanic | Current control | Win condition |
| --- | --- | --- | --- |
| Snack Parade | Collect strawberries and avoid peppers | Tap to simulate opening the mouth; do nothing for peppers | Catch at least 5 of 6 treats across 9 items; three mistakes loses |
| Smile Stack | Align a moving block with the gold centre mark | Tap to simulate a closed-lip smile | Place at least 6 of 8 blocks; three misses loses |
| Comet Launch | Charge, then release inside a target power zone | Optional microphone: hum, then quiet; touch fallback: hold, then release | Land at least 4 of 6 rockets; three misses loses |

Each accepted treat, block placement or landing awards one coin. Early or wrong responses, pepper hits, missed alignments and off-target launches award none. Rewards cannot repeat for a resolved encounter. Coins use the existing saved local wallet. The main circle still pauses/resumes/retries; the secondary square stops. All games can use optional silent camera-plus-game recording.

## Research and adaptation

[Halfbrick's Fruit Ninja](https://www.halfbrick.com/games/fruit-ninja-classic) describes a simple collect/avoid distinction: slice fruit and avoid bombs. Snack Parade adapts that general decision pattern into treat/pepper timing with a planned mouth expression; it does not reuse Fruit Ninja art or slicing controls.

[Ketchapp's Stack](https://www.ketchappgames.com/game-stack) and the [publisher's App Store listing](https://apps.apple.com/ca/app/stack/id1080487957?platform=ipad) provide the stacking reference. Smile Stack uses alignment timing with a generous fixed target and a planned closed-lip smile input. It uses original pastel toy blocks and rules.

[Rovio's slingshot explanation](https://roviosupport.helpshift.com/hc/en/12-angry-birds-friends/faq/2720-what-are-slingshots/) describes pull-back and release. Comet Launch adapts the general charge/release decision to gentle sound volume and a moving power target. It uses original rocket and moon sprites rather than birds or a slingshot.

[Nintendo's WarioWare: Move It](https://www.nintendo.com/us/store/products/warioware-move-it-118742/) supports the broader direction of a varied collection of short, clearly prompted microgames. Face Chaos uses longer, more forgiving rounds; these sources do not validate our recognition accuracy or prove our games are fun.

## Input limits and testing

Snack Parade and Smile Stack are explicitly face-control previews: taps simulate the expression. Android facial recognition is not connected. Comet Launch reuses the optional foreground microphone sound-level gate, not speech or pitch recognition. Microphone samples are discarded, no audio is added to video, and pausing clears an unlaunched charge to prevent an accidental release. Real microphone sensitivity still requires a user test.

Pure Java checks pass all nine winning/losing playthroughs, game-specific timing, pepper decisions, stack alignment, charge/release, reward uniqueness, the prior sound gate and Star Duet sequence. Native rendering checks cover all nine game scenes/tutorial headers, extra new-game phases and compact portrait layouts without camera/microphone access. These checks do not replace playtesting.

Original art and saved prompt: [collection atlas](../mobile-design/concepts/collection-atlas-prompt.md).
