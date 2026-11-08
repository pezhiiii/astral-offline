package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class EnumerationQuoteTest {
 private val question="What are the main isotopes of carbon?"
 private val intro="There are three main isotopes of carbon."
 private val evidence="$intro Carbon atoms have six protons. The three isotopes are known as carbon-12, carbon-13, and carbon-14."

 @Test fun quotesTheExplicitListWithItsOriginalSourceId() {
  assertEquals(2 to evidence, EnumerationQuote.find(question,listOf(intro,evidence)))
 }

 @Test fun skipsMissingAndTruncatedLists() {
  assertNull(EnumerationQuote.find(question,listOf(intro)))
  assertNull(EnumerationQuote.find(question,listOf("$intro The three isotopes are known as carbon-12, carbon-13 ...")))
  assertNull(EnumerationQuote.find("What is the carbon cycle?",listOf(evidence)))
 }
}
