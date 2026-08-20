package services;

import http.CarthageConnection;
import http.CarthageException;

import java.io.IOException;
import java.util.Properties;

public class DataService {
    CarthageConnection carthageConnection;
    String botID;

    public DataService(Properties properties) {
        this.carthageConnection = new CarthageConnection(properties);
    }

    public void setBotID(String botID) {
        this.botID = botID;
    }

    public String getSteamIDForDiscordID(String discordID) throws CarthageException {
        try {
            return carthageConnection.getSteamIDForDiscordID(botID, discordID);
        } catch (IOException | InterruptedException ex) {
            throw new CarthageException("Failed to fetch steamID for discordID " + discordID + " from carthage: " + ex.getMessage());
        }
    }
}
