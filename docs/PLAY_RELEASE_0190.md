# Face Chaos - first Google Play upload

Upload **FaceChaos-0.19.0-47-play.aab** from this folder. This is the signed Android App Bundle, not an APK or ZIP archive.

- Package: `com.facechaos.mobile`
- Version: `0.19.0`
- Version code: `47`
- Android: minimum 8.0 / API 26; target API 36
- File size: 34,187,059 bytes (about 32.6 MiB)

In Play Console, open Face Chaos, then **Test and release > Testing > Internal testing > Create new release**. Upload the AAB, use Play App Signing for the first release, and paste the included release notes. Save and review the release. The bundle is prepared locally; it has not been uploaded or published by this task.

Complete any setup tasks Play Console displays. Before production, finish your privacy policy, Data safety, content rating, store listing and testing requirements. The app uses camera/microphone; country-name recognition can use the phone speech provider online when on-device English recognition is unavailable. Do not claim that this speech path is always offline. Videos are currently silent. Ratings inside this build are local prototype feedback; the Google Play in-app review API is not integrated. Some secondary diagnostic and decorative text remains English.

## Keep the upload key

The private identity and passwords are in `android-app/release-signing/` inside your project. The folder is restricted to your Windows account and ignored by Git. Back up the entire folder privately, preferably in encrypted storage. Reuse this identity for future bundles; do not regenerate it. Future uploads must use a higher version code. **Do not upload this private folder, keystore or password file to Play as the app bundle or to GitHub.**

`FaceChaos-upload-certificate.pem` is the public upload certificate. It contains no private key. Play can obtain the certificate from your signed bundle; the PEM is available if Play explicitly requests it. Let Google generate/manage the distribution app-signing key when enrolling in Play App Signing.

## Existing phone build

The development app on your phone uses the Android debug signing identity. A Play-distributed build uses Google's app-signing identity and may require uninstalling the debug app first. Export recordings with Save to Gallery before uninstalling; app-local clips, settings, coins and records are removed with the app. No phone app or data was removed while preparing this release.

## Verification

Release build and release lint passed. bundletool validated the AAB and dumped its final manifest/config. The manifest is non-debuggable with the expected package/version/SDK values. All 55 payload entries were signature-verified against the exported certificate. APK signing and zipalign checks passed. The bundle requests 16 KB native ZIP alignment, and all four native architectures passed LOAD alignment checks. RELRO occupies dedicated LOAD segments with subsequent writable data outside the rounded protection range, the documented valid exception to end-address modulo alignment. The face-landmark model is present; private signing files are absent. No runtime test on a 16 KB device was performed. Existing feature checks used the prior debug build of the same application code; the release APK was not installed over your debug app.

The JDK JAR verifier reports the expected self-signed upload certificate and streaming-entry-order warnings; JarFile cryptographic verification of every payload and bundletool validation both passed. Google Play's upload validation and pre-launch report remain authoritative for acceptance and device behavior.

SHA-256: `ddbfb02c57d0315f5b35e0f1f8fa969a88e883a29016f0c3e6568561c4789516`

References:
- https://support.google.com/googleplay/android-developer/answer/9859348
- https://support.google.com/googleplay/android-developer/answer/9842756
- https://developer.android.com/guide/practices/page-sizes
