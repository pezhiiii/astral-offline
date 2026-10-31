package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class RetrievalRegressionTest {
 @Test fun rejectsIncidentalVelocity() {
  val q="What is velocity, and does the cited source name the first person who measured it?"
  assertFalse(SearchPolicy.acceptsSubject(q,"Cartridge","Velocity is close to another cartridge but uses less powder."))
  assertFalse(SearchPolicy.acceptsSubject("What is speed?","Speed skating","The race was held in February."))
 }
 @Test fun acceptsReverseDefinition() {
  assertTrue(SearchPolicy.acceptsSubject("What is speed?","Velocity",
   "The scalar magnitude of velocity is called speed, measured in metres per second."))
 }
 @Test fun preservesCompleteUnitSentence() {
  val body="Mass is a physical quantity. The unit of mass in SI is the kilogram (kg)."
  assertTrue(SearchPolicy.definitionExcerpt(body,"mass").contains("kilogram"))
 }
 @Test fun rejectsOmittedHistory() {
  val q="What is velocity, and does the source name the first person who measured it?"
  val sources=listOf("Velocity is a vector quantity with direction.")
  assertNotNull(CitationPolicy.evidenceRejectionReason("Velocity is a vector quantity [1].",sources,true,q))
  assertNull(CitationPolicy.evidenceRejectionReason(
   "The source does not provide the name of the first person who measured velocity [1].",sources,true,q))
 }
 @Test fun rejectsVectorMagnitudeConflation() {
  assertNotNull(CitationPolicy.evidenceRejectionReason(
   "Velocity is defined as the magnitude of the velocity vector [1].",
   listOf("Velocity is a vector quantity."),true,"What is velocity?"))
 }
}
