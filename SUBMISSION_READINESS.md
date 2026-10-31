# Submission evidence — 0.1.59-dev

Status: preparation, not a verified bounty-compliance claim.

## Reproduce and identify the submitted build

Use README build prerequisites and the existing GitHub Actions workflow. Preserve the signing secret for updates; do not uninstall or clear data. Download the successful workflow's APK, record the public commit and APK SHA-256, and install over the existing app. Publish that exact APK and commit in a public release. A new contributor building independently must supply their own debug keystore; the private update signing key is not a reproducibility dependency.

For a fresh independent build only (never replace an existing updater key):

```sh
mkdir -p .signing
keytool -genkeypair -keystore .signing/astral-debug.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Astral local development"
```

Record the exact GGUF filename, upstream download URL, revision, license, SHA-256 and byte size. Do the same for every text/knowledge pack. Importing an instruct GGUF is necessary: the source patch does not include model weights or an APK. Test the published instructions from a fresh installation on a second device before claiming a few-minute setup.

## Device and storage evidence

Open Device & evidence after import and copy its report. Record idle and immediately-after-generation reports. These PSS readings are snapshots, NOT peak RAM measurements. Record the device's physical RAM and OS. Test on compatible GrapheneOS hardware separately; Android execution alone does not demonstrate that compatibility.

The report sums APKs, private data/cache, app-specific external data/cache and extracted native libraries. Add downloaded models/packs retained elsewhere on the phone. Logical bytes differ from allocated filesystem storage. Record temporary import/build copies separately. No measured values are filled in by the developer.

## Offline evidence

The current source manifest declares no INTERNET permission. Direct Gradle dependencies do not include Google Play Services. Inspect the final merged manifest/APK as part of release validation. Demonstrate airplane mode with Wi-Fi disabled, launch the installed app, and run several queries. Source inspection is not a packet capture.

## Observations already supplied by the tester

| Build / case | Total | Relevant context |
| --- | --- | --- |
| 0.1.57 mass, first run | 234.0 s | 4 threads; 184 input tokens evaluated; 88 output tokens |
| 0.1.57 same mass prompt repeated | 14.8 s | 2 threads; 183/184 input tokens cached; 88 output tokens |
| 0.1.58 weather/climate | 22.5 s | Model reused; 133/136 input tokens evaluated; 35 output tokens |

These are screenshot observations, not a controlled benchmark. Mass and weather/climate were development cases. They must not be counted as unseen test successes. The model was actually used in the 0.1.58 comparison run; older comparison quotation-only runs are not synthesis results.

## Evaluation before submission

Use EVALUATION.csv. Freeze the app, model, corpus and test questions before running. Do not add a bespoke answer source after seeing a failure and report the rerun as an unseen success. Mark missing corpus evidence explicitly. Preserve every result, including failures and cancellations.

Score each answer 0–4: 0 wrong/unanswered; 1 fragment with major errors; 2 partly useful but incomplete; 3 correct with minor omissions; 4 correct, useful, and fully supported. Separately score citation support, contradictions and runtime. A citation number alone does not prove support. A refusal can be correct when evidence is absent but does not demonstrate broad research coverage.

For a comparison to internet plus frontier AI, use identical questions and a declared reference system, preserve both complete outputs, and have an independent reviewer apply the same rubric. No current evidence establishes the >50% target. Claims that a question defeats a 1B model need an actual identified baseline run.

## Public proof and claim

Prepare a public X/Farcaster video with airplane/Wi-Fi state, multiple explanation/comparison/synthesis/reasoning queries, visible timings, source inspection and project link. Clearly label cuts and cached repeats. Provide the exact public repository/release and a short description: local lexical retrieval plus CPU GGUF inference, with deterministic handling of some numerical questions.

Before submitting to poidh, verify the public repository contains the functional release and all asset installation instructions. Attach the relevant screenshot plus public demo and repository links. Publication and a paid claim remain user actions; this document does not submit either.
