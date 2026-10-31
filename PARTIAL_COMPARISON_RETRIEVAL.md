# 0.1.85

Root cause: comparisonSourceIndices returned an empty list whenever either subject lacked a recognized definition, before its facet-selection loop could retain valid definition/unit passages. The stricter definition filter in 0.1.84 exposed this existing all-or-nothing behavior.

The selector now keeps definitions it found and continues collecting additional facets within the existing four-source limit. The pre-synthesis missing-evidence gate is unchanged: partial evidence remains visible, missing definitions/units are named, and no model synthesis runs when those requested facets are absent. Generic importance text is still excluded as a definition; no evidence or factual answer is fabricated.

Regression cases added: observed heat/temperature partial evidence returns heat definition and unit, unit-only evidence remains retrievable, unrelated text stays empty, and missing facets remain reported. Kotlin tests and Android build/device checks could not run because their toolchain is unavailable. Source/package checks passed; unchanged C++ grammar construction and JVM unit-notation checks passed. Device behavior remains unverified.

Build/install 0.1.85-dev and repeat heat/temperature. Expected with the observed partial corpus: INSUFFICIENT COMPARISON EVIDENCE listing missing temperature facets, with retained heat sources. This is retrieval recovery and honest abstention, not a successful complete research answer. No reimport is required for this code change.
