# Real-device acceptance record — NOT YET RUN

Record the app git commit and APK SHA-256; device model; physical RAM; OS build; model filename/revision/hash/license; corpus manifest; total installation size.

- Fresh install from documented steps, with timings.
- Airplane mode + Wi-Fi off. Verify `adb shell dumpsys package org.bl3.astral` lists no INTERNET permission.
- Import malformed GGUF, truncated GGUF, oversized text and ordinary UTF-8 multilingual text; record behavior.
- Run known-answer tests, synthesis from multiple source documents, missing-evidence questions and previously unseen questions.
- Record raw responses, wall time, first-visible-answer time, `adb shell dumpsys meminfo org.bl3.astral` throughout generation, thermal behavior over 20 questions and crashes. Snapshot RSS is not guaranteed peak memory.
- Cancel during prefill and generation; rotate, background and reopen; replace model after answering.
- Check model + database + APK + all assets together stay below 50 GB, and whole-device environment is <=12 GB RAM.
- Reproduce on supported Pixel / GrapheneOS hardware.
- Compare blind against web + frontier answers with a published scoring rubric and holdout questions. Report failures.

Do not submit a claim until functional version is public, device proof exists and requirements are met.
