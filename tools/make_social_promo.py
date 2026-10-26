"""Create a launch-ready social ad from existing promotional art and original audio.

No application code, phone settings or store listings are modified.
"""
from pathlib import Path
import json
import math
import subprocess
import wave

import imageio_ffmpeg
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "store-assets/social-promo"
ASSETS.mkdir(parents=True, exist_ok=True)
OUT = ROOT / "dist"
FONT = "C:/Windows/Fonts/segoeuib.ttf"
REGULAR = "C:/Windows/Fonts/segoeui.ttf"
W, H, FPS, TOTAL = 1080, 1920, 30, 24.0
TEAL, CREAM, GOLD = "#173e42", "#fffbf0", "#f7cf55"


def font(size, bold=True):
    return ImageFont.truetype(FONT if bold else REGULAR, size)


def centered(draw, text, y, size, fill=CREAM, bold=True):
    draw.text((W / 2, y), text, font=font(size, bold), fill=fill, anchor="mt")


def background():
    start, end = np.array([23, 62, 66]), np.array([10, 36, 43])
    colors = start[None, :] + np.linspace(0, 1, H)[:, None] * (end - start)[None, :]
    pixels = np.repeat(colors[:, None, :], W, axis=1).astype(np.uint8)
    return Image.fromarray(pixels)


def star(draw, x, y, radius, color):
    points = []
    for index in range(8):
        angle = -math.pi / 2 + index * math.pi / 4
        r = radius if index % 2 == 0 else radius * .28
        points.append((x + r * math.cos(angle), y + r * math.sin(angle)))
    draw.polygon(points, fill=color)


def title_cards():
    icon = Image.open(ROOT / "store-assets/facechaos-app-icon-512.png").convert("RGB")
    image = background()
    draw = ImageDraw.Draw(image)
    centered(draw, "FACE CHAOS", 230, 118)
    centered(draw, "Your face is", 410, 72)
    centered(draw, "the controller.", 505, 84, GOLD)
    image.paste(icon.resize((500, 500), Image.Resampling.LANCZOS), (290, 705))
    draw = ImageDraw.Draw(image)
    centered(draw, "15 playful mini-games", 1300, 57)
    centered(draw, "Face. Voice. Head movement.", 1400, 43, "#bde9dc", False)
    for x, y, radius in [(160, 700, 27), (935, 1210, 24), (170, 1520, 15), (920, 405, 17)]:
        star(draw, x, y, radius, GOLD)
    image.save(ASSETS / "opening.png", optimize=True)

    image = background()
    image.paste(icon.resize((260, 260), Image.Resampling.LANCZOS), (410, 205))
    draw = ImageDraw.Draw(image)
    centered(draw, "FACE CHAOS", 520, 81)
    for line, y in zip(["YOUR NEXT", "CHALLENGE", "STARTS HERE."], [740, 855, 970]):
        centered(draw, line, y, 94, GOLD if y == 855 else CREAM)
    draw.rounded_rectangle((115, 1215, 965, 1365), radius=42, fill=GOLD)
    centered(draw, "GET FACE CHAOS", 1242, 64, TEAL)
    centered(draw, "Link in bio", 1420, 59)
    centered(draw, "Play with your face & voice", 1535, 39, "#bde9dc", False)
    for x, y, radius in [(165, 565, 22), (930, 1125, 28), (170, 1600, 15)]:
        star(draw, x, y, radius, GOLD)
    image.save(ASSETS / "ending.png", optimize=True)


def read_audio(name, rate=48000):
    with wave.open(str(ROOT / "android-app/app/src/main/res/raw" / name)) as stream:
        old_rate = stream.getframerate()
        data = np.frombuffer(stream.readframes(stream.getnframes()), dtype="<i2").astype(np.float64) / 32768
    count = round(len(data) * rate / old_rate)
    return np.interp(np.arange(count) * old_rate / rate, np.arange(len(data)), data)


