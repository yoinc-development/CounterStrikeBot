package services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.ResponseData;
import net.dv8tion.jda.api.EmbedBuilder;
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
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.ioException"));
        } catch (NullPointerException | JsonSyntaxException ex) {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.privacySettings").replace("%s", requestedUser));
        }
    }

    public String handleCompareEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        try {
            String requestedUserOne = event.getOption("playerone").getAsString().toLowerCase();
            String requestedUserTwo = event.getOption("playertwo").getAsString().toLowerCase();
            return comparePlayers(handleGlobalOrNormalUser(requestedUserOne), handleGlobalOrNormalUser(requestedUserTwo));
        } catch (NullPointerException ex) {
            return resourceBundle.getString("error.wrongQueryParameters");
        } catch (InterruptedException ex) {
            return resourceBundle.getString("error.interruptedException");
        } catch (IOException ex) {
            return resourceBundle.getString("error.ioException");
        }
    }

    private String comparePlayers(ResponseData playerOneData, ResponseData playerTwoData) {

        StringBuilder returnString = new StringBuilder();

        returnString.append(resourceBundle.getString("compare.title").replace("%s", playerOneData.getSteamUserInfo().getPlayers().get(0).getPersonaname()).replace("%t",playerTwoData.getSteamUserInfo().getPlayers().get(0).getPersonaname())).append("\n\n");
        returnString.append(resourceBundle.getString("stats.kills") + getWinner(playerOneData, playerTwoData, "total_kills", true)).append("\n");;
        returnString.append(resourceBundle.getString("stats.deaths") + getWinner(playerOneData, playerTwoData, "total_deaths", false)).append("\n");;
        returnString.append(resourceBundle.getString("stats.planted") +getWinner(playerOneData, playerTwoData, "total_planted_bombs", true)).append("\n");;
        returnString.append(resourceBundle.getString("stats.defused") + getWinner(playerOneData, playerTwoData, "total_defused_bombs", true)).append("\n");;
        returnString.append(resourceBundle.getString("stats.wins") + getWinner(playerOneData, playerTwoData, "total_wins", true)).append("\n");;
        returnString.append(resourceBundle.getString("stats.damage") + getWinner(playerOneData, playerTwoData, "total_damage_done", true)).append("\n");;

        return returnString.toString();
    }

    private String getWinner(ResponseData playerOneData, ResponseData playerTwoData, String statName, boolean higherRequired) {

        long playerOneLong = playerOneData.getLongStatsForName(statName);
        long playerTwoLong = playerTwoData.getLongStatsForName(statName);

        if(higherRequired) {
            if(playerOneLong > playerTwoLong) {
                return "** :star: " + playerOneData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerOneLong + ") ** vs "
                        + playerTwoData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerTwoLong + ")";
            } else if(playerTwoLong > playerOneLong) {
                return "** :star: " + playerTwoData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerTwoLong + ") ** vs "
                        + playerOneData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerOneLong + ")";
            } else {
                return resourceBundle.getString("compare.equal").replace("%s", String.valueOf(playerOneLong));
            }
        } else {
            if(playerOneLong < playerTwoLong) {
                return "** :star: " + playerOneData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerOneLong + ") ** vs "
                        + playerTwoData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerTwoLong  + ")";
            } else if(playerTwoLong < playerOneLong) {
                return "** :star: " + playerTwoData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerTwoLong + ") ** vs "
                        + playerOneData.getSteamUserInfo().getPlayers().get(0).getPersonaname() + " (" + playerOneLong  + ")";
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
