package ch.yoinc.http;

import com.google.gson.*;
import ch.yoinc.model.steam.ResponseData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class ConnectionBuilder {

    Properties properties;

    private final String STEAM_API = "https://api.steampowered.com";

    public ConnectionBuilder(Properties properties) {
        this.properties = properties;
    }

    public ResponseData fetchSteamUserStats(String steamID) throws InterruptedException, IOException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request;
        ResponseData responseData;

        request = HttpRequest.newBuilder()
                .uri(URI.create(STEAM_API + "/ISteamUser/GetPlayerSummaries/v0002/?key=" + properties.get("steam.api") + "&steamids=" + steamID))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        responseData = new Gson().fromJson(response.body(), ResponseData.class);

        request = HttpRequest.newBuilder()
                .uri(URI.create(STEAM_API + "/ISteamUserStats/GetUserStatsForGame/v0002/?key=" + properties.get("steam.api") + "&appid=730&steamid=" + steamID))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        responseData.setPlayerstats(new Gson().fromJson(response.body(), ResponseData.class).getPlayerstats());

        return responseData;
    }
}
