package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class CitationPolicyTest {
 @Test fun rejectsTokenLimitEvenWhenLastSentenceHasValidCitation() {
  val answer="Mass is measured in kilograms [1]."
  val evidence=listOf("Mass is measured in kilograms.")
  assertNull(CitationPolicy.evidenceRejectionReason(answer,evidence,true))
  assertNotNull(CitationPolicy.evidenceRejectionReason(answer,evidence,false))
  val shown=CitationPolicy.present(answer,evidence,false)
  assertTrue(shown.startsWith("MODEL ANSWER REJECTED"))
  assertTrue(shown.contains("token limit"))
 }

 @Test fun rejectsObservedInventedSecondSource() {
  val draft="[1] Mass is matter. [2] Invented Weight source."
  val result=CitationPolicy.present(draft,listOf("Weight is a force, while mass is a property."))
  assertTrue(result.startsWith("MODEL ANSWER REJECTED"))
  assertFalse(result.contains("Invented Weight"))
  assertTrue(result.contains("[1] Weight is a force"))
 }
 @Test fun validReferencesPreserveOriginalAnswer() {
  val answer="A differs from B. [1] [2]"
  assertTrue(CitationPolicy.present(answer,listOf("A evidence.","B evidence.")).endsWith(answer))
 }
 @Test fun missingAndOverflowReferencesFailClosed() {
  assertNotNull(CitationPolicy.rejectionReason("Uncited assertion.",1))
  assertNotNull(CitationPolicy.rejectionReason("Assertion [9999999999999999999]",1))
  assertNotNull(CitationPolicy.rejectionReason("Assertion [0]",1))
 }
 @Test fun validNumbersDoNotProveTruth() {
  assertNull(CitationPolicy.rejectionReason("An unsupported claim. [1]",1))
 }
 @Test fun rejectsNewFormulaAndNumberWithoutTreatingCitationAsNumber() {
  val evidence=listOf("Weight is a force, while mass determines its magnitude.")
  assertNotNull(CitationPolicy.evidenceRejectionReason("Weight = mass × g. [1]",evidence))
  assertNotNull(CitationPolicy.evidenceRejectionReason("The weight is 12 N. [1]",evidence))
  assertNull(CitationPolicy.evidenceRejectionReason("Weight is a force. [1]",evidence))
 }
}
