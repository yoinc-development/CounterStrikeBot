package ch.yoinc.http;

import ch.yoinc.model.leetify.LeetifyMatchResponse;
import ch.yoinc.model.leetify.LeetifyProfileResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class LeetifyConnection {

    private final String apiKey;
    private final HttpClient client;
    private final Gson gson;
    private final String LEETIFY_API = "https://api-public.cs-prod.leetify.com";


    public LeetifyConnection(Properties properties) {
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

    public LeetifyProfileResponse getPlayerProfile(String steam64ID, String leetifyID) {
        String parameter = steam64ID == null ? "id=" + leetifyID : "steam64_id=" + steam64ID;
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(LEETIFY_API + "v3/profile?" + parameter))
                .header("Authorization", "Bearer " + apiKey)
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
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<LeetifyMatchResponse>>() {
                }.getType());
            }
            System.out.println("[CSBot - LeetifyConnection - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] getPlayerMatchHistory for " + parameter + " returned status " + response.statusCode() + ", body: " + response.body());
        } catch (IOException | InterruptedException ex) {
            System.out.println("[CSBot - LeetifyConnection - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] IOException / InterruptedException thrown: " + ex.getMessage());
        }
        return null;
    }
}
