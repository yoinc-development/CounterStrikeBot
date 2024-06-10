package services;

import java.sql.*;
import java.util.HashMap;

public class DataService {

    Connection connection;

    Statement statement;

    public DataService() throws  SQLException {
        connection = DriverManager.getConnection("");
        statement = connection.createStatement();
    }

    public void setupConnection() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS wow (wow_id int PRIMARY KEY AUTO_INCREMENT, user_id varchar(50) NOT NULL, url varchar(200) NOT NULL)";
        statement.execute(query);


    }

    public HashMap<String, String> returnAllWowEntries() throws SQLException{
        HashMap<String, String> returnMap = new HashMap<String, String>();
        String query = "SELECT * FROM wow";

        try (ResultSet resultSet = statement.executeQuery(query)) {
            while(resultSet.next()) {
                returnMap.put(resultSet.getString("user_id"), resultSet.getString("url"));
            }
        }

        return returnMap;
    }
}
