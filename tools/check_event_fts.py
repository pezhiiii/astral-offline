"""SQLite FTS4 query regression; does not execute Kotlin/Android code."""
import sqlite3
c=sqlite3.connect(':memory:')
c.execute('CREATE VIRTUAL TABLE passages USING fts4(title,body,tokenize=unicode61)')
c.executemany('INSERT INTO passages VALUES(?,?)', [('Unrelated '+str(i),'A German institute existed in 1903; football was mentioned.') for i in range(1000)])
c.execute('INSERT INTO passages VALUES(?,?)',('1903 German football championship','A national tournament.'))
old='"happened" AND "1903" AND "german" AND "football" AND "championship"'
new='title:1903 AND title:german AND title:football AND title:championship'
def rows(q):return c.execute('SELECT title FROM passages WHERE passages MATCH ? ORDER BY rowid LIMIT 80',(q,)).fetchall()
assert rows(old)==[]
assert rows(new)==[('1903 German football championship',)]
assert rows('title:1904 AND title:german AND title:football AND title:championship')==[]
print('PASS: old intent-word query misses target; new subject-title query finds it beyond 1000 distractors; missing year returns no match.')
