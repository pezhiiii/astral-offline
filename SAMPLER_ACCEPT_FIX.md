# 0.1.73: accept generated tokens exactly once

The 0.1.72 phone run failed with an unexpected empty grammar stack after
accepting the piece `kilograms`. The native generation loop called
llama_sampler_sample, then llama_sampler_accept on the same sampled token.

The pinned upstream llama.h contract documents llama_sampler_sample as applying
the sampler, selecting a token, calling llama_sampler_accept and returning it
(lines 1639–1646 in the pinned header). The second explicit accept incorrectly
advanced stateful grammar samplers twice. It was harmless for stateless greedy
sampling but invalid with the grammar chain. It could also explain premature
word-bound exhaustion in 0.1.72. The unbounded productions in 0.1.71 were another
independent defect; the screenshots do not establish their relative effects.

Remove only the redundant explicit accept. Keep bounded grammar, greedy
selection, citation checks and token-limit rejection unchanged. No citation
repair or relaxation. Model truth/coverage is still unverified.

Validation: C++ syntax check against pinned upstream llama headers with platform
stubs; grammar-construction assertions. The existing 70 Kotlin policy tests
previously passed in 0.1.72; no Kotlin logic changed here. No Android build/link
or on-device GGUF inference was run in this environment. Source contract review
establishes the double-accept bug but does not guarantee the full device run.

Install 0.1.73, run the same comparison once offline, capture answer and metrics
(or the error if it fails). The sampler label remains bounded citation grammar
+ greedy. Check the installed version to distinguish this native fix.
