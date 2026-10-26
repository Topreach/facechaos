# Internal billing setup — version 54

**Internal testing only. Not the finished monetized production app.**

File: `FaceChaos-0.21.0-54-internal-billing-setup.aab`. Package: `com.facechaos.mobile`. Version name: `0.21.0-setup`; version code: 54.

Purpose: Play Console requires billing support before selling one-time products. This build packages official Play Billing 9.1.0 and declares `com.android.vending.BILLING`. It does not connect a billing client, initiate purchases, display real ads, lock games or add watermarks. All existing gameplay remains available. Use the stable 0.20.0/53 release for the current production experience.

The Kotlin runtime modules are aligned with BOM 1.8.22 to resolve the older vision runtime's jdk7/jdk8 duplicate-class conflict when Billing is added.

Validation: release/debug/instrumentation builds pass; release lint reports zero errors. bundletool validation, manifest identity, SDK 36, ZIP integrity, 16 KB native LOAD/RELRO layouts and cryptographic verification of all 179 payload entries against the existing upload certificate pass. Manifest metadata confirms Billing 9.1.0. All 34 plain-Java policy/controller/game checks pass. A temporary read-only emulator did not complete ADB boot, so no runtime regression was completed for this dependency change; the user's phone was not modified. Run the model/game checks on the internal build before promotion.

Upload this AAB to **Internal testing**, then configure the draft one-time product from [account setup](MONETIZATION_ACCOUNT_SETUP.md). Do not activate purchases for customers until checkout, verification, restore, refund and parental-area flows are implemented and tested.

SHA-256:

```text
91fb695067e74aa4ecb2d9fca055c0dae9d63a641b0f18a863c2b3e2e320e0b0
```
