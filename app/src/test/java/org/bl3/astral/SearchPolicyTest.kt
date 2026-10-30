package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class SearchPolicyTest {
 @Test fun awardWordingRetainsWinnerInEventExcerpt() {
  val q="What happened in the 1903 German football championship?"
  val text="The 1903 German football championship was the first tournament. "+
   (1..95).joinToString(" ") { "background$it" }+
   " VfB Leipzig became the first club to be awarded the Viktoria, the trophy for the German champions. "+
   (1..40).joinToString(" ") { "ending$it" }
  assertTrue(SearchPolicy.eventExcerpt(text,SearchPolicy.terms(q),55).contains("VfB Leipzig became the first club to be awarded"))
  assertEquals(12,SearchPolicy.eventRank(q,"1903 German football championship",text))
 }

 @Test fun eventIntentDoesNotObscureSubjectQuery() {
  val queries=SearchPolicy.candidateQueries("What happened in the 1903 German football championship? Cite the source.")
  assertEquals("title:1903 AND title:german AND title:football AND title:championship",queries.first())
  assertFalse(queries.any { it.contains("happened") })
  assertEquals(listOf("title:aristotle","body:aristotle"),SearchPolicy.candidateQueries("Who was Aristotle?"))
  assertTrue(SearchPolicy.candidateQueries("cite sources").isEmpty())
 }

 @Test fun missingDatedEventCannotFallBackToOceanography() {
  val q="What happened in the 1903 German football championship? Cite the source."
  assertFalse(SearchPolicy.acceptsDatedSubject(q,"Marine biology","A German institute was founded in 1903."))
  assertFalse(SearchPolicy.acceptsDatedSubject(q,"1903 Cork football championship","Lees won."))
  assertTrue(SearchPolicy.acceptsDatedSubject(q,"1903 German football championship","The first national tournament."))
  assertTrue(SearchPolicy.acceptsDatedSubject(q,"Personal notes","The 1903 German football championship was held."))
  assertFalse(SearchPolicy.acceptsDatedSubject(q,"Unrelated — https://example.org/1903_German_football_championship","Marine biology."))
  assertTrue(SearchPolicy.acceptsDatedSubject("What is an ecosystem?","Ecosystem","An ecosystem includes organisms."))
 }

 @Test fun outcomePreferenceRequiresMatchingEventTitle() {
  val q="What happened in the 1903 German football championship?"
  assertEquals(12,SearchPolicy.eventRank(q,"1903 German football championship","Leipzig won the final."))
  assertEquals(0,SearchPolicy.eventRank(q,"1903 Cork championship","Lees won the final."))
  assertEquals(0,SearchPolicy.eventRank(q,"1903 German football championship","The first national tournament."))
 }

 @Test fun requestedCountDoesNotMatchFootballDivision() {
  val q="Who was Aristotle? Explain two of his contributions and cite the evidence."
  assertFalse(SearchPolicy.terms(q).contains("two"))
  assertEquals(0,SearchPolicy.score(SearchPolicy.terms(q),"1912 Scottish Division Two","The championship was won by Ayr United."))
 }

 @Test fun biographyRequiresItsSubject() {
  assertEquals("aristotle",SearchPolicy.subjectAnchor("Who was Aristotle? Explain two of his contributions and cite the evidence."))
  assertNull(SearchPolicy.subjectAnchor("Compare Aristotle and Plato"))
 }

 @Test fun datedEventFocusesOnCompleteTitle() {
  val titles=listOf("1903 German football championship — source", "1903 Cork Senior Football Championship — source")
  assertEquals("1903 German football championship",SearchPolicy.focusedTitle("What happened in the 1903 German football championship?",titles))
  assertNull(SearchPolicy.focusedTitle("Compare 1903 German football championship vs Cork",titles))
  assertNull(SearchPolicy.focusedTitle("What happened in the 1904 German football championship?",titles))
 }
 @Test fun yearInTitleOutranksAnotherYear() {
  val terms=SearchPolicy.terms("What happened in the 1903 German football championship?")
  val expected=SearchPolicy.score(terms,"1903 German football championship","The 1903 championship was the first.")
  val other=SearchPolicy.score(terms,"1930 German football championship","The 1930 final matched a record set in 1903.")
  assertTrue(expected>other)
 }
 @Test fun comparisonPrefersAnotherDocumentAndKeepsOrder() {
  assertEquals(listOf(0,2),SearchPolicy.modelSourceIndices("Compare Atlas and Nova",listOf("Atlas","Atlas","Nova"),2))
  assertEquals(listOf(0,1),SearchPolicy.modelSourceIndices("Explain Atlas",listOf("Atlas","Atlas","Nova"),2))
  assertEquals(listOf(0,1),SearchPolicy.modelSourceIndices("Compare",listOf("Atlas","Atlas"),2))
 }
 @Test fun eventExcerptKeepsBothSidesOfResult() {
  val text="The event began in 1903. "+(1..90).joinToString(" "){"background$it"}+" Leipzig won the final against Prague by a score of seven to two. "+(1..50).joinToString(" "){"ending$it"}
  assertTrue(SearchPolicy.eventExcerpt(text,listOf("1903","happened"),40).contains("Leipzig won the final against Prague"))
 }
 @Test fun distinctQuestionWordsFitInExcerpt() {
  val text="Atlas "+(1..80).joinToString(" "){"background$it"}+" Atlas and Nova were compared. "+(1..80).joinToString(" "){"ending$it"}
  val found=SearchPolicy.tokens(SearchPolicy.excerpt(text,listOf("atlas","nova"),30))
  assertTrue(found.containsAll(listOf("atlas","nova")))
 }
 @Test fun subjectQuestionDoesNotAnchorOnGenericHead() {
  val q="What is the capital of France?"
  assertNull(SearchPolicy.definitionAnchor(q,SearchPolicy.terms(q)))
  assertEquals("ecosystem",SearchPolicy.definitionAnchor("What is an ecosystem?",SearchPolicy.terms("What is an ecosystem?")))
 }
 @Test fun comparisonRequiresDefinitionAndUnitEvidenceForEachSubject() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ?"
  val partial=listOf("Heat is a process quantity.", "Temperature is important in all fields of natural science.", "The SI unit of heat is the joule (J).")
  assertFalse(SearchPolicy.comparisonDefinitions(q,partial[1]).containsKey("temperature"))
  assertEquals(setOf("definition:temperature","unit:temperature"),SearchPolicy.missingComparisonEvidence(q,partial).toSet())
  assertTrue(SearchPolicy.missingComparisonEvidence(q,partial+listOf("Temperature is a thermodynamic state property.","The SI unit of temperature is the kelvin (K).")).isEmpty())
  assertTrue(SearchPolicy.candidateQueries(q).first().contains("unit OR units"))
 }
 @Test fun partialComparisonSourcesSurviveMissingDefinition() {
  val q="Compare heat and temperature: what does each mean, what are their SI units, and how do they differ?"
  val bodies=listOf("Heat is a process quantity.","Temperature is important in all fields of science.","The SI unit of heat is the joule (J).")
  val indices=SearchPolicy.comparisonSourceIndices(q,bodies)
  assertEquals(listOf(0,2),indices)
  assertEquals(setOf("definition:temperature","unit:temperature"),SearchPolicy.missingComparisonEvidence(q,indices.map { bodies[it] }).toSet())
  assertTrue(SearchPolicy.comparisonSourceIndices(q,listOf("Unrelated text.")).isEmpty())
 }
 @Test fun unitOnlyComparisonEvidenceIsStillReturned() {
  val q="Compare heat and temperature: what does each mean and what are their SI units?"
  val bodies=listOf("The SI unit of heat is the joule (J).","The SI unit of temperature is the kelvin (K).")
  val indices=SearchPolicy.comparisonSourceIndices(q,bodies)
  assertEquals(setOf(0,1),indices.toSet())
  assertEquals(setOf("definition:heat","definition:temperature"),SearchPolicy.missingComparisonEvidence(q,bodies).toSet())
 }
 @Test fun adjacentScaleSentencesRetainUnitEvidence() {
  val text="Thermometers use temperature scales. The most common scales are the Celsius scale and the Kelvin scale (K). The kelvin is one of the seven base units in the International System of Units (SI)."
  assertEquals(text,SearchPolicy.unitExcerpt("SI unit of temperature",text))
  assertEquals(null,SearchPolicy.unitExcerpt("SI unit of temperature",text,10))
  assertEquals(null,SearchPolicy.unitExcerpt("SI unit of temperature","Temperature rises. Heat uses the joule scale. The joule is an SI unit."))
  assertEquals(null,SearchPolicy.unitExcerpt("SI unit of temperature","Temperature scales include kelvin. The kelvin is a base unit"))
 }
 @Test fun comparisonDefinitionsPreserveDomainQualifier() {
  val q="Compare heat and temperature: what does each mean and what are their SI units?"
  val definition="In classical thermodynamics and kinetic theory, temperature is a macroscopic state variable which reflects the average kinetic energy of the particles in a system."
  assertEquals(definition,SearchPolicy.comparisonDefinitions(q,definition)["temperature"])
  assertTrue(SearchPolicy.comparisonDefinitions(q,"In thermodynamics, heat is defined as energy in transfer.").containsKey("heat"))
  assertFalse(SearchPolicy.comparisonDefinitions(q,"In biology, temperature is important in many fields.").containsKey("temperature"))
  assertFalse(SearchPolicy.comparisonDefinitions(q,"A report says temperature is a state variable.").containsKey("temperature"))
  assertFalse(SearchPolicy.comparisonDefinitions(q,definition.dropLast(1)).containsKey("temperature"))
  val sources=listOf("Heat is a process quantity.",definition,"The SI unit of heat is the joule.","The SI unit of temperature is the kelvin.")
  assertTrue(SearchPolicy.missingComparisonEvidence(q,sources).isEmpty())
 }
 @Test fun weightDefinitionRecognizesDomainAndObjectQualifier() {
  val q="Compare mass and weight: what does each measure and what are their SI units?"
  val definition="In science and engineering, the weight of an object is a quantity associated with the gravitational force exerted on the object by other objects in its environment, although there is some variation and debate as to the exact definition."
  assertEquals(definition,SearchPolicy.comparisonDefinitions(q,definition)["weight"])
  assertTrue(SearchPolicy.comparisonDefinitions(q,"The weight of a body is a force.").containsKey("weight"))
  assertFalse(SearchPolicy.comparisonDefinitions(q,"A report says the weight of an object is a force.").containsKey("weight"))
  assertFalse(SearchPolicy.comparisonDefinitions(q,"In science and engineering, the weight of an object is important.").containsKey("weight"))
  assertFalse(SearchPolicy.comparisonDefinitions(q,definition.dropLast(1)).containsKey("weight"))
 }
}
