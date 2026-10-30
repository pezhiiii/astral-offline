package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class ExplanationEvidenceTest {
 private val q="Why does an object weigh less on the Moon while keeping the same mass? Cite the sources."
 private val evidence="An object on the Moon would weigh less than it does on Earth because of the lower gravity, but it would still have the same mass."
 @Test fun retrievesObservedEvidenceDespiteGrammaticalKeeping() {
  assertTrue(SearchPolicy.acceptsSubject(q,"Mass",evidence))
  assertEquals(evidence,SearchPolicy.explanationExcerpt(q,evidence))
 }
 @Test fun incidentalSharedWordIsInsufficient() {
  assertFalse(SearchPolicy.acceptsSubject(q,"Moon","The Moon has craters. The mass of an object is measured here."))
 }
 @Test fun negationInEvidenceIsPreserved() {
  val text="An object on the Moon does not weigh less merely because its mass is smaller."
  assertEquals(text,SearchPolicy.explanationExcerpt(q,text))
 }
 @Test fun anotherWhyQuestionAlsoIgnoresContinuationVerb() {
  val question="Why does ice float while remaining solid?"
  assertTrue(SearchPolicy.acceptsSubject(question,"Ice","Ice can float as a solid because its density is lower."))
 }
}
