# 0.1.77: focused part evidence and lexical unit-citation support

0.1.76 covered the tested definitions, units and Moon comparison, but cited [1]
for the newton claim, although newton appeared only in excerpt [2]. The previous
syntax/coverage gates did not check that a mentioned unit occurred in the cited
passage. Total cold three-part run: 88.4s. This remains one tuned example, not a
research-quality benchmark.

For decomposed questions, pick the first supplied excerpt containing the topic's
requested SI-unit sentence; for the scenario, pick the first matching scenario
excerpt. Keep original IDs (e.g. [2] remains [2]). If no such excerpt is found,
fall back to all selected excerpts. There is no after-the-fact citation reassignment.
The part prompt is shorter and asks to preserve conditions and disclose absent
details. A narrow excerpt may omit a useful definition or nuance; this strategy
can reduce quality. Device validation is required.

Reject references to any ID withheld from a part. The native grammar still permits
all original numeric IDs; withholding is enforced after generation, not by a new
native grammar. For questions requesting units, check each nonempty output line:
recognized unit names/symbols must occur (allowing catalog aliases) in the union
of that line's cited excerpts. This catches newton [1] in the observed example.
The catalog is unchanged from 0.1.75. Line scope is a limitation: multiple claims
or sentences on one line can share sources, and mentioning the unit is not proof
that the stated relationship, quantity, negation, number or scope is supported.
No full entailment verification is claimed. All previous gates remain.

The phrase `as per [1]` is a valid way of referring to a source, though awkward
here. It is not rejected merely for that phrase. The new prompt asks to avoid
citation introductions; no blanket `per` rejection was added.

Validation: 82 Kotlin tests passed, including wrong/correct/combined unit
references, cross-line isolation, alias matching and reordered evidence IDs.
Native code unchanged. Android Activity integration reviewed, not built or run.
No Android APK/link or GGUF inference executed in this environment.

Phone test: install 0.1.77 and run the comparison once offline. Capture answer,
Supplied source IDs for each part and total elapsed. Check factual support and
coverage manually. Different part evidence may reduce prompt-cache reuse;
no speed improvement or successful synthesis is promised.
