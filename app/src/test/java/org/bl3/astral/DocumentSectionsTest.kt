package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class DocumentSectionsTest {
 @Test fun oldImportedHeadersAreRemovedAtQuotationTime() {
  val sentence="Weather changes daily, whereas climate describes weather over decades."
  val old="ARTICLE: Weather and climate\nSOURCE: https://example.org/weather\n\n$sentence"
  assertEquals(sentence,DocumentSections.quotationBody(old))
  assertEquals(sentence,SearchPolicy.comparisonQuote("What is the difference between weather and climate?",old))
 }
 @Test fun ordinaryMetadataWordsRemainProse() {
  val text="The ARTICLE: label appears in this sentence.\nSOURCE: a personal observation"
  assertEquals(text,DocumentSections.quotationBody(text))
 }

 @Test fun plainTextRemainsOneDocument() {
  val sections=DocumentSections.split("notes.txt","ordinary text")
  assertEquals(1,sections.size)
  assertEquals("notes.txt",sections[0].title)
  assertEquals("ordinary text",sections[0].body)
 }
 @Test fun collectionKeepsArticleTitlesAndSources() {
  val text="Collection intro\n\nARTICLE: Ecosystem\nSOURCE: https://example.org/eco\nfirst\n\nARTICLE: Solar System\nSOURCE: https://example.org/solar\nsecond"
  val sections=DocumentSections.split("wiki.txt",text)
  assertEquals(3,sections.size)
  assertEquals("wiki.txt / Ecosystem — https://example.org/eco",sections[1].title)
  assertEquals("first",sections[1].body)
  assertEquals("second",sections[2].body)
  assertFalse(sections[1].body.contains("second"))
  assertEquals("wiki.txt / Solar System — https://example.org/solar",sections[2].title)
 }
}
