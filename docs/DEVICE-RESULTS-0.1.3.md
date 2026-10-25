# User device observations, 25 October 2026

Device: POCO X4 Pro 5G. Model: Qwen2.5-3B-Instruct Q4_K_M.
Evidence: one short memory.txt passage. Network isolation was not established
by these screenshots. Screen labels alone do not prove offline operation.

| Question | Process/run | Model | Input evaluation | First token | Total |
| --- | --- | --- | --- | --- | --- |
| RAM vs storage | 22263 / 1 | loaded | 148 tokens / 46.6 s | 51.1 s | 71.3 s |
| Same question | 22263 / 2 | reused | 148 tokens / 49.2 s | 49.2 s | 68.4 s |
| Storage fixes restarting apps? | 13193 / 1 | loaded | 165 tokens / 121.9 s | 129.3 s | 182.3 s |

Model reuse is confirmed for the second run, but input evaluation remains the
main delay. The third run was in a new process; screenshots do not establish why
it restarted or why processing slowed. Temperature and background load were not
controlled. These questions cannot establish general research quality.

The third answer correctly says storage does not necessarily fix restarting
apps, but then attributes the problem to RAM without enough source evidence.
Both displayed answers omit inline citations, despite displaying local evidence
separately. These are unresolved quality problems, not passing research tests.
