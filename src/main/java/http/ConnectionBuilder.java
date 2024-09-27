package http;

import com.google.gson.*;
import model.faceit.FaceitMatch;
import model.omdb.OMDBMovieResponse;
import model.retake.RetakePlayer;
import model.steam.ResponseData;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConnectionBuilder {

    Properties properties;

    public ConnectionBuilder(Properties properties) {
        this.properties = properties;
    }

    public String fetchAssistantRetakeMessage(RetakePlayer retakePlayer) throws InterruptedException, IOException {
        return null;
    }

    public ResponseData fetchSteamUserStats(String steamID) throws InterruptedException, IOException {

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

    public FaceitMatch fetchFaceitMatchDetails(String userId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String matchId = fetchFaceitMatchId(client, userId);

        HttpRequest request;
        FaceitMatch responseData;

        request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getProperty("faceit.matchDetails.url") + matchId))
                .header("Authorization", "Bearer " + properties.getProperty("faceit.api"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        responseData = new Gson().fromJson(response.body(), FaceitMatch.class);

        return responseData;
    }

    public OMDBMovieResponse fetchMovieDetails(String title) throws IOException, InterruptedException, JsonSyntaxException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request;
        OMDBMovieResponse responseData;

        request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getProperty("omdb.search.url") + prepareTitle(title) + "&apikey=" + properties.getProperty("omdb.api")))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        responseData = new Gson().fromJson(response.body(), OMDBMovieResponse.class);

        return responseData;
    }

    private String prepareTitle(String title) {
        return URLEncoder.encode(title, StandardCharsets.UTF_8);
    }

    private String fetchFaceitMatchId(HttpClient client, String userId) throws IOException, InterruptedException {
        HttpRequest request;
        String id = null;

        request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getProperty("faceit.matchId.url") + userId))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JsonObject payload = jsonObject.getAsJsonObject("payload");
        JsonArray ongoingArray = payload.getAsJsonArray("ONGOING");
        JsonArray readyArray = payload.getAsJsonArray("READY");
        if (ongoingArray != null && !ongoingArray.isEmpty()) {
            JsonObject firstOngoingItem = ongoingArray.get(0).getAsJsonObject();
            id = firstOngoingItem.get("id").getAsString();
        } else if (readyArray != null  && !readyArray.isEmpty()) {
            JsonObject firstReadyItem = readyArray.get(0).getAsJsonObject();
            id = firstReadyItem.get("id").getAsString();
        }
        return id;
    }
}
