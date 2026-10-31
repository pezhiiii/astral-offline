package org.bl3.astral

/** Recognize collections with ARTICLE and SOURCE header lines; preserve plain documents. */
internal object DocumentSections {
 data class Section(val title: String, val body: String)
 private val header=Regex("(?m)^ARTICLE: ([^\\r\\n]{1,180})\\r?\\nSOURCE: (https?://[^\\s]+)")
 /** Also clean previously imported passages without requiring re-import. Only
  * paired, structured headers are removed; ordinary prose is preserved. */
 fun quotationBody(text: String): String {
  val cleaned=header.replace(text, "").trim()
  // Older imports flatten all whitespace when storing passage chunks.
  // Match only a complete ARTICLE + SOURCE pair at the passage start.
  return Regex("^ARTICLE: [^\\r\\n]{1,180}?\\s+SOURCE: https?://[^\\s]+\\s+")
   .replaceFirst(cleaned, "").trim()
 }
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
   val url=match.groupValues[2]
   val body=text.substring(match.range.last+1,end).trim()
   if(body.isNotEmpty()) out.add(Section("$filename / $article — $url",body))
  }
  return out
 }
}
