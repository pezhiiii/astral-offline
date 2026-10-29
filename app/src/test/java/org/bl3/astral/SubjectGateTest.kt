package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class SubjectGateTest {
 @Test fun acceptsRepeatedTopicInOtherDefinition() {
  val q="What is the difference between weather and climate? Cite the sources."
  val source="Weather describes atmospheric conditions over a short time, whereas climate describes patterns and averages of weather over many years."
  assertEquals(source,SearchPolicy.comparisonQuote(q,source))
 }

 @Test fun rejectsWeatherMentionWithoutTwoComparisonSides() {
  val q="What is the difference between weather and climate? Cite the sources."
  assertNull(SearchPolicy.comparisonQuote(q,"Climate change-induced extreme weather poses challenges, while inherent uncertainty remains."))
  assertNull(SearchPolicy.comparisonQuote(q,"Currents produce a climate system with different climate regions and weather phenomena."))
 }

 @Test fun differenceQuestionUsesSubjectsInsteadOfDefinitionAnchor() {
  val q="What is the difference between weather and climate? Cite the sources."
  assertNull(SearchPolicy.definitionAnchor(q,SearchPolicy.terms(q)))
  assertEquals("title:weather AND title:climate",SearchPolicy.candidateQueries(q).first())
  assertEquals("Weather changes daily, whereas climate describes long-term patterns.",SearchPolicy.comparisonQuote(q,"Weather changes daily, whereas climate describes long-term patterns."))
 }

 @Test fun rejectsThirdPartyNucleusContrast() {
  assertNull(SearchPolicy.comparisonQuote("How are plant cells different from animal cells?", "Bacteria and archaea lack a membrane-bound nucleus; animal and plant cells have nuclei and other organelles."))
 }
 @Test fun rejectsSharedMechanisms() {
  assertNull(SearchPolicy.comparisonQuote("How are plant cells different from animal cells?", "While plants use many of the same mechanisms as animals, surrounding cells determine their fate."))
 }

 @Test fun selectsActualContrastSentence() {
  val sentence="Unlike animals, many plant cells remain totipotent."
  assertEquals(sentence,SearchPolicy.comparisonQuote("How are plant cells different from animal cells?", "Cells were studied in 1838. "+sentence))
 }
 @Test fun rejectsContrastOutsideSubjectSentence() {
  assertNull(SearchPolicy.comparisonQuote("How are plant cells different from animal cells?", "While studying cork, scientists observed dead cells. Plant and animal tissue were examined."))
 }

 @Test fun rejectsMusicForCellComparison() {
  assertFalse(SearchPolicy.acceptsSubject("How are plant cells different from animal cells? Cite the sources.", "(A) Senile Animal", "An album by the Melvins."))
 }
 @Test fun rejectsCellHistoryForComparison() {
  assertFalse(SearchPolicy.acceptsSubject("How are plant cells different from animal cells?", "Cell history", "Cells are functional units. Plant scientist Schleiden and animal scientist Schwann studied cells in plant and animal tissue, respectively."))
 }
 @Test fun acceptsComparisonWithBothSubjects() {
  assertTrue(SearchPolicy.acceptsSubject("How are plant cells different from animal cells? Cite the sources.", "Cells", "Plant cells have cell walls, whereas animal cells lack cell walls."))
 }
 @Test fun rejectsIncidentalBiographyMention() {
  assertFalse(SearchPolicy.acceptsSubject("Who was Aristotle? Explain two contributions.", "Science notes", "These observations formed the basis for Aristotle's meteorology."))
 }
 @Test fun acceptsBiographyDefinition() {
  assertTrue(SearchPolicy.acceptsSubject("Who was Aristotle? Explain two contributions.", "Notes", "Aristotle was a Greek philosopher."))
 }
 @Test fun rejectsIncidentalDefinitionMention() {
  assertFalse(SearchPolicy.acceptsSubject("What is photosynthesis?", "Ecology notes", "Primary production mainly occurs through photosynthesis."))
 }
 @Test fun acceptsDefinition() {
  assertTrue(SearchPolicy.acceptsSubject("What is photosynthesis?", "Notes", "Photosynthesis is the conversion of light energy."))
 }
}
