# FACE CHAOS — Complete Game Design

Version 1.1 · 23 October 2026 · Gameplay design baseline

Platform update, 24 October 2026: [Mobile application design](MOBILE_APP_DESIGN.md)
supersedes the original Windows-first platform decision and launch scope below.
Android is first, then iPhone. This document retains the broader gameplay vision;
the existing Windows build is a working prototype and reference harness.

This document defines the complete intended game. It is a design, not a claim that the features exist. The original supplied v1.0 specification is the vision source. This revision resolves gameplay rules and the later request for mouth → jump, eyebrows → fly, smile → shield, and teeth → attack. Numerical balance values are initial tuning values, not measured recognition results.

The complete product includes all seven modes. The first playable milestone and the first release are delivery steps toward that product, not replacements for it. Recognition-dependent features have explicit fallback designs below.

## 1. Product identity

Approved campaign extension: [sound controls and stage progression](SOUND_CONTROLS_AND_STAGES.md)
defines humming, sh, ah and optional whistling with eight sequential stages and
three levels each. This is an implementation target, not a completed campaign
or validated sound recognition.

**Pitch:** Your face is the controller. Survive ridiculous cartoon challenges while the game turns your expressions into spectacular mistakes and surprising victories.

**Primary audience:** casual PC players, friends sharing a computer, and streamers creating short, readable comedy moments. A session should be enjoyable without a livestream or audience connection.

**Platform:** Android first, then iPhone, with portrait camera-first presentation
and on-device processing. See the mobile design for launch scope and native
camera integration gates. The Windows Godot prototype remains useful for
gameplay experiments. Online multiplayer, cloud accounts and a level editor
remain outside the first mobile release.

### Experience pillars

1. The character immediately reflects the player's intended action.
2. Failure is funny, brief, and easy to retry.
3. Surprises change decisions while preserving readable warnings and a possible response.
4. Players can win through calibrated comfortable movements or alternative controls.
5. Every mode works as a game with an objective, progression, and an end condition.

### Session loop

Choose profile → confirm devices or keyboard → calibrate if necessary → choose mode/character/challenge → learn required reactions → countdown → play → results/rewards → retry or select another challenge.

A first session includes a roughly five-minute onboarding target. Returning players perform a short tracking and neutral-face check rather than repeating calibration unnecessarily. Typical rounds last two to five minutes; marathon survival and custom practice are optional exceptions.

## 2. Scope and delivery boundaries

| Delivery | Contents | Exit requirement |
| --- | --- | --- |
| Recognition milestone | Camera setup, calibration, preview, reaction feedback, four core action mappings, keyboard fallback | Measured inputs on the target PC; unreliable expressions have substitutes |
| Vertical slice | One finished obstacle-race level, Bouncy, five distinct obstacles, basic audio/animation, results, saved records, one warned comedy event | A new tester can set up, finish a facial-control run, understand failure, and retry |
| First public release | Polished Obstacle Race campaign, Try Not to Laugh, Chaos Survival, four characters, progression, accessibility, local records, OBS layouts | Release checks pass; no required unvalidated detector |
| Complete v1 | All seven modes, local Face Battle, expanded optional controls, four themes, highlights, studio, supported audience voting | Every shipped capability passes its own gate |

No release screen advertises a planned capability as working. Unsupported platform connections show an unavailable explanation. Advanced recognition is optional content; a failed detector gate changes its input method rather than leaving a required mode impossible.

## 3. Player controls and action model

### Core action set

| Action | Facial default | Keyboard | Controller default | Duration / recharge | Rules |
| --- | --- | --- | --- | --- | --- |
| Jump | Open mouth wide | Space | South face button | Instant / 0.35 s | Grounded; 100 ms coyote time; 120 ms early-input buffer |
| Fly | Eye widening, after validation | Up | North face button | 0.45 s / 1.8 s | Bounded upward lift; current legacy implementation uses an eyebrow proxy |
| Shield | Closed-lip smile | S | Left shoulder | 0.65 s / 2.5 s | Absorbs one eligible impact, then ends |
| Chomp | Teeth baring, if validated | A | West face button | 0.30 s / 1.2 s | Short forward attack, breaks designated obstacles |
| Pause | Explicit button | Escape | Menu | Immediate | Expressions never toggle menus unexpectedly |

Cooldown starts on accepted action. Every source uses the same cooldown and action rules. Fresh press or return to neutral is required; holding a key or expression does not repeat an ability. The HUD displays READY or remaining seconds. Rejected inputs show a small reason in feedback, not a full-screen interruption.

Show-teeth detection is experimental: landmark geometry alone cannot establish visible teeth. Ship it only after a separate visual detector passes testing. If it fails, offer a calibrated alternative gesture that the user can perform distinctly, or the attack button. Do not label a grin proxy as confirmed tooth detection. Current loud-sound → attack prototype behavior is retired when this action router is introduced.

