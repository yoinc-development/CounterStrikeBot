package ch.yoinc.services;

import com.google.gson.JsonSyntaxException;
import ch.yoinc.http.CarthageException;
import ch.yoinc.http.ConnectionBuilder;
import ch.yoinc.model.steam.ResponseData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsStatsService {
    ResourceBundle resourceBundle;

    //I know. I don't like it either.
    private int winsOne;
    private int winsTwo;
    ConnectionBuilder connectionBuilder;
    DataService dataService;

    public CsStatsService(Properties properties, DataService dataService) {
        this.dataService = dataService;
        connectionBuilder = new ConnectionBuilder(properties);
    }

    public EmbedBuilder handleStatsEvent(SlashCommandInteractionEvent event) {
        resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));

        try {
            ResponseData responseData = getUserResponseData(Objects.requireNonNull(event.getOption("player")).getAsMentionable().getId());
            return responseData.getBasicInfo(resourceBundle);
        } catch (InterruptedException | IOException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] InterruptedException / IOException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedexception"));
        } catch (NullPointerException | JsonSyntaxException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] NullPointerException / JSonSyntaxException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacysettings"));
        } catch (CarthageException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] CarthageException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
        }
    }

    public EmbedBuilder handleCompareEvent(SlashCommandInteractionEvent event) {
        resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));
        try {
            String requestedUserOneID = Objects.requireNonNull(event.getOption("playerone")).getAsMentionable().getId();
            String requestedUserTwoID = Objects.requireNonNull(event.getOption("playertwo")).getAsMentionable().getId();
            return comparePlayers(getUserResponseData(requestedUserOneID), getUserResponseData(requestedUserTwoID));
        } catch (NullPointerException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] NullPointerException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.wrongqueryparameters"));
        } catch (InterruptedException | IOException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] InterruptedException / IOException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedexception"));
        } catch (CarthageException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] CarthageException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
        }
    }

    private EmbedBuilder comparePlayers(ResponseData playerOneData, ResponseData playerTwoData) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        winsOne = 0;
        winsTwo = 0;

        embedBuilder.setTitle(resourceBundle.getString("compare.title").replace("%s", playerOneData.getSteamUserInfo().getPlayers().getFirst().getPersonaname()).replace("%t", playerTwoData.getSteamUserInfo().getPlayers().getFirst().getPersonaname()))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kills"), getWinner(playerOneData, playerTwoData, "total_kills", true), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.deaths"), getWinner(playerOneData, playerTwoData, "total_deaths", false), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.wins"), getWinner(playerOneData, playerTwoData, "total_wins", true), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.planted"), getWinner(playerOneData, playerTwoData, "total_planted_bombs", true), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.defused"), getWinner(playerOneData, playerTwoData, "total_defused_bombs", true), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.damage"), getWinner(playerOneData, playerTwoData, "total_damage_done", true), true));

        if (winsOne > winsTwo) {
            embedBuilder.setImage(playerOneData.getSteamUserInfo().getPlayers().getFirst().getAvatarmedium());
        } else if (winsTwo > winsOne) {
            embedBuilder.setImage(playerTwoData.getSteamUserInfo().getPlayers().getFirst().getAvatarmedium());
        }
        return embedBuilder;
    }

    private String getWinner(ResponseData playerOneData, ResponseData playerTwoData, String statName, boolean higherRequired) {
        long playerOneLong = playerOneData.getLongStatsForName(statName);
        long playerTwoLong = playerTwoData.getLongStatsForName(statName);

        if (higherRequired) {
            return getString(playerOneLong, playerTwoLong, playerOneLong > playerTwoLong, playerTwoLong > playerOneLong);
        } else {
            return getString(playerOneLong, playerTwoLong, playerOneLong < playerTwoLong, playerTwoLong < playerOneLong);
        }
    }

    @NotNull
    private String getString(long playerOneLong, long playerTwoLong, boolean b, boolean b2) {
        if (b) {
            winsOne++;
            return "** :star: " + playerOneLong + " ** vs " + playerTwoLong;
        } else if (b2) {
            winsTwo++;
            return playerOneLong + " vs ** " + playerTwoLong + " ** :star: ";
        } else {
            return resourceBundle.getString("compare.equal").replace("%s", String.valueOf(playerOneLong));
        }
    }

    private ResponseData getUserResponseData(String discordID) throws NullPointerException, InterruptedException, IOException, CarthageException {
        ResponseData responseData = null;
        String steamID = dataService.getSteamIDForDiscordID(discordID);

        if (StringUtils.isNotEmpty(steamID)) {
            responseData = connectionBuilder.fetchSteamUserStats(steamID);
        }
        return responseData;
    }
}
