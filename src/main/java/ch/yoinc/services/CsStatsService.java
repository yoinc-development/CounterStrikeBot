package ch.yoinc.services;

import ch.yoinc.http.CarthageException;
import ch.yoinc.http.ExternalApiConnection;
import ch.yoinc.model.leetify.LeetifyProfileResponse;
import ch.yoinc.model.leetify.LeetifyRankResponse;
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
            String steamID = dataService.getSteamIDForDiscordID(Objects.requireNonNull(event.getTargetMember()).getId());
            if (!StringUtils.isNotEmpty(steamID)) {
                return new EmbedBuilder().setTitle(resourceBundle.getString("error.noleetifyprofile"));
            }

            LeetifyProfileResponse profileResponse = connection.getPlayerProfile(steamID, null);
            if(profileResponse != null) {
                EmbedBuilder returnEmbed = discordService.createEmbedBuilder(profileResponse.name + "'s Leetify Stats", null, null,
                        "First match played at " + profileResponse.first_match_date);

                LeetifyRankResponse ranks = profileResponse.ranks;
                returnEmbed
                        .setThumbnail("https://cdn.brandfetch.io/idYPcZQLsh/w/820/h/820/theme/dark/logo.png?c=1dxbfHSJFAPEGdCLU4o5B")
                        .addField("Faceit", formatFaceitRank(ranks), true)
                        .addField("Premier", ranks == null ? "n/a" : formatNullable(ranks.premier), true)
                        .addField("Wingman", ranks == null ? "n/a" : getWingmanRankName(ranks.wingman), true)
                        .addField("Leetify Rating", ranks == null ? "n/a" : formatNullable(ranks.leetify), true)
                        .addField("Win Rate", discordService.formatRating(profileResponse.winrate), true)
                        .addField("Played matches", formatNullable(profileResponse.total_matches), true);

                LeetifyRatingResponse rating = profileResponse.rating;
                if (rating != null) {
                    returnEmbed
                            .addField("CT Rating", discordService.formatRating(rating.ct_leetify), true)
                            .addField("T Rating", discordService.formatRating(rating.t_leetify), true)
                            .addField("Aim", formatNullable(rating.aim), true)
                            .addField("Positioning", formatNullable(rating.positioning), true)
                            .addField("Utility", formatNullable(rating.utility), true)
                            .addField("Clutch", discordService.formatRating(rating.clutch), true)
                            .addField("Opening", discordService.formatRating(rating.opening), true);
                }

                return returnEmbed;
            }
        } catch (InterruptedException | IOException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.connectionerror"));
        } catch (NullPointerException ex) {
            log.error(ex.getMessage(), ex);
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacysettings"));
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

    private String formatFaceitRank(LeetifyRankResponse ranks) {
        if (ranks == null || ranks.faceit == null) {
            return "n/a";
        }
        return ranks.faceit + " (" + formatNullable(ranks.faceit_elo) + ")";
    }

    private String formatNullable(Integer value) {
        return value == null ? "n/a" : value.toString();
    }

    private String formatNullable(Double value) {
        return value == null ? "n/a" : String.format("%.2f", value);
    }

    private record ComparisonResult(String display, int winner) {
        private static final int TIE = 0;
        private static final int PLAYER_ONE = 1;
        private static final int PLAYER_TWO = 2;
    }

    private EmbedBuilder comparePlayers(ResponseData playerOneData, ResponseData playerTwoData) {
        ComparisonResult kills = getWinner(playerOneData, playerTwoData, "total_kills", true);
        ComparisonResult deaths = getWinner(playerOneData, playerTwoData, "total_deaths", false);
        ComparisonResult wins = getWinner(playerOneData, playerTwoData, "total_wins", true);
        ComparisonResult planted = getWinner(playerOneData, playerTwoData, "total_planted_bombs", true);
        ComparisonResult defused = getWinner(playerOneData, playerTwoData, "total_defused_bombs", true);
        ComparisonResult damage = getWinner(playerOneData, playerTwoData, "total_damage_done", true);

        List<ComparisonResult> results = List.of(kills, deaths, wins, planted, defused, damage);
        long winsOne = results.stream().filter(result -> result.winner() == ComparisonResult.PLAYER_ONE).count();
        long winsTwo = results.stream().filter(result -> result.winner() == ComparisonResult.PLAYER_TWO).count();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(resourceBundle.getString("compare.title").replace("%s", playerOneData.getSteamUserInfo().getPlayers().getFirst().getPersonaname()).replace("%t", playerTwoData.getSteamUserInfo().getPlayers().getFirst().getPersonaname()))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kills"), kills.display(), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.deaths"), deaths.display(), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.wins"), wins.display(), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.planted"), planted.display(), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.defused"), defused.display(), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.damage"), damage.display(), true));

        if (winsOne > winsTwo) {
            embedBuilder.setImage(playerOneData.getSteamUserInfo().getPlayers().getFirst().getAvatarmedium());
        } else if (winsTwo > winsOne) {
            embedBuilder.setImage(playerTwoData.getSteamUserInfo().getPlayers().getFirst().getAvatarmedium());
        }
        return embedBuilder;
    }

    private ComparisonResult getWinner(ResponseData playerOneData, ResponseData playerTwoData, String statName, boolean higherRequired) {
        long playerOneLong = playerOneData.getLongStatsForName(statName);
        long playerTwoLong = playerTwoData.getLongStatsForName(statName);

        boolean playerOneWins = higherRequired ? playerOneLong > playerTwoLong : playerOneLong < playerTwoLong;
        boolean playerTwoWins = higherRequired ? playerTwoLong > playerOneLong : playerTwoLong < playerOneLong;

        return getString(playerOneLong, playerTwoLong, playerOneWins, playerTwoWins);
    }

    @NotNull
    private ComparisonResult getString(long playerOneLong, long playerTwoLong, boolean playerOneWins, boolean playerTwoWins) {
        if (playerOneWins) {
            return new ComparisonResult("** :star: " + playerOneLong + " ** vs " + playerTwoLong, ComparisonResult.PLAYER_ONE);
        } else if (playerTwoWins) {
            return new ComparisonResult(playerOneLong + " vs ** " + playerTwoLong + " ** :star: ", ComparisonResult.PLAYER_TWO);
        } else {
            return new ComparisonResult(resourceBundle.getString("compare.equal").replace("%s", String.valueOf(playerOneLong)), ComparisonResult.TIE);
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
