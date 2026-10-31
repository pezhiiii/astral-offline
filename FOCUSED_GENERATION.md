# 0.1.81

The direction subquestion no longer repeats the original multi-part question. Its evidence selection prefers the shortest retrieved passage that explicitly mentions both requested topics and direction, retaining the original source ID. If none qualifies, all retrieved excerpts remain available. Definition/unit parts keep their existing evidence selection.

Subquestion instructions are simplified to positive, direct requests for a short complete sentence. The grammar ceiling and token budget are unchanged; all rejection checks and raw diagnostics remain active. This aims to reduce repetition, not guarantee brevity or factual correctness. No answer is inserted, trimmed or repaired.

Regression tests updated for isolated direction task, focused evidence selection, and fallback when no focused passage exists. Kotlin/Android tests and APK build could not run in this environment because the build toolchain is unavailable. Archive/source checks and unchanged C++ grammar test pass. Phone generation and performance remain unverified.

Build/install 0.1.81-dev. Repeat the speed/velocity query and capture main answer plus all part Validation/raw drafts. A rejected response remains a failed synthesis test even when rejection is correct.
