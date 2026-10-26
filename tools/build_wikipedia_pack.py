"""Build an Astral FTS4 pack from Wikimedia's 2023 English Wikipedia dataset.
Requires: pip install datasets. Run on a computer with internet and ample disk space.
"""
import argparse
import sqlite3
from urllib.parse import quote


def build(rows, output, limit=None):
    db = sqlite3.connect(output)
    try:
        db.execute('CREATE VIRTUAL TABLE passages USING fts4(title, body, tokenize=unicode61)')
        count = 0
        for row in rows:
            title = str(row.get('title') or '').strip()
            words = str(row.get('text') or '').split()
            if not title or not words:
                continue
            url = str(row.get('url') or ('https://en.wikipedia.org/wiki/' + quote(title.replace(' ', '_'))))
            source = title + ' — ' + url + ' (Wikipedia, 2023-11-01 snapshot)'
            for start in range(0, len(words), 180):
                body = ' '.join(words[start:start + 220])
                db.execute('INSERT INTO passages(title,body) VALUES (?,?)', (source, body))
                count += 1
                if start + 220 >= len(words):
                    break
            if count % 10000 < 20:
                db.commit()
            if limit is not None:
                limit -= 1
                if limit <= 0:
                    break
        db.commit()
        print(f'Created {output}: {count} passages')
    finally:
        db.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('output')
    parser.add_argument('--articles', type=int, default=None, help='optional article limit; omit for full dataset')
    args = parser.parse_args()
    from datasets import load_dataset
    rows = load_dataset('wikimedia/wikipedia', '20231101.en', split='train', streaming=True)
    build(rows, args.output, args.articles)
