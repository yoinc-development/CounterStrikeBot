package services;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class DataService {
    Properties properties;
    Connection connection;
    Statement statement;

    public DataService(Properties properties) throws  SQLException {
        this.properties = properties;
        connection = DriverManager.getConnection(properties.getProperty("db.url"));
        statement = connection.createStatement();
    }

    public void setupConnection() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS wow (wow_id int PRIMARY KEY AUTO_INCREMENT, username varchar(50) NOT NULL, url varchar(200) NOT NULL)";
        statement.execute(query);
    }

    public void addWowEvent(String username, String url) throws SQLException {
        String query = "INSERT INTO wow(username, url) VALUES('" + username + "', '" + url + "');";
        statement.execute(query);
    }

    public HashMap<String, String> returnAllWowEntries() throws SQLException{
        HashMap<String, String> returnMap = new HashMap<String, String>();
        String query = "SELECT * FROM wow";
        try (ResultSet resultSet = statement.executeQuery(query)) {
            while(resultSet.next()) {
                returnMap.put(resultSet.getString("username"), resultSet.getString("url"));
            }
        }
        return returnMap;
    }
}
