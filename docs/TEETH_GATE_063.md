# Teeth gate correction - version 63

Pass rule: show either row of front teeth comfortably for a brief moment, then relax. A smile with visible teeth counts as teeth; closed-lip smiling remains separate. No wide jaw opening or both tooth rows are required.

The image detector previously rejected inner-mouth height below 4% of mouth width before inspecting tooth colour. A nearly closed-jaw tooth display can have a thinner visible band. Removed this aspect-ratio rejection while retaining the minimum two image rows, broad coherent pale band, lip/tongue colour rejection and temporal stability checks. No smile or jaw score substitutes for visible tooth evidence.

36/36 algorithm checks passed, including a new thin tooth-band positive, thin pink-lip negative, toothy-smile event and mouth-only teeth-gate rejection. These synthetic checks do not establish real-player accuracy. Samsung was not detected by ADB; physical-device verification and installation remain pending.

MediaPipe supplies landmarks and blendshape coefficients; tooth image evidence is app-specific: https://developers.google.com/edge/mediapipe/solutions/vision/face_landmarker
