DROP TABLE badges;
CREATE TABLE badges (id INTEGER PRIMARY KEY AUTOINCREMENT, badgeName TEXT, badgeDesc TEXT,
createdOn TEXT);
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Beginner', 'Add 30 Phrases to your Phrasebook');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Doing Good', '30 Correct Guesses in Practice');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Novice', 'Add 100 Phrases to your Phrasebook');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Novice', '150 Correct Guesses in Practice');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Expert', 'Add 250 Phrases to your Phrasebook');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Beacon of Light', '300 Correct Guesses in Practice');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Keep Going', '10 Correct Guesses in Practice in one day');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Greedy', 'Add 20 Phrases in one day');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('High Fidelity', '20 Correct Guesses in Practice in a row');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Not Too Shabby', '10 Incorrect Guesses in Practice in a row');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('I Like It Difficult', 'Add one 25 characters phrase to your Phrasebook');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Get On My Level', 'Add 5 phrases of 25 characters to your Phrasebook');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Rise and Shine', '5 Correct Guesses in Practice between 04:00 and 10:00');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Night Owl', '5 Correct Guesses in Practice between 19:00 and 23:00');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('No Sleep', '5 Correct Guesses in Practice between 00:00 and 06:00');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Inspiring Dreams', 'Add 5 Phrases to your Phrasebook between 00:00 and 06:00');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Sudden Inspiration', 'Add 15 Phrases to your Phrasebook within 15 minutes');
INSERT INTO badges (badgeName,badgeDesc)
VALUES ('Extreme Stamina', '15 Correct Guesses in Practice within 15 minutes');