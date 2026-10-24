package org.bl3.astral
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Locale

data class Passage(val id: Long, val title: String, val text: String)
class Library(context: Context): SQLiteOpenHelper(context, "knowledge.db", null, 1) {
 override fun onCreate(db: SQLiteDatabase) {
  db.execSQL("CREATE VIRTUAL TABLE passages USING fts4(title, body, tokenize=unicode61)")
 }
 override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) { error("Unsupported schema migration") }
 fun importText(title: String, text: String): Int {
  require(title.isNotBlank() && text.isNotBlank()) { "Empty document" }
  val words=text.split(Regex("\\s+")); var count=0
  writableDatabase.beginTransaction()
  try {
   var start=0
   while(start<words.size) {
    val end=minOf(start+220,words.size)
    val values=android.content.ContentValues().apply { put("title", title); put("body", words.subList(start,end).joinToString(" ")) }
    writableDatabase.insertOrThrow("passages",null,values);count++
    if(end==words.size) break
    start+=180
   }
   writableDatabase.setTransactionSuccessful()
  } finally { writableDatabase.endTransaction() }
  return count
 }
 fun count(): Long = readableDatabase.rawQuery("SELECT count(*) FROM passages",null).use { it.moveToFirst(); it.getLong(0) }
 fun search(question: String): List<Passage> {
  val stop=setOf("what","which","the","is","are","of","a","an","and","to","in","how","does","do","for","between","compare","explain")
  val terms=Regex("[\\p{L}\\p{N}]{2,}").findAll(question.lowercase(Locale.ROOT)).map { it.value }.filter { it !in stop }.distinct().take(18).toList()
  if(terms.isEmpty()) return emptyList()
  val match=terms.joinToString(" OR ") { "\"$it\"" }
  val candidates=mutableListOf<Passage>()
  readableDatabase.rawQuery("SELECT rowid,title,body FROM passages WHERE passages MATCH ? LIMIT 120",arrayOf(match)).use { c ->
   while(c.moveToNext()) candidates+=Passage(c.getLong(0),c.getString(1),c.getString(2))
  }
  return candidates.sortedByDescending { p ->
   val body=p.text.lowercase(Locale.ROOT); val title=p.title.lowercase(Locale.ROOT)
   terms.sumOf { t -> (if(title.contains(t)) 4 else 0) + (if(body.contains(t)) 1 else 0) }
  }.distinctBy { it.text }.take(4)
 }
}
