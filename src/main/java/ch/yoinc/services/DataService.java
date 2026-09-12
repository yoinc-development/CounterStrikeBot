package ch.yoinc.services;

import ch.yoinc.http.CarthageConnection;
import ch.yoinc.http.CarthageException;
import ch.yoinc.model.internal.InternalUser;
import ch.yoinc.model.leetify.LeetifyMatchResponse;

import java.io.IOException;
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

    public String getSteamIDForDiscordID(String discordID) throws IOException, InterruptedException, CarthageException {
        return carthageConnection.getSteamIDForDiscordID(botID, discordID);
    }

    public List<InternalUser> getAllSteamUsers() throws IOException, InterruptedException, CarthageException {
        return carthageConnection.getAllSteamUsers(botID);
    }

    public List<String> insertAndGetNewMatches(List<LeetifyMatchResponse> matches, Integer userID) throws IOException, InterruptedException, CarthageException {
        if (matches != null && !matches.isEmpty()) {
            List<String> matchIDs = matches.stream().map(match -> match.id).toList();
            return carthageConnection.insertAndGetNewMatches(matchIDs, userID, botID);
        }
        return List.of();
    }
}
