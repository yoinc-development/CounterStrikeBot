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
import java.util.Locale;

public class LeetifyTask implements ScheduledTask {

    private DataService dataService;
    private LeetifyConnection leetifyConnection;

    @Override
    public void execute(JDA jda, Properties properties) {
        dataService = new DataService(properties);
        dataService.setBotID(jda.getSelfUser().getId());
        leetifyConnection = new LeetifyConnection(properties);

        List<InternalUser> internalUsers = dataService.getAllSteamUsers();

        //this map contains all newly played matches found during the next run of the
        //leetify task. its identifier is the match ID and the list contains all
        //match responses with this id. why? because that way it's easier to see if
        //for example a group has played in the same match. this would allow a different message
        //to be sent to the discord instead of the generic "person x has played a match."
        HashMap<String, List<LeetifyMatchResponse>> newlyPlayedMatches = setNewlyPlayedMatches(internalUsers);

        for (String matchID : newlyPlayedMatches.keySet()) {
            List<LeetifyMatchResponse> matches = newlyPlayedMatches.get(matchID);
            EmbedBuilder matchEmbed = new EmbedBuilder();
            LeetifyMatchResponse match = matches.getFirst();
            boolean hasWon = match.stats.getFirst().rounds_won >= match.stats.getFirst().rounds_lost; //tie is a victory, change my mind
            if (matches.size() > 1) {
                List<String> playerNames = new ArrayList<>();
                for (LeetifyMatchResponse playerMatch : matches) {
                    playerNames.add(playerMatch.stats.getFirst().name);
                }
                String players = String.join(", ", playerNames);

                matchEmbed = switch (match.data_source) {
                    case "faceit" -> returnFilledEmbed("New Faceit Match",
                            Color.ORANGE, players + " played a Faceit match together and " + ((hasWon) ? "**won**." :  "**lost**."),
                            match.map_name, matchID, "Finished at " + match.finished_at);
                    case "matchmaking_wingman" -> returnFilledEmbed("New Wingman Match",
                            Color.GREEN, players + " played a Wingman match together and " + ((hasWon) ? "**won**." :  "**lost**."),
                            match.map_name, matchID, "Finished at " + match.finished_at);
                    case "matchmaking" -> returnFilledEmbed("New Competitive Match",
                            Color.YELLOW, players + " played a Competitive match together and " + ((hasWon) ? "**won**." :  "**lost**."),
                            match.map_name, matchID, "Finished at " + match.finished_at);
                    default -> matchEmbed;
                };

                for (LeetifyMatchResponse playerMatch : matches) {
                    LeetifyPlayerStatsResponse stats = playerMatch.stats.getFirst();
                    matchEmbed.addField(
                            stats.name,
                            "Kills: " + stats.total_kills +
                                    "\nDeaths: " + stats.total_deaths +
                                    "\nADR: " + stats.dpr +
                                    "\nRating: " + formatRating(stats.leetify_rating) +
                                    "\nCT Rating: " + formatRating(stats.ct_leetify_rating) +
                                    "\nT Rating: " + formatRating(stats.t_leetify_rating),
                            true
                    );
                }
            } else {
                matchEmbed = switch (match.data_source) {
                    case "faceit" -> returnFilledEmbed("New Faceit Match", Color.ORANGE,
                            match.stats.getFirst().name + " played a Faceit match and " + ((hasWon) ? "**won**." :  "**lost**."),
                            match.map_name, matchID, "Finished at " + match.finished_at);
                    case "matchmaking_wingman" -> returnFilledEmbed("New Wingman Match", Color.GREEN,
                            match.stats.getFirst().name + " played a Wingman match and " + ((hasWon) ? "**won**." :  "**lost**."),
                            match.map_name, matchID, "Finished at " + match.finished_at);
                    case "matchmaking" -> returnFilledEmbed("New Competitive Match", Color.YELLOW,
                            match.stats.getFirst().name + " played a Competitive match and " + ((hasWon) ? "**won**." :  "**lost**."),
                            match.map_name, matchID, "Finished at " + match.finished_at);
                    default -> matchEmbed;
                };
                matchEmbed
                        .addField("Kills", Integer.toString(match.stats.getFirst().total_kills), true)
                        .addField("Deaths", Integer.toString(match.stats.getFirst().total_deaths), true)
                        .addField("ADR", Double.toString(match.stats.getFirst().dpr), true)
                        .addField("Rating", formatRating(match.stats.getFirst().leetify_rating), true)
                        .addField("CT Rating", formatRating(match.stats.getFirst().ct_leetify_rating), true)
                        .addField("T Rating", formatRating(match.stats.getFirst().t_leetify_rating), true);
            }
            Objects.requireNonNull(jda.getTextChannelById(properties.getProperty("discord.channelID"))).sendMessageEmbeds(matchEmbed.build()).queue();
        }
    }

    private EmbedBuilder returnFilledEmbed(String title, Color color, String description, String map_name, String matchID, String footer) {
        DiscordService discordService = new DiscordService();

        String DEFAULT_LEETIFY_URL = "https://leetify.com/app/match-details/%s/overview";
        String DEFAULT_MAP_LOGO_URL = "https://raw.githubusercontent.com/MurkyYT/cs2-map-icons/main/images/%s.png";
        String DEFAULT_MAP_URL = "https://raw.githubusercontent.com/MurkyYT/cs2-map-icons/main/images/thumbs/%s_1_png.png";

        EmbedBuilder returnEmbed = discordService.createEmbedBuilder(title, description, DEFAULT_MAP_URL.replace("%s", map_name), footer);
        returnEmbed
                .setTitle(title, DEFAULT_LEETIFY_URL.replace("%s", matchID))
                .setColor(color)
                .setThumbnail(DEFAULT_MAP_LOGO_URL.replace("%s", map_name));

        return returnEmbed;
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

    private static String formatRating(Double rating) {
        if (rating == null) {
            return "n/a";
        }
        return String.format(Locale.US, "%.2f", rating * 100.0);
    }
}
