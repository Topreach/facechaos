# Teeth colour correction - version 65

Only the pale-pixel colour rule changes. Closed-lip smile and all action timing/threshold rules are unchanged.

A new warm-ivory fixture (RGB 245,219,203) reproduced a rejection in version 64: its red-minus-green component exceeded the colour filter even though the low-saturation band was otherwise valid. Permit red/green difference up to the yellow component plus 18, retaining brightness, saturation, band-width, multi-row, exposure and temporal checks. Existing pink lips/tongue, dark mouth and tiny-highlight negatives still pass. This is not proof of the cause for the user's attached photograph; that photograph was visually inspected but was not available as a local inference fixture.

36/36 rule checks passed after the correction (35/36 before it with the new regression case). Real-player verification remains pending. This version produces an isolated phone test APK, not a Play release bundle.