def soundtrack():
    rate, length = 48000, int(TOTAL * 48000)
    music = read_audio("music_garden.wav")
    mono = np.tile(music, math.ceil(length / len(music)))[:length] * 1.8
    rng = np.random.default_rng(43)

    def add(audio, time, gain=1):
        start = round(time * rate)
        count = min(len(audio), length - start)
        if count > 0:
            mono[start:start + count] += audio[:count] * gain

    # Original synthesized percussion, matched to the project's original 100 BPM loop.
    for beat in range(40):
        t = np.arange(round(rate * .24)) / rate
        kick = np.sin(2 * np.pi * (52 * t + 3.8 * (1 - np.exp(-t * 35)))) * np.exp(-t * 18) * .24
        if beat % 2 == 0:
            add(kick, beat * .6)
        else:
            clap = rng.normal(0, 1, len(t)) * np.exp(-t * 35) * .065
            add(clap, beat * .6)
    for beat in range(80):
        t = np.arange(round(rate * .07)) / rate
        noise = rng.normal(0, 1, len(t))
        hat = (noise - np.roll(noise, 1)) * np.exp(-t * 65) * .018
        add(hat, beat * .3)
    for time in [2.1, 5.2, 8.3, 11.4, 14.5, 17.6]:
        add(read_audio("fx_coin.wav"), time, .43)
    add(read_audio("fx_go.wav"), .9, .5)
    add(read_audio("fx_complete.wav"), 20.75, .45)
    envelope = np.minimum(1, np.arange(length) / (rate * .22))
    envelope *= np.minimum(1, np.arange(length)[::-1] / (rate * .8))
    mono *= envelope
    mono *= .86 / max(.86, np.max(np.abs(mono)))
    stereo = np.column_stack([mono, mono * .985])
    with wave.open(str(ASSETS / "soundtrack.wav"), "wb") as stream:
        stream.setnchannels(2)
        stream.setsampwidth(2)
        stream.setframerate(rate)
        stream.writeframes((stereo * 32767).astype("<i2").tobytes())


def make_video():
    title_cards()
    soundtrack()
    posters = ROOT / "dist/FaceChaos-Play-Screenshots"
    scenes = [ASSETS / "opening.png"] + [posters / name for name in [
        "03-emoji-gates.png", "01-smile-stack.png", "02-melody-hop.png",
        "04-sound-grand-prix.png", "05-head-dodge.png", "06-flag-world-tour.png"
    ]] + [ASSETS / "ending.png"]
    durations = [2.4] + [3.4] * 6 + [3.3]
    ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
    command = [ffmpeg, "-y", "-hide_banner", "-filter_complex_threads", "2"]
    for scene, duration in zip(scenes, durations):
        command += ["-loop", "1", "-framerate", str(FPS), "-t", str(duration), "-i", str(scene)]
    command += ["-i", str(ASSETS / "soundtrack.wav")]
    filters = []
    for index, duration in enumerate(durations):
        zoom = "1.0+0.00021*on" if index not in (0, 7) else "1.0+0.00010*on"
        filters.append(
            f"[{index}:v]scale={W}:{H},zoompan=z='{zoom}':x='(iw-iw/zoom)/2':y='(ih-ih/zoom)/2':"
            f"d=1:s={W}x{H}:fps={FPS},trim=duration={duration},setpts=PTS-STARTPTS,setsar=1,format=yuv420p,fps={FPS},settb=1/{FPS}[v{index}]"
        )
    current = "v0"
    for index in range(1, len(scenes)):
        offset = sum(durations[:index]) - .3 * index
        name = "fade" + str(index)
        filters.append(f"[{current}][v{index}]xfade=transition=fade:duration=0.3:offset={offset:.3f},fps={FPS},settb=1/{FPS}[{name}]")
        current = name
    filters.append("[8:a]loudnorm=I=-16:TP=-1.5:LRA=9,aresample=48000[audio]")
    output = OUT / "FaceChaos-Social-Promo-Vertical.mp4"
    command += ["-filter_complex", ";".join(filters), "-map", "[" + current + "]", "-map", "[audio]",
                "-t", str(TOTAL), "-r", str(FPS), "-c:v", "libx264", "-preset", "veryfast", "-crf", "20",
                "-profile:v", "high", "-level:v", "4.1", "-pix_fmt", "yuv420p", "-threads", "4",
                "-c:a", "aac", "-b:a", "160k", "-movflags", "+faststart", str(output)]
    (ASSETS / "render-command.json").write_text(json.dumps(command, indent=2))
    with (ASSETS / "render.log").open("w") as log:
        result = subprocess.run(command, stdout=log, stderr=log)
    if result.returncode:
        raise SystemExit("Video render failed; inspect store-assets/social-promo/render.log")
    print("Rendered", output.name, "bytes", output.stat().st_size)


if __name__ == "__main__":
    make_video()
