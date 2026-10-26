# Original Face Chaos audio

All twelve WAV files in app/src/main/res/raw are original synthesized PCM assets made for this project. No external samples or melodies were downloaded. Regenerate with Python: `python audio/generate_audio.py` from android-app.

The set contains eleven short cues (start, countdown, go, correct, coin, miss, stage, complete, fail, pause and click) plus a gentle original loop. Files are mono PCM16 at 22,050 Hz. The synthesizer checks valid lengths and clamps peaks. SoundPool loads them asynchronously; no audio is played by the native asset-decode test.

Music and effects are muted during microphone-driven rounds to avoid input feedback. Stage results can play after listening stops. Audio is not yet mixed into exported video.
