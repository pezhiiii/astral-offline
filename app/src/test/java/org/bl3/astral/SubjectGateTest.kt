package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class SubjectGateTest {
 @Test fun rejectsMusicForCellComparison() {
  assertFalse(SearchPolicy.acceptsSubject("How are plant cells different from animal cells? Cite the sources.", "(A) Senile Animal", "An album by the Melvins."))
 }
 @Test fun rejectsCellHistoryForComparison() {
  assertFalse(SearchPolicy.acceptsSubject("How are plant cells different from animal cells?", "Cell history", "Cells are functional units. Plant scientist Schleiden and animal scientist Schwann studied cells in plant and animal tissue, respectively."))
 }
 @Test fun acceptsComparisonWithBothSubjects() {
  assertTrue(SearchPolicy.acceptsSubject("How are plant cells different from animal cells? Cite the sources.", "Cells", "Plant cells have cell walls. Animal cells lack cell walls."))
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
