package ch.yoinc.tasks;

import ch.yoinc.http.LeetifyConnection;
import ch.yoinc.model.internal.InternalUser;
import ch.yoinc.model.leetify.LeetifyMatchResponse;
import ch.yoinc.model.leetify.LeetifyPlayerStatsResponse;
import ch.yoinc.services.DataService;
import ch.yoinc.services.DiscordService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LeetifyTask implements ScheduledTask {

    private DataService dataService;
    private LeetifyConnection leetifyConnection;

    @Override
    public void execute(JDA jda, Properties properties) {
        dataService = new DataService(properties);
        dataService.setBotID(jda.getSelfUser().getId());
        leetifyConnection = new LeetifyConnection(properties);

        DiscordService discordService = new DiscordService();

        String DEFAULT_LEETIFY_URL = "https://leetify.com/app/match-details/%s/overview";
        String DEFAULT_MAP_LOGO_URL = "https://raw.githubusercontent.com/MurkyYT/cs2-map-icons/main/images/%s.png";
        String DEFAULT_MAP_URL = "https://raw.githubusercontent.com/MurkyYT/cs2-map-icons/main/images/thumbs/%s_1_png.png";

        List<InternalUser> internalUsers = dataService.getAllSteamUsers();

        //this map contains all newly played matches found during the next run of the
        //leetify task. its identifier is the match ID and the list contains all
        //match responses with this id. why? because that way it's easier to see if
        //for example a group has played in the same match. this would allow a different message
        //to be sent to the discord instead of the generic "person x has played a match."
        HashMap<String, List<LeetifyMatchResponse>> newlyPlayedMatches = setNewlyPlayedMatches(internalUsers);

        for (String matchID : newlyPlayedMatches.keySet()) {
            List<LeetifyMatchResponse> matches = newlyPlayedMatches.get(matchID);
            if (matches.size() > 1) {
                LeetifyMatchResponse match = matches.getFirst();
                String title = "";
                Color color = Color.BLUE;
                String imageUrl = "";
                String thumbnailUrl = "";
                String footer = "";

                List<String> playerNames = new ArrayList<>();
                for (LeetifyMatchResponse playerMatch : matches) {
                    playerNames.add(playerMatch.stats.getFirst().name);
                }
                String players = String.join(", ", playerNames);
                String description = "";

                switch (match.data_source) {
                    case "faceit":
                        title = "New Faceit Match";
                        color = Color.ORANGE;
                        description = players + " played a new Faceit match together.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                    case "wingman":
                    case "matchmaking_wingman":
                        title = "New Wingman Match";
                        color = Color.GREEN;
                        description = players + " played a new Wingman match together.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                    case "premier":
                        title = "New Premier Match";
                        color = Color.YELLOW;
                        description = players + " played a new Premier match together.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                    case "matchmaking":
                        title = "New Competitive Match";
                        color = Color.RED;
                        description = players + " played a new Competitive match together.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                }

                EmbedBuilder updateMessage = discordService.createEmbedBuilder(title, description, imageUrl, footer);
                updateMessage
                        .setTitle(title, DEFAULT_LEETIFY_URL.replace("%s", matchID))
                        .setColor(color)
                        .setThumbnail(thumbnailUrl);

                for (LeetifyMatchResponse playerMatch : matches) {
                    LeetifyPlayerStatsResponse stats = playerMatch.stats.getFirst();
                    updateMessage.addField(
                            stats.name,
                            "Kills: " + stats.total_kills +
                                    "\nDeaths: " + stats.total_deaths +
                                    "\nADR: " + stats.dpr +
                                    "\nRating: " + stats.leetify_rating +
                                    "\nCT Rating: " + stats.ct_leetify_rating +
                                    "\nT Rating: " + stats.t_leetify_rating,
                            true
                    );
                }
                Objects.requireNonNull(jda.getTextChannelById(properties.getProperty("discord.channelID"))).sendMessageEmbeds(updateMessage.build()).queue();
            } else {
                LeetifyMatchResponse match = matches.getFirst();
                String title = "";
                Color color = Color.BLUE;
                String description = "";
                String imageUrl = "";
                String thumbnailUrl = "";
                String footer = "";

                switch (match.data_source) {
                    case "faceit":
                        title = "New Faceit Match";
                        color = Color.ORANGE;
                        description = match.stats.getFirst().name + " played a new Faceit match.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                    case "wingman":
                    case "matchmaking_wingman":
                        title = "New Wingman Match";
                        color = Color.GREEN;
                        description = match.stats.getFirst().name + " played a new Wingman match.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                    case "premier":
                        title = "New Premier Match";
                        color = Color.YELLOW;
                        description = match.stats.getFirst().name + " played a new Premier match.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                    case "matchmaking":
                        title = "New Competitive Match";
                        color = Color.RED;
                        description = match.stats.getFirst().name + " played a new Competitive match.";
                        imageUrl = DEFAULT_MAP_URL.replace("%s", match.map_name);
                        thumbnailUrl = DEFAULT_MAP_LOGO_URL.replace("%s", match.map_name);
                        footer = "Finished at " + match.finished_at;
                        break;
                }
                EmbedBuilder updateMessage = discordService.createEmbedBuilder(title, description, imageUrl, footer);
                updateMessage
                        .setTitle(title, DEFAULT_LEETIFY_URL.replace("%s", matchID))
                        .setColor(color)
                        .setThumbnail(thumbnailUrl)
                        .addField("Kills", Integer.toString(match.stats.getFirst().total_kills), true)
                        .addField("Deaths", Integer.toString(match.stats.getFirst().total_deaths), true)
                        .addField("ADR", Double.toString(match.stats.getFirst().dpr), true)
                        .addField("Rating", Double.toString(match.stats.getFirst().leetify_rating), true)
                        .addField("CT Rating", Double.toString(match.stats.getFirst().ct_leetify_rating), true)
                        .addField("T Rating", Double.toString(match.stats.getFirst().t_leetify_rating), true);
                Objects.requireNonNull(jda.getTextChannelById(properties.getProperty("discord.channelID"))).sendMessageEmbeds(updateMessage.build()).queue();
            }
        }
    }

    private HashMap<String, List<LeetifyMatchResponse>> setNewlyPlayedMatches(List<InternalUser> steamInternalUsers) {
        HashMap<String, List<LeetifyMatchResponse>> results = new HashMap<>();

        for (InternalUser user : steamInternalUsers) {
            List<LeetifyMatchResponse> matches = leetifyConnection.getPlayerMatchHistory(user.steamID, null);

            if (matches != null && !matches.isEmpty()) {
                List<String> newMatches = dataService.insertAndGetNewMatches(matches, user.userID);

                for (String matchID : newMatches) {
                    for (LeetifyMatchResponse match : matches) {
                        if (match.id.equals(matchID)) {
                            results.computeIfAbsent(matchID, k -> new ArrayList<>()).add(match);
                        }
                    }
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return results;
            }
        }
        return results;
    }

    @Override
    public String getTaskName() {
        return "LeetifyTask";
    }
}
