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
  "please answer question using use requirement requirements requiring " +
  "meet meets off").split(' ').toSet()

 fun tokens(text: String): Set<String> = word.findAll(text.lowercase(Locale.ROOT))
  .map { it.value }.toSet()

 fun terms(question: String): List<String> = tokens(question).filter { it !in stop }.take(32)

 fun score(terms: List<String>, title: String, body: String): Int {
  val titleWords=tokens(title)
  val bodyWords=tokens(body)
  return terms.sumOf { (if(it in titleWords) 4 else 0) + (if(it in bodyWords) 1 else 0) }
 }
}
