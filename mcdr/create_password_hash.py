from __future__ import annotations

import base64
import hashlib
import secrets
import sys


def main() -> None:
    if len(sys.argv) != 2 or not sys.argv[1]:
        raise SystemExit("usage: uv run python create_password_hash.py <password>")
    salt = secrets.token_bytes(16)
    digest = hashlib.pbkdf2_hmac("sha256", sys.argv[1].encode("utf-8"), salt, 210_000)
    print("pbkdf2_sha256$210000${}${}".format(
        base64.urlsafe_b64encode(salt).decode("ascii"),
        base64.urlsafe_b64encode(digest).decode("ascii"),
    ))


if __name__ == "__main__":
    main()
