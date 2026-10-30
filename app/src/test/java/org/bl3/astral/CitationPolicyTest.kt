package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class CitationPolicyTest {
 @Test fun rejectsObservedInventedSecondSource() {
  val draft="[1] Mass is matter. [2] Invented Weight source."
  val result=CitationPolicy.present(draft,listOf("Weight is a force, while mass is a property."))
  assertTrue(result.startsWith("MODEL ANSWER REJECTED"))
  assertFalse(result.contains("Invented Weight"))
  assertTrue(result.contains("[1] Weight is a force"))
 }
 @Test fun validReferencesPreserveOriginalAnswer() {
  val answer="A differs from B. [1] [2]"
  assertEquals(answer,CitationPolicy.present(answer,listOf("A evidence.","B evidence.")))
 }
 @Test fun missingAndOverflowReferencesFailClosed() {
  assertNotNull(CitationPolicy.rejectionReason("Uncited assertion.",1))
  assertNotNull(CitationPolicy.rejectionReason("Assertion [9999999999999999999]",1))
  assertNotNull(CitationPolicy.rejectionReason("Assertion [0]",1))
 }
 @Test fun validNumbersDoNotProveTruth() {
  assertNull(CitationPolicy.rejectionReason("An unsupported claim. [1]",1))
 }
}
