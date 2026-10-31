# 0.1.74: space for complete sentences and conservative defect rejection

The 0.1.73 device run completed without the double-accept crash, but its draft
mixed Chinese into English, ended with `changes with`, omitted Earth/Moon, and
used punctuation inside a grammar word to join text. It passed reference syntax;
this is not evidence of good synthesis. Total 71.1s, 64 output tokens, 3.97 tok/s.

Changes:
- Up to 32 words per sentence (previously 18), up to 32 core characters per word.
- Commas/semicolons/colons/closing parentheses occur only at word ends, preventing
  `location,Weight` from fitting one grammar word. Apostrophes/hyphens remain.
- ASCII questions select ASCII output grammar. Other questions retain Unicode.
  This currently limits English output to transliterated/spelled-out notation;
  scientific Greek symbols and accented names cannot be emitted in that mode.
- Comparison prompt assigns first subject, second subject and scenario to three
  lines. Output ceiling 256 tokens; runtime and quality impact is not measured.
- Policy rejects unseen non-ASCII letters for ASCII questions, a short list of
  dangling English endings, missing comparison subjects and missing requested
  from/to location terms. Checks are lexical, can overreject valid paraphrases,
  and cannot prove factual support or general grammatical completeness.
- Rejected drafts remain diagnostic-only. No retries, automatic citation repair,
  or factual-verification claims. Token-limit rejection remains.

Validation: 73 Kotlin tests, including the observed mixed-script defect,
dangling ending, omitted scenario and a valid scoped comparison; native syntax
with platform stubs and grammar-construction assertions. Android build/link,
upstream grammar parser runtime, and GGUF inference not run here.

Device: install 0.1.74, same offline comparison once; capture answer and finish/
sampler/timing report, or error. Check every claim against its actual evidence.
