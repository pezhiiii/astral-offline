#!/usr/bin/env python3
"""Compare a verified apksigner report against the restored public DER certificate.

Run apksigner verify --print-certs successfully BEFORE invoking this check.
No private key or password is read or printed by this script.
"""
import hashlib
from pathlib import Path
import re
import sys

def verify(expected_der: bytes, report: str) -> str:
    signers = re.findall(r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})\s*$", report, re.MULTILINE)
    expected = hashlib.sha256(expected_der).hexdigest()
    if len(signers) != 1 or signers[0].lower() != expected:
        raise ValueError("APK signer does not match the restored key; refusing to publish artifact")
    return expected

if __name__ == "__main__":
    try:
        if len(sys.argv) != 3:
            raise ValueError("Usage: verify_apk_signer.py expected.der apksigner-report.txt")
        fingerprint = verify(Path(sys.argv[1]).read_bytes(), Path(sys.argv[2]).read_text())
        print("Persistent signer verified (SHA-256): " + fingerprint)
    except (OSError, ValueError) as error:
        sys.exit(str(error))