### Expanded control vocabulary

These defaults apply in advanced presets or mode-specific challenges, not all at once. Setup shows the active vocabulary before a round; starter levels use at most two required reactions and later normal levels at most four.

| Input | Action | Gameplay definition / eligibility |
| --- | --- | --- |
| Close mouth quickly | Drop | End lift and accelerate downward; ignored when grounded |
| Eye widening | Fly | Same named movement as the core fly candidate; do not create a duplicate eye-wide reaction |
| Deliberate blink | Dodge | 0.35 s low-profile roll; 1.2 s recharge; ordinary blink discrimination required |
| Smile | Shield; bonus magnet in an explicit advanced preset | Preset changes are shown before play; never silently remapped |
| Puff cheeks | Inflate | 1.5 s slow fall and larger collision silhouette; optional challenge |
| Tongue out | Grab | Pull one marked collectible within 180 world units; optional detector |
| Wink | Teleport | Move to a visible safe anchor; 3 s recharge; no unannounced destination |
| Face scrunch | Grumpy power | Frown removed at user request; gently scrunch nose/cheeks/brows; optional after validation |
| Tilt head | Steer | Bounded horizontal steering in arenas; calibrated neutral dead zone |
| Nod | Dash / confirm | Dash in gameplay; menu confirmation requires explicit opt-in dwell confirmation |
| Shake head | Reverse / reject | Mode-specific direction change; never quits an active run |

### Audio vocabulary

| Input | Action | Detector and fallback |
| --- | --- | --- |
| Comfortable loud vocal sound | Super jump | Calibrated loudness envelope; no requirement to scream |
| Laughter | Bounce boost | Optional classifier; use pulse/button challenge if unavailable |
| Whistle | Summon helper | Optional spectral detector; alternative button |
| Humming | Power platforms; bridge variant only in an explicitly named mode preset | Sound-stage mapping is primary; classifier unimplemented |
| Sh sound | Wind push | Candidate sound-stage input; local classifier and alternative button required |
| Ah vowel | Sonic pulse | Candidate sound-stage input; suppress jaw-open jump during the audio cue |
| Sing | Dance obstacles | Optional music/vocal classifier; rhythm-input alternative |
| Clap | Confetti power | Transient detector; button alternative and noise rejection |
| Blow | Push marked objects | Optional detector; sustained button alternative |
| Say “No” | Reverse | Optional local speech recognition; remappable button |
| Say “Wow” | Grow | Optional local speech recognition; remappable button |
| Silence | Stealth | Calibrated room-relative threshold; exclude startup, mute, and disconnect |
| Short repeated sounds | Rhythm hops | Onset detector with minimum separation |

No classifier claims to determine a real emotion. When sound is disabled, mode rules use explicit alternative inputs. A muted/disconnected microphone is a device state, not successful silence.

### Combinations and conflicts

Combination inputs must begin within 250 ms of one another. Evaluate combinations before constituent actions, waiting at most that window in explicit combo challenges; ordinary modes dispatch actions immediately and do not secretly delay jump. Constituents consumed by a combination do not also fire their actions.

| Combination | Effect | Recharge |
| --- | --- | --- |
| Wide eyes + calibrated loud sound | Rocket Face: powerful bounded launch | 5 s |
| Smile + validated laughter | Laugh Explosion: three timed bounces with cosmetic confetti | 6 s |
| Puff cheeks + blow | Balloon Storm: slow fall and push nearby marked objects | 6 s |
| Teeth + clap | Super Chomp: enlarged forward bite | 5 s |

Combo challenges require validated inputs or two mapped alternative buttons. Jump has priority over simultaneous flight. Chomp has priority over shield on the same tick; suppress shield without charging its cooldown. Movement transformations are mutually exclusive; a new transformation cannot overwrite another unless the mode explicitly announces that rule. Pause, tracking recovery, death, and level finish cancel pending combinations.

## 4. Shared gameplay rules

Use fixed physics ticks and swept/continuous checks where speed could cross a small obstacle between ticks. Visual squash/stretch never changes collision size except for clearly signaled transformations. Player action, obstacle, and scoring definitions are data resources, not duplicated across modes.

**Run lifecycle:** setup → tutorial prompt → three-second countdown → active → finished/failed → results. Pause and device recovery are distinct suspended states. Results are committed once. Actions received outside active play are discarded; never replay queued camera inputs after resuming.

**Default normal difficulty:** three hearts per level. A hit removes one heart, removes the overlapping hazard, grants 1.0 s invulnerability, and breaks the score chain. Falling into a pit consumes a heart and restores the latest safe anchor. Zero hearts ends the round. Hard mode may use one heart; the setup screen states it. Retry begins within two seconds of confirmation, without device setup repetition.

