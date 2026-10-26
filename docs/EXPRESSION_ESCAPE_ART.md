# Clear face area ? Android 0.4.4

Expression and Good job coin popups now share a compact horizontal banner near the top, below the hearts and coin balance. The banner respects the top inset and leaves the central selfie area clear in both tall and compact portrait test views. This is a fixed top position, not automatic head tracking.

# Face prompts and coins ? Android 0.4.3

Expression Escape now hides the separate Jump/Shield/Chomp buttons. Its compact illustrated speech card advances through Open your mouth, Smile with lips closed and Show your teeth as the obstacles progress. The active action window highlights the card. This is still a touch preview: tapping the overlay simulates the current expression, as in the other illustrated games. Android face recognition is not yet connected.

Each accepted, correctly timed response awards exactly one coin and displays Good job! +1 coin. Wrong, early, paused, repeated and missed inputs earn nothing. A local wallet persists across rounds and app restarts; these are in-game coins, with no cash value or purchases. The instruction cards describe the popups and touch-preview behavior.

# Expression Escape presentation update — Android 0.4.2

Expression Escape previously used a flat circle character, simple geometric obstacles and an unrelated Emoji Gates tutorial image. It now uses a shared transparent sprite atlas in both gameplay and instruction cards.

| Action | Planned facial cue | Game and tutorial artwork | Current preview input |
| --- | --- | --- | --- |
| Jump | Open mouth | Surprised leaping peach hero and coral spikes | Jump button |
| Shield | Closed-lip smile | Smiling peach inside an aqua shield and shooting stars | Shield button |
| Chomp | Show upper and lower teeth comfortably | Grinning peach and a bitten biscuit | Chomp button |

The brief explains the eight encounters, six-clear target, three hearts and highlighted action window. Each illustrated card explains the corresponding touch button. Gameplay uses the same artwork, a floating confectionery track, a timing cue, jump movement and success effects. Native recording composites this renderer through the existing game overlay.

Android facial recognition remains disconnected. These are touch controls, with the intended future facial mappings explained in the instructions.

The atlas was generated with the built-in imagegen tool. See [the saved prompt and resource path](../mobile-design/concepts/escape-atlas-prompt.md). Alpha transparency is preserved; sprites are read with source rectangles and are not destructively sliced.
