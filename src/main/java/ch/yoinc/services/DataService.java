package ch.yoinc.services;

import ch.yoinc.http.CarthageConnection;
import ch.yoinc.http.CarthageException;
import ch.yoinc.model.internal.InternalUser;
import ch.yoinc.model.leetify.LeetifyMatchResponse;
import okhttp3.internal.Internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public List<InternalUser> getAllSteamUsers() throws CarthageException {
        try {
            return carthageConnection.getAllSteamUsers();
        } catch (IOException | InterruptedException ex) {
            throw new CarthageException("Failed to fetch all steam users from carthage: " + ex.getMessage());
        }
    }

    public List<String> insertAndGetNewMatches(List<LeetifyMatchResponse> matches) throws CarthageException {
        try {
            if(matches != null || !matches.isEmpty()) {
                StringBuilder matchesString = new StringBuilder();
                for(LeetifyMatchResponse match : matches) {
                    matchesString.append("\"").append(match.id).append("\",\n");
                }
                return carthageConnection.insertAndGetNewMatches(matchesString.toString());
            }
        } catch (IOException | InterruptedException ex) {
            throw new CarthageException("Failed to insert and get new matches from carthage: " + ex.getMessage());
        }
        return List.of();
    }
}
