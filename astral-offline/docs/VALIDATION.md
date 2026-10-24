# Validation performed in authoring environment

- Python bootstrap script parsed successfully.
- Android manifest parsed; no uses-permission declarations present.
- CMake vendor path resolves to project-root vendor directory.
- Host SQLite FTS4 accepted the app schema, retrieved synthetic evidence and returned no rows for an absent term.
- JNI engine.cpp passed g++ -std=c++17 -fsyntax-only against the exact pinned llama.cpp headers and OpenJDK 17 JNI headers.

These are limited source checks. Kotlin/Gradle compilation, CMake linking, APK install, actual model loading/inference, cancellation on device and real-device resource use were not tested. No claim of a working APK is made.
