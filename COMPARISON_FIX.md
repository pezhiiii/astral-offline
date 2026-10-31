# 0.1.60-dev: comparison evidence selection

The previous gate required both subjects in one explicit contrast sentence. This version also accepts complete sentence-initial definitions from separate passages, provided definitions for BOTH parsed topics are available. It sends at most two passages to the local model. Qualifiers after a comma remain in the user question but no longer become mandatory subject keywords for contrast extraction.

This is a conservative lexical rule, not semantic verification. It supports explicit two-topic Compare X and Y / difference between X and Y forms. Definitions using other wording can still be missed. The 80-result-per-query candidate cap and corpus coverage remain limitations. No new dataset or bespoke evaporation/boiling answer was added.

Observed on the installed 0.1.59 build: the comparison was unanswered. Single-word lookup returned water-cycle mentions for evaporation and astronomical contexts for boiling. A local scan of the available science reference text found one sentence-initial evaporation definition and zero boiling definitions; the available foundations text had neither. These local files are not a complete inventory of the phone database.

Five Kotlin regression tests cover topic parsing, separate definitions, rejecting incidental mentions, direct contrast with a qualifier, and an unrelated topic pair. They are included for the existing GitHub Actions Gradle test step. They were NOT executed here: Android SDK, Gradle and Kotlin compiler are unavailable. This package is source only, not a built APK.

Apply at repository root and let Actions build/test. Preserve the signing secret and install as an update; never uninstall or clear data. The unchanged-source evaporation/boiling test is not guaranteed to pass after this update because adequate evidence may be absent. Do not count it as an unseen test after modifying the app.
