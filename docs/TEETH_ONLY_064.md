# Teeth-only adjustment - version 64

Closed-lip smile rules, thresholds and recognition adapter are unchanged from version 63.

Teeth evidence previously subtracted 10% of mouth width and normalized the remaining pale band against 45% of mouth width. Even a supported band of visible front teeth could consequently score below the event threshold. Normalize the supported band directly against 35% of mouth width instead. Keep the two supported image rows, minimum 20% band width, pale-colour, exposure, area and temporal checks. Mouth/smile blendshapes do not substitute for tooth pixels.

36/36 plain-Java checks passed, including a new partial front-tooth band positive and existing pink lips/tongue, dark mouth, tiny highlight and one-row highlight negatives. This is a test build; real-player teeth detection still needs validation. No new Play bundle was generated for version 64.
