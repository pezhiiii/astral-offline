# 0.1.70 — inspect model and prompt delivery

Source update, not an APK. Apply over the existing repository and build with the existing signed workflow. Version code 71; version name 0.1.70-dev.

Investigation: engine.cpp already calls llama_model_chat_template and llama_chat_apply_template with system/user messages and uses greedy sampling. The screenshots alone do not establish a broken template, wrong model type or engine corruption. This release does not claim to fix citation compliance.

Adds model description, general.name, general.architecture, GGUF-default template fingerprint and sampler mode to successful-run metrics. The FNV-1a fingerprint is a stable diagnostic identifier, not an integrity/security checksum. Control characters in metadata are flattened. Prompt text and local model paths are not printed.

Checks that the formatted prompt contains the exact supplied instructions and question before tokenization/generation. Missing or modified content stops the run. Exact-text matching can conservatively reject legitimate templates that transform whitespace. It does not verify role semantics, correct tokenizer/template pairing, model training quality or instruction following.

Validation: native diagnostic helper assertions passed, including missing instructions, empty inputs, known FNV-1a vector and metadata sanitization. C++17 syntax check passed for engine.cpp using the pinned llama.cpp headers, with JNI and Android-log platform stubs. This is not an Android compilation or link test. No device inference or APK build performed here. Kotlin search/citation logic is unchanged from the preceding 69-test run.

After signed installation, run the previous question once and capture bottom metrics including Model description, Model name, Architecture, Chat template, Sampler and Prompt content. If the template-content check fails, capture its error instead. Do not repeatedly rerun for a favorable result. Keep the citation rejection gate enabled.

Standalone helper check:
c++ -std=c++17 -Wall -Wextra -Werror -I app/src/main/cpp app/src/test/cpp/PromptDiagnosticsTest.cpp -o /tmp/astral-prompt-check
/tmp/astral-prompt-check
