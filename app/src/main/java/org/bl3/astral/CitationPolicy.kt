package org.bl3.astral

/** Checks reference syntax/range only. It cannot establish factual entailment. */
internal object CitationPolicy {
 fun rejectionReason(answer: String, sourceCount: Int): String? {
  if(answer.isBlank()) return "The model returned an empty answer."
  val refs=Regex("\\[(\\d+)\\]").findAll(answer).map { it.groupValues[1].toIntOrNull() }.toList()
  if(refs.isEmpty()) return "The model supplied no numeric source references."
  if(refs.any { it==null || it<1 || it>sourceCount }) return "The model cited a source number that was not supplied."
  return null
 }
 /** Placement only: a citation beside a sentence does not prove its truth. */
 fun hasUncitedSentence(answer: String): Boolean {
  val ref=Regex("\\[\\d+\\]")
  var start=0
  var i=0
  while(i<answer.length) {
   val end=answer[i] in ".!?" && (i+1==answer.length || answer[i+1].isWhitespace())
   if(end) {
    val sentence=answer.substring(start,i+1).trim()
    var next=i+1
    while(next<answer.length && answer[next].isWhitespace()) next++
    val trailing=ref.find(answer,next)?.takeIf { it.range.first==next }
    val before=Regex("\\[\\d+\\]\\s*[.!?]$").containsMatchIn(sentence)
    if(sentence.any { it.isLetter() } && !before && trailing==null) return true
    if(trailing!=null) {
     next=trailing.range.last+1
     while(true) {
      while(next<answer.length && answer[next].isWhitespace()) next++
      val extra=ref.find(answer,next)?.takeIf { it.range.first==next } ?: break
      next=extra.range.last+1
     }
    }
    start=next;i=next
   } else i++
  }
  val tail=answer.substring(start).trim()
  return tail.any { it.isLetter() } && !Regex("\\[\\d+\\]\\s*$").containsMatchIn(tail)
 }
 fun evidenceRejectionReason(answer: String, excerpts: List<String>, completed: Boolean = true, question: String = "", supplementaryQuotes: List<String> = emptyList()): String? {
  if(!completed) return "Generation reached the token limit; the draft is incomplete."
  rejectionReason(answer,excerpts.size)?.let { return it }
  if(hasUncitedSentence(answer)) return "Each answer sentence must have an adjacent numeric source reference."
  qualityRejectionReason(question,answer,excerpts,supplementaryQuotes)?.let { return it }
  if(Regex("(?i)\\bunits?\\b").containsMatchIn(question)) unitCitationRejection(answer,excerpts)?.let { return it }
  val plain=answer.replace(Regex("\\[\\d+\\]"),"")
  val evidence=excerpts.joinToString(" ")
  val number=Regex("(?<![\\p{L}\\p{N}])\\d+(?:[.,]\\d+)*%?")
  val known=number.findAll(evidence).map { it.value }.toSet()
  if(number.findAll(plain).any { it.value !in known }) return "The model introduced a number absent from the supplied excerpts."
  if(Regex("[=×÷]").containsMatchIn(plain) && !Regex("[=×÷]").containsMatchIn(evidence))
   return "The model introduced a formula absent from the supplied excerpts."
  return null
 }
 // Limited lexical unit coverage; neither quantity association nor correctness is proven.
 private val unitPatterns=listOf(
  "kilogram" to Regex("(?i:\\bkilograms?\\b)|\\bkg\\b"),
  "newton" to Regex("(?i:\\bnewtons?\\b)|\\bN\\b"),
  "metre" to Regex("(?i:\\b(?:metres?|meters?)\\b)|\\bm\\b"),
  "second" to Regex("(?i:\\bseconds?\\b)|\\bs\\b"),
  "kelvin" to Regex("(?i:\\bkelvins?\\b)|\\bK\\b"),
  "ampere" to Regex("(?i:\\bamperes?\\b)|\\bA\\b"),
  "joule" to Regex("(?i:\\bjoules?\\b)|\\bJ\\b")
 )
 /** Lexical support for known unit mentions in each generated line, not entailment. */
 fun missingUnitRejection(answer: String, unitEvidence: String, topic: String): String? {
  for((name,pattern) in unitPatterns) {
   if(pattern.containsMatchIn(unitEvidence) && !pattern.containsMatchIn(answer))
    return "The answer omits the requested unit for $topic ($name)."
  }
  return null
 }
 fun unitCitationRejection(answer: String, excerpts: List<String>): String? {
  for(line in answer.lines().filter { it.isNotBlank() }) {
   val ids=Regex("\\[(\\d+)\\]").findAll(line).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()
   val cited=ids.mapNotNull { excerpts.getOrNull(it-1) }.joinToString(" ")
   val text=line.replace(Regex("\\[\\d+\\]"),"")
   if(Regex("\\b[A-Za-z]+\\s*/\\s*[A-Za-z]+\\s*(?:\\^\\s*)?[-−⁻]\\s*[0-9¹²³]+").containsMatchIn(text))
    return "The answer uses ambiguous slash/negative-exponent unit notation; use a full unit name."
   // Conservative lexical support for a common squared-time compound unit.
   // No dimensional correctness or quantity association is inferred.
   val squared=Regex("(?i)(?:\\bm\\s*/\\s*s\\s*(?:\\^\\s*2|2|²)|\\b(?:metres?|meters?) per second squared\\b)")
   if(squared.containsMatchIn(text) && !squared.containsMatchIn(cited))
    return "The cited source does not supply the stated squared-time unit."
   for((name,pattern) in unitPatterns) {
    if(pattern.containsMatchIn(text) && !pattern.containsMatchIn(cited))
     return "A cited source does not contain the stated unit ($name) on this answer line."
   }
  }
  return null
 }
 /** Observable output defects only; this is not an entailment checker. */
 fun qualityRejectionReason(question: String, answer: String, excerpts: List<String>, supplementaryQuotes: List<String> = emptyList()): String? {
  if(Regex("(\\[\\d+\\])(?:\\s*\\1)+").containsMatchIn(answer))
   return "The answer repeats an adjacent source reference."
  if(question.isBlank()) return null
  if(question.all { it.code<128 } && answer.any { it.isLetter() && it.code>=128 } &&
   answer.filter { it.isLetter() && it.code>=128 }.any { it !in excerpts.joinToString(" ") })
   return "The English answer introduced letters absent from the question and evidence."
  val clean=answer.replace(Regex("\\[\\d+\\]"),"")
  if(Regex("(?i)\\b(with|of|to|from|because|and|or|while|than|due|in|on|at|by|for|is|are|the|an|a|being)\\s*[,;:]*\\s*[.!?](?:\\s|$)").containsMatchIn(clean))
   return "A sentence ends with an unfinished phrase."
  if(SearchPolicy.unitTopics(question).isNotEmpty() && clean.trim().split(Regex("\\s+")).size>32)
   return "A unit-only answer must be at most 32 words."
  if(SearchPolicy.isComparison(question)) {
   if(SearchPolicy.missingComparisonTopics(question,answer).isNotEmpty())
    return "The answer omits a requested comparison subject."
   if(Regex("(?i)\\bunits?\\b").containsMatchIn(question)) {
    val evidence=excerpts.joinToString("\n")
    for(topic in SearchPolicy.comparisonTopics(question)) {
     val unitEvidence=SearchPolicy.unitExcerpt("SI unit of $topic",evidence) ?: continue
     for((name,pattern) in unitPatterns) {
      if(pattern.containsMatchIn(unitEvidence) && !pattern.containsMatchIn(clean))
       return "The answer omits the requested unit for $topic ($name)."
     }
    }
   }
   val scenario=Regex("(?i)from\\s+(.+?)\\s+to\\s+(?:the\\s+)?([\\p{L}]+)").find(question.substringBefore('?'))
   if(scenario!=null) {
    val places=SearchPolicy.terms(scenario.groupValues[1])+SearchPolicy.terms(scenario.groupValues[2])
    // Only exact text from its declared source may supplement location coverage.
    val quoted=supplementaryQuotes.mapNotNull { quote ->
     val match=Regex("^\\[(\\d+)\\]\\s+([\\s\\S]+)$").matchEntire(quote.trim()) ?: return@mapNotNull null
     val source=excerpts.getOrNull((match.groupValues[1].toIntOrNull() ?: 0)-1) ?: return@mapNotNull null
     match.groupValues[2].takeIf { it.isNotBlank() && source.contains(it) }
    }.joinToString(" ")
    val words=Regex("[\\p{L}]+").findAll((answer+" "+quoted).lowercase()).map { it.value }.toSet()
    if(places.any { it !in words }) return "The answer omits a location in the requested comparison."
   }
  }
  return null
 }
 fun present(answer: String, excerpts: List<String>, completed: Boolean = true, question: String = "", extraRejection: String? = null, supplementaryQuotes: List<String> = emptyList()): String {
  val reason=extraRejection ?: evidenceRejectionReason(answer,excerpts,completed,question,supplementaryQuotes) ?: return "MODEL ANSWER — source IDs checked; factual support has not been verified.\n\n$answer"
  val quotes=excerpts.mapIndexed { i,text -> "[${i+1}] $text" }.joinToString("\n\n")
  return "MODEL ANSWER REJECTED — $reason\nSOURCE QUOTATIONS ONLY — these are the supplied excerpts, not a generated or repaired answer. Coverage may be partial.\n\n$quotes"
 }
}
