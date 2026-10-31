# 0.1.68 — operational definitions and citation placement

Source update package; apply over the existing repository and use the existing signed build workflow. Version code 69; version name 0.1.68-dev. No APK included.

Definition selection now recognizes "can be experimentally defined" and "can be understood" and prefers existing sentences describing measurement, resistance or inertia. Sentences remain verbatim. This is a generic lexical ranking heuristic, not scientific fact checking; poor imported source definitions can still be selected when no better definition is present.

The prompt requests one claim per line with an adjacent citation. The citation policy rejects drafts with uncited sentences, including paragraphs with references collected only after their final sentence. Existing rejection behavior displays supplied source quotations without inventing citations or repairing the model answer. Rejected drafts remain diagnostic-only in the source view.

Placement checks accept citations before or immediately after terminal punctuation, multiple adjacent references, decimal values and a cited final sentence lacking punctuation. This is a lightweight English sentence-boundary heuristic; abbreviations and unusual punctuation can produce conservative rejection. Missing numeric references and out-of-range references are still rejected. Adjacent citations do not establish entailment or source quality.

Validation: Kotlin 1.9.24 compilation; all 68 JUnit tests passed. Added regressions for operational definition preference, no invented replacement definition, collected references, prefix-only references, adjacent references, decimal values and uncited tails. Android build, installed SQLite retrieval, local-model compliance and phone timing remain untested here.

Retest Research offline with airplane mode and Wi-Fi off:
Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources.

Inspect the actual excerpts for an inertia definition, both units and the Earth/Moon statement. Check that every generated sentence has an adjacent citation. If the model fails placement, quotation-only output is expected and should be recorded as a generation failure, not a successful generated answer.
