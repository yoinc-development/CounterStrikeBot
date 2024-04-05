package services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.ResponseData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class CsStatsService {

    private Properties properties;
    ResourceBundle resourceBundle;

    //I know. I don't like it either.
    private int winsOne;
    private int winsTwo;

    public CsStatsService(Properties properties) {
        this.properties = properties;
    }

    public EmbedBuilder handleStatsEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String requestedUser = event.getOption("player").getAsString().toLowerCase();

        try {
            ResponseData responseData = handleGlobalOrNormalUser(requestedUser);
            return responseData.returnBasicInfo(resourceBundle);
        } catch (InterruptedException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (IOException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (NullPointerException | JsonSyntaxException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacySettings").replace("%s", requestedUser));
        }
    }

    public EmbedBuilder handleCompareEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        try {
            String requestedUserOne = event.getOption("playerone").getAsString().toLowerCase();
            String requestedUserTwo = event.getOption("playertwo").getAsString().toLowerCase();
            return comparePlayers(handleGlobalOrNormalUser(requestedUserOne), handleGlobalOrNormalUser(requestedUserTwo));
        } catch (NullPointerException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.wrongQueryParameters"));
        } catch (InterruptedException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
        } catch (IOException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.interruptedException"));
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

    private ResponseData handleGlobalOrNormalUser(String requestedUser) throws NullPointerException, InterruptedException, IOException {

        ResponseData responseData;

        switch (requestedUser) {
            case "aatha":
            case "aathavan":
            case "doge":
                responseData = getUserAndStats("76561198077352267");
                break;
            case "dario":
            case "däse":
                responseData = getUserAndStats("76561198213130649");
                break;
            case "janes":
            case "jay":
            case "grey":
                responseData = getUserAndStats("76561198014462666");
                break;
            case "juan":
            case "juanita":
                responseData = getUserAndStats("76561198098219020");
                break;
            case "korunde":
            case "koray":
            case "ossas":
                responseData = getUserAndStats("76561198071064798");
                break;
            case "nabil":
            case "drifter":
                responseData = getUserAndStats("76561198088520949");
                break;
            case "nassim":
                responseData = getUserAndStats("76561198203636285");
                break;
            case "nici":
            case "nigglz":
            case "n'lölec":
                responseData = getUserAndStats("76561198401419666");
                break;
            case "ravi":
            case "vi24":
                responseData = getUserAndStats("76561198071074164");
                break;
            case "pavi":
            case "seraph":
                responseData = getUserAndStats("76561198102224384");
                break;
            case "sani":
            case "baka":
            case "mugiwarabaka":
                responseData = getUserAndStats("76561197984892194");
                break;
            case "vantriko":
            case "v4ntr1ko":
            case "enrico":
                responseData = getUserAndStats("76561198316963738");
                break;
            default:
                if(StringUtils.isNumeric(requestedUser)) {
                    responseData = getUserAndStats(requestedUser);
                } else {
                    throw new IOException();
                }
                break;
        }

        return responseData;
    }

    private ResponseData getUserAndStats(String steamID) throws InterruptedException, IOException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request;
        ResponseData responseData;

        request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + properties.get("steam.api") + "&steamids=" + steamID))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        responseData = new Gson().fromJson(response.body(), ResponseData.class);

        request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=" + steamID))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        responseData.setPlayerstats(new Gson().fromJson(response.body(), ResponseData.class).getPlayerstats());

        return responseData;
    }
}
