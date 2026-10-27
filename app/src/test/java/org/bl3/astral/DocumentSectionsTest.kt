package org.bl3.astral

import org.junit.Assert.*
import org.junit.Test

class DocumentSectionsTest {
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
  assertTrue(sections[1].body.contains("https://example.org/eco"))
  assertFalse(sections[1].body.contains("second"))
  assertEquals("wiki.txt / Solar System — https://example.org/solar",sections[2].title)
 }
}
