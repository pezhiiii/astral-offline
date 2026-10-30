package org.bl3.astral

/** Narrow decomposition only for two-topic measurement/unit questions. No factual answers. */
internal object ComparisonPlan {
 /** Keep original IDs; select a passage with the requested unit or scenario. */
 fun sourceIndices(question: String, part: Int, excerpts: List<String>): List<Int> {
  val topics=SearchPolicy.comparisonTopics(question)
  val selected=if(part in topics.indices && Regex("(?i)\\bunits?\\b").containsMatchIn(question))
   excerpts.indices.firstOrNull { SearchPolicy.unitExcerpt("SI unit of ${topics[part]}",excerpts[it])!=null }
  else if(part==topics.size) excerpts.indices.firstOrNull { "scenario" in SearchPolicy.comparisonFacets(question,excerpts[it]) }
  else null
  return selected?.let { listOf(it) } ?: excerpts.indices.toList()
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
  return tasks
 }
}
