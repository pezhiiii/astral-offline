# 0.1.71: citation-constrained decoding

The 0.1.70 phone report identified qwen2.5-1.5b-instruct, GGUF default template,
exact instruction/question presence, and another citation-placement rejection.
This does not establish that chat role semantics or factual support are correct.

The native sampler now applies a GBNF grammar before greedy selection. It allows
one to three sentence lines, each ending in one or more model-selected references
from the supplied source range, followed by a period. Decimal points are supported;
ASCII abbreviations with periods must be spelled out. The app does not append,
move or guess references after generation. Grammar initialization failure aborts.
The JNI source-count parameter must be deployed with the matching Kotlin code.

This is a format constraint, not factual verification. The model can still cite
the wrong passage, omit requested facts, or produce unsupported statements. The
existing rejection and quotation fallback remain. A token limit can interrupt a
grammar production; incomplete output is still subject to the existing checks.

Validation in this environment: native C++ syntax against pinned upstream llama
headers with platform stubs; C++ grammar-construction/range assertions; 69 existing
Kotlin policy tests. No Android APK build/link, upstream grammar-parser runtime,
or GGUF inference was available here. Device testing is required. The report must
say `Sampler: citation grammar + greedy` to confirm the new native path is active.

Device test: airplane mode, Wi-Fi off; run the same mass/weight comparison once.
Capture the answer and sampler/finish/timing report. Inspect every sentence's
reference and compare the actual claim with the supplied passage; syntax alone
is insufficient. If the grammar fails, capture the error without retrying.
