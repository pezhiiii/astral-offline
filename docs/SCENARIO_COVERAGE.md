# 0.1.79

Changes: direct concise subquestion instructions, explicit instruction to cover each scenario subject, grammar safety ceiling increased from 32 to 64 words, and per-scenario lexical subject/location omission rejection. Existing incomplete-ending checks and raw diagnostics are preserved. No generated answer is repaired or inserted.

The observed draft ended with an unfinished relative clause at the 32-word syntax ceiling. The higher ceiling allows room to finish; it does not guarantee better generation. Presence checks do not establish correct relationships or factual support.

Validation: C++ CitationGrammarTest compiled and passed. Kotlin regression cases added for missing subject, missing location, complete coverage, and non-scenario parts; not executed because Kotlin/Gradle/Android build tooling is unavailable. APK build and phone behavior remain unverified.

On device: build/install 0.1.79-dev, repeat the mass/weight question, inspect all three raw drafts and Validation results.
