package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class DefinitionCitationTest {
 @Test fun bareDefinitionQuotesExactCompleteSourceSentence() {
  val definition="Photosynthesis is a process that converts light energy into chemical energy."
  assertTrue(DirectDefinitionQuote.isBareDefinition("What is photosynthesis?", "photosynthesis"))
  assertTrue(DirectDefinitionQuote.isBareDefinition("Define an ecosystem.", "ecosystem"))
  assertEquals(definition,DirectDefinitionQuote.directDefinitionQuote("$definition Photosynthesis occurs in plants.", "photosynthesis"))
 }

 @Test fun qualifiedQuestionsAndIncompleteEvidenceDoNotQualify() {
  assertFalse(DirectDefinitionQuote.isBareDefinition("What is photosynthesis and why does it matter?", "photosynthesis"))
  assertFalse(DirectDefinitionQuote.isBareDefinition("What is the SI unit of mass?", "mass"))
  assertNull(DirectDefinitionQuote.directDefinitionQuote("Photosynthesis is a process that converts light ...", "photosynthesis"))
  assertNull(DirectDefinitionQuote.directDefinitionQuote("Photosynthesis is discussed in this article.", "photosynthesis"))
  assertNull(DirectDefinitionQuote.directDefinitionQuote("Respiration is a process distinct from photosynthesis.", "photosynthesis"))
 }

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
 @Test fun observedPhoneDraftRemainsRejectedAndAdjacentVersionPasses() {
  val facts=listOf("Mass is a measure of inertia. The unit of mass is the kilogram. On the Moon it has the same mass.","Weight is a force, measured in newtons; it decreases on the Moon.")
  val draft="Mass is a measure of inertia. Its unit is the kilogram. Weight is measured in newtons. Mass remains the same on the Moon. [1] [2]"
  assertNotNull(CitationPolicy.evidenceRejectionReason(draft,facts))
  val cited="Mass measures inertia and its unit is the kilogram [1].\nWeight is a force measured in newtons [2].\nOn the Moon mass stays the same while weight decreases [1] [2]."
  assertNull(CitationPolicy.evidenceRejectionReason(cited,facts))
 }
}
