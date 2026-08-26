package ch.yoinc.services;

import ch.yoinc.http.CarthageConnection;
import ch.yoinc.http.CarthageException;
import ch.yoinc.model.internal.InternalUser;
import ch.yoinc.model.leetify.LeetifyMatchResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<InternalUser> getAllSteamUsers() {
        try {
            return carthageConnection.getAllSteamUsers(botID);
        } catch (IOException | InterruptedException | CarthageException ex) {
            System.out.println("[CSBot - DataService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] InterruptedException / IOException / CarthageException thrown: " + ex.getMessage());
        }
        return List.of();
    }

    public List<String> insertAndGetNewMatches(List<LeetifyMatchResponse> matches, Integer userID) {
        try {
            if (matches != null && !matches.isEmpty()) {
                List<String> matchIDs = matches.stream().map(match -> match.id).toList();
                return carthageConnection.insertAndGetNewMatches(matchIDs, userID, botID);
            }
        } catch (IOException | InterruptedException | CarthageException ex) {
            System.out.println("[CSBot - DataService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] InterruptedException / IOException / CarthageException thrown: " + ex.getMessage());
        }
        return List.of();
    }
}
