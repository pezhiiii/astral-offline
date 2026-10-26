package org.bl3.astral

import java.math.BigDecimal

/** Conservative English runtime/weight calculator. null means unsupported, not verified. */
object NumericEvidence {
 data class Source(val text: String)
 private data class Facts(val name: String, val hours: BigDecimal, val grams: BigDecimal?, val citation: Int)
 private val name = "([A-Za-z][A-Za-z0-9_-]{0,39})"
 private val number = "([0-9]{1,6}(?:\\.[0-9]{1,3})?)"
 private val runtime = Regex("$name runs for $number hours? per charge\\.", RegexOption.IGNORE_CASE)
 private val weight = Regex("$name weighs $number grams?\\.", RegexOption.IGNORE_CASE)
 private val offline = Regex("$name works offline\\.", RegexOption.IGNORE_CASE)
 private val trip = Regex("For a $number-hour trip without charging, does either $name or $name meet the requirement\\? Cite both sources\\.", RegexOption.IGNORE_CASE)
 private val lighter = Regex("For a $number-hour trip, which is lighter: $name or $name, and by how many grams\\? Cite both sources\\.", RegexOption.IGNORE_CASE)
 private val compare = Regex("Compare $name and $name for a trip requiring at least $number hours of battery life without charging\\. Which meets the requirement, and what is the weight trade-off\\? Cite both sources\\.", RegexOption.IGNORE_CASE)

 private val combined = Regex("For a $number-hour trip, compare $name and $name on battery life and weight\\. Which meets the runtime requirement, and how much heavier is it\\? Cite the evidence\\.", RegexOption.IGNORE_CASE)

 private fun parse(text: String, citation: Int): List<Facts>? {
  // Consume the entire passage: unknown qualifiers, ranges, negation and instructions decline.
  var rest = text.trim().replace(Regex("\\s+"), " ")
  val values=mutableListOf<Triple<String,String,BigDecimal>>()
  while(rest.isNotEmpty()) {
   if(rest.startsWith("Fictional test data.", true)) { rest=rest.substring("Fictional test data.".length).trimStart(); continue }
   val r=runtime.find(rest)?.takeIf { it.range.first==0 }
   val w=weight.find(rest)?.takeIf { it.range.first==0 }
   val o=offline.find(rest)?.takeIf { it.range.first==0 }
   val m=r?:w?:o?:return null
   val kind=if(r!=null) "hours" else if(w!=null) "grams" else "offline"
   values+=Triple(m.groupValues[1],kind,if(o!=null) BigDecimal.ZERO else m.groupValues[2].toBigDecimal())
   rest=rest.substring(m.range.last+1).trimStart()
  }
  if(values.isEmpty()) return null
  val groups=values.groupBy { it.first.lowercase(java.util.Locale.ROOT) }
  return groups.values.map { group ->
   if(group.groupBy { it.second }.any { it.value.size>1 }) return null
   val h=group.singleOrNull { it.second=="hours" }?.third?:return null
   val g=group.singleOrNull { it.second=="grams" }?.third
   if(h<=BigDecimal.ZERO || (g!=null && g<=BigDecimal.ZERO)) return null
   Facts(group.first().first,h,g,citation)
  }
 }

 private fun fmt(n: BigDecimal)=n.stripTrailingZeros().toPlainString()
 fun answer(question: String, sources: List<Source>): String? {
  val q=question.trim().replace(Regex("\\s+"), " ")
  val t=trip.matchEntire(q); val l=lighter.matchEntire(q); val c=compare.matchEntire(q)
  val combinedMatch=combined.matchEntire(q)
  val match=t?:l?:c?:combinedMatch?:return null
  val required: BigDecimal
  val names: List<String>
  if(c!=null) { names=listOf(match.groupValues[1],match.groupValues[2]); required=match.groupValues[3].toBigDecimal() }
  else { required=match.groupValues[1].toBigDecimal(); names=listOf(match.groupValues[2],match.groupValues[3]) }
  if(required<=BigDecimal.ZERO || names[0].equals(names[1],true)) return null
  // If any retrieved passage cannot be fully parsed, leave the question to the ordinary path.
  val all=sources.mapIndexed { i,s -> parse(s.text,i+1)?:return null }.flatten()
  val selected=names.map { n -> all.filter { it.name.equals(n,true) }.singleOrNull()?:return null }
  if(l!=null || c!=null || combinedMatch!=null) { if(selected.any { it.grams==null }) return null }
  val checks=selected.joinToString("\n") {
   "${it.name}: ${fmt(it.hours)} hours ${if(it.hours>=required) ">=" else "<"} ${fmt(required)} hours; ${if(it.hours>=required) "meets" else "does not meet"} the runtime requirement [${it.citation}]."
  }
  val eligible=selected.filter { it.hours>=required }
  var result=when(eligible.size) {
   0 -> "Neither meets the runtime requirement."
   1 -> "Only ${eligible[0].name} meets the runtime requirement."
   else -> "Both meet the runtime requirement."
  }
  if(l!=null || c!=null || combinedMatch!=null) {
   val a=selected[0];val b=selected[1];val ag=a.grams!!;val bg=b.grams!!
   val diff=ag.subtract(bg).abs()
   result+="\nWeight calculation: ${fmt(ag)} grams [${a.citation}] - ${fmt(bg)} grams [${b.citation}] = ${fmt(ag.subtract(bg))} grams. "
   result+=if(ag.compareTo(bg)==0) "Their weights are equal." else "${if(ag<bg) a.name else b.name} is ${fmt(diff)} grams lighter."
   if(l!=null && eligible.size==1) result+=" Only ${eligible[0].name} is eligible for this trip."
   if(l!=null && eligible.isEmpty()) result+=" The lighter item still cannot meet the trip duration."
  }
  return "CALCULATED FROM LOCAL EVIDENCE — no language model used\n\n$checks\n$result"
 }
}
