from __future__ import annotations

import json
import pathlib
import zipfile


ROOT = pathlib.Path(__file__).resolve().parent
OUTPUT = ROOT / "dist" / "zyu-cobblemon-web.mcdr"
INCLUDE = ("mcdreforged.plugin.json", "requirements.txt", "zyu_cobblemon_web")


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(OUTPUT, "w", zipfile.ZIP_DEFLATED) as archive:
        for entry in INCLUDE:
            path = ROOT / entry
            if path.is_file():
                archive.write(path, path.relative_to(ROOT))
            else:
                for child in path.rglob("*"):
                    if child.is_file() and "__pycache__" not in child.parts:
                        archive.write(child, child.relative_to(ROOT))
    with zipfile.ZipFile(OUTPUT) as archive:
        required = {"mcdreforged.plugin.json", "zyu_cobblemon_web/__init__.py"}
        missing = required.difference(archive.namelist())
        if missing:
            raise RuntimeError(f"MCDR package missing: {sorted(missing)}")
        json.loads(archive.read("mcdreforged.plugin.json"))
    print(OUTPUT)


if __name__ == "__main__":
    main()
