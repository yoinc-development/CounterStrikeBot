package services;

import com.google.gson.JsonSyntaxException;
import http.ConnectionBuilder;
import model.steam.ResponseData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

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

    public EmbedBuilder handleStatsEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String requestedUser = event.getOption("player").getAsString().toLowerCase();

        try {
            ResponseData responseData = getUserResponseData(requestedUser);
            return responseData.getBasicInfo(resourceBundle);
        } catch (InterruptedException ex) {
            System.out.println("[CSBot - CsStatsService] InterruptedException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (IOException ex) {
            System.out.println("[CSBot - CsStatsService] IOException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (NullPointerException | JsonSyntaxException ex) {
            System.out.println("[CSBot - CsStatsService] NullPointerException / JSonSyntaxException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacySettings").replace("%s", requestedUser));
        } catch (SQLException ex) {
            System.out.println("[CSBot - CsStatsService] SQLException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorError"));
        }
    }

    public EmbedBuilder handleCompareEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        try {
            String requestedUserOne = event.getOption("playerone").getAsString().toLowerCase();
            String requestedUserTwo = event.getOption("playertwo").getAsString().toLowerCase();
            return comparePlayers(getUserResponseData(requestedUserOne), getUserResponseData(requestedUserTwo));
        } catch (NullPointerException ex) {
            System.out.println("[CSBot - CsStatsService] NullPointerException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.wrongQueryParameters"));
        } catch (InterruptedException ex) {
            System.out.println("[CSBot - CsStatsService] InterruptedException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (IOException ex) {
            System.out.println("[CSBot - CsStatsService] IOException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (SQLException ex) {
            System.out.println("[CSBot - CsStatsService] SQLException thrown: " + ex.getMessage());
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
        }
    }

    private EmbedBuilder comparePlayers(ResponseData playerOneData, ResponseData playerTwoData) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        winsOne = 0;
        winsTwo = 0;

        embedBuilder.setTitle(resourceBundle.getString("compare.title").replace("%s", playerOneData.getSteamUserInfo().getPlayers().get(0).getPersonaname()).replace("%t",playerTwoData.getSteamUserInfo().getPlayers().get(0).getPersonaname()))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kills"), getWinner(playerOneData, playerTwoData, "total_kills", true), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.deaths"),getWinner(playerOneData, playerTwoData, "total_deaths", false),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.wins"),getWinner(playerOneData, playerTwoData, "total_wins", true),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.planted"),getWinner(playerOneData, playerTwoData, "total_planted_bombs", true),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.defused"),getWinner(playerOneData, playerTwoData, "total_defused_bombs", true),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.damage"),getWinner(playerOneData, playerTwoData, "total_damage_done", true),true));

        if(winsOne > winsTwo) {
            embedBuilder.setImage(playerOneData.getSteamUserInfo().getPlayers().get(0).getAvatarmedium());
        } else if(winsTwo > winsOne) {
            embedBuilder.setImage(playerTwoData.getSteamUserInfo().getPlayers().get(0).getAvatarmedium());
        }
        return embedBuilder;
    }

    private String getWinner(ResponseData playerOneData, ResponseData playerTwoData, String statName, boolean higherRequired) {
        long playerOneLong = playerOneData.getLongStatsForName(statName);
        long playerTwoLong = playerTwoData.getLongStatsForName(statName);

        if(higherRequired) {
            if(playerOneLong > playerTwoLong) {
                winsOne++;
                return "** :star: " + playerOneLong + " ** vs " + playerTwoLong;
            } else if(playerTwoLong > playerOneLong) {
                winsTwo++;
                return playerOneLong + " vs ** " + playerTwoLong + " ** :star: ";
            } else {
                return resourceBundle.getString("compare.equal").replace("%s", String.valueOf(playerOneLong));
            }
        } else {
            if(playerOneLong < playerTwoLong) {
                winsOne++;
                return "** :star: " + playerOneLong + " ** vs " + playerTwoLong;
            } else if(playerTwoLong < playerOneLong) {
                winsTwo++;
                return playerOneLong + " vs ** " + playerTwoLong + " ** :star: ";
            } else {
                return resourceBundle.getString("compare.equal").replace("%s", String.valueOf(playerOneLong));
            }
        }
    }

    private ResponseData getUserResponseData(String requestedUser) throws NullPointerException, InterruptedException, IOException, SQLException {
        ResponseData responseData = null;
        String steamID = dataService.getSteamIDForUsername(requestedUser);

        if(steamID == null || steamID.isEmpty()) {
            if(StringUtils.isNumeric(requestedUser)) {
                responseData = connectionBuilder.fetchSteamUserStats(requestedUser);
            }
        } else {
            responseData = connectionBuilder.fetchSteamUserStats(steamID);
        }
        return responseData;
    }
}
