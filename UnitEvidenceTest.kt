package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class UnitEvidenceTest {
 private val q="What are the SI units of mass and weight? Cite the sources."
 private val mass="The unit of mass in the International System of Units (SI) is the kilogram (kg)."
 private val weight="The unit of measurement for weight is that of force, which in the International System of Units (SI) is the newton."
 @Test fun quantitiesAreSubjectsRatherThanSI() {
  assertEquals(listOf("mass","weight"),SearchPolicy.unitTopics(q))
  assertNull(SearchPolicy.definitionAnchor(q,SearchPolicy.terms(q)))
  assertTrue(SearchPolicy.candidateQueries(q).contains("title:weight"))
 }
 @Test fun keepsCompleteUnitSentences() {
  assertEquals(mass,SearchPolicy.unitExcerpt(q,mass))
  assertEquals(weight,SearchPolicy.unitExcerpt(q,weight))
  assertTrue(SearchPolicy.acceptsSubject(q,"Mass",mass))
 }
 @Test fun rejectsIncidentalAndHistoricalMatches() {
  assertNull(SearchPolicy.unitExcerpt(q,"Stars have mass. A Julian year is 365.25 days of SI seconds."))
  assertNull(SearchPolicy.unitExcerpt(q,"In 1795 the gram was defined using water; the kilogram was a metallic standard."))
 }
 @Test fun selectsEvidenceForBothQuantities() {
  assertEquals(listOf(0,2),SearchPolicy.unitSourceIndices(q,listOf(mass,mass,weight)))
 }
 @Test fun comparisonQualifiersDoNotBecomeTopicNames() {
  val question="Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources."
  assertEquals(listOf("mass","weight"),SearchPolicy.comparisonTopics(question))
  assertNotNull(SearchPolicy.comparisonEvidence(question,"Weight is a force, while mass is a property of a body."))
 }
 @Test fun unknownPersonalInformationStillRejected() {
  assertFalse(SearchPolicy.acceptsSubject("What is the exact serial number of my refrigerator? Cite the sources.","Mass",mass))
 }
}
