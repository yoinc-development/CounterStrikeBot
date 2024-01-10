package messages;

import com.google.gson.Gson;
import model.CSPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class CSStatsMessage extends ListenerAdapter {

    Properties properties;
    public CSStatsMessage(Properties properties) {
        super();

        this.properties = properties;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if ("stats".equals(event.getName())) {
            try {
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request;

                String requestedUser = event.getOption("player").getAsString().toLowerCase();
                switch (requestedUser) {
                    case "aatha":
                    case "aathavan":
                    case "doge":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198077352267"))
                                .build();
                        break;
                    case "dario":
                    case "däse":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198213130649"))
                                .build();
                        break;
                    case "janes":
                    case "jay":
                    case "grey":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198014462666"))
                                .build();
                        break;
                    case "juan":
                    case "juanita":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198098219020"))
                                .build();
                        break;
                    case "korunde":
                    case "koray":
                    case "ossas":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198071064798"))
                                .build();
                        break;
                    case "nabil":
                    case "drifter":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198088520949"))
                                .build();
                        break;
                    case "nassim":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198203636285"))
                                .build();
                        break;
                    case "nici":
                    case "nigglz":
                    case "n'lölec":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198401419666"))
                                .build();
                        break;
                    case "ravi":
                    case "vi24":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198071074164"))
                                .build();
                        break;
                    case "pavi":
                    case "seraph":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561198102224384"))
                                .build();
                        break;
                    case "sani":
                    case "baka":
                    case "mugiwarabaka":
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=76561197984892194"))
                                .build();
                        break;
                    default:
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=" + requestedUser))
                                .build();
                        break;
                }


                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                CSPlayer csPlayer = new Gson().fromJson(response.body(), CSPlayer.class);
                csPlayer.setNickname(requestedUser);

                event.reply(csPlayer.returnBasicInfo()).queue();

            } catch (InterruptedException ex) {

            } catch (IOException ex) {

            } catch (NullPointerException ex) {
                event.reply("Für " + event.getOption("player").getAsString() + " können keine Stats geladen werden. (Steam Privacy Settings?)").queue();
            }
        }
    }

}
