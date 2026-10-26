# Established detector review — 23 October 2026

The current custom tongue colour/geometry heuristic and personal cheek classifier
have failed user camera tests. Passing synthetic tests does not validate them.
Do not keep treating threshold changes as a demonstrated recognition fix.

## Primary sources checked

- [Google MediaPipe output definitions](https://ai.google.dev/edge/api/mediapipe/python/mp/tasks/vision/drawing_styles/face_landmarker/Blendshapes):
  52 named outputs, including cheekPuff, eyeWide and jawOpen; no tongueOut output.
  A named coefficient is not evidence that it responds reliably on this camera.
- [Google blendshape model card](https://storage.googleapis.com/mediapipe-assets/Model%20Card%20Blendshape%20V2.pdf):
  pretrained landmark-based model, Apache 2.0, documented environmental and landmark
  limitations. This is already the underlying library for many existing lab rows.
- [Project Babble](https://babble.diy/): explicitly lists tongueOut and separate
  cheekPuffLeft/Right among lower-face tracking outputs. Designed for VR mouth
  tracking. These are project capabilities, not an independent accuracy benchmark.
- [Babble camera source](https://raw.githubusercontent.com/Project-Babble/ProjectBabble/main/BabbleApp/camera.py):
  supports OpenCV capture as well as specialist sources. Accepting webcam input
  does not establish model accuracy at a normal laptop-camera distance/viewpoint.
- [Babble repository](https://github.com/Project-Babble/ProjectBabble) and
  [licence](https://raw.githubusercontent.com/Project-Babble/ProjectBabble/main/LICENSE.md):
  repository requests contacting the project for commercial licensing. Published
  terms include non-commercial restrictions. Do not vendor its code/model into
  a commercial Face Chaos distribution under an assumed permissive licence.

## Decision

Keep existing working pretrained-model controls. Treat Babble as a candidate for
a separate lower-face evaluation, not as a verified replacement already installed.
Before adoption, verify exact model/version and model licence, expected camera
crop, Windows runtime and output interface; compare neutral, lip motion, open
mouth, exposed teeth, tongue protrusion and cheek puff on this user's camera.
Do not require purchasing hardware based on this review.

No replacement model was installed or copied during this research. Existing
failed tongue/cheek controls remain unaccepted. The public sources reviewed do
not establish a permissively licensed drop-in model verified for every requested
gesture on an ordinary Windows webcam.
