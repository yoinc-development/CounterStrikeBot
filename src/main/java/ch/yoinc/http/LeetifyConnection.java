package ch.yoinc.http;

import ch.yoinc.model.leetify.LeetifyMatchResponse;
import ch.yoinc.model.leetify.LeetifyProfileResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeetifyConnection {

    private final String LEETIFY_API = "https://api-public.cs-prod.leetify.com";

    private final HttpClient client;
    private final Gson gson = new Gson();

    public LeetifyConnection() {
        this.client = HttpClient.newHttpClient();
    }

    public LeetifyProfileResponse getPlayerProfile(String steam64ID, String leetifyID) {
        String parameter = steam64ID == null ? "id=" + leetifyID : "steam64_id=" + steam64ID;
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(LEETIFY_API + "v3/profile?" + parameter))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), LeetifyProfileResponse.class);
        } catch (IOException | InterruptedException ex) {
            System.out.println("[CSBot - LeetifyConnection - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] IOException / InterruptedException thrown: " + ex.getMessage());
        }
        return null;
    }

    public List<LeetifyMatchResponse> getPlayerMatchHistory(String steam64ID, String leetifyID) {
        String parameter = steam64ID == null ? "id=" + leetifyID : "steam64_id=" + steam64ID;
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(LEETIFY_API + "/v3/profile/matches?" + parameter))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<LeetifyMatchResponse>>() {}.getType());
        } catch (IOException | InterruptedException ex) {
            System.out.println("[CSBot - LeetifyConnection - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] IOException / InterruptedException thrown: " + ex.getMessage());
        }
        return null;
    }
}
