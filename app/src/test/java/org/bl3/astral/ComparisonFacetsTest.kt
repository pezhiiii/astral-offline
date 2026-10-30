package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class ComparisonFacetsTest {
 private val q="Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources."
 private val contrast="Weight is a force, while mass is a property of a body."
 private val mass="Mass is an intrinsic physical quantity of a body."
 private val kg="The unit of mass in the International System of Units (SI) is the kilogram (kg)."
 private val newton="The unit of measurement for weight is that of force, which in the International System of Units (SI) is the newton."
 private val moon="An object on the Moon would weigh less than it does on Earth because of the lower gravity, but it would still have the same mass."
 @Test fun allObservedFacetsReachModelAcrossSeparateSources() {
  val bodies=listOf("$mass $kg $moon $contrast",newton)
  val indices=SearchPolicy.comparisonSourceIndices(q,bodies)
  assertEquals(listOf(0,1),indices)
  val excerpts=indices.map { SearchPolicy.comparisonModelContext(q,bodies[it]).orEmpty() }
  val evidence=excerpts.joinToString("\n")
  for(sentence in listOf(mass,kg,newton,moon)) assertTrue(sentence,evidence.contains(sentence))
 }
 @Test fun unitOnlyPassageIsEligibleForQualifiedComparison() {
  assertTrue(SearchPolicy.acceptsSubject(q,"Weight",newton))
  assertFalse(SearchPolicy.acceptsSubject(q,"Astronomy","A Julian year is a unit of time. Stars have mass."))
 }
 @Test fun missingFacetsAreNotInvented() {
  val excerpt=SearchPolicy.comparisonModelContext(q,contrast).orEmpty()
  assertFalse(excerpt.contains("kilogram"))
  assertFalse(excerpt.contains("Moon"))
 }
 @Test fun simpleComparisonsRetainSourceSelection() {
  assertEquals(listOf(0),SearchPolicy.comparisonSourceIndices("Compare mass and weight.",listOf(contrast,newton)))
 }
 @Test fun sourceBudgetNeverCutsSentences() {
  assertNull(SearchPolicy.comparisonModelContext(q,kg,3))
 }
}
