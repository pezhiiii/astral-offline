# 0.1.65-dev — grammatical terms in why questions

Observed 0.1.64: a mass-versus-weight run completed in 27.1 seconds with a valid reference but added an unsupported definition. A follow-up why-Moon-weight question returned no evidence despite an earlier screenshot showing a directly relevant sentence in the installed source.

Code cause: acceptsSubject required every non-stopword in the question, including `keeping`, to occur in title/body. The visible source instead says `would still have the same mass`. This patch ignores while/keeping/maintaining/remaining for sentence-level why-question evidence matching. It retains the other content terms, requires them together in a complete sentence, and sends that sentence verbatim (up to 90 words) instead of a generic word window. It does not substitute weigh/weight as synonyms: the known source already contains weigh.

Limitations: lexical matching remains conservative, cannot handle general paraphrases or multi-passage reasoning, and may reject evidence split across sentences. Ignoring continuation verbs can change relevance. Keeping all subject words in one sentence is not proof that it answers why. Negative source statements are preserved verbatim. No topic-specific answer text or dataset has been added; factual-grounding limitations from earlier versions remain.

Four Kotlin tests cover the observed evidence, incidental mentions, negative wording, and a second topic. They have NOT run locally: Android/Kotlin build tools are unavailable. Run Actions compile/tests before installing. Local validation covers manifest and archive integrity only.

Cumulative SOURCE patch; no APK or model included. Apply at repo root, preserve the existing signing key, build/test in Actions and install over the app without uninstalling/clearing data. Re-run the failed Moon question as a regression test, preserving answer, sent evidence and timing. A passed retrieval test would not establish broad bounty quality.
