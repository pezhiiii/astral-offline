# 0.1.61-dev: two-sided comparison prompting and omission warning

The 0.1.60 device run for mass versus weight completed in 24.3 seconds (127 input tokens evaluated, no cached input, 22 generated tokens, 6.22 generation tokens/second). Its evidence explicitly described weight as a force, but its answer mentioned only mass. It used one contrast passage; it did not validate the separate-passage path.

This patch asks the model to explain each topic and then the supported difference, including missing-evidence disclosure. A lexical post-check flags omitted comparison topics with INCOMPLETE COMPARISON. The original generated text and the evidence remain visible. No second generation, fabricated completion, new answer dataset or CPU change is introduced.

The omission check ignores standalone colon headings. Mentioning both topics does NOT prove factual support, a meaningful comparison, or complete coverage of requested details. Synonyms can trigger false warnings. This is not a semantic verifier. Prompt improvement has not been measured on the phone yet.

Two added Kotlin regression tests cover the observed one-sided output and a misleading heading. Android/Kotlin tests could not be executed in this workspace because the toolchain is unavailable. The existing Actions workflow must compile and run all tests before installation. ZIP integrity and manifest XML were checked locally.

Apply this cumulative source patch at the repository root. Build with Actions and install the signed APK over the existing app. Preserve the signing secret and app data. Re-run the mass/weight development case and inspect both the answer and the evidence; report it as a regression test, not an unseen benchmark.
