# Game switching — 0.19.3 (50)

The existing Games/circular play-pause-record/Stop layout is unchanged.

When an unfinished round is active, Games and “Choose another game” ask “Do you want to close this game?” with Yes and No choices. The question pauses the round and any recording while the player decides.

- No or dismissal keeps the same session and restores whether it was playing or already paused.
- Yes stops the old session, saves its run record and finishes any recording before opening the game list.
- Selecting a game from the list starts a new Stage 1 run with a fresh countdown, score, run coins, hearts and input state. Previously earned account coins and per-game best records remain saved.
- Finished rounds and initial game selection do not require closing an active game.

Switching closes the previous face, head, microphone, speech and animal-guide input sessions and suspends old sound playback. The old game is stopped, rather than left running behind the next game. Late input callbacks are invalidated by their existing generation checks; permission results now also have session ownership. Rapid requests during clip finalization start only the latest selection. Recorder error recovery from an old session cannot resume or change the new session.

Explicit stage progression still starts the requested next stage and carries the successful run's coins, as before. Fresh selection from the game list starts at Stage 1.

## Verification

Samsung SM-A176B, `SceneCheck -e isolationOnly 1`:

- All 225 source/destination game pairs use a new game object and reset progress and pending input state; the previous object is stopped and cannot advance. Synthetic test progress is excluded from persistence.
- Stale microphone/speech permission results cannot change the new session.
- No resumes the same game, No on an already-paused round leaves it paused, and cancellation acts as No.
- Clicking Games during recording asks first; Yes closes the old game and finalizes the clip before showing the picker. A different game then starts fresh.
- Rapid selections during recording finalization leave only the latest selected game running, with its own recorder.

Debug/test APKs and the signed release bundle build successfully. Android lint: 0 errors, 33 existing warnings. This verifies lifecycle separation, not recognition accuracy for every expression or sound. Test clips stay local and player preference values/records are preserved.
