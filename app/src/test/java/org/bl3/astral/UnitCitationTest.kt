package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class UnitCitationTest {
 private val evidence=listOf("The SI unit of mass is the kilogram (kg).", "The SI unit of weight is the newton.")
 @Test fun rejectsObservedWrongReferenceAndAcceptsCorrectOrCombinedSources() {
  assertNotNull(CitationPolicy.unitCitationRejection("Weight uses newtons [1].",evidence))
  assertNull(CitationPolicy.unitCitationRejection("Weight uses N [2].",evidence))
  assertNull(CitationPolicy.unitCitationRejection("Mass uses kg and weight uses newtons [1] [2].",evidence))
 }
 @Test fun anotherLineCannotSupplyTheMissingReference() {
  assertNotNull(CitationPolicy.unitCitationRejection("Weight uses newtons [1].\nMass uses kg [1] [2].",evidence))
 }
 @Test fun focusedSourcesPreserveOriginalIndexWithReversedInput() {
  val q="Compare mass and weight: what are their SI units?"
  assertEquals(listOf(0),ComparisonPlan.sourceIndices(q,0,evidence))
  assertEquals(listOf(1),ComparisonPlan.sourceIndices(q,1,evidence))
  assertEquals(listOf(0),ComparisonPlan.sourceIndices(q,1,evidence.reversed()))
 }
}
