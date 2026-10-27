package org.bl3.astral
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Passage(val id: Long, val title: String, val text: String)
class Library(private val context: Context): SQLiteOpenHelper(context, "knowledge.db", null, 1) {
 private val pack get()=java.io.File(context.filesDir,"knowledge-pack.db")
 fun packFile()=pack
 fun validatePack(file: java.io.File) {
  SQLiteDatabase.openDatabase(file.path,null,SQLiteDatabase.OPEN_READONLY).use { db ->
   db.rawQuery("SELECT title,body FROM passages LIMIT 1",null).use { it.moveToFirst() }
   db.rawQuery("SELECT rowid FROM passages WHERE passages MATCH ? LIMIT 1",arrayOf("astral" )).use { it.moveToFirst() }
  }
 }
 override fun onCreate(db: SQLiteDatabase) {
  db.execSQL("CREATE VIRTUAL TABLE passages USING fts4(title, body, tokenize=unicode61)")
 }
 override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) { error("Unsupported schema migration") }
 fun importText(title: String, text: String): Int {
  require(title.isNotBlank() && text.isNotBlank()) { "Empty document" }
  val digest=java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
  val fingerprint=digest.joinToString("") { "%02x".format(it.toInt() and 0xff) }
  val db=writableDatabase
  var count=0
  db.beginTransaction()
  try {
   db.execSQL("CREATE TABLE IF NOT EXISTS import_fingerprints (filename TEXT NOT NULL, sha256 TEXT NOT NULL, PRIMARY KEY(filename,sha256))")
   val alreadyImported=db.rawQuery("SELECT 1 FROM import_fingerprints WHERE filename=? AND sha256=? LIMIT 1",arrayOf(title,fingerprint)).use { it.moveToFirst() }
   if(alreadyImported) { db.setTransactionSuccessful(); return 0 }
   val sections=DocumentSections.split(title,text)
   for(section in sections) {
   val words=section.body.split(Regex("\\s+"))
   var start=0
   while(start<words.size) {
    val end=minOf(start+220,words.size)
    val values=android.content.ContentValues().apply { put("title", section.title); put("body", words.subList(start,end).joinToString(" ")) }
    writableDatabase.insertOrThrow("passages",null,values);count++
    if(end==words.size) break
    start+=180
   }
   }
   val record=android.content.ContentValues().apply { put("filename",title);put("sha256",fingerprint) }
   db.insertOrThrow("import_fingerprints",null,record)
   db.setTransactionSuccessful()
  } finally { db.endTransaction() }
  return count
 }
 fun sourceSummary(): List<Pair<String,Long>> {
  val result=mutableListOf<Pair<String,Long>>()
  readableDatabase.rawQuery("SELECT title, count(*) FROM passages GROUP BY title ORDER BY title LIMIT 201",null).use { c ->
   while(c.moveToNext()) result.add(c.getString(0) to c.getLong(1))
  }
  return result
 }
 fun count(): Long = readableDatabase.rawQuery("SELECT count(*) FROM passages",null).use { it.moveToFirst(); it.getLong(0) }
 fun search(question: String): List<Passage> {
  val terms=SearchPolicy.terms(question)
  if(terms.isEmpty()) return emptyList()
  val candidates=mutableListOf<Passage>()
  // Focus definition questions on their subject; generic words like "living" do not
  // justify sending unrelated documents to the language model.
  val anchor=SearchPolicy.subjectAnchor(question) ?: SearchPolicy.definitionAnchor(question,terms)
  val focus=terms.take(8)
  val pairs=focus.take(4).flatMapIndexed { i,a -> focus.take(4).drop(i+1).map { b -> "\"$a\" AND \"$b\"" } }
  val queries=if(anchor!=null) listOf("title:$anchor", "body:$anchor") else
   (listOf(focus.joinToString(" AND ") { "\"$it\"" }) + pairs + focus.flatMap { listOf("title:$it", "body:$it") }).distinct()
  val databases=mutableListOf(readableDatabase)
  val external=if(pack.exists()) SQLiteDatabase.openDatabase(pack.path,null,SQLiteDatabase.OPEN_READONLY) else null
  if(external!=null) databases.add(external)
  try { for ((dbIndex,db) in databases.withIndex()) for (query in queries) {
   db.rawQuery("SELECT rowid,title,body FROM passages WHERE passages MATCH ? ORDER BY rowid LIMIT 80",arrayOf(query)).use { c ->
    while(c.moveToNext()) candidates+=Passage(if(dbIndex==0) c.getLong(0) else -c.getLong(0),c.getString(1),c.getString(2))
   }
  }
  } finally { external?.close() }
  val perTitle=mutableMapOf<String,Int>()
  val ranked=candidates.distinctBy { it.id }.filter { anchor==null || anchor in SearchPolicy.tokens(it.title.substringBefore(" — ")+" "+it.text) }.map { it to SearchPolicy.score(terms,it.title,it.text) }
   .filter { it.second>0 }
   .sortedWith(compareByDescending<Pair<Passage,Int>> { it.second + SearchPolicy.definitionRank(anchor,it.first.text) }.thenBy { kotlin.math.abs(it.first.id) })
   .map { it.first }.distinctBy { it.text }.filter { val key=it.title.substringBefore(" — "); val n=perTitle.getOrDefault(key,0); perTitle[key]=n+1; n<2 }.take(4)
  val focused=SearchPolicy.focusedTitle(question,ranked.map { it.title })
  return if(focused==null) ranked else ranked.filter { it.title.substringBefore(" — ").equals(focused,ignoreCase=true) }
 }
}
