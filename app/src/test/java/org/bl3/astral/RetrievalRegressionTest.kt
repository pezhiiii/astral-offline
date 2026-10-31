package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class RetrievalRegressionTest {
 @Test fun rejectsIncidentalVelocity() {
  val q="What is velocity, and does the cited source name the first person who measured it?"
  assertFalse(SearchPolicy.acceptsSubject(q,"Cartridge","Velocity is close to another cartridge but uses less powder."))
  assertFalse(SearchPolicy.acceptsSubject("What is speed?","Speed skating","The race was held in February."))
 }
 @Test fun acceptsQualifiedScientificDefinition() {
  val body="In physics, acceleration is a measure of how velocity changes. The SI unit for acceleration is metre per second squared."
  for(q in listOf("What is acceleration?", "What is acceleration? Give its definition and SI unit. Cite the source.")) {
   assertTrue(SearchPolicy.acceptsSubject(q,"Acceleration",body))
   assertEquals(listOf("title:acceleration","body:acceleration"),SearchPolicy.candidateQueries(q))
  }
  assertTrue(SearchPolicy.definitionRank("acceleration",body)>0)
  assertTrue(SearchPolicy.definitionExcerpt(body,"acceleration").contains("metre per second squared"))
  assertFalse(SearchPolicy.acceptsSubject("What is acceleration?","Acceleration",
   "In physics, acceleration is close to another value."))
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
 @Test fun rejectsDanglingExample() {
  val evidence=listOf("Speed is the magnitude of velocity, measured in metres per second.")
  for(draft in listOf("Speed is measured in metres per second, such as in the statement: [1].",
   "Speed is a scalar quantity; [1].", "Speed is a scalar quantity, for example [1].")) {
   assertNotNull(CitationPolicy.evidenceRejectionReason(draft,evidence,true,"What is speed?"))
  }
  assertNull(CitationPolicy.evidenceRejectionReason(
   "Speed is the magnitude of velocity, measured in metres per second [1].",
   evidence,true,"What is speed?"))
 }
}
