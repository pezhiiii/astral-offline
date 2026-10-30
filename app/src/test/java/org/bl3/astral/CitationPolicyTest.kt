package org.bl3.astral
import org.junit.Assert.*
import org.junit.Test
class CitationPolicyTest {
 private val unitQuestion="Compare mass and weight: what are their SI units?"
 private val unitEvidence=listOf("The SI unit of mass is the kilogram (kg).", "The unit of weight in SI is the newton.")
 @Test fun rejectsDanglingEndingDespitePunctuationAndReferences() {
  for(end in listOf("and,", "with;", "because:", "and"))
   assertNotNull(CitationPolicy.qualityRejectionReason("What is weight?","Weight is force $end [1] [2].",unitEvidence))
 }
 @Test fun rejectsOmittedUnitAndAllowsNamesOrSymbols() {
  assertNotNull(CitationPolicy.qualityRejectionReason(unitQuestion,"Mass is measured in kilograms while weight is a force [1] [2].",unitEvidence))
  assertNull(CitationPolicy.qualityRejectionReason(unitQuestion,"Mass uses kg and weight uses N [1] [2].",unitEvidence))
  assertNull(CitationPolicy.qualityRejectionReason(unitQuestion,"Mass uses kilograms and weight uses newtons [1] [2].",unitEvidence))
 }
 @Test fun unitGateRequiresRequestedUnitsAndIdentifiableEvidence() {
  assertNull(CitationPolicy.qualityRejectionReason("Compare mass and weight.","Mass and weight differ [1].",unitEvidence))
  assertNull(CitationPolicy.qualityRejectionReason(unitQuestion,"Mass and weight differ [1].",listOf("Mass and weight differ.")))
 }

 private val comparison="Compare mass and weight: what changes when moving from Earth to the Moon? Cite the sources."
 @Test fun rejectsObservedMixedScriptAndDanglingSentence() {
  assertNotNull(CitationPolicy.qualityRejectionReason(comparison,"Mass is constant or引力強度 [1].",listOf("Mass is inertia.")))
  assertNotNull(CitationPolicy.qualityRejectionReason(comparison,"Weight changes with [1].",listOf("Weight is a force.")))
 }
 @Test fun rejectsOmittedRequestedLocationButAllowsScopedComparison() {
  val evidence=listOf("Mass is constant and weight decreases from Earth to the Moon.")
  assertNotNull(CitationPolicy.qualityRejectionReason(comparison,"Mass is inertia; weight is force [1].",evidence))
  assertNull(CitationPolicy.qualityRejectionReason(comparison,"From Earth to the Moon, mass stays constant while weight decreases [1].",evidence))
 }
 @Test fun preservesNonEnglishQuestionsAndEvidenceNotation() {
  assertNull(CitationPolicy.qualityRejectionReason("جرم چیست؟","جرم ویژگی جسم است [1].",listOf("جرم")))
  assertNull(CitationPolicy.qualityRejectionReason("What is alpha?","The symbol is α [1].",listOf("The symbol is α.")))
 }

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