**Failure readability:** freeze briefly up to 100 ms when reduced-motion settings permit, show the impact source, and display a brief specific explanation such as “Chomp cannot break steel.” All effects can be reduced.

Difficulty changes warning time, obstacle speed, and pattern complexity; it never changes a person's calibrated threshold to make detection harder. Assist settings may extend action windows, simplify patterns, or enable auto-run; their use is recorded with the result.

## 5. Seven modes

### Mode 1 — Crazy Face Obstacle Race

**Objective:** reach a finite finish line through a scrolling obstacle course. The latest approved campaign is eight sequential stages with three levels each, detailed in [sound-stage progression](SOUND_CONTROLS_AND_STAGES.md). The introductory prototype uses 28/34/40-second teaching courses. The earlier 120-second course with three safe-anchor sections remains a full vertical-slice target, not a completed feature or a requirement that introductory levels last two minutes. Endless is an optional variant, not the campaign completion rule.

The character auto-runs. The player jumps, flies, shields, and chomps. Coins invite optional risk; finishing never requires collecting every coin. Level one teaches jump and shield, level two introduces flight, and level three introduces attack after its chosen input is usable. Tutorial gates can use keyboard or remapped inputs.

| Obstacle | Behavior | Correct responses | Fairness rules |
| --- | --- | --- | --- |
| Rolling toilet | Ground hazard rolling toward player | Jump, shield, or chomp porcelain variant | Flush sound and readable outline; steel variant uses a different silhouette |
| Giant chicken | Telegraphs a peck across ground | Jump over peck, shield, or chomp during exposed phase | At least 1.0 s peck warning on normal |
| Banana trap | Visible slippery ground patch | Jump or accept reduced traction | No forced instant collision after slip; optional drop/dodge route |
| Bouncing jelly | Repeating ground/air arc | Time passage, shield, or fly above safe arc | Landing marker and stable timing |
| Spring boxing glove | Fixed piston with windup | Jump or shield; cannot chomp metal | Clear windup, retract gap, no off-screen strike |

Later obstacles: fan gusts, swinging plungers, breakable biscuit walls, safe gap crossings, conveyor belts, and a giant foam head. Every hazard declares damage type, counters, telegraph, bounds, and score eligibility.

Pattern generation uses authored templates with supported-action requirements. Minimum visible decision time on normal is 1.2 s, including tracking/action latency. Spawn sequences reserve recovery space after a required action so cooldowns cannot make the next obstacle impossible. Two mutually exclusive mandatory actions never overlap.

**Win:** cross finish with at least one heart. **Lose:** zero hearts. Score includes progress, clean obstacles, timing, coins, and finish bonus.

### Mode 2 — Try Not to Laugh

**Objective:** keep a neutral face through five 20-second comedy scenes, separated by five-second breaks. Player sees the scene and a composure meter; live reaction feedback can be hidden without changing detection.

Calibrated smile onset adds 25 composure stress, at most once per 1.5 s. Validated laughter adds 35, but is optional and announced. Stress decays by 8/s after two neutral seconds. At 100 stress, a ridiculous cosmetic penalty occurs and one of three composure hearts is lost; reset stress to 30. No penalty changes camera thresholds. Ordinary speech and face loss are not laughter.

Scene families: serious chicken interview, robot malfunction, suspiciously formal potato parade, dramatic toilet launch, and Bouncy's fake awards ceremony. Replays shuffle authored variations; players may skip disliked scenes.

**Win:** finish five scenes with a heart. **Lose:** lose all three. Score rewards neutral time and surviving scenes. If smile recognition is unavailable, this mode is explicitly manual practice, with records separated from recognized-input rounds; it does not claim to test laughter through keyboard input.

### Mode 3 — Don't Scream!

**Objective:** escape three monster rooms under changing sound rules. Each room lasts 45 seconds. A 15-second setup measures ambient noise and a comfortable vocal level.

Green “MAKE A SOUND” segments use sound pulses to open gates; blue “QUIET” segments increase monster suspicion if the calibrated envelope exceeds the quiet threshold for 300 ms. Suspicion falls in quiet periods; at 100, lose a heart and reset to 30. Normal rounds allow three hearts. Warning changes appear two seconds before taking effect and never demand loud-to-quiet transitions without that grace period.

Microphone disconnect pauses this audio-dependent mode. Keyboard alternative uses a clearly labeled sound-action button and separate records. **Win:** open the exit in all three rooms. **Lose:** zero hearts or the final exit timer expires. No scoring rewards higher vocal volume beyond crossing the threshold.

### Mode 4 — Mouth Power

**Objective:** collect three keys and reach the exit in a 180-second puzzle course. Use mouth-open jump, quick-close drop, and attack; optional inflate/grab routes are bonus shortcuts.

Each room introduces one interaction: bounce switches, breakable biscuit doors, movable balloon platforms, and tongue-grab tokens. Required inputs are chosen from the player's validated mappings. Three checkpoints preserve acquired keys on damage. Three hearts; key items cannot be permanently destroyed or moved beyond recovery. Holding restart resets only the current puzzle after explicit confirmation.

