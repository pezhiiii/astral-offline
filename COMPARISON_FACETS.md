# 0.1.67 — evidence for multipart comparisons

Source update, not an APK. Apply over the existing repository and build with the existing signed workflow. Version code 68; version name 0.1.67-dev.

Observed on the phone: the model answered the multipart mass/weight question, but its supplied excerpt omitted unit and Earth/Moon facts. A valid citation number did not establish support for those claims.

Changes:
- Select definitions, explicitly requested SI-unit sentences, and a sentence mentioning the requested from/to locations and comparison subjects as independent evidence facets.
- Retain complementary passages instead of stopping at the first contrast. Include up to four source passages.
- Send complete source sentences within a 150-word per-source budget; never insert inferred facts or units into evidence.
- Allow up to 100 words for a comparison and instruct the model to identify requested details absent from evidence.
- Preserve existing citation checks and unknown-subject gates. Facet selection is lexical, not semantic verification; it can still miss paraphrases or choose imperfect evidence.

Validation: Kotlin 1.9.24 compilation succeeded; all 63 JUnit tests passed. Android build, installed SQLite integration, live model behavior and phone timing have not been tested in this environment.

Phone retest, airplane mode and Wi-Fi off:
Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources.

Inspect EVIDENCE SENT TO MODEL for both units and the Earth/Moon sentence. Inspect the generated answer for support and citations. A correct-looking answer alone is insufficient.
