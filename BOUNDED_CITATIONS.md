# 0.1.72: bound citation productions and reject token-limit drafts

Observed on device in 0.1.71: grammar sampler active, 128 generated tokens,
no numeric references, token-limit finish, 82.4 seconds total. The previous body
and reference repetitions were unbounded. This allowed generation to keep
extending the first body until the token limit. The rejected draft itself was
not supplied in the screenshots, so its exact text has not been inspected.

The grammar now allows at most 18 space-delimited words per sentence, each
non-decimal word at most 24 grammar characters. Decimal components have at most
6 digits. Each sentence ends with 1–2 model-selected valid references and a
period. There are at most 3 lines. Prompt instructions match the word bounds.
The generation ceiling is 192 tokens; character/word bounds do NOT guarantee
completion within this tokenizer budget. Tokens can split words in many ways.

Any `Finish: token limit` result now fails the citation policy even when its
last visible sentence looks complete. The application passes the completion
flag from the native metrics' existing explicit finish field. No generated
text or citations are repaired or guessed. Citation support remains unverified;
forcing a reference does not make the claim true. Bounds may shorten or distort
content; factual quality and coverage must be checked separately on device.

The pinned llama.cpp grammars/README.md documents `{m,n}` repetition and warns
against long chains of optional repetitions; bounded repetition is used here.

Validation: 70 Kotlin policy tests, including rejection of a token-limited draft
with valid-looking citations; C++ grammar-construction assertions; native syntax
against pinned upstream headers with Android/JNI stubs. No APK build/link or
GGUF inference in this environment. The grammar parser was not executed here.

One phone test: airplane mode and Wi-Fi off, same mass/weight comparison.
Capture answer and metrics. Expect `Sampler: bounded citation grammar + greedy`.
A success requires end-of-answer finish, usable coverage, and actual supporting
passages for each claim; syntax or non-rejection alone is insufficient.
