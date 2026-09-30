"""Fail CI if a frozen Forest asset changes without updating its reviewed baseline."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
baseline = json.loads((root / 'docs/forest-baseline.json').read_text())
errors = []
for asset in baseline['assets']:
    path = root / asset['path']
    if not path.is_file():
        errors.append(f"Missing asset: {asset['path']}")
        continue
    data = path.read_bytes()
    digest = hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
    if digest != asset['git_blob_sha']:
        errors.append(f"Changed approved asset: {asset['path']}")
if errors:
    raise SystemExit('\n'.join(errors))
print(f"Verified {len(baseline['assets'])} approved Forest assets")
