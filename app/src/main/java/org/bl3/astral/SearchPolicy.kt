package org.bl3.astral

import java.util.Locale

/** Shared token boundaries for the query and candidate scoring. No model calls. */
internal object SearchPolicy {
 private val word=Regex("[\\p{L}\\p{N}]{2,}")
 private val stop=("a an the is are was were be been being of and or to in on at by " +
  "as it its this that these those with without from for into than then " +
  "what which who how why when where do does did can could would should " +
  "will may might must have has had not no yes if but so also only " +
  "more most less least both each any all some such same other " +
  "compare explain describe summarize cite source sources according " +
  "please answer question using use available their they them requirement requirements requiring " +
  "give given information based evidence direct directly example examples briefly short " +
  "meet meets off").split(' ').toSet()

 fun tokens(text: String): Set<String> = word.findAll(text.lowercase(Locale.ROOT))
  .map { it.value }.toSet()

 fun terms(question: String): List<String> = tokens(question).filter { it !in stop }.take(32)

 fun definitionAnchor(question: String, terms: List<String>): String? =
  if(Regex("(?i)^\\s*(what is|what are|define)\\b").containsMatchIn(question)) terms.firstOrNull() else null

 fun excerpt(text: String, terms: List<String>, maxWords: Int = 90): String {
  val words=text.split(Regex("\\s+"))
  if(words.size<=maxWords) return text
  val anchor=words.indexOfFirst { tokens(it).any { token -> token in terms } }
  val start=if(anchor<0) 0 else (anchor-maxWords/4).coerceIn(0,words.size-maxWords)
  return (if(start>0) "… " else "") + words.subList(start,start+maxWords).joinToString(" ") + if(start+maxWords<words.size) " …" else ""
 }

 fun score(terms: List<String>, title: String, body: String): Int {
  val titleWords=tokens(title.substringBefore(" — "))
  val bodyWords=tokens(body)
  // A requested year is often the main distinguishing feature between near-identical
  // articles (for example football championships in different years).
  return terms.sumOf { term ->
   val year=term.length==4 && term.all { it in '0'..'9' }
   (if(term in titleWords) if(year) 24 else 4 else 0) +
    (if(term in bodyWords) if(year) 3 else 1 else 0)
  }
 }

 /** Rank definition-bearing passages above bibliography-like passages. */
 fun definitionRank(anchor: String?, body: String): Int {
  if(anchor==null) return 0
  val escaped=Regex.escape(anchor)
  val definition=Regex("(?i)\\b$escaped\\s+(?:is|are|refers to|means|consists of)\\b")
  val citations=Regex("(?i)\\b(?:doi|isbn|retrieved|archived|journal|bibliography)\\b")
   .findAll(body).take(4).count()
  return (if(definition.containsMatchIn(body)) 8 else 0) - citations * 2
 }
}
