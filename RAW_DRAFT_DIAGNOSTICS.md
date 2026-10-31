# 0.1.78: visible rejection diagnostics

Each part now reports the raw model output between explicit delimiters and its own validation result beside the timing report. These drafts are labeled diagnostic, not verified answers. Source-quotation fallback and all validation rules remain unchanged.

Reproduce the mass/weight comparison and capture PART 1, including BEGIN DRAFT through END DRAFT and Validation. This distinguishes an actual unfinished output from a validator false positive. This release does not claim to fix generation or improve speed.

Validation: source/package inspection only. No Android SDK, Gradle wrapper or device test was available in this workspace; build and runtime remain unverified.
