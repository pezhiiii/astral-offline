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
  "meet meets off one two three four five six seven eight nine ten his her").split(' ').toSet()

 fun tokens(text: String): Set<String> = word.findAll(text.lowercase(Locale.ROOT))
  .map { it.value }.toSet()

 fun terms(question: String): List<String> = tokens(question).filter { it !in stop }.take(32)

 /** For a specific dated event, keep passages from its matching title together.
  * Fall back to the existing ranking when no strong title match is available. */
 fun focusedTitle(question: String, titles: List<String>): String? {
  if(Regex("(?i)\\b(compare|comparison|versus|difference|differences|vs)\\b").containsMatchIn(question)) return null
  val queryTerms=terms(question).filter { it !in setOf("happened", "happen", "won", "winner", "result", "outcome") }
  val years=queryTerms.filter { it.length==4 && it.all { c -> c in '0'..'9' } }
  if(years.size!=1 || queryTerms.size<3) return null
  return titles.firstOrNull { title ->
   val words=tokens(title.substringBefore(" — "))
   queryTerms.all { it in words }
  }?.substringBefore(" — ")
 }

 /** Conservative lexical gate for a single dated subject. Shared years alone
  * are not evidence of relevance. URLs are excluded from the title match.
  * This can reject synonyms; it is not a semantic entailment check. */
 fun acceptsDatedSubject(question: String, title: String, body: String): Boolean {
  if(Regex("(?i)\\b(compare|comparison|versus|difference|differences|vs)\\b").containsMatchIn(question)) return true
  val subject=terms(question.substringBefore('?')).filter {
   it !in setOf("happened", "happen", "won", "winner", "result", "outcome")
  }
  val years=subject.filter { it.length==4 && it.all { c -> c in '0'..'9' } }
  if(years.size!=1 || subject.size<3) return true
  val available=tokens(title.substringBefore(" — ")+" "+body)
  return subject.all { it in available }
 }

 /** Query the actual subject before broad term fallbacks. Keep intent words
  * in terms() for excerpt selection, but do not require them in documents. */
 fun candidateQueries(question: String): List<String> {
  val queryTerms=terms(question)
  val anchor=subjectAnchor(question) ?: definitionAnchor(question,queryTerms)
  if(anchor!=null) return listOf("title:$anchor", "body:$anchor")
  val focus=queryTerms.filter { it !in setOf("happened", "happen", "won", "winner", "result", "outcome", "different", "difference", "differences", "between", "versus", "vs", "comparison") }.take(8)
  if(focus.isEmpty()) return emptyList()
  val exactTitle=focus.joinToString(" AND ") { "title:$it" }
  val exact=focus.joinToString(" AND ") { "\"$it\"" }
  val pairs=focus.take(4).flatMapIndexed { i,a -> focus.take(4).drop(i+1).map { b -> "\"$a\" AND \"$b\"" } }
  return (listOf(exactTitle,exact)+pairs+focus.flatMap { listOf("title:$it","body:$it") }).distinct()
 }

 /** Lexical subject gate, not a claim verifier. Reject incidental mentions and
  * require comparison subjects to occur together in the candidate passage. */
 fun acceptsSubject(question: String, title: String, body: String): Boolean {
  if(isComparison(question)) return comparisonEvidence(question,body)!=null
  val titleWords=tokens(title.substringBefore(" — "))
  val bodyWords=tokens(body)
  val anchor=subjectAnchor(question) ?: definitionAnchor(question,terms(question))
  if(anchor!=null) {
   if(anchor in titleWords) return true
   val escaped=Regex.escape(anchor)
   val defining=directDefinition(escaped)
   return defining.containsMatchIn(body)
  }
  val subject=terms(question.substringBefore('?')).filter {
   it !in setOf("different", "difference", "differences", "between", "versus", "vs", "happened", "happen", "won", "winner", "result", "outcome", "contributions", "contribution")
  }
  if(subject.isEmpty()) return false
  // Simple inflections: cell/cells, plant/plants. Do not use substring matching.
  fun forms(term: String)=if(term.endsWith("s") && !term.endsWith("ss") && term.length>3) setOf(term,term.dropLast(1)) else setOf(term,term+"s")
  val available=titleWords+bodyWords
  return subject.all { forms(it).any { form -> form in available } }
 }

 fun isComparison(question: String): Boolean = Regex("(?i)\\b(compare|comparison|different|difference|differences|versus|vs)\\b").containsMatchIn(question)

 /** Parse only explicit two-topic comparisons; qualifiers stay in the model question. */
 fun comparisonTopics(question: String): List<String> {
  val head=question.substringBefore(',').substringBefore('?').substringBefore('.')
  val match=Regex("(?i)^\\s*(?:compare\\s+|(?:what\\s+(?:is|are)\\s+)?(?:the\\s+)?differences?\\s+between\\s+)(.+?)\\s+(?:and|versus|vs)\\s+(.+)$").find(head) ?: return emptyList()
  return match.groupValues.drop(1).map { it.trim().lowercase(Locale.ROOT).removePrefix("the ") }
   .takeIf { it.size==2 && it.distinct().size==2 && it.all { t -> tokens(t).isNotEmpty() && tokens(t).size<=5 } } ?: emptyList()
 }

 /** A lexical omission check only; mentioning both topics does not prove correctness. */
 fun missingComparisonTopics(question: String, answer: String): List<String> {
  val content=answer.replace(Regex("(?m)^\\s*[^\\n:]{1,80}:\\s*$"),"")
  val available=tokens(content)
  return comparisonTopics(question).filter { topic ->
   tokens(topic).any { term ->
    val base=if(term.endsWith("s") && !term.endsWith("ss") && term.length>3) term.dropLast(1) else term
    term !in available && base !in available && base+"s" !in available
   }
  }
 }

 /** Definitions may come from separate passages. Incidental mentions do not qualify. */
 fun comparisonDefinitions(question: String, body: String): Map<String,String> {
  val sentences=DocumentSections.quotationBody(body).split(Regex("(?<=[.!?])\\s+|\\n+"))
  return comparisonTopics(question).mapNotNull { topic ->
   val defining=Regex("(?i)^\\s*(?:the\\s+)?"+Regex.escape(topic)+"\\s+(?:is|are|refers\\s+to|means)\\b")
   sentences.firstOrNull { defining.containsMatchIn(it) && Regex("[.!?]$").containsMatchIn(it.trim()) && it.split(Regex("\\s+")).size<=70 }
    ?.let { topic to it.trim() }
  }.toMap()
 }

 fun comparisonEvidence(question: String, body: String): String? =
  comparisonQuote(question,body) ?: comparisonDefinitions(question,body).values.distinct().joinToString("\n").ifBlank { null }

 /** Keep verbatim context around the selected comparison, within a fixed budget. */
 fun comparisonContext(question: String, body: String, maxWords: Int = 120): String? {
  val seed=comparisonEvidence(question,body) ?: return null
  val sentences=DocumentSections.quotationBody(body).split(Regex("(?<=[.!?])\\s+|\\n+"))
   .map { it.trim() }.filter { it.isNotEmpty() }
  val selected=sentences.indices.filter { sentences[it] in seed }.toMutableSet()
  if(selected.isEmpty()) return seed
  var used=selected.sumOf { sentences[it].split(Regex("\\s+")).size }
  val topics=comparisonTopics(question).flatMap { tokens(it) }.toSet()
  val neighbors=selected.toList().flatMap { i -> listOf(i-1,i+1,i-2,i+2) }.distinct()
  for(i in neighbors) {
   if(i !in sentences.indices || i in selected) continue
   val sentence=sentences[i]
   if(!Regex("[.!?]$").containsMatchIn(sentence) || tokens(sentence).intersect(topics).isEmpty()) continue
   if(Regex("(?i)\\b(attribution|license|https?|article|source):").containsMatchIn(sentence)) continue
   val n=sentence.split(Regex("\\s+")).size
   if(used+n<=maxWords) { selected.add(i);used+=n }
  }
  return selected.sorted().joinToString(" ") { sentences[it] }
 }

 fun comparisonSourceIndices(question: String, bodies: List<String>): List<Int> {
  val direct=bodies.indexOfFirst { comparisonQuote(question,it)!=null }
  if(direct>=0) return listOf(direct)
  val topics=comparisonTopics(question)
  if(topics.isEmpty()) return emptyList()
  val indices=topics.map { topic -> bodies.indexOfFirst { topic in comparisonDefinitions(question,it) } }
  return if(indices.any { it<0 }) emptyList() else indices.distinct()
 }

 /** Return source sentences verbatim; no synthesized comparison claims.
  * All content subject terms must occur in the same contrast sentence.
  * Conservative lexical extraction, not semantic verification. */
 fun comparisonQuote(question: String, body: String): String? {
  if(!isComparison(question)) return null
  val subjects=(comparisonTopics(question).takeIf { it.isNotEmpty() }?.flatMap { terms(it) } ?: terms(question.substringBefore('?'))).filter {
   it !in setOf("different","difference","differences","between","versus","vs","comparison")
  }
  if(subjects.isEmpty()) return null
  val contrast=Regex("(?i)\\b(whereas|while|unlike|differ|differs|different|difference|differences|lack|lacks|instead)\\b")
  return DocumentSections.quotationBody(body).split(Regex("(?<=[.!?])\\s+|\\n+"))
   .filter { sentence ->
    val words=tokens(sentence)
    comparisonContrast(subjects,sentence) && contrast.containsMatchIn(sentence) && subjects.all { term ->
     val singular=if(term.endsWith("s") && !term.endsWith("ss") && term.length>3) term.dropLast(1) else term
     term in words || singular in words || singular+"s" in words
    }
   }.filter { it.split(Regex("\\s+")).size<=100 }
   .distinct().take(2).joinToString("\n").ifBlank { null }
 }

 /** Reject shared-property statements and contrasts led by an unrelated
  * third subject. Still lexical: no semantic correctness guarantee. */
 private fun comparisonContrast(subjects: List<String>, sentence: String): Boolean {
  if(Regex("(?i)\\b(same|similar|similarly|both|shared)\\b").containsMatchIn(sentence)) return false
  fun base(term: String)=if(term.endsWith("s") && !term.endsWith("ss") && term.length>3) term.dropLast(1) else term
  val wanted=subjects.map { base(it) }.toSet()
  fun firstTopic(text: String): String? = word.findAll(text.lowercase(Locale.ROOT))
   .map { base(it.value) }.firstOrNull { it in wanted }
  fun separated(left: String,right: String): Boolean {
   val l=firstTopic(left);val r=firstTopic(right)
   // A topic may naturally be repeated in the other definition. Use the
   // first topic on each side rather than requiring disjoint vocabularies.
   return l!=null && r!=null && l!=r
  }
  val unlike=Regex("(?i)^\\s*unlike\\s+([^,]+),(.+)$").find(sentence)
  if(unlike!=null) return separated(unlike.groupValues[1],unlike.groupValues[2])
  val leadingWhile=Regex("(?i)^\\s*while\\s+([^,]+),(.+)$").find(sentence)
  if(leadingWhile!=null) return separated(leadingWhile.groupValues[1],leadingWhile.groupValues[2])
  val divider=Regex("(?i)\\b(whereas|while|differs?\\s+from|different\\s+from)\\b").find(sentence) ?: return false
  return separated(sentence.substring(0,divider.range.first),sentence.substring(divider.range.last+1))
 }

 fun subjectAnchor(question: String): String? {
  val match=Regex("(?i)^\\s*who\\s+(?:is|was|were|are)\\s+([^?]+)").find(question) ?: return null
  return terms(match.groupValues[1]).firstOrNull()
 }

 private val genericDefinitionHeads=setOf("capital", "population", "president", "founder", "history", "location", "date", "year", "name", "cause")
 fun definitionAnchor(question: String, terms: List<String>): String? {
  if(isComparison(question)) return null
  if(!Regex("(?i)^\\s*(what is|what are|define)\\b").containsMatchIn(question)) return null
  val first=terms.firstOrNull() ?: return null
  // "capital of France" needs both terms, unlike "what is an ecosystem?".
  return if(terms.size>1 && first in genericDefinitionHeads) null else first
 }

 fun excerpt(text: String, terms: List<String>, maxWords: Int = 90): String {
  val words=text.split(Regex("\\s+"))
  if(words.size<=maxWords) return text
  // Prefer the window containing the most distinct question terms. Ties favor
  // the earlier window, which preserves the previous bias toward introductions.
  val wanted=terms.toSet()
  val wordTokens=words.map { tokens(it).intersect(wanted) }
  val counts=mutableMapOf<String,Int>()
  fun add(index: Int) { for(token in wordTokens[index]) counts[token]=counts.getOrDefault(token,0)+1 }
  fun remove(index: Int) { for(token in wordTokens[index]) { val n=counts.getValue(token)-1; if(n==0) counts.remove(token) else counts[token]=n } }
  for(i in 0 until maxWords) add(i)
  var start=0; var best=counts.size
  for(i in 1..(words.size-maxWords)) {
   remove(i-1);add(i+maxWords-1)
   if(counts.size>best) { best=counts.size;start=i }
  }
  return (if(start>0) "… " else "") + words.subList(start,start+maxWords).joinToString(" ") + if(start+maxWords<words.size) " …" else ""
 }

 /** Include a result-bearing window for event questions when the passage has one.
  * The normal query-focused window is retained to preserve context. */
 /** Whole source sentences for short definition questions. No added claims.
  * Skip over-budget sentences rather than sending a cut-off definition. */
 fun definitionExcerpt(text: String, anchor: String, maxWords: Int = 90): String {
  if(maxWords<=0) return ""
  val cleaned=DocumentSections.quotationBody(text)
  val sentences=cleaned.split(Regex("(?<=[.!?])\\s+|\\n+"))
   .map { it.trim() }.filter { it.isNotEmpty() &&
    !it.startsWith("Attribution:") && !it.startsWith("License:") &&
    !it.startsWith("SOURCE:") && !it.startsWith("ARTICLE:") &&
    Regex("[.!?]$").containsMatchIn(it) }
  val head=Regex("(?i)^(?:the\\s+)?${Regex.escape(anchor)}\\s+(?:is|are|can|refers|means)\\b")
  val ranked=sentences.withIndex().sortedWith(compareByDescending<IndexedValue<String>> {
   (if(head.containsMatchIn(it.value)) 10 else 0) +
    (if(head.containsMatchIn(it.value) && Regex("(?i)\\b(defined|measure|measures)\\b").containsMatchIn(it.value)) 3 else 0)
  }.thenBy { it.index })
  var used=0
  val selected=mutableListOf<IndexedValue<String>>()
  for(item in ranked) {
   val count=item.value.split(Regex("\\s+")).size
   if(used+count<=maxWords && tokens(item.value).contains(anchor)) {
    selected.add(item);used+=count
   }
  }
  return selected.sortedBy { it.index }.joinToString("\n") { it.value }
 }

 fun eventExcerpt(text: String, terms: List<String>, maxWords: Int): String {
  val standard=excerpt(text,terms,maxWords)
  if(terms.none { it in setOf("happened", "won", "winner", "result", "outcome") }) return standard
  val words=text.split(Regex("\\s+"))
  val result=words.indexOfFirst { tokens(it).any { token -> token in setOf("won", "winner", "defeated", "beat", "awarded", "victorious") } }
  if(result<0) return standard
  // Center the result word: a matching word at the end of a window can omit
  // the winner or opponent, even though the word itself is already present.
  val width=(maxWords / 2).coerceAtLeast(1).coerceAtMost(words.size)
  val start=(result-width/2).coerceIn(0, words.size-width)
  val resultWindow=words.subList(start,start+width).joinToString(" ")
  return if(standard.contains(resultWindow)) standard else standard+"\n… "+resultWindow+(if(start+width<words.size) " …" else "")
 }

 /** Keep ranked first source, preferring a second document for comparison queries. */
 fun modelSourceIndices(question: String, titles: List<String>, limit: Int): List<Int> {
  if(limit<=0 || titles.isEmpty()) return emptyList()
  val count=minOf(limit,titles.size)
  val compare=Regex("(?i)\\b(compare|comparison|versus|difference|differences|vs)\\b").containsMatchIn(question)
  if(!compare || count==1) return (0 until count).toList()
  val first=titles[0].substringBefore(" — ")
  val second=(1 until titles.size).firstOrNull { titles[it].substringBefore(" — ") != first }
  val selected=mutableListOf(0)
  if(second!=null) selected.add(second)
  for(i in 1 until titles.size) if(selected.size<count && i !in selected) selected.add(i)
  return selected.take(count)
 }

 fun score(terms: List<String>, title: String, body: String): Int {
  val titleWords=tokens(title.substringBefore(" — "))
  val bodyWords=tokens(body)
  // A requested year is often the main distinguishing feature between near-identical
  // articles (for example football championships in different years).
  return terms.sumOf { term ->
   val year=term.length==4 && term.all { it in '0'..'9' }
   val subject=terms.size>1 && terms.first() in genericDefinitionHeads && term !in genericDefinitionHeads
   (if(term in titleWords) if(year) 24 else if(subject) 8 else 4 else 0) +
    (if(term in bodyWords) if(year) 3 else if(subject) 6 else 1 else 0)
  }
 }

 /** Prefer outcome-bearing passages within an already identified event title. */
 fun eventRank(question: String, title: String, body: String): Int {
  if(terms(question).none { it in setOf("happened", "won", "winner", "result", "outcome") }) return 0
  if(focusedTitle(question,listOf(title))==null) return 0
  return if(tokens(body).any { it in setOf("won", "winner", "defeated", "beat", "awarded", "victorious") }) 12 else 0
 }

 /** Rank definition-bearing passages above bibliography-like passages. */
 /** Require the requested term to lead a sentence, rather than be the
  * tail of a different subject such as "conservation of mass". */
 private fun directDefinition(escaped: String): Regex = Regex(
  "(?i)(?:^|[.!?]\\s+|\\n+|={2,}\\s*)(?:the\\s+)?$escaped\\s+(?:is|are|was|were|refers to|means|consists of)\\b"
 )

 fun definitionRank(anchor: String?, body: String): Int {
  if(anchor==null) return 0
  val escaped=Regex.escape(anchor)
  val definition=directDefinition(escaped)
  val citations=Regex("(?i)\\b(?:doi|isbn|retrieved|archived|journal|bibliography)\\b")
   .findAll(body).take(4).count()
  return (if(definition.containsMatchIn(body)) 8 else 0) - citations * 2
 }
}
