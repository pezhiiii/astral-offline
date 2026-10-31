# 0.1.69 — shorter generation instructions

Source update, not an APK. Apply over the existing repository and build with the existing signing workflow. Version code 70; version name 0.1.69-dev.

The phone draft in 0.1.68 used the inertia definition but collected citations at the end of four sentences. It was correctly rejected. This update changes only generation instructions and release metadata; retrieval and citation acceptance rules are preserved.

Comparisons now request three short lines, one sentence per line, at most 70 words total. Instructions follow the supplied evidence to emphasize citation placement. They ask the model to retain conditions and scope and avoid turning specific examples into universal claims. No software inserts citations into the model's output, and there is no automatic retry.

This is a prompt experiment, not verified model compliance. A three-line limit can compress answers or omit details in more complex comparisons. The numeric-reference and sentence-placement checks remain syntactic rather than entailment verification. If the model fails them, quotation-only output remains the expected fallback.

Validation: Kotlin 1.9.24 compilation succeeded; all 69 tests in nine JUnit classes passed. The added regression preserves rejection of collected citations and accepts separately cited sentences. Android build and on-phone generation have not been tested here.

Phone retest in airplane mode with Wi-Fi disabled:
Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources.

Capture the answer, supplied evidence and timings. If rejected, capture REJECTED MODEL DRAFT from View source passages; do not rerun solely to obtain a preferred result.
