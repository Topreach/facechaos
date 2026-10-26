# Privacy-policy publishing and Play bundle 48

Live policy: https://topreach.github.io/facechaos/privacy.html
Support page: https://topreach.github.io/facechaos/
Public support email: saheedseunpeter@gmail.com

The policy was published to the dedicated gh-pages branch of Topreach/facechaos, commit a8c12ca. GitHub Pages serves the branch root. Only index.html, privacy.html and .nojekyll were added to that branch; application source, upload signing keys and passwords were not published. The pre-existing public README is inherited from main. Source copies are in web/privacy/. No changes were pushed to main.

The public HTTPS URL was fetched without authentication and returned 200 text/html. The site uses no third-party scripts/fonts. Actual 412px mobile and 1280px desktop layouts passed horizontal-overflow checks, and the mobile screenshot was visually reviewed. The policy describes local face/head/sound processing, optional online Android speech recognition, local clips/ratings/progress, exports, user-initiated sharing, support correspondence, retention/deletion, children and GitHub hosting logs. It uses the app identity Face Chaos rather than inventing a legal business name. This is an implementation-based policy, not a guarantee of store approval or legal compliance in every jurisdiction.

## Updated application bundle

Upload **FaceChaos-0.19.1-48-play.aab** from dist instead of the earlier build 47 when preparing this release. Package com.facechaos.mobile, version 0.19.1, versionCode 48. The three-dot menu now contains Privacy policy in all five core interface languages; it opens the public page in the browser and pauses the game. The developer upload certificate is unchanged. The updated release bundle was built and linted, validated with bundletool, checked for the expected package/version/non-debuggable/SDK configuration, inspected for native alignment, and signature-verified against the existing public upload certificate. It has not been uploaded to Google Play or installed over the debug phone app.

Enter the HTTPS policy URL in Play Console's Privacy policy field. A GitHub source-code URL or the AAB file itself is not the policy URL. Keep the site accessible and update the policy when behavior changes. Complete the separate Data safety and app-content forms accurately; publishing the page does not fill those forms automatically.

SHA-256 of build 48: cf02c71d5ecc689a804761cd75771f37dfeb8f5c6555509854f7b85612d48267
