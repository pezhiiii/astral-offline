package org.bl3.astral

/** Narrow decomposition only for two-topic measurement/unit questions. No factual answers. */
internal object ComparisonPlan {
 private fun definitionDistinction(question: String, part: Int): Boolean =
  part==2 && SearchPolicy.comparisonTopics(question).size==2 &&
   Regex("(?i)\\b(differ|difference|differences|distinguish)\\b").containsMatchIn(question) &&
   !Regex("(?i)\\bdirection\\b|\\bfrom\\s+.+\\s+to\\b").containsMatchIn(question)
 private fun distinctionDefinitions(question: String, excerpts: List<String>): List<Int> =
  SearchPolicy.comparisonTopics(question).mapNotNull { topic ->
   excerpts.indices.filter { SearchPolicy.comparisonDefinitions(question,excerpts[it]).containsKey(topic) }
    .maxByOrNull { SearchPolicy.comparisonDefinitions(question,excerpts[it])[topic]?.length ?: 0 }
  }.distinct()
 /** Select complete location-bearing sentences without changing source wording. */
 private fun scenarioExcerpt(question: String, part: Int, text: String): String? {
  if(part!=2 || SearchPolicy.comparisonTopics(question).size!=2) return null
  val scenario=Regex("(?i)from\\s+(.+?)\\s+to\\s+(?:the\\s+)?([\\p{L}]+)").find(question.substringBefore('?')) ?: return null
  val places=SearchPolicy.terms(scenario.groupValues[1])+SearchPolicy.terms(scenario.groupValues[2])
  return text.split(Regex("(?<=[.!?])\\s+|\\n+"))
   .filter { sentence ->
    val words=SearchPolicy.tokens(sentence)
    places.all { it in words } &&
     Regex("[.!?]$").containsMatchIn(sentence.trim())
   }.joinToString("\n").ifBlank { null }
 }
 /** Verbatim sentence selections; the original excerpts remain available for review. */
 fun partExcerpt(question: String, part: Int, text: String): String {
  scenarioExcerpt(question,part,text)?.let { return it }
  if(definitionDistinction(question,part))
   return SearchPolicy.comparisonDefinitions(question,text).values.distinct().joinToString("\n").ifBlank { text }
  val topic=SearchPolicy.comparisonTopics(question).getOrNull(part) ?: return text
  val definition=SearchPolicy.comparisonDefinitions(question,text)[topic]
  val unit=SearchPolicy.unitExcerpt("SI unit of $topic",text)
  val unitSentence=unit?.split(Regex("(?<=[.!?])\\s+|\\n+"))?.lastOrNull {
   Regex("(?i)\\bunits?\\b").containsMatchIn(it) &&
    ("si" in SearchPolicy.tokens(it) || "international system" in it.lowercase())
  }
  return listOfNotNull(definition,unitSentence).distinct().joinToString("\n").ifBlank { text }
 }
 /** Lexical source roles for the current subject; these are not factual validation. */
 fun citationGuidance(question: String, part: Int, excerpts: List<String>): String {
  if(part==2 && excerpts.any { scenarioExcerpt(question,part,it)!=null })
   return "State only how both subjects change between the two locations, in at most 25 words. Do not repeat definitions or units. End directly with the supporting numeric citation."
  if(definitionDistinction(question,part)) {
   val refs=distinctionDefinitions(question,excerpts).joinToString(" ") { "[${it+1}]" }
   return "Compare the supplied definitions directly. When combining them, cite their source IDs: $refs. Write no prose about evidence or sources."
  }
  val topic=SearchPolicy.comparisonTopics(question).getOrNull(part) ?: return ""
  val selected=sourceIndices(question,part,excerpts)
  val definition=selected.firstOrNull { SearchPolicy.comparisonDefinitions(question,excerpts[it]).containsKey(topic) }
  val unit=selected.firstOrNull { SearchPolicy.unitExcerpt("SI unit of $topic",excerpts[it])!=null }
  val roles=listOfNotNull(
   definition?.let { "Definition evidence for $topic: [${it+1}]." },
   unit?.let { "SI unit evidence for $topic: [${it+1}]." }
  ).joinToString(" ")
  val refs=listOfNotNull(definition,unit).distinct().joinToString(" ") { "[${it+1}]" }
  return roles+if(definition!=null && unit!=null)
   " If your sentence combines this definition and unit, end it with $refs. Cite each distinct ID once. Do not use the definition reference alone for the unit."
   else " Cite only evidence supporting the facts you state."
 }
 /** Keep original IDs; select a passage with the requested unit or scenario. */
 fun sourceIndices(question: String, part: Int, excerpts: List<String>): List<Int> {
  val topics=SearchPolicy.comparisonTopics(question)
  if(definitionDistinction(question,part)) {
   val selected=distinctionDefinitions(question,excerpts)
   if(selected.isNotEmpty()) return selected
  }
  if(part==topics.size && Regex("(?i)\\bdirection\\b").containsMatchIn(question)) {
   // Prefer a concise passage explicitly covering both subjects and the requested distinction.
   val focused=excerpts.indices.filter {
    Regex("(?i)\\bdirection\\b").containsMatchIn(excerpts[it]) &&
     SearchPolicy.missingComparisonTopics(question,excerpts[it]).isEmpty()
   }.minByOrNull { excerpts[it].length }
   return focused?.let { listOf(it) } ?: excerpts.indices.toList()
  }
  if(part in topics.indices) {
   val topic=topics[part]
   val unit=excerpts.indices.firstOrNull { SearchPolicy.unitExcerpt("SI unit of $topic",excerpts[it])!=null }
   val definition=if(unit!=null && SearchPolicy.comparisonDefinitions(question,excerpts[unit]).containsKey(topic)) unit
    else excerpts.indices.firstOrNull { SearchPolicy.comparisonDefinitions(question,excerpts[it]).containsKey(topic) }
   val selected=listOfNotNull(definition,unit).distinct()
   if(selected.isNotEmpty()) return selected
  }
  val selected=if(part in topics.indices && Regex("(?i)\\bunits?\\b").containsMatchIn(question))
   excerpts.indices.firstOrNull { SearchPolicy.unitExcerpt("SI unit of ${topics[part]}",excerpts[it])!=null }
  else if(part==topics.size) excerpts.indices.firstOrNull { "scenario" in SearchPolicy.comparisonFacets(question,excerpts[it]) }
  else null
  return selected?.let { listOf(it) } ?: excerpts.indices.toList()
 }
 /** Per-part lexical coverage, not semantic verification. */
 fun partRejection(question: String, part: Int, answer: String, excerpts: List<String>): String? {
  if(answer.lines().count { it.isNotBlank() }!=1)
   return "Each comparison part must contain exactly one answer line."
  val plain=answer.replace(Regex("\\[\\d+\\]"),"").trim()
  if(plain.split(Regex("\\s+")).size>40)
   return "Each comparison part must contain at most 40 words."
  if(Regex("(?im)^\\s*(sources?|references?|bibliography)\\s*:").containsMatchIn(answer))
   return "A separate source list is not an answer sentence."
  val topic=SearchPolicy.comparisonTopics(question).getOrNull(part)
   ?: return scenarioRejection(question,part,answer)
  if(!SearchPolicy.tokens(topic).all { it in SearchPolicy.tokens(answer) })
   return "The answer omits the requested subject: $topic."
  if(Regex("(?i)\\bunits?\\b").containsMatchIn(question)) {
   val unitEvidence=sourceIndices(question,part,excerpts).firstNotNullOfOrNull {
    SearchPolicy.unitExcerpt("SI unit of $topic",excerpts[it])
   }
   if(unitEvidence!=null) CitationPolicy.missingUnitRejection(answer,unitEvidence,topic)?.let { return it }
  }
  return null
 }
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
   (if(Regex("(?i)\\b(means?|define|definition)\\b").containsMatchIn(question)) "What does $topic mean" else "What does $topic measure")+(if(units) ", and what is its SI unit" else "")+"? Answer only this part in one complete sentence, using the supplied sources and citations."
  }.toMutableList()
  val scenario=Regex("(?i)from\\s+(.+?)\\s+to\\s+(?:the\\s+)?([\\p{L}]+)").find(question.substringBefore('?'))
  if(scenario!=null) tasks.add("What happens to ${topics[0]} and ${topics[1]} when moving from ${scenario.groupValues[1]} to ${scenario.groupValues[2]}? Name both locations and explain the changes in one complete sentence, using the supplied sources and citations. Use at most 25 words. Do not repeat definitions or units.")
  if(scenario==null) {
   if(Regex("(?i)\\bdirection\\b").containsMatchIn(question))
    tasks.add("How does direction distinguish ${topics[0]} and ${topics[1]}? Name both subjects and explain only the direction distinction in one short sentence with citations.")
   else if(Regex("(?i)\\b(differ|difference|differences|distinguish)\\b").containsMatchIn(question))
    tasks.add("How do ${topics[0]} and ${topics[1]} differ? Name both subjects and explain only their defining distinction in one short sentence with citations. Do not repeat their units.")
   else return emptyList() // Preserve the original question when its remaining requests are not understood.
  }
  return tasks
 }
}
