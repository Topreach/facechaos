# Welcome homepage - version 57

Version `0.21.0-setup.3` introduces a native welcome screen as the public launcher. It explains the camera-overlay mini-games before the player enters the camera screen.

The screen includes an original vector illustration, a short introduction, three instructions (choose a challenge, follow its cue, optionally record/share), a language selector, a privacy-policy link and a prominent Let's play button. It explains that recordings are silent and sharing is the player's choice.

WelcomeActivity does not create a camera, face recognizer or microphone, or request their permissions. Let's play opens the existing MainActivity, which retains camera permission handling and the game flow. Back from the camera returns to welcome. Rapid repeated entry taps are guarded. MainActivity is no longer externally exported; only WelcomeActivity has the launcher intent.

The layout scrolls for narrow/short windows and large text, uses a centered maximum content width on tablets, respects system bars/cutouts and mirrors for Arabic. All copy is included in the existing five-language resource pipeline. The generator now preserves explicit line breaks.

The separate verification app uses its own clip-sharing authority, matching its application ID, so its recording links cannot point into the user's Play-installed app. Production sharing keeps the same authority as before.

The change retains R8 optimization, bundled translations, Fragment 1.9.1 and orientation/resizing support. Billing and other monetization remain inactive.

## Checks

- 35/35 game-rule suites pass.
- Optimized Samsung verification: face inference, expression adapter, synthetic recording/pause/resume/decode, 75 game/language renders, 20 welcome layouts and all three welcome callbacks.
- Welcome layouts cover phone, landscape, tablet and larger-text contexts for English, Arabic, French, Spanish and Portuguese.
- Actual Samsung welcome screen visually inspected; camera entry and return to welcome checked without replacing the Play app or its records.
- Final release lint: 0 errors, 34 warnings. Android instrumentation sources compile.
- Bundletool, final launcher/manifest checks, R8 metadata, bundled languages, model/private-key checks and all four native 16 KB layouts pass. All 133 bundle payload entries verify against the existing upload certificate.

The test copy is labelled **Face Chaos verification** and has package `com.facechaos.mobile.verify`. It is separate from the Play-installed app.

Signed Play bundle: `dist/FaceChaos-0.21.0-57-welcome-play.aab`.

SHA-256: `45309989d4b2415dd81bb9a82cf0a97c0a42325168d6a86b53d44f91edb31e0a`.

## Build 58 instruction refinement

Version `0.21.0-setup.4` clarifies that players tap **Games** on the camera screen to select from **15 games**, then read their chosen game's instructions. It explains turning on **Record my round (silent)** before playing and sharing the finished clip on social media. The recording option's actual localized label is inserted into the explanation, keeping the instructions aligned with the switch in all five languages.

This updates guidance only; recording remains optional, silent and explicitly shared by the player. There is no automatic social posting. Build 57 remains unchanged; build 58 is a separate upload.

Build 58 checks: five-language formatting validation, actual Samsung accessibility text confirms the 15-game count and the complete recording/social-sharing instructions with the switch label substituted; release lint 0 errors, 34 warnings; bundletool validation, final payload/native-layout checks and all 133 payload signatures pass. The separate phone verification copy is updated.

Signed bundle: `dist/FaceChaos-0.21.0-58-welcome-play.aab`. SHA-256: `855ace0e701bb26977a1b4eb43cf4123ec2c73697be506b3f245f0cb190517ad`.
