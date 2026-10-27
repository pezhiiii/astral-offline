"""Prepare an Astral pack from English Wikipedia's May 2026 article abstracts.
Run on a laptop: pip install datasets; python tools/build_wikipedia_2026.py wikipedia2026.db
The entire English parquet dataset is large. This builds an abstract-only index.
"""
import argparse
import os
import sqlite3


def build(rows, destination, limit=None):
    if os.path.exists(destination):
        raise FileExistsError('Choose a new output name; existing files are never overwritten.')
    db = sqlite3.connect(destination)
    count = 0
    try:
        db.execute('CREATE VIRTUAL TABLE passages USING fts4(title, body, tokenize=unicode61)')
        for row in rows:
            title = row.get('name')
            abstract = row.get('abstract')
            url = row.get('url')
            if not all(isinstance(v, str) and v.strip() for v in (title, abstract, url)):
                continue
            words = abstract.split()
            label = f'{title} — {url} (Wikipedia, May 2026 snapshot; abstract)'
            for start in range(0, len(words), 180):
                db.execute('INSERT INTO passages(title,body) VALUES (?,?)', (label, ' '.join(words[start:start+220])))
                if start + 220 >= len(words):
                    break
            count += 1
            if count % 1000 == 0:
                db.commit()
                print(f'{count} articles indexed', flush=True)
            if limit and count >= limit:
                break
        db.commit()
        print(f'Created {destination}: {count} article abstracts', flush=True)
    finally:
        db.close()

if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('output')
    p.add_argument('--articles', type=int, default=None)
    args = p.parse_args()
    from datasets import load_dataset
    rows = load_dataset('parquet', data_files='hf://datasets/wikimedia/structured-wikipedia/enwiki/data/*.parquet', split='train', streaming=True)
    build(rows, args.output, args.articles)