**Win:** three keys and exit. **Lose:** zero hearts or timeout; retry from the room in practice, from the course start in scored play. Score rewards completion and optional tokens, not uncomfortable mouth endurance.

### Mode 5 — Face Battle

**Objective:** win best-of-three short challenges against another local player. Initial complete-v1 implementation is alternating turns using one camera and separate profiles. Equal challenge seeds and isolated player calibration make comparisons fair. Players cannot see the next player's unrevealed prompts during their turn.

Round types: a 60-second obstacle sprint, ten reaction prompts with at least two seconds per prompt, and a 45-second collection challenge. Finish distance/score determines the winner; prompt rounds use correct count then median correct response time. A tie goes to five extra prompts; a second tie is a shared round point. If both reach two points simultaneously, the match is a draw.

Simultaneous split-screen with two cameras is a later optional enhancement within this mode after hardware testing. One-camera two-face identity tracking is not required. Each player may choose accessible alternative controls; result categories record control presets without making medical judgments.

### Mode 6 — Sound Madness

**Objective:** complete a three-song comedy set, each 60 seconds. Follow visual beats and create bridges, hops, or confetti through calibrated audio onsets. Starter content uses loudness pulses and silence only; humming, whistle, and clap are optional validated variants.

Each beat has a normal ±180 ms input window, adjustable for comfort and measured device offset. Grade Perfect within ±80 ms, Good within the remaining window, otherwise Miss. One input scores one beat; room noise cannot repeat-trigger a sustained sound. Track accuracy and a chain; missing a beat breaks the chain but does not fail the song. End the set with a bronze/silver/gold rating. A comfortable-input or keyboard rhythm preset provides the complete set without specialist classifiers.

### Mode 7 — Chaos Survival

**Objective:** survive consecutive 30-second rounds with three shared hearts. Each round draws one compatible challenge and at most one warned modifier, followed by a five-second rest. Every five rounds offers a safe choice between two temporary perks: shorter cooldown, extra shield duration, or recover one heart up to three.

Challenge pool borrows obstacle patterns, sound rules, and reaction prompts only if the current profile supports them. Difficulty increases every three rounds to a tested cap. Cosmetic surprises continue rotating after the cap; speed does not rise forever. **End:** zero hearts or voluntary bank-and-exit between rounds. Save a resumable solo checkpoint only at those breaks; resuming preserves seed, round, health, and perk state.

## 6. Comedy Director

The Director selects authored event definitions; no generative AI service is required. Track mode, available actions, health, recent damage, performance over the last 20 seconds, event history, and safe event windows.

Start with a budget of one disruptive event per 20–35 seconds, at most one active mechanical event. After damage, defer disruptive events for at least eight seconds. After a new required action is taught, allow ten seconds of normal play. Never trigger during countdown, calibration, recovery, a finish transition, or a blocking tutorial.

| Event | Warning | Active effect | Duration / restrictions |
| --- | --- | --- | --- |
| Giant head | “BIG HEAD IN 3…” | Oversized visual head; normal collider | 6 s, purely cosmetic |
| Chicken parade | Horn and incoming silhouettes | Chickens dance behind gameplay | 5 s, no occlusion of hazards |
| Egg mode | Cracking shell countdown | Egg animation and small bounce | 5 s; collider unchanged in starter levels |
| Slippery floor | Floor highlights + 3 s countdown | Reduced horizontal traction in arena modes | 6 s; no incompatible pit sequence |
| Reverse rule | Explicit action labels + 3 s countdown | Swap two supported actions | 5 s; advanced opt-in, never first session |
| Fake finish | Suspicious finish and comic reveal | Visual joke before real visible finish | No removal of earned completion; cannot hide a pit |
| Tiny wings | Feather cue | Cosmetic flight embellishment | Next valid flight; no forced activation |
| Confetti sneeze | Character windup | Short particle burst | Non-obscuring; reduced-effects alternative |

Do not repeat the same event within the last three events. Event eligibility checks include disabled effects, required controls, device availability, hazard schedule, and cooldown feasibility. If no safe event exists, skip the event. Performance adjusts template complexity within selected difficulty; it does not secretly guarantee wins or punish successful detection.

## 7. Characters, art, and animation

Characters have identical competitive physics and action timing. Personality comes from silhouette, poses, sound, and effects. No character purchase or unlock is required to obtain an easier action.

| Character | Shape / palette | Signature comedy | Unlock |
| --- | --- | --- | --- |
| Bouncy | Rounded yellow blob, dark readable features | Stretchy mouth, wobbling landings | Available initially |
| Captain Chicken | White feather body, red comb, orange beak | Panic flaps and feather puffs | Complete tutorial + first course |
| Potato Panic | Irregular brown potato, tiny limbs | Rolling recovery and peeling costume gag | Earn 20 campaign stars |
| Robo Goof | Teal box robot, flexible antenna | Eye-display errors and harmless sparks | Complete any three modes |

