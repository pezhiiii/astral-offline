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
 fun evidenceRejectionReason(answer: String, excerpts: List<String>): String? {
  rejectionReason(answer,excerpts.size)?.let { return it }
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
