"""Download the official versioned model locally; no user data is uploaded."""
import hashlib
import json
import urllib.request
from pathlib import Path

URL = 'https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task'
ROOT = Path(__file__).resolve().parent


def main():
    target = ROOT / 'models/face_landmarker.task'
    target.parent.mkdir(parents=True, exist_ok=True)
    if not target.exists():
        temp = target.with_suffix('.download')
        try:
            with urllib.request.urlopen(URL, timeout=60) as response, temp.open('wb') as output:
                while chunk := response.read(1024 * 1024):
                    output.write(chunk)
            temp.replace(target)
        finally:
            temp.unlink(missing_ok=True)
    checksum = hashlib.sha256(target.read_bytes()).hexdigest()
    manifest = ROOT / 'model-manifest.json'
    if manifest.exists():
        expected = json.loads(manifest.read_text(encoding='utf-8'))['sha256']
        if checksum != expected:
            raise RuntimeError('Model checksum mismatch. Remove the local model and run setup again.')
    else:
        manifest.write_text(json.dumps({'url': URL, 'sha256': checksum}, indent=2), encoding='utf-8')
    print(f'Model ready; SHA256 {checksum}')


if __name__ == '__main__':
    main()
