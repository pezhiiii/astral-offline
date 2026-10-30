package org.bl3.astral

/** Narrow decomposition only for two-topic measurement/unit questions. No factual answers. */
internal object ComparisonPlan {
 /** Keep original IDs; select a passage with the requested unit or scenario. */
 fun sourceIndices(question: String, part: Int, excerpts: List<String>): List<Int> {
  val topics=SearchPolicy.comparisonTopics(question)
  if(part==topics.size && Regex("(?i)\\bdirection\\b").containsMatchIn(question)) {
   // Prefer a concise passage explicitly covering both subjects and the requested distinction.
   val focused=excerpts.indices.filter {
    Regex("(?i)\\bdirection\\b").containsMatchIn(excerpts[it]) &&
     SearchPolicy.missingComparisonTopics(question,excerpts[it]).isEmpty()
   }.minByOrNull { excerpts[it].length }
   return focused?.let { listOf(it) } ?: excerpts.indices.toList()
  }
  val selected=if(part in topics.indices && Regex("(?i)\\bunits?\\b").containsMatchIn(question))
   excerpts.indices.firstOrNull { SearchPolicy.unitExcerpt("SI unit of ${topics[part]}",excerpts[it])!=null }
  else if(part==topics.size) excerpts.indices.firstOrNull { "scenario" in SearchPolicy.comparisonFacets(question,excerpts[it]) }
  else null
  return selected?.let { listOf(it) } ?: excerpts.indices.toList()
 }
 /** Per-part lexical coverage, not semantic verification. */
 fun scenarioRejection(question: String, part: Int, answer: String): String? {
  val topics=SearchPolicy.comparisonTopics(question)
  if(topics.size!=2 || part!=topics.size) return null
  val missing=SearchPolicy.missingComparisonTopics(question,answer)
  if(missing.isNotEmpty()) return "The scenario answer omits a requested subject: "+missing.joinToString()
  if(Regex("(?i)\\bdirection\\b").containsMatchIn(question) &&
   !Regex("(?i)\\bdirection\\b").containsMatchIn(answer))
   return "The comparison answer omits the requested direction distinction."
  val scenario=Regex("(?i)from\\s+(.+?)\\s+to\\s+(?:the\\s+)?([\\p{L}]+)").find(question.substringBefore('?')) ?: return null
  val places=SearchPolicy.terms(scenario.groupValues[1])+SearchPolicy.terms(scenario.groupValues[2])
  val words=Regex("[\\p{L}]+").findAll(answer.lowercase()).map { it.value }.toSet()
  return if(places.any { it !in words }) "The scenario answer omits a requested location." else null
 }
 fun questions(question: String): List<String> {
  if(!SearchPolicy.isComparison(question)) return emptyList()
  val topics=SearchPolicy.comparisonTopics(question)
  if(topics.size!=2 || !Regex("(?i)\\b(measures?|units?)\\b").containsMatchIn(question)) return emptyList()
  val units=Regex("(?i)\\bunits?\\b").containsMatchIn(question)
  val tasks=topics.map { topic ->
   "What does $topic measure"+(if(units) ", and what is its SI unit" else "")+"? Answer only this part in one complete sentence, using the supplied sources and citations."
  }.toMutableList()
  val scenario=Regex("(?i)from\\s+(.+?)\\s+to\\s+(?:the\\s+)?([\\p{L}]+)").find(question.substringBefore('?'))
  if(scenario!=null) tasks.add("What happens to ${topics[0]} and ${topics[1]} when moving from ${scenario.groupValues[1]} to ${scenario.groupValues[2]}? Name both locations and explain the changes in one complete sentence, using the supplied sources and citations.")
  if(scenario==null) {
   if(Regex("(?i)\\bdirection\\b").containsMatchIn(question))
    tasks.add("How does direction distinguish ${topics[0]} and ${topics[1]}? Name both subjects and explain only the direction distinction in one short sentence with citations.")
   else return emptyList() // Preserve the original question when its remaining requests are not understood.
  }
  return tasks
 }
}
