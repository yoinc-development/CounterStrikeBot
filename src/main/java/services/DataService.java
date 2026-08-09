package services;

import http.CarthageConnection;
import http.CarthageException;
import model.retake.RetakePlayer;
import model.steam.SteamUIDConverter;
import model.retake.RankStats;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class DataService {
    Properties properties;
    Connection connection;
    CarthageConnection carthageConnection;
    String botID;

    public DataService(Properties properties) {
        this.properties = properties;
        this.carthageConnection = new CarthageConnection(properties);
    }

    /**
     * The bot's own Discord application ID, sent with every carthage request so the dashboard
     * can check it against its bots table. Only known once JDA is ready, so this is set
     * separately from the constructor - see CounterStrikeBotListener#onReady.
     */
    public void setBotID(String botID) {
        this.botID = botID;
    }

    public String getDiscordIdForUsername(String username) throws CarthageException {
        try {
            return carthageConnection.getDiscordIdForUsername(botID, username);
        } catch (IOException | InterruptedException ex) {
            throw new CarthageException("Failed to fetch discordID for username " + username + " from carthage: " + ex.getMessage());
        }
    }

    public String getSteamIDForDiscordID(String discordID) throws CarthageException {
        try {
            return carthageConnection.getSteamIDForDiscordID(botID, discordID);
        } catch (IOException | InterruptedException ex) {
            throw new CarthageException("Failed to fetch steamID for discordID " + discordID + " from carthage: " + ex.getMessage());
        }
    }

    public void addWowEvent(String discordID, String url) throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO wow(f_user_id, url) VALUES(?,?)");
        preparedStatement.setInt(1, getUserIDForDiscordID(discordID));
        preparedStatement.setString(2, url);
        preparedStatement.executeUpdate();
        connection.close();
    }

    public void updateWowEvent(String discordID, String url) throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE wow SET url = ? WHERE f_user_id = ?");
        preparedStatement.setString(1, url);
        preparedStatement.setInt(2, getUserIDForDiscordID(discordID));
        preparedStatement.executeUpdate();
        connection.close();
    }

    public HashMap<String, String> getAllWowEntries() throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        HashMap<String, String> returnMap = new HashMap<String, String>();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT u.discordID, w.url FROM wow AS w LEFT JOIN users AS u ON w.f_user_id = u.user_id");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            returnMap.put(resultSet.getString("u.discordID"), resultSet.getString("w.url"));
        }
        connection.close();
        return returnMap;
    }

    public RankStats getRanksStatsForDiscordID(String discordID) throws SQLException, CarthageException, NumberFormatException {
        String steamId64 = getSteamIDForDiscordID(discordID);
        if (steamId64 == null) {
            return null;
        }
        String steamId = SteamUIDConverter.getSteamId(Long.parseLong(steamId64));

        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM lvl_base WHERE steam = ?");
        preparedStatement.setString(1, steamId);
        ResultSet resultSet = preparedStatement.executeQuery();

        RankStats rankStats = null;
        if (resultSet.next()) {
            rankStats = mapRowToRankStats(resultSet);
        }
        connection.close();
        return rankStats;
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
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM retake_watchdog");
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean hasSent = resultSet.next() && resultSet.getBoolean("has_sent_invite");
        connection.close();
        return hasSent;
    }

    public String getRetakeInviteMsgId() throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM retake_watchdog WHERE has_sent_invite = 1");
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultId = resultSet.next() ? resultSet.getString("msg_id") : "";
        connection.close();
        return resultId;
    }

    public void addRetakeInvite(String msg_id, String timestamp) throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO retake_watchdog VALUES (?,?,1)");
        preparedStatement.setString(1, msg_id);
        preparedStatement.setString(2, timestamp);
        preparedStatement.executeUpdate();
        connection.close();
    }

    public void removeRetakeInvite() throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM retake_watchdog");
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next() && resultSet.getInt(1) != 0) {
            preparedStatement = connection.prepareStatement("DELETE FROM retake_watchdog WHERE has_sent_invite = 1");
            preparedStatement.executeUpdate();
            connection.close();
        }
    }

    public RetakePlayer getHighestRetakeScoreAndPlayer() throws SQLException {
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        RetakePlayer retakePlayer = null;
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT l.name, l.value FROM lvl_base as l WHERE l.value in (SELECT MAX(value) FROM lvl_base)");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            retakePlayer = new RetakePlayer(resultSet.getString("l.name"), resultSet.getInt("l.value"));
        }
        connection.close();
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
