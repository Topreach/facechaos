# Integrated camera and gameplay screen

The player-facing direction follows the supplied reference's composition: the
live player remains visible, a cartoon reacts to facial movement, and a game
with prompts/progress runs in the same screen. The game mechanics can vary.
The current implementation applies this to Expression Escape, not Emoji Gates.

## Try it

1. Close old game/lab versions. Open this project and press F5.
2. Choose **Connect Facial Controls**. Calibrate or load the desired reactions.
3. Keep **Send controls to game** and **Show live camera in game (local only)**
   enabled in the setup window. The game launcher enables preview by default;
   the standalone lab launcher does not.
4. Use **Minimize setup - play in Godot**, return to Godot, then choose
   **Play with Face + Keyboard**. Stay neutral through the countdown.
5. During play, your mirrored camera image is the background. The avatar mirrors
   blinks and mouth opening, and the same screen shows hazards, prompts, meters,
   hearts, finish progress, accepted actions and results.
6. **Hide camera** clears the in-game picture and stops its preview requests;
   facial control remains active. **Show camera** restores it. To stop sending
   preview frames entirely, uncheck the setup window's camera-sharing option.

Keep the minimized recognition process running: closing it releases the camera
and disables face input. Calibration/device setup is still a separate development
window before play; its eventual full in-game replacement is unfinished. There
is no need to switch to it during a normal calibrated run.

## Local processing and failure behavior

- Recognition and preview share one physical camera capture; Godot does not open
  a competing camera device.
- UDP on loopback carries small control events, validated blink/jaw/smile render
  parameters, and optional preview port/token metadata. No JPEG is placed in UDP.
- An opt-in loopback HTTP server serves only its latest token-addressed JPEG from
  memory, bounded to 640x480 and 350 KB, without browser caching or disk writes.
- Godot requests bounded frames at roughly eight per second on a separate path
  from physics and action delivery. Preview responsiveness is not measured
  facial action latency. No Internet destination is used for preview.
- Source stop, disabled sharing, bad frames, request failures and stale frames
  clear the image. They are not displayed as a frozen live camera. Recognition
  tracking loss retains the existing pause/recovery rules.
- The background is centre-cropped to fit the landscape viewport and shaded for
  readable game overlays. This does not yet provide a portrait broadcast layout.

## Evidence and remaining limits

84 Python tests pass. A synthetic Python camera source passed real loopback JPEG
delivery into a Godot texture, avatar telemetry, hide/show and source shutdown
checks. Invalid preview targets and avatar values are rejected. Facial event
transport, normal gameplay and all nine course simulations pass. Hidden setup
UI tests pass without camera/microphone access.

The composed screen was visually inspected using an explicitly marked synthetic
camera image. This is not a live user-camera playtest, a frame-rate benchmark or
proof of recognition accuracy. Real webcam framing, comfort, latency, final
character animation and portrait presentation remain to be tested/refined.
