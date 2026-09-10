from __future__ import annotations

import json
import pathlib
import zipfile


ROOT = pathlib.Path(__file__).resolve().parent
MANIFEST = ROOT / "mcdreforged.plugin.json"
INCLUDE = ("mcdreforged.plugin.json", "requirements.txt", "zyu_cobblemon_web")


def main() -> None:
    metadata = json.loads(MANIFEST.read_text(encoding="utf-8"))
    version = metadata["version"]
    output = ROOT / "dist" / f"zyu-cobblemon-web-{version}.mcdr"
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for entry in INCLUDE:
            path = ROOT / entry
            if path.is_file():
                archive.write(path, path.relative_to(ROOT))
            else:
                for child in path.rglob("*"):
                    if child.is_file() and "__pycache__" not in child.parts:
                        archive.write(child, child.relative_to(ROOT))
    with zipfile.ZipFile(output) as archive:
        required = {"mcdreforged.plugin.json", "zyu_cobblemon_web/__init__.py"}
        missing = required.difference(archive.namelist())
        if missing:
            raise RuntimeError(f"MCDR package missing: {sorted(missing)}")
        json.loads(archive.read("mcdreforged.plugin.json"))
    print(output)


if __name__ == "__main__":
    main()
