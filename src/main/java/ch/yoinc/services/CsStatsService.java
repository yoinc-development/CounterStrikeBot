package ch.yoinc.services;

import ch.yoinc.http.CarthageException;
import ch.yoinc.http.ExternalApiConnection;
import ch.yoinc.model.steam.ResponseData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class CsStatsService {

    private static final Logger log = LoggerFactory.getLogger(CsStatsService.class);

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
