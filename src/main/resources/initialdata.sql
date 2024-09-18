CREATE TABLE IF NOT EXISTS s4032_csbot.users (user_id INT AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, steamID VARCHAR(250) UNIQUE, faceitID VARCHAR(250) UNIQUE, discordID VARCHAR(250) UNIQUE, hasGregflix boolean NOT NULL, PRIMARY KEY (user_id));
CREATE TABLE IF NOT EXISTS s4032_csbot.wow (wow_id INT AUTO_INCREMENT, f_user_id INT NOT NULL, url VARCHAR(200) NOT NULL, PRIMARY KEY(wow_id), FOREIGN KEY (f_user_id) REFERENCES users(user_id));
CREATE TABLE IF NOT EXISTS s4032_csbot.gregflix(title VARCHAR(200), imdbid VARCHAR(20), uploaded boolean, PRIMARY KEY(imdbid));

UPDATE s4032_csbot.users SET steamID = "76561198077352267" WHERE username = "aatha";
UPDATE s4032_csbot.users SET steamID = "76561198213130649" WHERE username = "dase8760";
UPDATE s4032_csbot.users SET steamID = "76561198316963738" WHERE username = "vantriko";
UPDATE s4032_csbot.users SET steamID = "76561198014462666" WHERE username = "jay_th";
UPDATE s4032_csbot.users SET steamID = "76561198098219020" WHERE username = "juan828";
UPDATE s4032_csbot.users SET steamID = "76561198071064798" WHERE username = "korunde";
UPDATE s4032_csbot.users SET steamID = "76561198203636285" WHERE username = "nassim1234";
UPDATE s4032_csbot.users SET steamID = "76561198088520949" WHERE username = "nbldrifter";
UPDATE s4032_csbot.users SET steamID = "76561198401419666" WHERE username = "nigglz";
UPDATE s4032_csbot.users SET steamID = "76561198071074164" WHERE username = "vi24ra";
UPDATE s4032_csbot.users SET steamID = "76561198102224384" WHERE username = "pavi9028";
UPDATE s4032_csbot.users SET steamID = "76561197984892194" WHERE username = "sani1991";

INSERT INTO s4032_csbot.wow(f_user_id, url) VALUES( (SELECT user_id FROM s4032_csbot.users WHERE username = 'jay_th') , "https://cdn.discordapp.com/attachments/449281855175393280/1221510017354563674/loud.mov");
INSERT INTO s4032_csbot.wow(f_user_id, url) VALUES( (SELECT user_id FROM s4032_csbot.users WHERE username = 'vi24ra') , "https://cdn.discordapp.com/attachments/288367861515419649/1167948820525621248/Dropshot.mp4");
INSERT INTO s4032_csbot.wow(f_user_id, url) VALUES( (SELECT user_id FROM s4032_csbot.users WHERE username = 'aatha') , "https://cdn.discordapp.com/attachments/844510835241910303/1225082807110336653/Me_Is_Sorry_Janes.mp4");
INSERT INTO s4032_csbot.wow(f_user_id, url) VALUES( (SELECT user_id FROM s4032_csbot.users WHERE username = 'CSBot') , "https://www.youtube.com/watch?v=2qTHmSyqrok");
INSERT INTO s4032_csbot.wow(f_user_id, url) VALUES( (SELECT user_id FROM s4032_csbot.users WHERE username = 'korunde') , "https://www.youtube.com/watch?v=Ygjd-t8btgY");