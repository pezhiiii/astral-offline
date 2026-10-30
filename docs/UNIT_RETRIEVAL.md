# 0.1.66 — unit-question retrieval

Source update package, not an APK or a complete standalone build tree. Apply its files over the existing Astral repository and use the existing signed build workflow. Version code 67, version name 0.1.66-dev. Keep the existing signing key and app data.

Observed failures: Research offline returned no evidence for SI units of mass and weight, although Quick source lookup exposed the kilogram and newton sentences. SearchPolicy treated SI as a definition anchor. Separately, the comparison topic parser included colon-delimited instructions in the second subject.

Changes:
- Parse explicit SI-unit requests into quantity subjects, generate quantity queries, and require complete unit-bearing source sentences.
- Select evidence covering each requested quantity when available; exclude incidental/historical matches lacking a direct SI-unit statement.
- Send the selected unit sentences to the model instead of a generic 55-word window.
- End comparison subject names at a colon.
- Preserve citation rejection and unknown-subject gates. These are lexical checks, not factual verification.

Validation: compiled SearchPolicy, DocumentSections, CitationPolicy and all seven included JUnit classes using Kotlin 1.9.24; 58 tests passed. New regression cases cover the observed unit sentences, two-quantity source selection, unrelated astronomy, historical text, comparison qualifiers and unknown personal information.

Not validated: Android build, APK signing, UI, live SQLite retrieval against the user's installed database, model compliance, GrapheneOS or device performance. Multi-part comparisons may still omit requested details and need device evaluation. Partial unit evidence is allowed; the model must acknowledge missing information.

Phone retest after installing the signed update, airplane mode with Wi-Fi off:
1. What are the SI units of mass and weight? Cite the sources.
2. Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources.
3. What is the exact serial number of my refrigerator? Cite the sources.

Use Research offline. Check complete answers, supporting excerpts and numeric citations; record failures as well as successes.
