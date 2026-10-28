"""Inspect the built AAB, dumped manifest/config and native LOAD/RELRO layout."""
from pathlib import Path
import json
import re
import struct
import xml.etree.ElementTree as ET
import zipfile

root = Path(__file__).resolve().parents[1]
out = root.parent / "dist"
bundle = root / "app/build/outputs/bundle/release/app-release.aab"
ns = "{http://schemas.android.com/apk/res/android}"
manifest = ET.parse(out / "FaceChaos-release-manifest.xml").getroot()
assert manifest.attrib["package"] == "com.facechaos.mobile"
build_settings = (root / "app/build.gradle").read_text(encoding="utf-8")
expected_code = re.search(r"versionCode\s+(\d+)", build_settings).group(1)
expected_name = re.search(r"versionName\s+'([^']+)'", build_settings).group(1)
assert manifest.attrib[ns + "versionCode"] == expected_code
assert manifest.attrib[ns + "versionName"] == expected_name
assert manifest.find("application").attrib.get(ns + "debuggable", "false") == "false"
assert manifest.find("uses-sdk").attrib[ns + "targetSdkVersion"] == "36"
activity = manifest.find("application/activity")
launcher = next(a for a in manifest.findall("application/activity") if a.find("intent-filter") is not None)
assert launcher.attrib[ns + "name"] == "com.facechaos.mobile.WelcomeActivity"
assert activity.attrib.get(ns + "exported") == "false", "Camera screen must be entered through welcome"
assert activity.attrib.get(ns + "resizeableActivity") == "true"
assert ns + "screenOrientation" not in activity.attrib
assert ns + "maxAspectRatio" not in activity.attrib
config = json.loads((out / "FaceChaos-bundle-config.json").read_text())
assert config["optimizations"]["uncompressNativeLibraries"]["alignment"] == "PAGE_ALIGNMENT_16K"
dimensions = config["optimizations"]["splitsConfig"].get("splitDimension", [])
assert any(d.get("value") == "LANGUAGE" and d.get("negate") for d in dimensions), "Translations must be bundled together"
report = []
with zipfile.ZipFile(bundle) as archive:
    assert archive.testzip() is None
    names = archive.namelist()
    assert "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map" in names, "R8 mapping missing"
    r8 = json.loads(archive.read("BUNDLE-METADATA/com.android.tools/r8.json"))
    for option in ("isObfuscationEnabled", "isOptimizationsEnabled", "isShrinkingEnabled"):
        assert r8["options"][option], "Release optimization disabled: " + option
    assert "base/assets/face_fixture.jpg" not in names, "Verification fixture leaked into release"
    assert "base/assets/face_landmarker.task" in names
    assert not any(n.endswith((".p12", ".jks", ".keystore", "upload.properties")) for n in names)
    for name in names:
        if not (name.startswith("base/lib/") and name.endswith(".so")):
            continue
        data = archive.read(name)
        wide, endian = data[4] == 2, "<" if data[5] == 1 else ">"
        phoff = struct.unpack_from(endian + ("Q" if wide else "I"), data, 32 if wide else 28)[0]
        size, count = struct.unpack_from(endian + "HH", data, 54 if wide else 42)
        loads, relros = [], []
        for index in range(count):
            pos = phoff + index * size
            kind = struct.unpack_from(endian + "I", data, pos)[0]
            if wide:
                offset, address, _, file_size, memory_size, alignment = struct.unpack_from(endian + "QQQQQQ", data, pos + 8)
                flags = struct.unpack_from(endian + "I", data, pos + 4)[0]
            else:
                offset, address, _, file_size, memory_size, flags, alignment = struct.unpack_from(endian + "IIIIIII", data, pos + 4)
            if kind == 1:
                assert alignment >= 16384 and (address - offset) % 16384 == 0, name
                loads.append((address, memory_size, flags, alignment))
            if kind == 0x6474E552:
                relros.append((address, memory_size))
        statuses = []
        for address, memory_size in relros:
            end = address + memory_size
            if end % 16384 == 0:
                statuses.append("aligned end")
                continue
            # Android's documented exception: RELRO occupies its entire LOAD,
            # and rounding read-only protection cannot overlap writable data.
            owner = [segment for segment in loads if segment[:2] == (address, memory_size)]
            assert owner, name + ": RELRO shares a LOAD with writable data"
            protected_end = (end + 16383) // 16384 * 16384
            assert not any((a, n) != (address, memory_size) and flags & 2 and a < protected_end and a + n > end for a, n, flags, _ in loads), name
            statuses.append("dedicated RELRO LOAD; writable data outside rounded protection")
        report.append({"library": name, "minimumLoadAlignment": min(x[3] for x in loads), "relro": statuses})
(out / "FaceChaos-native-alignment.json").write_text(json.dumps(report, indent=2))
print("PASS: release identity, non-debuggable manifest, target 36, bundle 16 KB packaging, model, ZIP integrity, private-key exclusion and four native LOAD/RELRO layouts")
