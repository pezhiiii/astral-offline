# Astral — offline Android research prototype

Astral searches user-provided offline text and an optional local knowledge pack, then can generate a source-cited answer using a user-provided GGUF model. This is a research prototype: answer quality, coverage, performance, and suitability for any bounty have not been established by systematic evaluation.

## What it does

- Runs on Android 9 or later, arm64; the manifest declares no INTERNET permission. Model and knowledge files must be obtained separately and transferred to the device.
- Imports an instruct GGUF model (up to 6 GB), UTF-8 text files (up to 5 MB each), and a compatible SQLite FTS4 knowledge pack. PDF and Word files must first be converted to plain text.
- Searches local passages using lexical matching and reranking. Quick source lookup displays excerpts without invoking the model.
- Generates answers with a local llama.cpp engine and displays the excerpt sent to the model. A source-passage view shows retrieved context. The citation check detects missing or out-of-range numbers, not factual support.
- Uses CPU inference. Source versions with the CPU-thread control offer 2, 4, or 6 threads; actual performance varies by device, model, temperature and question. The loaded model and compatible prompt prefix may be reused.
- Some supported numerical questions may be answered by a deterministic calculation without model inference; this is labeled in the output.

## What has been observed

A user-provided screenshot of version 0.1.21-dev shows a sourced ecosystem answer and a full retrieved-passage view on an Android phone. That run reported 22.1 seconds for one question. Another screenshot reported 19.8 seconds for the same question on the preceding version. These individual observations do not establish an average, a speed improvement, or performance on other devices and questions. Later source changes and GrapheneOS have not been independently verified here.

## Build

Requirements: JDK 17, Git, Python 3, Gradle 8.11.1, Android SDK platform 35, build-tools 35.0.0, NDK 27.2.12479018 and CMake 3.22.1. Configure `ANDROID_HOME` or `sdk.dir` in `local.properties`. Building downloads dependencies and requires network access.

```sh
python3 tools/bootstrap.py
gradle --no-daemon :app:assembleDebug :app:testDebugUnitTest
```

The debug signing configuration expects `.signing/astral-debug.keystore` with the configured alias and password. The GitHub Actions workflow restores this keystore from the `ASTRAL_DEBUG_KEYSTORE_BASE64` repository secret, builds, runs the included search-policy tests and checks the APK signer. Treat the secret and keystore as private. Installing over an existing app requires the same signing key and a compatible version code. Keep the installed app and its data until an update succeeds.

The workflow's result for the latest revision has not been checked here. A successful build and unit tests do not prove correct behavior on a phone.

## Use

1. Transfer a compatible instruct GGUF and a plain UTF-8 document or compatible FTS4 knowledge pack to the phone. Astral does not bundle these assets.
2. Import the model and sources using the buttons in the app. The pack is optional if you have imported text.
3. For an offline demonstration, enable airplane mode and verify Wi-Fi is also disabled. Ask a question covered by the local sources.
4. Use **Quick source lookup** for retrieval only, or **Research offline** for a locally generated answer. Inspect the evidence and **View source passages**. Opening a web link requires connectivity.

## Limits

- A source citation number is not proof that the answer is supported. The model can misunderstand, invent facts, follow instructions within source material, or fail to answer.
- Keyword search and short evidence excerpts can miss relevant passages. Coverage depends entirely on imported material and its snapshot date; offline knowledge is not automatically current.
- Importing a model or pack can require additional temporary storage. Model loading and SQLite searches may not stop immediately.
- No systematic answer-quality comparison, peak-RAM measurement, thermal or battery benchmark, or GrapheneOS validation is available here. A single successful example does not establish bounty eligibility.
- The included tests focus on search-policy behavior; they do not test the Android UI, model inference, full retrieval database, or all citation claims.

## Attribution

Astral source is MIT licensed. The build fetches llama.cpp at commit `6b9ba4f2e8c1436511956f0b3c309e2c9e642071`; its license is maintained upstream. Imported models and datasets retain their own licenses. Record exact model and dataset filenames, sources, revisions, licenses, checksums, and installed sizes before distributing or making claims about them.

## Submission evidence

See [submission readiness](docs/SUBMISSION_READINESS.md) and the [evaluation worksheet](docs/EVALUATION.csv). Version 0.1.59 adds a copyable device/storage report; it does not establish peak RAM or research quality.
