package ch.yoinc.services;

import ch.yoinc.http.CarthageException;
import ch.yoinc.http.ExternalApiConnection;
import ch.yoinc.model.leetify.LeetifyProfileResponse;
import ch.yoinc.model.leetify.LeetifyRatingResponse;
import ch.yoinc.model.steam.ResponseData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class CsStatsService {

    private static final Logger log = LoggerFactory.getLogger(CsStatsService.class);

    private static final String[] WINGMAN_RANK_NAMES = {
            "Silver I", "Silver II", "Silver III", "Silver IV", "Silver Elite", "Silver Elite Master",
            "Gold Nova I", "Gold Nova II", "Gold Nova III", "Gold Nova Master",
            "Master Guardian I", "Master Guardian II", "Master Guardian Elite", "Distinguished Master Guardian",
            "Legendary Eagle", "Legendary Eagle Master", "Supreme Master First Class", "Global Elite"
    };

    ResourceBundle resourceBundle;
    ExternalApiConnection connection;
    DataService dataService;
    DiscordService discordService;

    public CsStatsService(Properties properties, DataService dataService) {
        this.dataService = dataService;
        connection = new ExternalApiConnection(properties);
        discordService = new DiscordService();
    }

    public EmbedBuilder handleStatsEvent(SlashCommandInteractionEvent event) {
        resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));

        try {
            ResponseData responseData = getUserResponseData(Objects.requireNonNull(event.getOption("player")).getAsMentionable().getId());
            return responseData.getBasicInfo(resourceBundle);
        } catch (IOException | InterruptedException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.connectionerror"));
        } catch (NullPointerException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacysettings"));
        } catch (CarthageException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
        }
    }

    public EmbedBuilder handleCompareEvent(SlashCommandInteractionEvent event) {
        resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));
        try {
            ResponseData userDataOne = getUserResponseData(Objects.requireNonNull(event.getOption("playerone")).getAsMentionable().getId());
            ResponseData userDataTwo = getUserResponseData(Objects.requireNonNull(event.getOption("playertwo")).getAsMentionable().getId());

            if(userDataOne == null || userDataTwo == null) {
                return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacysettings"));
            }
            return comparePlayers(userDataOne, userDataTwo);
        } catch (InterruptedException | IOException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.connectionerror"));
        } catch (CarthageException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
        }
    }

    public EmbedBuilder handleLeetifyUserContext(UserContextInteractionEvent event) {
        resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));
        try {
            String steamID = dataService.getSteamIDForDiscordID(Objects.requireNonNull(event.getMember()).getId());
            LeetifyProfileResponse profileResponse = connection.getPlayerProfile(steamID, null);
            if(profileResponse != null) {
                EmbedBuilder returnEmbed = discordService.createEmbedBuilder(profileResponse.name + "'s Leetify Stats", null, null,
                        "First match played at " + profileResponse.first_match_date);

                returnEmbed
                        .setThumbnail("https://cdn.brandfetch.io/idYPcZQLsh/w/820/h/820/theme/dark/logo.png?c=1dxbfHSJFAPEGdCLU4o5B")
                        .addField("Faceit", profileResponse.ranks.faceit == null ? "n/a" + "(" + profileResponse.ranks.faceit_elo + ")" : profileResponse.ranks.faceit + "(" + profileResponse.ranks.faceit_elo + ")", true)
                        .addField("Premier", Integer.toString(profileResponse.ranks.premier), true)
                        .addField("Wingman", getWingmanRankName(profileResponse.ranks.wingman), true)
                        .addField("Leetify Rating", String.format("%.2f", profileResponse.ranks.leetify), true)
                        .addField("Win Rate", discordService.formatRating(profileResponse.winrate), true)
                        .addField("Played matches", Integer.toString(profileResponse.total_matches), true);

                LeetifyRatingResponse rating = profileResponse.rating;
                if (rating != null) {
                    returnEmbed
                            .addField("CT Rating", discordService.formatRating(rating.ct_leetify), true)
                            .addField("T Rating", discordService.formatRating(rating.t_leetify), true)
                            .addField("Aim", Double.toString(rating.aim), true)
                            .addField("Positioning", Double.toString(rating.positioning), true)
                            .addField("Utility", Double.toString(rating.utility), true)
                            .addField("Clutch", discordService.formatRating(rating.clutch), true)
                            .addField("Opening", discordService.formatRating(rating.opening), true);
                }

                return returnEmbed;
            }
        } catch (InterruptedException | IOException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.connectionerror"));
        } catch (CarthageException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
        }
        return new EmbedBuilder().setTitle(resourceBundle.getString("error.noleetifyprofile"));
    }

    private String getWingmanRankName(Integer wingmanRank) {
        if (wingmanRank == null || wingmanRank < 1 || wingmanRank > WINGMAN_RANK_NAMES.length) {
            return "n/a";
        }
        return WINGMAN_RANK_NAMES[wingmanRank - 1];
    }

    private EmbedBuilder comparePlayers(ResponseData playerOneData, ResponseData playerTwoData) {
        int[] wins = new int[2]; // wins[0] = playerOne, wins[1] = playerTwo

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(resourceBundle.getString("compare.title").replace("%s", playerOneData.getSteamUserInfo().getPlayers().getFirst().getPersonaname()).replace("%t", playerTwoData.getSteamUserInfo().getPlayers().getFirst().getPersonaname()))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kills"), getWinner(playerOneData, playerTwoData, "total_kills", true, wins), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.deaths"), getWinner(playerOneData, playerTwoData, "total_deaths", false, wins), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.wins"), getWinner(playerOneData, playerTwoData, "total_wins", true, wins), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.planted"), getWinner(playerOneData, playerTwoData, "total_planted_bombs", true, wins), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.defused"), getWinner(playerOneData, playerTwoData, "total_defused_bombs", true, wins), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.damage"), getWinner(playerOneData, playerTwoData, "total_damage_done", true, wins), true));

        if (wins[0] > wins[1]) {
            embedBuilder.setImage(playerOneData.getSteamUserInfo().getPlayers().getFirst().getAvatarmedium());
        } else if (wins[1] > wins[0]) {
            embedBuilder.setImage(playerTwoData.getSteamUserInfo().getPlayers().getFirst().getAvatarmedium());
        }
        return embedBuilder;
    }

    private String getWinner(ResponseData playerOneData, ResponseData playerTwoData, String statName, boolean higherRequired, int[] wins) {
        long playerOneLong = playerOneData.getLongStatsForName(statName);
        long playerTwoLong = playerTwoData.getLongStatsForName(statName);

        boolean playerOneWins = higherRequired ? playerOneLong > playerTwoLong : playerOneLong < playerTwoLong;
        boolean playerTwoWins = higherRequired ? playerTwoLong > playerOneLong : playerTwoLong < playerOneLong;

        return getString(playerOneLong, playerTwoLong, playerOneWins, playerTwoWins, wins);
    }

    @NotNull
    private String getString(long playerOneLong, long playerTwoLong, boolean playerOneWins, boolean playerTwoWins, int[] wins) {
        if (playerOneWins) {
            wins[0]++;
            return "** :star: " + playerOneLong + " ** vs " + playerTwoLong;
        } else if (playerTwoWins) {
            wins[1]++;
            return playerOneLong + " vs ** " + playerTwoLong + " ** :star: ";
        } else {
            return resourceBundle.getString("compare.equal").replace("%s", String.valueOf(playerOneLong));
        }
    }

    private ResponseData getUserResponseData(String discordID) throws IOException, InterruptedException, CarthageException {
        ResponseData responseData = null;
        String steamID = dataService.getSteamIDForDiscordID(discordID);

        if (StringUtils.isNotEmpty(steamID)) {
            responseData = connection.fetchSteamUserStats(steamID);
        }
        return responseData;
    }
}
