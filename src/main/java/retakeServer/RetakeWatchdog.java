package retakeServer;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ResourceBundle;

public class RetakeWatchdog {

    public static EmbedBuilder getJoinMessage(ResourceBundle resourceBundle, String playerName, String currentMap) {
        return new EmbedBuilder()
                .setTitle(resourceBundle.getString("serverwatchdog.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .setDescription(resourceBundle.getString("serverwatchdog.playerJoined")
                        .replace("%playerName", playerName).replace("%map", currentMap));
    }
}
