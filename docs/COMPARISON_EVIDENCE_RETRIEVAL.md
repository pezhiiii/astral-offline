# 0.1.84

Two-topic comparison retrieval now issues separate topic/unit and topic/definition queries before broad topic queries. This gives later unit passages their own 80-result query budget. Generic importance/use statements are excluded from the definition selector. Before model synthesis, requested definition and SI-unit facets must be present for each topic in the actual excerpts; otherwise the app reports missing facets and source passages without generating an answer.

This is conservative lexical coverage, not semantic fact checking. Valid paraphrases may be missed. The existing four-source cap and first-80 limit per query remain; the fix does not guarantee all necessary passages will be retrieved. It adds no physics facts and does not change source text or repair model drafts. Evidence packs lacking temperature definition/unit content will still require additional sources.

Validation: SQLite FTS4 fixture found a unit passage after 100 generic topic passages using the new targeted query shape. Existing C++ grammar construction and seven JVM notation checks passed. Kotlin regression added for the observed heat/temperature missing-facet case and complete evidence. Kotlin tests, APK build and device generation remain unverified because their toolchain is unavailable.

Build/install 0.1.84-dev and repeat heat/temperature. Capture either the generated answer and cited sources or INSUFFICIENT COMPARISON EVIDENCE with its missing facets. Correct abstention does not count as a successful answer.
