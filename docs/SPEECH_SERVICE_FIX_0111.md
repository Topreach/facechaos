# Speech service correction ? 0.11.1

The connected Samsung SM-A176B reports an installed on-device model of `en-GB`, while `en-US` is only downloadable. Version 0.11.0 checked service availability then always requested `en-US`. This did not verify the requested language was installed and could immediately fail even with microphone permission granted.

Version 0.11.1 queries installed models through Android RecognitionSupport (Android 13+). It selects an installed English locale: US English if actually installed, otherwise another installed English variant, including the UK English model on this Samsung. If no installed English model is reported, the query is unavailable/times out, or local recognition fails with a service/language error, it tries the default speech provider once. Default fallback no longer forces offline mode; the provider may use a network connection, as explained in the existing pre-game brief. Permission, audio, busy, silence/no-match and request-limit errors do not trigger a second microphone request.

Support queries are bounded to two seconds each and the complete request to thirteen seconds. The speaking timer continues while listening; provider setup and answer processing pause the game clock. Cancelled engines, stale support callbacks, duplicate results and partial transcripts cannot answer another flag. Diagnostic logs contain only model locale, service route and numeric error code, not audio or spoken text. No model download or phone-wide settings change was performed.

Tests drive the production adapter with fake recognition engines to check UK/US selection, missing-model fallback, unsupported default-provider queries, runtime language errors, connection failures, no fallback loops, cancelled queries, partial transcripts and duplicate/stale finals. A separate real-phone test queries model support without recording: on-device installed `[en-GB]`, pending `[]`, US English available for download; default provider does not implement support-query API (error 14), so recognition is attempted through that provider rather than rejected for its missing query API.

References: [Android SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer), [RecognitionSupport installed language API](https://developer.android.com/reference/android/speech/RecognitionSupport).

User test: open Games ? Flag World Tour, enable spoken names, Start, wait for Listening and say the country. A real voice trial is still needed; model metadata and synthetic adapter checks do not prove microphone transcription accuracy.

Build and Android lint passed on 25 October 2026. Production-adapter fake-engine checks passed on the Samsung; real non-recording support queries reconfirmed en-GB installed. Installed versionCode 26 / 0.11.1-speech. The update preserves existing app data. APK: `dist/FaceChaos-Android-0.11.1.apk`.
