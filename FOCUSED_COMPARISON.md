# 0.1.64-dev — focused neighboring evidence and rejected-draft diagnostics

Observed 0.1.63 run: 49.1 seconds, 287 input tokens evaluated/no cache, 128 output tokens, token-limit termination. Citation filter rejected the output for missing numeric references. The actual raw rejected draft was not available from the screenshots, so the cause of verbosity is not established.

The comparison-context neighbor rule now requires all parsed comparison topics in an added neighboring sentence. Existing seed definitions may still come from separate passages. Neighbor expansion budget changes from 120 to 90 words (the seed itself is not truncated). This excludes the observed single-topic Higgs neighbor without a mass-specific blacklist. It may also exclude useful single-topic context: this is conservative lexical selection, not semantic understanding.

Simplified prompt: two short comparison sentences, at most 40 words, with each citation immediately after its sentence. Token cap remains 128. Output may still ignore instructions or be rejected; speed and quality improvements are not yet measured.

Rejected raw model drafts and timing diagnostics are retained at the end of View source passages, explicitly labeled diagnostic only and NOT a supported answer. Final display stops live-progress updates before presentation to avoid a queued update replacing the final result. Existing numeric/formula/reference checks remain limited and do not prove factual entailment.

Two regression tests exercise single-topic neighbor exclusion on mass/weight and weather/climate. Android compilation and Kotlin tests are not available here; run the existing Actions build/tests. Local checks validate manifest syntax, archive integrity and inclusion of changed files only.

Apply this cumulative source patch to repository root. Preserve the signing secret and install the built APK over the current app, without uninstalling or clearing data. If a run is rejected, inspect its diagnostic draft before choosing another fix. Do not count source-quotation fallback as successful synthesis.
