package services;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class DataService {
    Properties properties;
    Connection connection;
    Statement statement;

    public DataService(Properties properties) throws SQLException {
        this.properties = properties;
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        statement = connection.createStatement();

        setupConnection();
    }

    private void setupConnection() throws SQLException {
        String setupQuery = "CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, steamID VARCHAR(250) NOT NULL, PRIMARY KEY (user_id));";
        statement.execute(setupQuery);

        setupQuery = "CREATE TABLE IF NOT EXISTS wow (wow_id INT AUTO_INCREMENT, f_user_id INT NOT NULL, url VARCHAR(200) NOT NULL, PRIMARY KEY(wow_id), FOREIGN KEY (f_user_id) REFERENCES users(user_id));";
        statement.execute(setupQuery);
    }

    public String getSteamIDForUser(String requestedUser) throws SQLException {
        String query = "SELECT * FROM users WHERE user = '" + requestedUser + "'";
        ResultSet resultSet = statement.executeQuery(query);

        while(resultSet.next()) {
            return resultSet.getString("steamID");
        }
        return "";
    }

    public void addWowEvent(String username, String url) throws SQLException {
        String query = "INSERT INTO wow(f_user_id, url) VALUES('" + getUserIDForUsername(username) + "', '" + url + "');";
        statement.execute(query);
    }

    public void updateWowEvent(String username, String url) throws SQLException {
        String query = "UPDATE wow SET url = '" + url + "' WHERE f_user_id = '" + getUserIDForUsername(username) + "';";
        statement.execute(query);
    }

    public HashMap<String, String> returnAllWowEntries() throws SQLException {
        HashMap<String, String> returnMap = new HashMap<String, String>();
        String query = "SELECT u.username, w.url FROM wow AS w LEFT JOIN users AS u ON w.f_user_id = u.user_id";
        try (ResultSet resultSet = statement.executeQuery(query)) {
            while(resultSet.next()) {
                returnMap.put(resultSet.getString("u.username"), resultSet.getString("w.url"));
            }
        }
        return returnMap;
    }

    private int getUserIDForUsername(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users where username = '" + username + "'";
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            int returnedRows = resultSet.getInt(1);
            if (returnedRows < 1) {
                throw new SQLException("More than one user found for the same username.");
            } else if (returnedRows == 0) {
                query = "INSERT INTO users VALUES ('" + username + "', '')";
                statement.execute(query);
            }
        }

        query = "SELECT user_id FROM users WHERE username = '" + username + "'";
        resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            return resultSet.getInt(1);
        }
        throw new SQLException("No userID can be returned");
    }
}
