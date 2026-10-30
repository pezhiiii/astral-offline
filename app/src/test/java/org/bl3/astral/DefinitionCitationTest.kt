package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class DefinitionCitationTest {
 @Test fun operationalDefinitionPreferredWhenAvailable() {
  val first="Mass is an intrinsic physical quantity of a body."
  val operational="Mass can be experimentally defined as a measure of the body's inertia, meaning its resistance to acceleration when a net force is applied."
  assertEquals(operational,SearchPolicy.comparisonDefinitions("Compare mass and weight.","$first $operational")["mass"])
 }
 @Test fun doesNotInventMissingOperationalDefinition() {
  val text="Mass is a physical quantity."
  assertEquals(text,SearchPolicy.comparisonDefinitions("Compare mass and weight.",text)["mass"])
 }
 @Test fun rejectsCollectedEndReferences() {
  assertTrue(CitationPolicy.hasUncitedSentence("Mass has a unit. Weight has another unit. [1] [2]"))
  assertTrue(CitationPolicy.hasUncitedSentence("[1] Mass has a unit. Weight has another unit. [2]"))
 }
 @Test fun allowsAdjacentReferencesBeforeOrAfterPunctuation() {
  assertFalse(CitationPolicy.hasUncitedSentence("Mass has a unit. [1]\nWeight has another unit. [2]"))
  assertFalse(CitationPolicy.hasUncitedSentence("Mass has a unit [1]. Weight has another unit [2]."))
  assertFalse(CitationPolicy.hasUncitedSentence("Weight is 9.8 newtons. [1]"))
 }
 @Test fun uncitedTailIsRejected() {
  assertTrue(CitationPolicy.hasUncitedSentence("First claim. [1] Another claim"))
  assertFalse(CitationPolicy.hasUncitedSentence("First claim [1]"))
 }
}
