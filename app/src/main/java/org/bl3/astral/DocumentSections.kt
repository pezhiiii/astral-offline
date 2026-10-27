package org.bl3.astral

/** Recognize collections with ARTICLE and SOURCE header lines; preserve plain documents. */
internal object DocumentSections {
 data class Section(val title: String, val body: String)
 private val header=Regex("(?m)^ARTICLE: ([^\\r\\n]{1,180})\\r?\\nSOURCE: https?://[^\\s]+")
 fun split(filename: String, text: String): List<Section> {
  val matches=header.findAll(text).toList()
  if(matches.isEmpty()) return listOf(Section(filename,text))
  val out=mutableListOf<Section>()
  val prefix=text.substring(0,matches.first().range.first).trim()
  if(prefix.isNotEmpty()) out.add(Section(filename,prefix))
  for(i in matches.indices) {
   val match=matches[i]
   val end=if(i+1<matches.size) matches[i+1].range.first else text.length
   val article=match.groupValues[1].trim()
   val body=text.substring(match.range.first,end).trim()
   if(body.isNotEmpty()) out.add(Section("$filename / $article",body))
  }
  return out
 }
}
