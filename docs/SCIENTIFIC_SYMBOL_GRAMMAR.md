# 0.1.83

The citation grammar now permits a small scientific symbol set inside words in English mode: middle dot, superscript 1/2/3, superscript minus, mathematical minus and caret. The source spelling m·s−1 and notation m·s⁻¹ can now be expressed without replacing the dot with a slash. Broad non-English letter ranges remain unavailable in English mode. Word/token limits are unchanged.

This removes an observed representational limitation, not a proven sole cause of the model error. There is no runtime evidence yet that the model will choose correct symbols. Existing rejection of ambiguous slash-negative-exponent notation remains active. Drafts and evidence are not rewritten, and semantic source support remains unverified.

Validation: C++ grammar construction test compiled and passed for 1–4 sources, symbol presence and existing bounds. Seven JVM notation-validator regression cases passed again. These do not run the actual llama grammar parser or model. Kotlin/Android build and phone generation remain unverified; toolchain/model unavailable here.

Build/install 0.1.83-dev, repeat speed/velocity, and inspect part 2 raw output, validation and elapsed time. If output still uses m/s-1 it must remain rejected.
