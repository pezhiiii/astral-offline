# 0.1.76: decomposed measurement comparisons

The 0.1.75 phone run correctly rejected the previous malformed output; synthesis
still combined both definitions into a sentence that ended `and,` and omitted
the weight unit. This release changes generation strategy, not just rejection.

For a recognized two-topic comparison mentioning measurement/units, generate
separate subquestions: first subject, second subject, and optional from/to
scenario. Topic names and places come from the query parser, not hardcoded
physics answers. Each call sees the same selected evidence and original source
numbering. The system prompt asks for one complete sentence per call; each call
has a 96-token ceiling. The existing grammar is unchanged (it permits up to three
lines, so one sentence is requested, not structurally guaranteed).

Concatenate the raw drafts with newlines; no model rewrite, reference renumbering,
reference repair, or automatic retry. Validate each draft, then the combined
answer against the original question. A failed part rejects the combined answer
and displays the existing source-quotation fallback. Diagnostics retain all
part requests, drafts and timing reports. Cancellation is checked between parts
and is still available within native evaluation. An engine exception aborts.

All parts are attempted even if an earlier part fails, to obtain one complete
set of diagnostics; this can cost additional time. Same-prefix cache reuse may
help but is not guaranteed if context sizes change. No latency improvement is
claimed. Other questions retain the existing single-call behavior. Parsing is
narrow and English-oriented; it does not cover arbitrary multi-part research.
Source retrieval/excerpts, grammar, model, and rejection policy remain as before.
Factual entailment is not verified. A valid citation does not prove the claim.

Validation: 79 Kotlin tests passed: decomposition, other topics, unsupported
question fallback, stable topic/place requests with no fabricated facts, and
propagation of a failed part despite valid-looking aggregate citations. Android
Activity/JNI integration was inspected, not compiled or run here. No APK build,
Android linking or GGUF inference in this environment; native code unchanged.

Device: install 0.1.76, run the same comparison once offline. Capture the final
answer or rejection and PART 1/2/3 finish/timing reports; a single run now invokes
up to three model calls. Use total Elapsed for end-to-end latency, not an
individual part's First token or cached evaluation time.