Required animation states per character: idle, run, jump rise, fall, land, fly, shield, attack, hit, recover, defeated, victory, plus mode-specific dodge/inflate/grab/teleport/grumpy where enabled. Action timing comes from gameplay data; animation must not delay accepted inputs. Use sprite sheets or 2D cutouts with squash/stretch, visible anticipation for hazards, and brief recovery poses.

**Art direction:** bright warm characters against quieter cool backgrounds; thick silhouettes; large expressive eyes; hazards readable without color alone. Keep faces and action effects legible in a cropped vertical stream. No third-party reference image in the source specification is automatically licensed production art.

**Four environment themes:** Bathroom Boulevard, Chicken Carnival, Potato Factory, Robo Disco. Each needs layered background, floor/platform set, obstacle variations, safe-anchor landmark, finish gate, collectible treatment, and a small comedy prop set.

**Audio:** short distinct cues for accepted jump/flight/shield/chomp, cooldown rejection, damage, collection, countdown, rule change, connection loss, victory, and defeat. Separate music, effects, voice, and interface sliders. Captions/icon equivalents accompany essential cues. Avoid loud surprises; announcer lines are short and can be disabled.

## 8. Campaign, scoring, and rewards

Obstacle Race campaign contains 12 authored levels: three per theme. First two levels teach the vocabulary; later levels combine it. Each level has an authored seed/pattern schedule for records; optional daily-style local challenges use visible seeds without requiring an online service.

| Score event | Points | Anti-farming rule |
| --- | --- | --- |
| Correct prompted reaction | 10 | One award per prompt |
| Perfect prompt timing | Additional 25 | Only explicitly timed prompts |
| Clear an obstacle safely | 50 | One award per obstacle ID, including a successful shield/chomp counter |
| Coin | 5 | One collection per token ID |
| First combo discovery | 100 | Once per profile per combo |
| Finish level | 250 | Once per run |
| Finish without damage | 500 | Once per run |

Race does not award points for repeatedly performing actions in empty space. A chain multiplier rises by 0.25 after each five clean obstacle clears to a cap of 2×; damage resets it. Multiply obstacle points only. Survival adds 100 per completed round. Other mode scoring is defined in its mode section and appears under separate leaderboards.

Each campaign level offers three stars: finish; meet an authored score threshold achievable without optional inputs; finish without damage. Stars unlock cosmetics and later themes. Completing a level unlocks the next; assist completion still advances the campaign. Cosmetics use earned tickets, never livestream gifts. Initial plan has no cash shop or paid random rewards.

Results show score breakdown, objective completion, stars, personal best comparison, control preset, and one useful suggestion. Unlock animations are skippable. Leaderboards are local per mode/challenge/difficulty and control/assist category; no global anti-cheat claim is made.

## 9. Screens and navigation

| View | Main contents | Primary exits / behavior |
| --- | --- | --- |
| Main menu | Play, Continue Survival, Practice, Studio, Settings, Help | Play → mode selection; first play → setup |
| Mode selection | Seven mode cards, objectives, prerequisites | Select → character/level setup; unavailable capability explained |
| Camera setup | Device list, preview, tracking health, mirrored-preview switch | Test → calibration; keyboard-only bypass |
| Microphone setup | Device list, live level, room noise, comfortable voice test | Save → calibration; skip audio |
| Reaction calibration | Neutral and individual reaction prompts, sample quality, retest | Pass → mapping; retry or disable failed input |
| Control mapping | Reaction/action assignment, conflicts, keyboard/controller bindings | Test arena or save |
| Character selection | Four characters, costume preview, unlock requirements | Select → level/challenge |
| Level selection | Theme map, stars, difficulty, required inputs, estimated duration | Start → preflight and countdown |
| Gameplay | World, health, score, action readiness, current objective | Pause, finish, defeat, recovery |
| Reaction feedback | Tracking, measured activation bars, accepted/suppressed events | Docked panel in setup/practice; optional compact HUD |
| Results | Breakdown, rating/stars, record, retry, next | Rewards if unlocked; otherwise retry/select |
| Rewards | New character/costume/theme and ticket total | Equip or continue |
| Local multiplayer | Player profiles, turn order, mappings, match rules | Begin → Face Battle |
| Livestream studio | Layout preview, safe areas, privacy, host shortcuts | Apply layout, open clean game view |
| Audience voting | Provider status, vote choices, timer, moderation, manual mode | Start/close vote; safe-window application |
| Leaderboard | Local records, filters, date, preset | Replay metadata if available; clear with confirmation |
| Highlights | Event timeline, game-only clips if recorded, deletion | Play/export/delete; never enable recording implicitly |
| Accessibility | Alternatives, sensitivity, hold/toggle support where appropriate, effects | Save and return; test inputs |
| Settings | Audio, display, device profiles, storage/privacy | Apply; reversible display confirmation |
| Help/tutorial | Illustrated controls, practice challenges, troubleshooting | Practice or return |

