# Star Duet — Android 0.5.1

Star Duet replaces the old circle-and-star board with a shared illustrated smile character, singing star, musical note, cloud stages and a six-point constellation. The compact instruction/reward banner remains near the top. The same character, star and note appear in the three instruction cards.

Complete three pairs: tap the character to simulate a closed-lip smile, then hum gently to light a star. Facial recognition remains pending. With microphone mode off, a second tap simulates the hum too. Each pair gives one saved in-game coin; three missed cues ends the round.

Optional microphone mode uses the existing local sound-volume gate, not speech or pitch recognition. Stay quiet for the first second. On a sound cue, release any previous hum, then make a fresh gentle sound. Sound is ignored during smile cues. Held sound cannot carry over into the next cue or award duplicate coins. The microphone stops on menus, pause, game switching, completion and app backgrounding. Audio samples are discarded; video clips remain silent.

Open Games > Star Duet, read the illustrated instructions, select microphone or touch practice and press Let's play. Allow microphone access if requested. If the game is paused after the permission prompt, tap the circle to resume.

Verification: six game win/loss paths, Star Duet action order, quiet-before-hum gating, rejection of held sound, one coin per pair, pause and three-pair completion pass pure Java checks. Camera-free native scene tests cover smile, sound, reward, win, loss and compact portrait layout. Real microphone sensitivity still requires a user test.

Artwork reuses existing project assets in android-app/app/src/main/res/drawable-nodpi/emoji_atlas.png and voice_atlas.png. No new generated image was needed. See the existing [Voice Runner asset prompt](../mobile-design/concepts/voice-atlas-prompt.md) and [Emoji atlas prompt](../mobile-design/concepts/emoji-atlas-prompt.md).
