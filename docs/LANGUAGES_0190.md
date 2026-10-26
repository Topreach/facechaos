# Five-language core interface - 0.19.0

Languages: English, Arabic, French, Spanish and Portuguese. Open the three-dot menu, then Language. Choose a language or Use phone language. Confirming a change recreates the camera screen and ends the current unsaved round; saved coins, stage unlocks, recordings and records remain. Changing language is blocked during recording.

Android 13+ uses LocaleManager and declares locales_config.xml so the app language is synchronized with Android's per-app settings. Android 8-12 use a stored configuration context. Resources are bundled for offline use. Arabic menus mirror their layout, and primary Canvas captions use StaticLayout for Arabic shaping and bidirectional text. Gameplay coordinates, head-steering directions, recognition thresholds and difficulty do not change with language.

## Translated scope

136 core resource strings in each language, including all fifteen game instruction summaries, the main options panel, basic game options, recording/rating/share labels, pre-game control toggles, primary action cues, countdowns, stage summaries and results. Non-English pre-game pages use translated instruction summaries instead of the long English tutorial paragraphs. Game names and world names remain original titles.

This is an initial core-interface localization, not a claim of complete localization of every secondary diagnostic or decorative label. Some specialized camera/microphone failure details, subsidiary panels, animal example words and scene decorations remain English. Country-name speech recognition and typed country answers remain English and this is stated in the translated instructions. Professional native-speaker review is still needed before advertising fully localized store releases.

## Maintenance

Edit localization/ui.json, then run `.venv/Scripts/python.exe android-app/localization/generate.py` from the project root. The generator enforces five nonempty translations per entry, unique English lookup labels and matching numbered format arguments. It writes Android XML resources and AppText.java. GameLanguage.java adapts live game state to localized resources without changing pure game rules.

## Validation

Android build and lint, resource formatting, Arabic RTL layout, translated instructions for all fifteen modes and native offscreen rendering are checked separately from actual gameplay recognition. Tests use configuration contexts and never change the user's selected app/system language or access camera/microphone.

Platform reference: https://developer.android.com/guide/topics/resources/app-languages

Samsung SM-A176B: versionCode 46 / 0.19.0-languages installed. Final build and lint passed. Five compiled resource sets and format arguments, ten menu layouts, five rating panels and seventy-five game-caption scenes passed. Corrected Arabic numeric order and power-label placement visually reviewed. Framework app-language selection persisted after process stop and switched between Arabic/French; the original follow-phone preference was restored. Device/system language was never changed. Temporary test package removed and app opened. Android 8-12 fallback has not been tested on an older physical device.
