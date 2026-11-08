package org.bl3.astral

internal object DirectDefinitionQuote {
 fun isBareDefinition(question: String, subject: String): Boolean =
  Regex("""(?i)^\s*(?:what\s+is|define)\s+(?:(?:a|an|the)\s+)?${Regex.escape(subject)}\s*[?.]?\s*$""")
   .matches(question)

 /** Only promote a complete, explicit definition already present in the passage. */
 fun directDefinitionQuote(text: String, subject: String): String? {
  val lead=Regex("""(?i)^(?:(?:a|an|the)\s+)?${Regex.escape(subject)}\s+(?:is\s+(?:an?|the|defined\s+as)|means|refers\s+to)\s+""")
  return SearchPolicy.definitionExcerpt(text,subject).lineSequence().firstOrNull { sentence ->
   val match=lead.find(sentence)
   match!=null && sentence.endsWith(".") &&
    !sentence.contains("...") && !sentence.contains("…") &&
    Regex("""[\p{L}\p{N}]{2,}""").containsMatchIn(sentence.substring(match.range.last+1))
  }
 }
}
