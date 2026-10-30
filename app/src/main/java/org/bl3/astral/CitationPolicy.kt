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
 fun evidenceRejectionReason(answer: String, excerpts: List<String>): String? {
  rejectionReason(answer,excerpts.size)?.let { return it }
  if(hasUncitedSentence(answer)) return "Each answer sentence must have an adjacent numeric source reference."
  val plain=answer.replace(Regex("\\[\\d+\\]"),"")
  val evidence=excerpts.joinToString(" ")
  val number=Regex("(?<![\\p{L}\\p{N}])\\d+(?:[.,]\\d+)*%?")
  val known=number.findAll(evidence).map { it.value }.toSet()
  if(number.findAll(plain).any { it.value !in known }) return "The model introduced a number absent from the supplied excerpts."
  if(Regex("[=×÷]").containsMatchIn(plain) && !Regex("[=×÷]").containsMatchIn(evidence))
   return "The model introduced a formula absent from the supplied excerpts."
  return null
 }
 fun present(answer: String, excerpts: List<String>): String {
  val reason=evidenceRejectionReason(answer,excerpts) ?: return "MODEL ANSWER — source IDs checked; factual support has not been verified.\n\n$answer"
  val quotes=excerpts.mapIndexed { i,text -> "[${i+1}] $text" }.joinToString("\n\n")
  return "MODEL ANSWER REJECTED — $reason\nSOURCE QUOTATIONS ONLY — these are the supplied excerpts, not a generated or repaired answer. Coverage may be partial.\n\n$quotes"
 }
}
