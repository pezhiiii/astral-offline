package org.bl3.astral
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
  val terms=SearchPolicy.terms(question)
  if(terms.isEmpty()) return emptyList()
  val match=terms.joinToString(" OR ") { "\"$it\"" }
  val candidates=mutableListOf<Passage>()
  readableDatabase.rawQuery("SELECT rowid,title,body FROM passages WHERE passages MATCH ? ORDER BY rowid LIMIT 120",arrayOf(match)).use { c ->
   while(c.moveToNext()) candidates+=Passage(c.getLong(0),c.getString(1),c.getString(2))
  }
  return candidates.map { it to SearchPolicy.score(terms,it.title,it.text) }
   .filter { it.second>0 }
   .sortedWith(compareByDescending<Pair<Passage,Int>> { it.second }.thenBy { it.first.id })
   .map { it.first }.distinctBy { it.text }.take(4)
 }
}
