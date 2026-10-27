package org.bl3.astral

fun main() {
 val ram="RAM is temporary working memory used by running applications. Its contents are normally lost when power is turned off. Storage keeps applications and files even when power is off. More RAM helps a device work with more active data at once. More storage provides room for more files, but does not replace RAM."
 val atlas="Fictional test data. Atlas weighs 900 grams. Atlas runs for 8 hours per charge. Atlas works offline."
 val nova="Fictional test data. Nova weighs 600 grams. Nova runs for 5 hours per charge. Nova works offline."
 val query="Compare Atlas and Nova for a trip requiring at least 7 hours of battery life without charging. Which meets the requirement, and what is the weight trade-off? Cite both sources."
 val terms=SearchPolicy.terms(query)
 check(SearchPolicy.score(terms,"memory.txt",ram)==0) { "Unrelated RAM passage retained" }
 check(SearchPolicy.score(terms,"111.txt",atlas)>0)
 check(SearchPolicy.score(terms,"22233.txt",nova)>0)
 check(SearchPolicy.score(SearchPolicy.terms("Explain the difference between RAM and storage in three short sentences."),"memory.txt",ram)>0)
 check(SearchPolicy.score(SearchPolicy.terms("art"),"earth.txt","part cart earth")==0) { "Substring false positive" }
 check(SearchPolicy.score(SearchPolicy.terms("RAM"),"memory.txt","ram")>0)
 check(SearchPolicy.score(SearchPolicy.terms("حافظه"),"test.txt","این حافظه موقت است")>0)
 check(SearchPolicy.terms("the and cite both sources").isEmpty())
 check(SearchPolicy.score(SearchPolicy.terms("zephyrium"),"memory.txt",ram)==0)
 check(SearchPolicy.terms("\" OR * (ram)")==listOf("ram"))
 val eco=SearchPolicy.terms("What is an ecosystem? Explain how living organisms and their environment interact, using the available sources.")
 check(SearchPolicy.definitionAnchor("What is an ecosystem?",eco)=="ecosystem")
 val capital=SearchPolicy.terms("What is the capital of France?")
 check(SearchPolicy.definitionAnchor("What is the capital of France?",capital)==null)
 check(SearchPolicy.score(capital,"Paris","Paris is the capital of France.") > SearchPolicy.score(capital,"Capital","A capital city is the seat of government."))
 val definition="An ecosystem is a community of living organisms and their physical environment."
 val bibliography="Ecosystem ecology. Archived 2017. Retrieved 2020. Journal of Ecology. doi:1234."
 check(SearchPolicy.definitionRank("ecosystem",definition)>SearchPolicy.definitionRank("ecosystem",bibliography))
 check(SearchPolicy.definitionRank(null,definition)==0)
 check(SearchPolicy.score(eco,"Ecosystem — https://en.wikipedia.org/wiki/Ecosystem","living organisms")>SearchPolicy.score(eco,"Aristotle — https://en.wikipedia.org/wiki/Aristotle","living organisms"))
 check(SearchPolicy.excerpt((1..200).joinToString(" "){"word$it"}, listOf("word100")).contains("word100"))
 val eventText="The championship began in 1903. " + (1..100).joinToString(" "){"background$it"} + " Leipzig won the final against Prague."
 check(SearchPolicy.eventExcerpt(eventText, listOf("1903", "happened"), 40).contains("won"))
 check(SearchPolicy.eventExcerpt(eventText, listOf("1903"), 40)==SearchPolicy.excerpt(eventText, listOf("1903"), 40))
 val twoSubjects="Atlas " + (1..80).joinToString(" "){"background$it"} + " Atlas and Nova were compared. " + (1..80).joinToString(" "){"ending$it"}
 val bothExcerpt=SearchPolicy.excerpt(twoSubjects, listOf("atlas", "nova"), 30)
 check(SearchPolicy.tokens(bothExcerpt).containsAll(listOf("atlas", "nova"))) { "Excerpt should keep both query subjects when possible" }
 val football=SearchPolicy.terms("What happened in the 1903 German football championship?")
 val target=SearchPolicy.score(football,"1903 German football championship","The 1903 championship was the first.")
 val other=SearchPolicy.score(football,"1930 German football championship","The 1930 final equalled a record set in 1903.")
 check(target>other) { "Exact year title should outrank a different year" }
 println("Search policy regression checks passed: multi-source inclusion, unrelated-source exclusion, RAM lookup, whole-word boundaries, case, Unicode, empty query, unknown term, punctuation.")
 println("FTS_TERMS="+terms.joinToString("|"))
}
