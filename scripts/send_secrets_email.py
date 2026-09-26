#!/usr/bin/env python3
"""Send the local CFA EUR GitHub secrets as a plain-text email."""

import argparse
import base64
import os
import smtplib
import ssl
from email.message import EmailMessage
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SECRETS_FILE = ROOT / ".local-secrets" / "github-secrets.env"
KEYSTORE_FILE = ROOT / ".local-secrets" / "cfaeur-release.jks"
SENDER = "vincenzo.sagristano@ottonovembre.it"
RECIPIENT = "vincenzo.sagristano@yahoo.com"
SMTP_HOST = "smtp.ionos.it"
SMTP_PORT = 587
EXPECTED_KEYS = (
    "ANDROID_KEYSTORE_BASE64",
    "ANDROID_KEYSTORE_PASSWORD",
    "ANDROID_KEY_ALIAS",
    "ANDROID_KEY_PASSWORD",
)


def load_secrets() -> dict[str, str]:
    values = {}
    for line in SECRETS_FILE.read_text(encoding="utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        key, separator, value = line.partition("=")
        if not separator or key not in EXPECTED_KEYS or not value or key in values:
            raise ValueError(f"Invalid secret entry: {key!r}")
        values[key] = value
    if set(values) != set(EXPECTED_KEYS):
        raise ValueError("Missing GitHub secret values")
    decoded = base64.b64decode(values["ANDROID_KEYSTORE_BASE64"], validate=True)
    if decoded != KEYSTORE_FILE.read_bytes():
        raise ValueError("The Base64 value does not match the local keystore")
    return values


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="validate without sending")
    args = parser.parse_args()

    values = load_secrets()
    if args.check:
        print("Four secrets and local keystore validated; no email sent.")
        return

    password = os.environ.get("CFAEUR_SMTP_PASSWORD")
    if not password:
        parser.error("set CFAEUR_SMTP_PASSWORD to the IONOS mailbox password")

    body = "GitHub Actions Secrets per xprss/cfaeur:\n\n"
    body += "\n".join(f"{key}={values[key]}" for key in EXPECTED_KEYS) + "\n"

    message = EmailMessage()
    message["From"] = SENDER
    message["To"] = RECIPIENT
    message["Subject"] = "cfaeur - chiavi di firma Android"
    message.set_content(body, subtype="plain", charset="utf-8")

    with smtplib.SMTP(SMTP_HOST, SMTP_PORT, timeout=30) as server:
        server.ehlo()
        server.starttls(context=ssl.create_default_context())
        server.ehlo()
        server.login(SENDER, password)
        server.send_message(message)

    print(f"Email sent from {SENDER} to {RECIPIENT}.")


if __name__ == "__main__":
    main()
