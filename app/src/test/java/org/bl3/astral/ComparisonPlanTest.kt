package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class ComparisonPlanTest {
 @Test fun scenarioRejectsUnsupportedParaphraseAndWrongCitation() {
  val q="Compare mass and weight: what are their SI units, and which changes from Earth to the Moon?"
  val quote="An object on the Moon would weigh less than it does on Earth because of the lower gravity, but it would still have the same mass."
  assertNull(ComparisonPlan.scenarioQuoteRejection(q,2,quote.dropLast(1)+" [1].",listOf(quote)))
  assertNotNull(ComparisonPlan.scenarioQuoteRejection(q,2,"On Earth an object has higher mass and weight; on the Moon it has the same mass [1].",listOf(quote)))
  assertNotNull(ComparisonPlan.scenarioQuoteRejection(q,2,quote.dropLast(1)+" [2].",listOf(quote,"Unrelated source.")))
  assertNull(ComparisonPlan.scenarioQuoteRejection(q,0,"Mass measures inertia [1].",listOf(quote)))
 }
 @Test fun scenarioEvidenceOmitsDefinitionsAndUnits() {
  val q="Compare mass and weight: what are their SI units, and which changes from Earth to the Moon?"
  val change="An object on the Moon would weigh less than it does on Earth because of the lower gravity, but it would still have the same mass."
  val text="Mass is a measure of inertia. The SI unit of mass is the kilogram. $change"
  assertEquals(change,ComparisonPlan.partExcerpt(q,2,text))
  assertEquals("Mass is a measure of inertia.",ComparisonPlan.partExcerpt(q,2,"Mass is a measure of inertia."))
  assertTrue(ComparisonPlan.questions(q)[2].contains("do not paraphrase"))
  assertTrue(ComparisonPlan.citationGuidance(q,2,listOf(text)).contains("verbatim"))
 }
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
 @Test fun meaningAndUnitComparisonHasThreeFocusedParts() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ? Cite the sources."
  val tasks=ComparisonPlan.questions(q)
  assertEquals(3,tasks.size)
  assertTrue(tasks[0].startsWith("What does heat mean"))
  assertTrue(tasks[1].startsWith("What does temperature mean"))
  assertTrue(tasks[2].contains("How do heat and temperature differ"))
  val excerpts=listOf("Heat is energy in transfer.","Temperature is a state variable. The SI unit of temperature is the kelvin.","The SI unit of heat is the joule.")
  assertEquals(listOf(0,2),ComparisonPlan.sourceIndices(q,0,excerpts))
  assertEquals(listOf(1),ComparisonPlan.sourceIndices(q,1,excerpts))
 }
 @Test fun guidanceMapsDefinitionAndUnitToOriginalReferences() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ?"
  val excerpts=listOf("Heat is energy in transfer.","Temperature is a state variable. The SI unit of temperature is the kelvin.","The SI unit of heat is the joule.")
  val heat=ComparisonPlan.citationGuidance(q,0,excerpts)
  assertTrue(heat.contains("Definition evidence for heat: [1]."))
  assertTrue(heat.contains("SI unit evidence for heat: [3]."))
  assertTrue(heat.contains("end it with [1] [3]."))
  val temperature=ComparisonPlan.citationGuidance(q,1,excerpts)
  assertTrue(temperature.contains("end it with [2]."))
  assertFalse(temperature.contains("[2] [2]"))
  assertTrue(ComparisonPlan.citationGuidance(q,2,excerpts).contains("[1] [2]"))
 }
 @Test fun eachSubjectPartMustNameSubjectAndUnit() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ?"
  val sources=listOf("Heat is energy transfer. The SI unit of heat is the joule.","Temperature is a state variable. The SI unit of temperature is the kelvin.")
  assertNotNull(ComparisonPlan.partRejection(q,0,"Heat is energy transfer [1].",sources))
  assertNotNull(ComparisonPlan.partRejection(q,1,"Kelvin [2].",sources))
  assertNull(ComparisonPlan.partRejection(q,0,"Heat is energy transfer, measured in joules [1].",sources))
  assertNull(ComparisonPlan.partRejection(q,1,"Temperature is a state variable, measured in kelvin [2].",sources))
  assertNotNull(ComparisonPlan.partRejection(q,2,"Heat is energy transfer [1].",sources))
 }
 @Test fun subjectPromptKeepsDefinitionAndSIUnitSentence() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ?"
  val definition="Temperature is a state variable."
  val unit="The kelvin is one of the seven base units in the International System of Units (SI)."
  val text=definition+" Thermometers use temperature scales. The scales include the Celsius scale and Kelvin scale. "+unit
  assertEquals(definition+"\n"+unit,ComparisonPlan.partExcerpt(q,1,text))
  assertNotNull(ComparisonPlan.partRejection(q,2,"Heat differs from temperature [1].\nSources: [1].",listOf(text)))
  assertNotNull(ComparisonPlan.partRejection(q,2,"Heat temperature "+"word ".repeat(41)+"[1].",listOf(text)))
 }
 @Test fun distinctionExcludesUnitOnlySource() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ?"
  val sources=listOf("Heat is energy transfer.","Temperature is a macroscopic state variable.","The SI unit of heat is the joule.")
  assertEquals(listOf(0,1),ComparisonPlan.sourceIndices(q,2,sources))
  assertEquals(sources[1],ComparisonPlan.partExcerpt(q,2,sources[1]+" The SI unit of temperature is the kelvin."))
 }
}
