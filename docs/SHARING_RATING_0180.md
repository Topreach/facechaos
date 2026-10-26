# Recordings, social sharing and local ratings - 0.18.0

## Finding videos

Open the top-right options menu, then My recordings. Clips are listed newest first, with date and size. Tap a clip to replay, Save to Gallery, Share or Delete. The original file is in Android/data/com.facechaos.mobile/files/Movies (app-specific external storage). Save to Gallery copies it through MediaStore to Movies/Face Chaos. App-specific originals are removed when the app is uninstalled; Gallery exports remain. Clips currently contain camera and game video without audio.

## Sharing

The Share menu offers TikTok, Instagram, YouTube, WhatsApp and Other apps. Each destination receives only the selected clip via a read-only content URI. If a destination is unavailable, Android's chooser offers compatible installed apps. Users review and post in the recipient app; Face Chaos does not upload automatically or claim that a post succeeded. Recipient app versions and account permissions can affect acceptance.

Sharing sends a video, not a playable game. A published download link is still needed so viewers can install Face Chaos. Native TikTok gameplay/effects and direct Content Posting API integration are separate projects requiring platform setup/review.

## Rating prompt

This sideloaded prototype collects optional local 1-5-star feedback. All star values are accepted equally. The prompt is offered between rounds after at least three completed stages/rounds, whether won or lost. It has a seven-day cooldown, Not now and Don't ask again; saving any rating stops automatic prompts. Manual rating is in the options menu. Gameplay pauses while the dialog is open; no prompt interrupts recording. There are no rating rewards.

Ratings are saved on the device only, not collected by a server or submitted as public reviews. Official Google Play in-app reviews require a Play Store release/test track and the Play Review integration. No listing is configured for this prototype, so the app does not open a nonexistent store page or imitate a public review submission.

## Sources checked

- https://developers.tiktok.com/docs/en/share-kit-android-quickstart-v2 (Android SEND intents are supported without Share Kit).
- https://developer.android.com/social-and-messaging/guides/media-sharing
- https://developer.android.com/guide/playcore/in-app-review
- https://developer.android.com/guide/playcore/in-app-review/test

## Validation

Rating eligibility, cooldown, clock rollback, opt-out and unbiased star validation have pure Java checks. Native checks use an isolated synthetic file and test preferences: selected-clip intent grants for six targets, read-only provider access, invalid filenames, local persistence and rating panels at two widths. They never open a social app, post content or inspect a user's clip. Real social-app handoff and actual posting remain user tests.

Android build and lint passed. Samsung SM-A176B: versionCode 44 / 0.18.0-share installed. Native selected-file sharing/provider checks and four rating panels passed, including a compact-width star-fit assertion. Final compact panel visually reviewed with all five stars and full explanation visible. Synthetic clip and test preferences were removed, and the test package was uninstalled. Recipient social-app acceptance and actual posting remain manual user tests.
