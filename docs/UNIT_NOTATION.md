# 0.1.82

Subquestion prompts request full unit names. The unit citation validator conservatively rejects slash-plus-negative-exponent spellings such as m/s-1, m/s−1, m/s^-1 and m/s⁻¹. This prevents the observed bad alternative from passing; it does not prove the correctness of all other unit notation. Legitimate negative powers in denominators are also rejected by this conservative format rule; callers should spell out the unit. No model draft is rewritten.

Validation: the exact new JVM regex passed seven positive/negative cases executed with Java. The unchanged C++ grammar test passed. Kotlin regression cases were added but could not run; Android build and on-device generation remain unverified because the Kotlin/Gradle/Android toolchain is unavailable.

Build/install 0.1.82-dev. Repeat speed/velocity and inspect unit spelling, raw drafts and validation. A separate new-topic test is still required; this release does not establish broad answer quality or semantic source support.
