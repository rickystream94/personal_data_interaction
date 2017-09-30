CREATE TABLE languages (id INTEGER PRIMARY KEY AUTOINCREMENT, languageName TEXT);
BEGIN TRANSACTION;
ALTER TABLE phrases RENAME TO tmp_phrases;
CREATE TABLE phrases (id INTEGER PRIMARY KEY AUTOINCREMENT,
            lang1Code TEXT,lang2Code TEXT,lang1Value TEXT,lang2Value TEXT,isMastered INTEGER,
            correctCount INTEGER,lastPracticedOn TEXT,createdOn TEXT);
INSERT INTO phrases(id,lang1Value,lang2Value,isMastered,correctCount,createdOn) SELECT id,
motherLangString,foreignLangString,archived,correctCount,createdOn FROM tmp_phrases;
DROP TABLE tmp_phrases;
COMMIT;