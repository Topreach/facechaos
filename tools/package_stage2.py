"""Create a clean source snapshot, excluding generated and personal data."""
import hashlib
import json
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
DESTINATION = ROOT / 'dist/FaceChaos_Stage2_2026-10-24.zip'
FOLDERS = ('scripts', 'recognition', 'docs', 'tests', 'tools')
ROOT_FILES = ('.gitignore', 'project.godot', 'main.tscn', 'README.md', 'STAGE2_START_HERE.md',
              'FACIAL_LAB_START_HERE.md', 'Test-Facial-Reactions.cmd', 'PLAY_EXPRESSION_ESCAPE.md')


def main():
    files = [ROOT / name for name in ROOT_FILES]
    for name in FOLDERS:
        for path in (ROOT / name).rglob('*'):
            # Personal calibration/report JSON may have been saved outside profiles/.
            static_json = {'recognition/model-manifest.json', 'docs/facial-crosscheck-results.json'}
            if path.suffix.lower() == '.json' and path.relative_to(ROOT).as_posix() not in static_json:
                continue
            if (path.is_file() and not set(path.relative_to(ROOT).parts) &
                    {'__pycache__', 'profiles', 'models'} and path.suffix != '.log'):
                files.append(path)
    files = sorted(set(files))
    manifest = {path.relative_to(ROOT).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest()
                for path in files}
    DESTINATION.parent.mkdir(exist_ok=True)
    with ZipFile(DESTINATION, 'w', ZIP_DEFLATED) as archive:
        for path in files:
            archive.write(path, 'FaceChaos_Stage2/' + path.relative_to(ROOT).as_posix())
        archive.writestr('FaceChaos_Stage2/PACKAGE_MANIFEST.json', json.dumps(manifest, indent=2))
    with ZipFile(DESTINATION) as archive:
        assert archive.testzip() is None, 'ZIP integrity failure'
        for name, checksum in manifest.items():
            assert hashlib.sha256(archive.read('FaceChaos_Stage2/' + name)).hexdigest() == checksum
        names = archive.namelist()
        assert all(not set(Path(name).parts) & {'.venv', '.godot', 'profiles', '__pycache__'} for name in names)
        assert 'FaceChaos_Stage2/project.godot' in names
    print(json.dumps({'archive': str(DESTINATION), 'files': len(files) + 1,
                      'bytes': DESTINATION.stat().st_size, 'integrity': 'passed',
                      'sha256': hashlib.sha256(DESTINATION.read_bytes()).hexdigest()}, indent=2))


if __name__ == '__main__':
    main()
