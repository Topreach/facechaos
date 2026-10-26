# Face Chaos commercial design

Status: owner confirmed one-time Pro at a US$2.99 base price and a children/adults audience. The owner has a Google Play developer account but no AdMob account. Store product, purchase verification and ad configuration remain pending. This design and its policy tests do not activate billing, ads, locks or watermarks in released version 0.20.0.

## Free and Pro

| Feature | Free | One-time Pro |
|---|---|---|
| Games | Five complete games with all stages | Full Pro collection |
| Camera/game recording | Available, with Face Chaos branding on new clips | Available without the branding watermark |
| Sharing and Gallery save | Included | Included |
| Ads | Only family-safe ads at eligible breaks | No ads |
| Restore purchase | Included | Included |

Pro product: `facechaos_pro_lifetime`, a non-consumable one-time purchase. No subscription or sale of earned gameplay coins is planned. The word “token” means a small purchase price here, not a new virtual currency. Real store-localised prices must be loaded from product details; the app must not hardcode a fake checkout price.

Recommended free set: Emoji Gates, Melody Hop, Smile Stack, Sound Grand Prix and Head Dodge. This gives players expression, sound, timing, racing and head-control experiences. These choices are a proposed product selection, not an activated paywall. The other ten existing games would belong to Pro.

No charge for camera calibration, accessibility/input settings, recording permission recovery, viewing existing clips or restoring a verified purchase. Existing clips are not overwritten, and saved gameplay coins/records remain intact. Explain access changes before applying a paywall to an existing installation; no previous purchase may be silently revoked by migration.

## Watermark

Add the original Face Chaos smile mark and “FACE CHAOS” to the encoded video only, in a small legible pill near a safe bottom corner. Avoid the player's face, instructions, score and game hazards. Do not clutter the live camera UI. Free recordings retain the mark after sharing. Pro removes branding from new recordings; it must not promise to erase a watermark already burned into an older clip. Existing clips remain unchanged.

## Family-safe purchase and ad experience

Use a parent area for purchase, restore, privacy management and external links. No countdown sales, pressure on children, paid retries, paid competitive advantages, or ads disguising themselves as game controls. Do not call a collection with locked games “all free”. A parent's store purchase approval still follows the store's own account controls.

For the initial children/adults release, the recommended ad mode treats the whole app as child-directed with age-appropriate contextual advertising. This is simpler and more private than collecting birth dates, at the cost of lower ad revenue. The actual SDK/ad-source setup must satisfy Google Families requirements. Ads cannot be enabled merely by selecting a teen/adult label that contradicts the product's audience.

No banners over the camera, gameplay or record controls. No interstitial on launch, game selection, permission recovery or recording. An eligible interstitial is considered only after at least three normally completed rounds and at least two minutes, with no active recording, audio capture, purchase, confirmation or calibration dialog. Ad load failure never blocks play. Frequency counters advance on an actual shown-ad callback, not on a failed load. The first version should omit rewarded ads and paid currencies, avoiding complexity for children.

Before requesting any ad, resolve the applicable privacy/consent flow and verify the family-safe provider configuration. Privacy choices remain accessible later. Pro stops ad requests/loading as well as hiding ad views. Do not send face frames, facial measurements, recordings or microphone samples to advertising or purchase-verification services.

## Store purchase architecture

1. Query native store product details and show the actual localised price.
2. Open the native purchase flow from the parent area.
3. Keep pending or cancelled payments free; no Pro unlock merely because checkout returned.
4. Verify the completed purchase with a secure entitlement service, then grant Pro and acknowledge it. Do not use a plain local `pro=true` flag as payment verification.
5. Restore/query owned purchases at app launch, resume and explicit Restore. Handle reinstall and a second device using the same eligible store account.
6. Process refund/revocation notifications and refresh entitlements. Preserve a bounded, verified offline entitlement cache; do not require an ad network connection for a paid player to play.
7. Test store-unavailable, pending, cancellation, network loss, duplicate callbacks, refund, restored purchase and upgrade during recording. A purchase must not change watermark settings midway through an active clip; snapshot the entitlement when recording starts.

Android uses Google Play Billing. A future iOS build uses Apple's in-app purchase system. The current repository has an Android app, not a completed iOS app. A Google Play purchase is not automatically an App Store purchase, and no cross-store entitlement or family-sharing claim should be made without implementing it.

Live activation needs a Play Console one-time product, base price, a secure purchase verification/restore integration, AdMob app/ad-unit configuration, consent configuration, privacy-policy and Data safety updates, and internal-track licensed purchase tests. These IDs/services have not been configured by this task. Do not publish placeholder purchase buttons or use test ad units as a production monetization implementation.

## New Pro game designs

These five are proposals; they are not yet in the app's 15-game catalog.

| Game | Player action | Distinct gameplay and progression |
|---|---|---|
| Balloon Boulevard | Gentle hum inflates; quiet deflates | Guide a balloon through differently sized archways. Over-inflation touches thorns; five neighbourhoods add moving wind corridors and narrower gaps. No shouting or blowing onto the microphone. |
| Head Harbour | Move head left/centre/right | Steer a little tugboat through buoys, rescue floating stars, avoid cargo ships. Later harbours add currents and moving ferry crossings. |
| Blink Lanterns | One deliberate blink on the cue | Light lanterns carried by slow fireflies. Later patterns change the target lane and timing; generous rest/neutral windows avoid repeated rapid blinking. |
| Sound Sculptor | Match a displayed comfortable sound level | Raise a soft jelly bridge to the silhouette height, then go quiet to let the character cross. Later worlds combine low ceilings, stepped gaps and moving supports. |
| Peek-a-Comet | Wide eyes to reveal a safe route; relax to travel | Briefly reveal hidden constellation paths, remember a safe branch and collect stars. Add this only after the new wide-eye detector passes real-player tests. |

Every game needs its own tutorial, action-to-motion feedback, failure/retry behaviour, five authored stages, sound design, per-game score and controller/device tests. A new title or a reskinned existing game is not a finished premium game. Start with Balloon Boulevard, then Head Harbour; validate each before adding the next.

## Implementation completed in this preparation

`CommercialPolicy.java` defines the five-free/verified-Pro matrix and ad break eligibility. `CommercialPolicyCheck` covers pending/cancelled/revoked purchases, invalid game IDs, recording/microphone/dialog suppression, consent/configuration requirements and frequency limits. This is an isolated foundation: it is deliberately not wired to the released app while billing and ad configuration are absent. No payments, ads or paid unlocks are live, and the Google Play/GitHub v0.20.0 release has not been replaced.

## Official references reviewed

- [Google Play digital feature payments](https://support.google.com/googleplay/android-developer/answer/9858738)
- [Play Billing purchase processing and acknowledgement](https://developer.android.com/google/play/billing/integrate)
- [Google Play target audience and Families](https://support.google.com/googleplay/android-developer/answer/9867159)
- [AdMob consent setup](https://developers.google.com/admob/android/privacy)
- [AdMob interstitial placement](https://support.google.com/admob/answer/6201362)
- [Apple App Review guidelines](https://developer.apple.com/app-store/review/guidelines/)
