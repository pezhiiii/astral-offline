# 0.1.75: reject observed dangling punctuation and missing units

The latest 0.1.74 phone draft ended its first sentence with `and, [1] [2].`,
omitted newton, and was displayed as MODEL ANSWER. The Earth/Moon clause was
consistent with the supplied excerpt. Cold run: 66.3 s; 80 output tokens,
4.62 tok/s. This was an output-validation failure, not successful synthesis.

This release changes rejection policy only. It does not change the model,
prompt, decoder, grammar or generation budget. It does not improve generation
quality or repair an answer. The same draft will now fall back to quotations.

The dangling-ending check tolerates comma, semicolon or colon before the final
sentence punctuation and numeric references. Comparison questions requesting
units now check for recognizable unit names/symbols from the selected unit
sentences in the evidence. The small catalog covers kilogram/kg, newton/N,
metre/meter/m, second/s, kelvin/K, ampere/A and joule/J. No unrecognized unit is
inferred. This is lexical coverage only: it cannot verify that units are linked
to the right quantity, that a negation is correct, or that all claims follow.
Symbols are case-sensitive; names accept plural forms. The gate can overreject
valid alternative expressions. Single-subject unit coverage is not expanded by
this change. Existing uncertainty labelling remains.

Validation: 76 Kotlin policy tests passed, including the exact dangling-punctuation
pattern, omitted unit, named/symbol unit forms and no-unit/no-evidence controls.
No Android build or device inference run here. Native code unchanged from
0.1.74. This source update requires the existing full Android project.

For submission, retain failed and successful runs and report observed limits.
Do not claim benchmark parity, GrapheneOS validation, peak RAM, comprehensive
factual verification or guaranteed bounty eligibility based on these checks.
