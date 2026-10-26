# Flag World Tour ? Android 0.11.0

A fifteenth selfie-overlay game. See a flag without its country name, say the country in English, earn one coin for a correct answer, then see the next flag. A wrong country costs a life and keeps the same flag for another attempt. An expired timer also costs a life. Empty answers and recognition-service errors do not count as wrong countries.

## Campaign

| Stage | Available countries | Correct answers needed | Answer time | Lives |
| --- | --- | --- | --- | --- |
| 1 | 8 | 5 | 18.0 s | 3 |
| 2 | 12 | 6 | 15.0 s | 3 |
| 3 | 16 | 7 | 12.4 s | 3 |
| 4 | 20 | 8 | 10.3 s | 3 |
| 5 | 24 | 9 | 8.6 s | 1 |

Decks shuffle without repetitions within a stage. Countries are Japan, France, Germany, Italy, Nigeria, Ukraine, Poland, Ireland, Belgium, Netherlands, Austria, Sweden, Finland, Denmark, Switzerland, Bangladesh, Romania, Hungary, Bulgaria, Estonia, Indonesia, Norway, Iceland and Palau. Progress and coins use the existing saved campaign system.

Original Canvas vector drawings preserve country flag proportions, stripe orientation, circle positions and Nordic cross geometry. Flag references include [Nordic Cooperation official flag measurements](https://www.norden.org/en/information/nordic-flags) and the [CIA flag descriptions archive](https://start.csail.mit.edu/mirror/cia.gov/library/publications/the-world-factbook/fields/2081.html). The country name is displayed only after a correct answer, except in the explicitly labelled tutorial example.

## Spoken input

Uses Android SpeechRecognizer final transcription, not the sound-volume gate used for racing and jumping. English for this version; version 0.11.1 selects an installed English locale, including en-GB. One top final hypothesis is checked against exact normalized country names and explicit aliases. Case, punctuation and common short wrappers such as ?This is Japan? are accepted. Partial transcripts, arbitrary substrings and lists of country names do not earn coins. No expected answer is injected into the recognizer.

On-device recognition is used when Android reports it available. Otherwise the installed phone speech service is used with an offline preference. That preference is not a guarantee: the external speech provider may process audio online. The brief explains this before play. Face Chaos saves no speech audio or transcript history, adds no network permission, and keeps optional recorded video silent. [Android speech API reference](https://developer.android.com/reference/android/speech/SpeechRecognizer).

The answer clock runs while the player is speaking. Initial service setup and final transcription processing pause it; each entire recognition request is bounded to thirteen seconds including model-support queries. Pause, background, stop, a changed flag or expired attempt cancels recognition and rejects stale callbacks. App music and effects are suppressed during voice mode. A service error switches to a visible retry/type message without consuming a life. Tap the lower flag card to retry speech or type. The typed-answer dialog pauses the game for accessible play; typed mode does not claim voice recognition.

## Phone test

1. Games ? Flag World Tour ? read instructions ? enable ?Say country names? ? Start. Allow microphone access.
2. Wait for ?Listening?, then say the country shown. Confirm Good job, exactly one coin and a different flag.
3. Say a wrong country. Confirm a life is lost, no coin is awarded and the flag remains.
4. Tap the flag card to select typed answers; enter the same country's name.
5. Pause during listening, resume, then stop. No late transcript should affect another attempt or game.
6. Finish a stage and verify the next stage introduces a wider country pool and a shorter answer window.

Automated checks use synthetic answers and rendered scenes, never user microphone/camera recordings. A real spoken-name trial on the owner's phone is still required to assess accent, room noise, provider language support and latency. Football clubs and car brands are separate future quiz modes; this release adds countries only.

## Verification ? 25 October 2026

Android build and lint passed (existing lint warnings remain). All 75 game/stage winning paths and the previous face/head/racing regression checks passed. Flag-specific tests cover 100 seeded campaign paths, all 24 names, explicit aliases, wrong and empty answers, pause/countdown input blocking, duplicate rewards, speaking versus processing clocks and timed failures. Native Samsung scenes include the 24-flag catalog, six quiz states, compact layout and matching tutorial. The installed package reports versionCode 25 / 0.11.0-flags. Samsung reports both speech-provider and on-device recognizer availability; this does not establish English-model readiness or real spoken-answer accuracy. A live voice trial remains the next user test. APK: `dist/FaceChaos-Android-0.11.0.apk`.
