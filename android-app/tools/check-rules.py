"""Compile production rule classes and run all plain-Java checks without Android hardware."""
from pathlib import Path
import os
import shutil
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
source = root / "app/src/main/java/com/facechaos/mobile"
output = root / "checks/out"
output.mkdir(parents=True, exist_ok=True)
classes = "AdaptiveViewport AnimalGuide AnimalPatterns CommercialPolicy FaceGameInput FaceSignals FlagQuiz GameCatalog GameSections HeadSignals MelodyCourse MiniGame OverlayLayout PreviewGeometry RacingGame RatingPolicy SoundEvents SoundGate SpeechPolicy StageRules TeethEvidence VoiceCourse".split()
checks = sorted((root / "checks").glob("*Check.java"))

def java_tool(name):
    home = os.environ.get("JAVA_HOME")
    tool = Path(home) / "bin" / (name + (".exe" if os.name == "nt" else "")) if home else None
    found = str(tool) if tool and tool.is_file() else shutil.which(name)
    if not found:
        raise SystemExit(f"Set JAVA_HOME to a JDK 17+ installation; {name} was not found")
    return found

subprocess.run([java_tool("javac"), "--release", "17", "-d", str(output), *[str(source / (name + ".java")) for name in classes], *map(str, checks)], check=True)
results = []
failed = []
for check in checks:
    result = subprocess.run([java_tool("java"), "-cp", str(output), "com.facechaos.mobile." + check.stem], capture_output=True, text=True)
    status = "PASS" if result.returncode == 0 else "FAIL"
    print(f"{check.stem}: {status}", flush=True)
    results.append(f"{check.stem}: {status}\n{result.stdout}{result.stderr}")
    if result.returncode:
        failed.append(check.stem)

report = root.parent / "dist" / "FaceChaos-rule-checks.txt"
report.parent.mkdir(exist_ok=True)
report.write_text("\n".join(results), encoding="utf-8")
print(f"{len(checks) - len(failed)}/{len(checks)} passed; report: {report}")
sys.exit(1 if failed else 0)
