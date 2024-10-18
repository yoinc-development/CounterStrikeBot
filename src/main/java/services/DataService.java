package services;

import model.retake.RetakePlayer;
import model.steam.SteamUIDConverter;
import model.retake.RankStats;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;

public class DataService {
    Properties properties;
    Connection connection;

    public DataService(Properties properties) throws SQLException {
        this.properties = properties;
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
    }

    public String getDiscordIdForUsername(String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return resultSet.getString("discordID");
        }
        return null;
    }

    public String getSteamIDForDiscordID(String discordID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE discordID = ?");
        preparedStatement.setString(1, discordID);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return resultSet.getString("steamID");
        }
        return null;
    }

    public void addWowEvent(String discordID, String url) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO wow(f_user_id, url) VALUES(?,?)");
        preparedStatement.setInt(1, getUserIDForDiscordID(discordID));
        preparedStatement.setString(2, url);
        preparedStatement.executeUpdate();
    }

    public void updateWowEvent(String discordID, String url) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE wow SET url = ? WHERE f_user_id = ?");
        preparedStatement.setString(1, url);
        preparedStatement.setInt(2, getUserIDForDiscordID(discordID));
        preparedStatement.executeUpdate();
    }

    public void addUserToDatabase(String username, String discordID) {
        try {
            if (!isDiscordIdInDatabase(discordID)) {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, discordID) VALUES(?,?)");
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, discordID);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println("[CSBot - DataService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] Can't add " + username + " with discordID " + discordID);
        }
    }

    private boolean isDiscordIdInDatabase(String discordID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT u.discordID FROM users AS u WHERE u.discordID = ?");
        preparedStatement.setString(1, discordID);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return true;
        }
        return false;
    }

    public String getUsernameForFaceitID(String faceitID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT u.username FROM users AS u WHERE u.faceitID = ?");
        preparedStatement.setString(1, faceitID);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return resultSet.getString("u.username");
        }
        throw new SQLException("No user for FaceitID could be found");
    }

    public HashMap<String, String> getAllWowEntries() throws SQLException {
        HashMap<String, String> returnMap = new HashMap<String, String>();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT u.discordID, w.url FROM wow AS w LEFT JOIN users AS u ON w.f_user_id = u.user_id");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            returnMap.put(resultSet.getString("u.discordID"), resultSet.getString("w.url"));
        }
        return returnMap;
    }

    public RankStats getRanksStatsForDiscordID(String discordID) throws SQLException, NumberFormatException {
        String steamId64 = getSteamIDForDiscordID(discordID);
        String steamId = SteamUIDConverter.getSteamId(Long.parseLong(steamId64));

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM lvl_base WHERE steam = ?");
        preparedStatement.setString(1, steamId);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return mapRowToRankStats(resultSet);
        }
        return null;
    }

    private int getUserIDForDiscordID(String discordID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE discordID = ?");
        preparedStatement.setString(1, discordID);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            int returnedRows = resultSet.getInt(1);
            if (returnedRows < 1) {
                throw new SQLException("More than one user found for the same discordID.");
            } else if (returnedRows == 0) {
                preparedStatement = connection.prepareStatement("INSERT INTO users(discordID) VALUES (?)");
                preparedStatement.setString(1, discordID);
                preparedStatement.executeUpdate();
            }
        }

        preparedStatement = connection.prepareStatement("SELECT user_id FROM users WHERE discordID = ?");
        preparedStatement.setString(1, discordID);
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return resultSet.getInt(1);
        }
        throw new SQLException("No userID can be returned");
    }

    public boolean hasSentRetakeInvite() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM retake_watchdog");
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() && resultSet.getBoolean("has_sent_invite");
    }

    public String getRetakeInviteMsgId() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM retake_watchdog WHERE has_sent_invite = 1");
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? resultSet.getString("msg_id") : "";
    }

    public void addRetakeInvite(String msg_id, String timestamp) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO retake_watchdog VALUES (?,?,1)");
        preparedStatement.setString(1, msg_id);
        preparedStatement.setString(2, timestamp);
        preparedStatement.executeUpdate();
    }

    public void removeRetakeInvite() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM retake_watchdog");
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next() && resultSet.getInt(1) != 0) {
            preparedStatement = connection.prepareStatement("DELETE FROM retake_watchdog WHERE has_sent_invite = 1");
            preparedStatement.executeUpdate();
        }
    }

    public RetakePlayer getHighestRetakeScoreAndPlayer() throws SQLException {
        RetakePlayer retakePlayer = null;

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT l.name, l.value FROM lvl_base as l WHERE l.value in (SELECT MAX(value) FROM lvl_base)");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            retakePlayer = new RetakePlayer(resultSet.getString("l.name"), resultSet.getInt("l.value"));
        }
        return retakePlayer;
    }

    private RankStats mapRowToRankStats(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        int experience = resultSet.getInt("value");
        int rank = resultSet.getInt("rank");
        int kills = resultSet.getInt("kills");
        int deaths = resultSet.getInt("deaths");
        int shoots = resultSet.getInt("shoots");
        int hits = resultSet.getInt("hits");
        int headshots = resultSet.getInt("headshots");
        int assists = resultSet.getInt("assists");
        int roundWin = resultSet.getInt("round_win");
        int roundLose = resultSet.getInt("round_lose");
        long playtime = resultSet.getLong("playtime");
        long lastConnect = resultSet.getLong("lastconnect");
        return new RankStats(name,
                experience, rank, kills, deaths, shoots, hits,
                headshots, assists, roundWin, roundLose, playtime, lastConnect);

    }
}
