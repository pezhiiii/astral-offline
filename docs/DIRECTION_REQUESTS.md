# 0.1.80

Direction comparisons retain a third task including the original question and all retrieved source IDs. Missing subject names or the word direction reject that task. Unrecognized non-scenario comparisons now use the original question rather than silently dropping remaining requests. Existing Earth/Moon decomposition is retained.

Unit citation checks now reject m/s2, m/s^2, m/s² or metres/meters per second squared when no squared-time expression occurs in the cited source line. This narrow lexical rule does not establish dimensional correctness, factual entailment, or whether a supported unit was assigned to the correct quantity.

Regression cases added for preserved direction request, source availability, omitted direction, full-question fallback and squared-time support. Kotlin/Android tests and build could not run: Kotlin compiler, Gradle and Android SDK are unavailable. Source/package inspection passed. The unchanged C++ grammar test passes. Generated answer quality and latency require device testing.

Build/install 0.1.80-dev and repeat the speed/velocity comparison. Check three parts, the final direction distinction, and whether any unsupported acceleration unit is rejected. No factual answer is hardcoded or repaired.
