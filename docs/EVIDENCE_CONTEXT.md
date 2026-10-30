# 0.1.63-dev — preserve comparison context; detect new numeric/formula content

Observed 0.1.62 mass/weight run: 27.0 seconds, input 161 evaluated/no cache, output 75 tokens, 7.47 generation tokens/s. Numeric reference [1] existed, but the answer added a formula and claims absent from the one-sentence excerpt. This was not a fully grounded success.

Comparison context now retains complete nearby sentences mentioning the compared topics, in original order, adding neighbors only within a 120-word budget. Seed comparison excerpts remain verbatim; up to two sources are used. This can supply context previously discarded but cannot create missing evidence. Additional prompt tokens can increase runtime.

The output filter rejects newly introduced numeric literals and rejects equation/multiplication/division symbols when no such symbols occur in the evidence. Citation numbers are excluded from numeric checking. Actual arithmetic handled by the existing deterministic numerical path is unchanged. Rejected output falls back to labeled source quotations, not claimed synthesis.

This is deliberately limited: it is NOT semantic verification. Valid numbers can be used incorrectly; formulas written in words or with other notation may pass. Legitimate equivalent numeric formatting or a derived number may be rejected. Unsupported prose can still pass. Accepted model output is explicitly labeled as not factually verified. No lexical test is advertised as proving entailment.

Added regression tests cover omitted context and the observed new-formula/new-number class. Android/Kotlin compilation and tests have not run here; run the existing Actions workflow. Local validation covers package integrity, manifest syntax and a scan for invalid Kotlin string escapes. Device quality and speed remain unmeasured for this revision.

Apply this cumulative SOURCE patch at repository root. Build/test in Actions; install APK over the existing app with the same signing key. Never uninstall or clear data. Re-run mass/weight as a development regression, preserving the whole answer and evidence. Do not report quotation fallback as a successful generated answer.
