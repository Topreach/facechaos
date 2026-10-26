# Account setup for Face Chaos Pro

Confirmed: one-time Pro, US$2.99 base price, children and adults. Do not replace the existing v0.20.0 production release with an unfinished paywall build.

## Google Play purchase product

In Face Chaos's Play Console, configure the payments profile if the console requests it. Product setup is under Monetize with Play → Products → One-time products (older consoles may call this In-app products).

Use these prepared product details:

| Field | Value |
|---|---|
| Product ID | `facechaos_pro_lifetime` |
| Title | Face Chaos Pro |
| Description | Unlock all Pro games, record without the Face Chaos watermark, and enjoy an ad-free experience. One-time purchase. |
| Type | One-time, non-consumable entitlement |
| Purchase option | Buy; not Rent or subscription |
| Suggested purchase-option ID | `lifetime` |
| Base price | USD 2.99; review store-converted local prices and regional availability |
| Multi-quantity | Off |

Product IDs cannot be changed/reused after creation. Leave the product unavailable for general sale until native billing, verification, restore and licensed internal-track tests are complete. No code in the current app grants Pro or charges a purchase yet.

Google requires the BILLING manifest permission for in-app products. Version 53 does not declare it; if the console asks for a billing-enabled upload, an internal setup bundle is needed before the product can be configured. This is a prerequisite, not evidence that purchases work.

## AdMob

1. Use a Google account to register at https://admob.google.com/ and complete the account's own verification/payment setup. Google Play registration does not create this configuration automatically.
2. Add Face Chaos as an Android app, package `com.facechaos.mobile`. Link the correct store listing when available; do not claim an unpublished app is live.
3. Create one interstitial unit for eligible post-round breaks. Banners and rewarded units are not part of the initial design.
4. Provide the **AdMob App ID** (`ca-app-pub-...~...`) and **interstitial ad-unit ID** (`ca-app-pub-.../...`). These are configuration identifiers; never send passwords, payment/bank details or private service-account keys in chat.
5. Configure applicable privacy messages, family-safe treatment/content rating and ad-source selection before enabling real ad requests. Real ads are not used for development clicking/testing; the integration must use Google's test ads/test devices.

The implementation plan treats all initial ad requests as child-directed/contextual, with maximum G content rating. It still requires review of the actual store audience declaration, provider settings and privacy disclosures. Raw face frames, facial measurements and microphone samples must remain outside the ad SDK.

App verification or ad-serving approval may be required by AdMob. Developer website/app-ads.txt setup should be completed using the publisher information AdMob supplies. Do not invent a publisher ID or app-ads.txt entry.

## Still required after account setup

- Native Play Billing integration and a secure purchase-verification/restore service.
- Consent/family-safe ad integration with the real app/ad-unit IDs.
- Free/Pro game selection UI and parent area.
- Watermark rendering in newly encoded free clips, with entitlement fixed at recording start.
- Privacy policy and store Data safety/ads/purchases declarations reflecting the actual SDKs.
- Test purchases, pending/cancelled/refunded/restored states and device tests before a monetized production release.

Official references: [one-time products](https://support.google.com/googleplay/android-developer/answer/16430488), [billing permission/product prerequisites](https://support.google.com/googleplay/android-developer/answer/1153481), [AdMob getting started](https://support.google.com/admob/answer/15948559), [app/ad-unit identifiers](https://support.google.com/admob/answer/7356431), [Families setup](https://support.google.com/admob/answer/6223431).
