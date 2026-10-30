package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class ComparisonSourcesTest {
 private val q="Compare evaporation and boiling, including when each can occur. Cite the sources."
 @Test fun separatesSubjectsFromRequestedDetails() {
  assertEquals(listOf("evaporation","boiling"),SearchPolicy.comparisonTopics(q))
 }
 @Test fun combinesDefinitionsFromSeparatePassages() {
  val bodies=listOf("Evaporation is vaporization at a liquid surface.","Boiling is vaporization throughout a liquid.")
  assertEquals(listOf(0,1),SearchPolicy.comparisonSourceIndices(q,bodies))
  assertTrue(SearchPolicy.acceptsSubject(q,"Water",bodies[0]))
 }
 @Test fun incidentalMentionsCannotReplaceMissingDefinition() {
  val bodies=listOf("Evaporation is vaporization at a liquid surface.","The boiling sometimes visible at the limb of the Moon is poor seeing.")
  assertTrue(SearchPolicy.comparisonSourceIndices(q,bodies).isEmpty())
  assertNull(SearchPolicy.comparisonEvidence(q,bodies[1]))
 }
 @Test fun existingContrastCanSatisfyPairWithQualifier() {
  val body="Evaporation occurs at the surface, whereas boiling occurs throughout the liquid."
  assertEquals(listOf(0),SearchPolicy.comparisonSourceIndices(q,listOf(body)))
 }
 @Test fun alsoHandlesAnotherPair() {
  val q3="Compare conduction and convection."
  assertEquals(listOf(0,1),SearchPolicy.comparisonSourceIndices(q3,listOf("Conduction is heat transfer through direct interaction.","Convection is heat transfer by fluid motion.")))
 }
}
