# 0.1.62-dev — reject invalid model references

Observed 0.1.61 regression: mass/weight output invented source [2] and a Weight filename despite receiving only source [1]. Total time 32.2 s, 158 input tokens evaluated, 75 output tokens, 6.77 generation tokens/s. Output also introduced a definition not present in the supplied excerpt. This run is not a successful grounded comparison.

Changes: list only the actual allowed IDs in the prompt; omit filenames from model input while preserving real titles in the evidence view; label streaming output UNVERIFIED MODEL DRAFT. At completion, blank, uncited or out-of-range numeric-reference output is rejected. Display actual supplied excerpts under SOURCE QUOTATIONS ONLY, explicitly stating that this is not a generated/repaired answer. Do not silently renumber fabricated references. No automatic second model call.

Limits: this is reference validation, not semantic fact checking. Unsupported claims with valid IDs can still pass. References in alternative formats are not accepted. Genuine no-evidence refusals without numeric references also fall back to source excerpts. The app does not claim that rejected synthesis becomes successful research. Source quotations may themselves be incomplete or erroneous.

Four Kotlin tests cover the observed fake second source, preserving valid output, missing/zero/overflow IDs, and the explicit semantic limitation. These tests and Android compilation were NOT run here because the toolchain is unavailable; run the existing Actions build/tests before installation. Local checks cover manifest XML, packaging and inclusion only.

This is a cumulative source patch, not an APK. Apply at repository root; keep the signing secret and install the resulting APK as an update without uninstalling or clearing data. Re-run the same development case and preserve the complete output, whether accepted or rejected.