Shared settings panels can combine functional views without removing functionality. All screens support mouse, keyboard navigation, and controller focus. Facial-only menu navigation is optional; never rely on a grin to confirm deletion or device consent.

### Gameplay layout

Landscape logical canvas is 1280×720, scaling to the window with safe margins. Top left: score/objective; top right: health and pause; bottom: four action icons with recharge rings and current mappings. Center is reserved for play. Warnings appear above the character's route without covering the landing zone.

Portrait stream output is 720×1280: title/score at top, complete gameplay viewport in the middle, reaction prompt and optional camera/avatar panel below. Do not crop away upcoming hazards. The separate portrait composition preserves the full gameplay view, with letterboxing if necessary. Player desktop controls remain accessible outside the clean output.

## 10. Recognition and device experience

Calibration flow: select camera → neutral face for three seconds → three comfortable repetitions per required expression → individual sensitivity adjustment → 30-second mixed test with neutral periods → save. A calibration step fails clearly if samples overlap neutral too strongly, face size is too small, or tracking is unstable. Failed inputs stay disabled; the user can choose another input or keyboard fallback.

Use aspect-correct landmark measurements, smoothing with a bounded delay, separate activation/release thresholds, and minimum dwell/release durations. Save device/profile-specific ranges rather than universal facial assumptions. Sensitivity changes detector thresholds within measured ranges; it never directly changes player speed.

Preview shows a mirrored local image by default, face presence, per-input activation meter, enabled/disabled state, and the last accepted action. Color is supplemented with labels/icons. An activation meter is not a calibrated probability; display detector confidence only if its meaning is documented.

Show explicit states: camera unavailable, camera open/no face, calibrating, ready, tracking lost, bridge disconnected, and keyboard-only. A bound UDP port is never labeled “recognition working.” Feedback must distinguish reaction detected from action rejected by cooldown or character state.

On face loss: stop accepting facial actions immediately; after 300 ms of continuous loss pause a facial-control round, with no simulation advance during recovery. On bridge timeout after one second, pause and offer reconnect or keyboard continuation. Reacquire stable tracking, require explicit resume, then give a three-second countdown. Clear input history on every transition. A keyboard-only round continues without a camera. Microphone loss pauses an audio-required mode; other modes disable audio inputs and continue with a notice.

## 11. Technical design

### Systems and ownership

| System | Responsibility |
| --- | --- |
| Application state controller | Screens, round lifecycle, pause and recovery |
| Recognition service | Camera/audio acquisition, calibrated measurements and local detection |
| Bridge adapter | Packet validation, heartbeat, sequence/event deduplication, bounded processing |
| Input interpreter | Mapping, action eligibility, cooldown, combinations, rejection reasons |
| Character controller | Fixed-step movement, bounded abilities, damage and action execution |
| Mode controller | Objectives, timers, end conditions, mode-specific scoring |
| Level/pattern scheduler | Authored obstacles, seeds, safe anchors, solvability constraints |
| Comedy Director | Eligible event selection, warnings, history, budget |
| Presentation | Animation, audio, particles, HUD and clean stream layouts |
| Profile/save service | Calibration metadata, preferences, progression, local records |
| Highlights service | Game event timeline and explicit optional clip capture |
| Voting adapter | Provider-independent choices, vote windows, validated result application |

Godot owns all authoritative gameplay. Python never directly edits score, position, health, or rewards. Recognition runs separately so a camera failure cannot crash the game. Use MediaPipe Face Landmarker and OpenCV for the intended implementation; specialist audio/visual models are added only when justified by validation. Pin versions and model checksums after a reproducible setup test. No cloud AI dependency is required for core play.

### Local recognition protocol

Versioned UDP on 127.0.0.1:47777 is the initial transport. Packets include schema version, bridge session ID, increasing sequence, tracking state, calibration state, device capability flags, and an event list with unique IDs and reaction names. Detailed constraints live in [recognition-contract.md](recognition-contract.md).

Target heartbeat is 30 Hz; drop outdated samples rather than blocking render. Events repeat over three packets and are deduplicated. Maximum packet is 4 KiB and maximum processing budget is 64 packets/tick. Reject malformed data and unknown versions. Bound session IDs, event list length, and numeric ranges. Do not include images, audio samples, names, or credentials. Use monotonic receive time for timeout. If local event delivery proves inadequate, move to a reliable local transport before release.

### Content and persistence

