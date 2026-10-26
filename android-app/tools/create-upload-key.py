"""Create the first upload identity once. Never replace an existing signing file."""
import os
from pathlib import Path
import secrets
import subprocess

project = Path(__file__).resolve().parents[1]
private = project / "release-signing"
private.mkdir(exist_ok=True)
owner = subprocess.check_output(["whoami"], text=True).strip()
subprocess.run(["icacls", str(private), "/inheritance:r", "/grant:r", owner + ":(OI)(CI)F"], check=True, capture_output=True)
keystore = private / "facechaos-upload.p12"
properties = private / "upload.properties"
if keystore.exists() or properties.exists():
    raise SystemExit("An upload identity already exists. Restore or reuse it; never overwrite it.")
password = secrets.token_urlsafe(36)
env = dict(os.environ, FC_UPLOAD_PASSWORD=password)
keytool = Path("C:/Program Files/Android/Android Studio/jbr/bin/keytool.exe")
result = subprocess.run([
    str(keytool), "-genkeypair", "-keystore", str(keystore), "-storetype", "PKCS12",
    "-storepass:env", "FC_UPLOAD_PASSWORD", "-keypass:env", "FC_UPLOAD_PASSWORD",
    "-alias", "facechaos-upload", "-keyalg", "RSA", "-keysize", "3072",
    "-validity", "10000", "-dname", "CN=Face Chaos Upload", "-noprompt",
], env=env, capture_output=True, text=True)
if result.returncode:
    raise SystemExit("Key generation failed; inspect the private signing folder before retrying.")
properties.write_text(
    "storeFile=release-signing/facechaos-upload.p12\n"
    f"storePassword={password}\nkeyAlias=facechaos-upload\nkeyPassword={password}\n",
    encoding="ascii",
)
destination = project.parent / "dist"
destination.mkdir(exist_ok=True)
subprocess.run([
    str(keytool), "-exportcert", "-rfc", "-alias", "facechaos-upload",
    "-keystore", str(keystore), "-storepass:env", "FC_UPLOAD_PASSWORD",
    "-file", str(destination / "FaceChaos-upload-certificate.pem"),
], env=env, check=True, capture_output=True)
print("Created a new RSA upload key in the owner-restricted release-signing folder. Public certificate exported. No passwords displayed.")
