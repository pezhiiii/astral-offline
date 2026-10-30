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
  assertEquals(2,tasks.size)
  assertTrue(tasks[0].contains("length"))
  assertTrue(tasks[1].contains("time"))
  assertTrue(ComparisonPlan.questions("What is mass?").isEmpty())
  assertTrue(ComparisonPlan.questions("Compare cats and dogs.").isEmpty())
 }
 @Test fun partFailureCannotBeHiddenByCombinedValidCitations() {
  val draft="Mass is measured in kg [1]."
  val result=CitationPolicy.present(draft,listOf("Mass is measured in kg."),extraRejection="Part 2: incomplete")
  assertTrue(result.startsWith("MODEL ANSWER REJECTED — Part 2: incomplete"))
 }
}
