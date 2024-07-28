CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, steamID VARCHAR(250), faceitID VARCHAR(250), PRIMARY KEY (user_id));
CREATE TABLE IF NOT EXISTS wow (wow_id INT AUTO_INCREMENT, f_user_id INT NOT NULL, url VARCHAR(200) NOT NULL, PRIMARY KEY(wow_id), FOREIGN KEY (f_user_id) REFERENCES users(user_id));

INSERT INTO users(username, steamID, faceitID)
VALUES("CSBot", "321664337364385792", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("aatha", "76561198077352267", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("dase8760", "76561198213130649", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("vantriko", "76561198316963738", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("jay_th", "76561198014462666", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("juan828", "76561198098219020", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("korunde", "76561198071064798", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("nbldrifter", "76561198088520949", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("nassim1234", "76561198203636285", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("nigglz", "76561198401419666", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("vi24ra", "76561198071074164", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("pavi9028", "76561198102224384", "");
INSERT INTO users(username, steamID, faceitID)
VALUES("sani1991", "76561197984892194", "");

INSERT INTO wow(f_user_id, url)
VALUES(5, "https://cdn.discordapp.com/attachments/449281855175393280/1221510017354563674/loud.mov");
INSERT INTO wow(f_user_id, url)
VALUES(11, "https://cdn.discordapp.com/attachments/288367861515419649/1167948820525621248/Dropshot.mp4");
INSERT INTO wow(f_user_id, url)
VALUES(2, "https://cdn.discordapp.com/attachments/844510835241910303/1225082807110336653/Me_Is_Sorry_Janes.mp4");
INSERT INTO wow(f_user_id, url)
VALUES(1, "https://www.youtube.com/watch?v=2qTHmSyqrok");
INSERT INTO wow(f_user_id, url)
VALUES(7, "https://www.youtube.com/watch?v=Ygjd-t8btgY");