package org.bl3.astral

/** Promote an explicit source list without editing or trusting the model draft. */
internal object EnumerationQuote {
 private val request=Regex("""(?i)^\s*what\s+are\s+(?:the\s+)?(?:main|major|primary|principal)\s+(.+?)\s*\?\s*$""")
 private val sentences=Regex("""(?<=[.!?])\s+""")
 private val namedList=Regex("""(?i)\b(?:known as|called|named)\s+[^.!?]+,\s*(?:and\s+)?[^.!?]+[.!?]$""")
 private val colonList=Regex("""(?i):\s*[^.!?]+,\s*(?:and\s+)?[^.!?]+[.!?]$""")

 fun find(question: String, excerpts: List<String>): Pair<Int,String>? {
  val subject=request.matchEntire(question)?.groupValues?.get(1) ?: return null
  val terms=SearchPolicy.tokens(subject)
  if(terms.isEmpty()) return null
  for((source,excerpt) in excerpts.withIndex()) {
   val lines=excerpt.trim().split(sentences)
   for((start,line) in lines.withIndex()) {
    if(!SearchPolicy.tokens(line).containsAll(terms)) continue
    for(end in start..minOf(start+2,lines.lastIndex)) {
     val tail=lines[end].trim()
     val explicitList=if(end==start) namedList.containsMatchIn(tail) || colonList.containsMatchIn(tail)
      else namedList.containsMatchIn(tail) && SearchPolicy.tokens(tail).any { it in terms }
     if(!explicitList || tail.endsWith("...")) continue
     val quote=lines.subList(start,end+1).joinToString(" ").trim()
     if(quote.startsWith("…") || quote.startsWith("...") || quote.contains("…") || quote.contains("...")) continue
     return (source+1) to quote
    }
   }
  }
  return null
 }
}
