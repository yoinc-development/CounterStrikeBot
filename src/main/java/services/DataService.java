package services;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class DataService {
    Properties properties;
    Connection connection;

    public DataService(Properties properties) throws SQLException {
        this.properties = properties;
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        setupConnection();
    }

    private void setupConnection() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, steamID VARCHAR(250) NOT NULL, PRIMARY KEY (user_id));");
        preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS wow (wow_id INT AUTO_INCREMENT, f_user_id INT NOT NULL, url VARCHAR(200) NOT NULL, PRIMARY KEY(wow_id), FOREIGN KEY (f_user_id) REFERENCES users(user_id));");
        preparedStatement.executeUpdate();
    }

    public String getSteamIDForUser(String requestedUser) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE user = ?");
        preparedStatement.setString(1, requestedUser);
        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            return resultSet.getString("steamID");
        }
        return null;
    }

    public void addWowEvent(String username, String url) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO wow(f_user_id, url) VALUES(?,?)");
        preparedStatement.setInt(1, getUserIDForUsername(username));
        preparedStatement.setString(2, url);
        preparedStatement.execute();
    }

    public void updateWowEvent(String username, String url) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE wow SET url = ? WHERE f_user_id = ?");
        preparedStatement.setString(1, url);
        preparedStatement.setInt(2, getUserIDForUsername(username));
        preparedStatement.execute();
    }

    public String getUsernameForFaceitID(String faceitID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT u.username FROM users AS u WHERE u.faceitID = ?");
        preparedStatement.setString(1, faceitID);
        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            return resultSet.getString("u.username");
        }
        throw new SQLException("No user for FaceitID could be found");
    }

    public HashMap<String, String> getAllWowEntries() throws SQLException {
        HashMap<String, String> returnMap = new HashMap<String, String>();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT u.username, w.url FROM wow AS w LEFT JOIN users AS u ON w.f_user_id = u.user_id");
        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            returnMap.put(resultSet.getString("u.username"), resultSet.getString("w.url"));
        }
        return returnMap;
    }

    private int getUserIDForUsername(String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            int returnedRows = resultSet.getInt(1);
            if (returnedRows < 1) {
                throw new SQLException("More than one user found for the same username.");
            } else if (returnedRows == 0) {
                preparedStatement = connection.prepareStatement("INSERT INTO users VALUES (?,'')");
                preparedStatement.setString(1, username);
                preparedStatement.execute();
            }
        }

        preparedStatement = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
        preparedStatement.setString(1, username);
        resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            return resultSet.getInt(1);
        }
        throw new SQLException("No userID can be returned");
    }
}
