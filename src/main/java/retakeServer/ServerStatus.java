package retakeServer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerStatus {

    private static final String playerSectionMarker = "---------players--------";
    private ServerState serverState;
    private String currentMap;
    private final List<String> playerNames;
    public ServerStatus(String input) {
        this.playerNames = new ArrayList<>();
        parseInput(input);
    }

    public static EmbedBuilder getInactiveStatusMessage(ResourceBundle resourceBundle) {
        return new EmbedBuilder()
                .setTitle(resourceBundle.getString("serverstatus.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("serverstatus.state"), "inactive", true));
    }

    private void parseInput(String input) {
        // Parse server state
        Pattern statePattern = Pattern.compile("\\((hibernating|not hibernating)\\)");
        Matcher stateMatcher = statePattern.matcher(input);
        if (stateMatcher.find()) {
            String state = stateMatcher.group(1);
            if (state.equals(ServerState.HIBERNATING.state)) {
                this.serverState = ServerState.HIBERNATING;
            } else {
                this.serverState = ServerState.ACTIVE;
            }
        }

        // Parse current map
        Pattern mapPattern = Pattern.compile("de_\\w+");
        Matcher mapMatcher = mapPattern.matcher(input);
        if (mapMatcher.find()) {
            this.currentMap = mapMatcher.group();
        }

        // Parse player names
        if (serverState == ServerState.ACTIVE) {
            int startIndex = input.indexOf(playerSectionMarker);
            if (startIndex != -1) {
                String playerSection = input.substring(startIndex);
                Pattern playerPattern = Pattern.compile("'\\w+'");
                Matcher playerMatcher = playerPattern.matcher(playerSection);
                while (playerMatcher.find()) {
                    String playerName = playerMatcher.group().replace("'", "");
                    this.playerNames.add(playerName);
                }
            }
        }
    }

    private String getServerState() {
        return serverState == ServerState.ACTIVE ? "active" : "active (empty)";
    }

    public EmbedBuilder getStatusMessage(ResourceBundle resourceBundle) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(resourceBundle.getString("serverstatus.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("serverstatus.state"), getServerState(), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("serverstatus.currentMap"), String.valueOf(currentMap), true));

        if (serverState == ServerState.ACTIVE) {
            for (String player : playerNames) {
                embedBuilder.addField(new MessageEmbed.Field(resourceBundle.getString("serverstatus.connectedPlayers"), player, true));
            }
        }
        return embedBuilder;
    }

    enum ServerState {
        ACTIVE("not hibernating"),
        HIBERNATING("hibernating");

        private final String state;

        ServerState(String state) {
            this.state = state;
        }
    }

}
