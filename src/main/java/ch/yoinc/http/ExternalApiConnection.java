package ch.yoinc.http;

import ch.yoinc.model.leetify.LeetifyMatchResponse;
import ch.yoinc.model.leetify.LeetifyProfileResponse;
import com.google.gson.*;
import ch.yoinc.model.steam.ResponseData;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class ExternalApiConnection {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiConnection.class);

    private final String apiKey;
    private final HttpClient client;
    private final Gson gson;
    private final Properties properties;
    private final String LEETIFY_API = "https://api-public.cs-prod.leetify.com";
    private final String STEAM_API = "https://api.steampowered.com";

    public ExternalApiConnection(Properties properties) {
        this.properties = properties;
        this.apiKey = properties.getProperty("leetify.apiToken");
        gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                    @Override
                    public void write(JsonWriter out, Instant value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
                    }

                    @Override
                    public Instant read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return Instant.parse(in.nextString());
                    }
                })
                .create();
        this.client = HttpClient.newHttpClient();
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
        if (response.statusCode() == 200) {
            responseData.setPlayerstats(new Gson().fromJson(response.body(), ResponseData.class).getPlayerstats());
        }
        if (response.statusCode() != 404) {
            log.warn("fetchSteamUserStats for {} returned status {}, body: {}", steamID, response.statusCode(), response.body());
        }

        return responseData;
    }

    public LeetifyProfileResponse getPlayerProfile(String steam64ID, String leetifyID) throws InterruptedException, IOException {
        String parameter = steam64ID == null ? "id=" + leetifyID : "steam64_id=" + steam64ID;
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(LEETIFY_API + "/v3/profile?" + parameter))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), LeetifyProfileResponse.class);
        }
        if (response.statusCode() != 404) {
            log.warn("getPlayerProfile for {} returned status {}, body: {}", parameter, response.statusCode(), response.body());
        }
        return null;
    }

    public List<LeetifyMatchResponse> getPlayerMatchHistory(String steam64ID, String leetifyID) throws InterruptedException, IOException {
        String parameter = steam64ID == null ? "id=" + leetifyID : "steam64_id=" + steam64ID;
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(LEETIFY_API + "/v3/profile/matches?" + parameter))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<LeetifyMatchResponse>>() {
            }.getType());
        }
        if (response.statusCode() != 404) {
            log.warn("getPlayerMatchHistory for {} returned status {}, body: {}", parameter, response.statusCode(), response.body());
        }
        return null;
    }
}