Author Godot resources for ActionDefinition, ObstacleDefinition, PatternDefinition, LevelDefinition, ModeDefinition, CharacterDefinition, and ComedyEventDefinition. Each has a stable ID and schema/version when serialized. Level patterns declare allowed counters, minimum spacing, and required capabilities; validation rejects unsolvable schedules.

User storage contains versioned preferences, profiles/calibration, progression, records, and optional highlight metadata. Write saves atomically through a temporary file and keep a previous valid backup. Validate types/ranges on load; corruption shows a recovery choice and preserves the damaged file for user export. Progress saves on results, unlock, settings changes, and survival rest points. Never require raw camera footage to restore a profile.

### Privacy and packaging

Camera and microphone access are user initiated. Preview stays local. Default storage excludes raw recordings. Highlights record game events only until the player explicitly enables game-video capture; webcam/audio inclusion needs separate opt-in. Provide delete-profile and delete-recording controls with clear effects.

Windows package includes the Godot executable, recognition runtime, required licensed models, third-party notices, and device troubleshooting instructions. Manage recognition child-process startup, shutdown, crash recovery, and logs. Final package must work on a clean test account without a manually installed developer Python environment. Hardware minimums are published only after benchmarking.

### Highlights and replay behavior

The default highlight is a local timeline entry: mode, level/seed, game version, event time, accepted actions, and the funny event type. Keep the last 20 session entries, with favorites exempt from automatic cleanup. Do not call a timeline entry a video clip. A highlight card shows whether it has a playable recording, compatible game replay, or metadata only.

Game-only replay captures action events, Director decisions, random state, and periodic authoritative snapshots. Verify repeatability on the same game version; if physics diverges, use snapshots for playback correction. Replays never affect records or rewards. Incompatible versions open metadata and any recorded video rather than playing an inaccurate reconstruction. Deterministic replay is not required to unlock the highlights screen.

Optional game-video recording uses an explicit enabled indicator, bounded disk budget, and configurable game-only capture. Webcam and microphone inclusion are separate choices defaulting off. Offer a 10-second-before/5-second-after highlight window only when an enabled recorder has that buffer; otherwise mark the moment without inventing a clip. If encoding causes frame stalls or storage fails, stop capture and show a notice while gameplay continues. Confirm before overwriting an existing export or deleting favorites.

## 12. Livestream studio and audience interaction

OBS captures the clean game window or a dedicated composition window. Provide landscape/portrait presets, movable safe-area camera/avatar panels, and a “hide real camera” toggle. A cartoon avatar may mirror measured mouth/eyebrow/blink parameters without claiming emotional interpretation.

Host shortcuts: mute announcer, hide preview, pause, show next prompt, start/close a vote, and disable audience events. Never require streaming credentials for normal play.

Voting opens for 15 seconds with two or three safe eligible options. One eligible vote per viewer per window, tie broken by seeded selection. Apply results only at a Director safe window; if conditions changed, choose an eligible replacement or skip. Announce the result and upcoming effect. Gifts and payments do not buy damage, wins, unlocks, or vote weight.

The provider adapter isolates authentication and platform-specific messages. YouTube/TikTok connectivity requires separate current interface and account validation during that milestone; this design does not promise API access. Ship manual host voting and local simulated-vote testing independently. Store tokens outside ordinary profile JSON and provide disconnect/revoke controls.

## 13. Accessibility and comfort

Every required action has a remappable alternative. Provide keyboard/controller-only play, audio-free play, low-expression-amplitude calibration, adjustable reaction windows, reduced motion, reduced particles, high-contrast indicators, readable text scaling, essential captions, and independent volume sliders.

Avoid repeated forced blinking, prolonged cheek inflation, extreme facial positions, and competitive shouting volume. Offer a break after approximately ten minutes, dismissible without punishment. Tutorials ask for comfortable movements and let users skip an expression. Disabling an input regenerates or filters challenges before a round; never discover a missing capability at an unavoidable hazard.

## 14. Validation and acceptance

All values here are targets to test. Passing automated tests does not prove that recognition works on a person's camera.

| Area | Acceptance evidence |
| --- | --- |
| Core detection | Per enabled expression: precision and recall each at least 95% in a defined controlled test; at least 50 prompted attempts per tester and a separate neutral/confound period |
| False activations | Fewer than 2 unintended accepted actions per 10-minute neutral/confound session, reported by expression and in aggregate |
| Response | Target p95 under 150 ms from observable expression threshold crossing to visible game action; report camera FPS, capture delay, instrumentation, and uncertainty |
| Participants | Initial gate: target PC/operator; before public release: at least ten testers with varied appearance, glasses, camera angles, and comfortable movement ranges; report failures and sample limits |
| Rendering | 60 FPS target on declared test hardware; measure representative heavy scenes and frame-time spikes |
| Stability | Two-hour mixed play session including reconnects, menu transitions, retries, and save reload |
| Recovery | Camera unplug, face occlusion, microphone unplug, bridge crash, invalid packet, port conflict, and reconnect all produce defined states |
| Gameplay | Every authored mandatory pattern solvable with selected capabilities, ability recharge, and normal warning budget |
| Modes | Each mode can start, explain its rules, finish/fail, score once, show results, retry, and save records |
| Accessibility | Complete a campaign level with keyboard only, controller only, camera audio-free, and assist preset |
| Packaging | Clean Windows test account installs/starts, sets up devices, plays, saves, exits, and restarts |
| Broadcast | Record one landscape and one portrait OBS session; verify legibility, privacy switches, audio, and voting fallback |

