package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class SearchPolicyTest {
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
}
