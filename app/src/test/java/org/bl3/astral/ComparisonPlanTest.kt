package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class ComparisonPlanTest {
 @Test fun separatesSubjectsUnitsAndScenarioWithoutInventingAnswers() {
  val tasks=ComparisonPlan.questions("Compare mass and weight: what does each measure, what are their SI units, and which changes when moving from Earth to the Moon? Cite the sources.")
  assertEquals(3,tasks.size)
  assertTrue(tasks[0].startsWith("What does mass measure"))
  assertTrue(tasks[1].startsWith("What does weight measure"))
  assertTrue(tasks.take(2).all { it.contains("SI unit") })
  assertTrue(tasks[2].contains("from Earth to Moon"))
  assertFalse(tasks.joinToString().contains("kilogram"))
  assertFalse(tasks.joinToString().contains("newton"))
 }
 @Test fun usesOtherTopicsAndSkipsUnsupportedForms() {
  val tasks=ComparisonPlan.questions("Compare length and time: what are their SI units?")
  assertTrue(tasks.isEmpty()) // Unparsed comparison requests use the full original question.
  assertTrue(ComparisonPlan.questions("What is mass?").isEmpty())
  assertTrue(ComparisonPlan.questions("Compare cats and dogs.").isEmpty())
 }
 @Test fun partFailureCannotBeHiddenByCombinedValidCitations() {
  val draft="Mass is measured in kg [1]."
  val result=CitationPolicy.present(draft,listOf("Mass is measured in kg."),extraRejection="Part 2: incomplete")
  assertTrue(result.startsWith("MODEL ANSWER REJECTED — Part 2: incomplete"))
 }
 @Test fun scenarioMustMentionBothSubjectsAndLocations() {
  val q="Compare mass and weight: what are their units, and which changes from Earth to the Moon?"
  assertNotNull(ComparisonPlan.scenarioRejection(q,2,"Mass stays constant from Earth to Moon [1]."))
  assertNotNull(ComparisonPlan.scenarioRejection(q,2,"Mass stays constant and weight decreases [1]."))
  assertNull(ComparisonPlan.scenarioRejection(q,2,"From Earth to Moon mass stays constant and weight decreases [1]."))
  assertNull(ComparisonPlan.scenarioRejection(q,0,"Mass measures inertia [1]."))
 }
 @Test fun preservesDirectionRequestAndItsSources() {
  val q="Compare speed and velocity: what does each measure, what are their SI units, and how does direction distinguish them?"
  val tasks=ComparisonPlan.questions(q)
  assertEquals(3,tasks.size)
  assertTrue(tasks[2].contains("direction"))
  assertFalse(tasks[2].contains("SI units"))
  assertFalse(tasks[2].contains("Original question"))
  assertEquals(listOf(0),ComparisonPlan.sourceIndices(q,2,listOf("Velocity includes direction; speed does not.","Both use metres per second.")))
  assertEquals(listOf(0,1),ComparisonPlan.sourceIndices(q,2,listOf("No relevant distinction.","Both use metres per second.")))
  assertNotNull(ComparisonPlan.scenarioRejection(q,2,"Speed and velocity use m/s [2]."))
  assertNull(ComparisonPlan.scenarioRejection(q,2,"Velocity includes direction whereas speed does not [1]."))
 }
}