Recognition logs store event timing and derived diagnostics, with no raw capture by default. Accuracy tests use separately consented labeled capture or a documented live ground-truth method. Report observed rates and confidence intervals; the initial test counts establish a gate, not a universal guarantee. Failed required detectors receive alternative mappings before release.

Automated coverage focuses on state transitions, packet validation/deduplication, cooldowns, rearming, combo timing, damage/score idempotency, progression, save migration, deterministic seeds, Director eligibility, and level constraints. Manual testing covers comedy, readability, comfort, device behavior, and actual recognition.

## 15. Implementation order and completion gates

| Milestone | Deliverable | Required before moving on |
| --- | --- | --- |
| M0 — Design baseline | This design, aligned recognition contract, content inventory and backlog | No conflicting core mappings; unvalidated features identified |
| M1 — Recognition | Device setup/preview, calibration, four mapped actions, telemetry, recovery | Actual target-device results; fallback for failed teeth detection |
| M2 — Vertical slice | One finite level, five obstacles, Bouncy, animation/audio, onboarding, results | A tester completes a facial-control round and understands actions/failure |
| M3 — Comedy foundation | Director, eight event definitions, safe scheduling, three campaign levels | No impossible overlaps; jokes remain readable during testing |
| M4 — Race and progression | Eight-stage campaign with 24 levels, four characters, unlocks, records, accessibility | Sound-dependent content requires validated inputs or alternatives; saves reliable |
| M5 — First-release modes | Try Not to Laugh and Chaos Survival | Both complete; neutral-face tests pass for recognized-input records |
| M6 — Expanded play | Don't Scream, Mouth Power, Sound Madness, Face Battle | Four complete mode loops; optional detectors separately gated |
| M7 — Studio and highlights | Layouts, host controls, event replay, optional recording, voting adapters | Real OBS tests; platform integrations enabled only if validated |
| M8 — Release candidate | Packaged app, content/license audit, performance and long-run fixes | Acceptance suite and clean-machine test pass |

Do not assign a completion percentage or a release date from the feature list alone. Estimate remaining effort after M2 provides measured development, asset, recognition, and testing costs.

## 16. Initial production inventory

| Asset/system | Complete-v1 planning quantity |
| --- | --- |
| Characters | 4, core animation set each, optional action animations as enabled |
| Campaign | 24 levels across eight sequential stages; four visual theme families may be reused |
| Race hazards | 5 required slice types; 5 expanded types |
| Comedy events | 8 initial definitions with theme variants |
| Neutral-face comedy scenes | 5 scene families with at least 3 variations each |
| Audio escape rooms | 3 with reusable rule vocabulary |
| Mouth puzzles | 3 linked rooms with recovery anchors |
| Sound sets | 3 original/licensed 60-second tracks with beat maps |
| Battle templates | Sprint, prompt duel, collection |
| Cosmetics | 2 additional costumes per character initially |
| Interface | 20 functional views using shared panels and components |
| Audio | 4 theme loops plus menu/results music, action/UI/hazard cues and optional announcer bank |

Asset acceptance: consistent scale and palette, transparent exports where needed, documented source and license, readable silhouettes, animation timing markers, reduced-effects alternative, and no unintended raw-reference reuse.

## 17. Design decision register

| Decision | Baseline |
| --- | --- |
| Core smile action | Shield, following the user's Stage 2 selection; magnet is an advanced explicit preset |
| Core attack expression | Show teeth only when validated; otherwise a user-selected distinct input/button |
| Race structure | Finite campaign levels plus optional endless variant |
| Standard health | Three hearts; single-hit challenge is a separate difficulty |
| Character balance | Identical competitive action physics |
| Multiplayer | Local alternating turns first; online excluded from v1 |
| Recognition location | Local by default; no required cloud recognition |
| Audience services | Optional adapters with manual fallback; no promised unsupported interface |
| Highlights | Event timeline by default; recording separately opt-in |
| Paid gifts | No gameplay advantage or progression dependency |
| Advanced expressions | Optional until individually validated |

The implementation follows this baseline. Changes to objectives, mappings, scope, or acceptance rules must update this document and the relevant contract before coding dependent features. Balance changes can be recorded as tuning revisions. An experimental implementation is not evidence that its acceptance gate has passed.
