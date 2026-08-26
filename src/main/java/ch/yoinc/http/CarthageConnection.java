package ch.yoinc.http;

import ch.yoinc.model.internal.InternalUser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;

/**
 * Talks to carthage's bot-facing /bot/** endpoints, which stand in for the direct MySQL access.
 * Carthage always answers with HTTP 200; failures are signaled via a "hasError" flag in the body,
 * so that must be checked explicitly rather than relying on the status code.
 */
public class CarthageConnection {

    private final Properties properties;
    private final HttpClient client;
    private final Gson gson = new Gson();

    public CarthageConnection(Properties properties) {
        this.properties = properties;
        this.client = HttpClient.newHttpClient();
    }

    public String getSteamIDForDiscordID(String botID, String discordID) throws IOException, InterruptedException, CarthageException {
        JsonObject body = new JsonObject();
        body.addProperty("botID", botID);
        body.addProperty("discordID", discordID);

        return extractString(post("/bot/users/steam", body), "steamID");
    }

    public List<InternalUser> getAllSteamUsers(String botID) throws IOException, InterruptedException, CarthageException {
        JsonObject body = new JsonObject();
        body.addProperty("botID", botID);

        return extractList(post("/bot/steam", body), "users", new TypeToken<List<InternalUser>>() {}.getType());
    }

    public List<String> insertAndGetNewMatches(List<String> matches, Integer userID, String botID) throws IOException, InterruptedException, CarthageException {
        JsonObject body = new JsonObject();
        body.addProperty("botID", botID);
        body.addProperty("userID", userID);
        body.add("matches", gson.toJsonTree(matches));

        return extractList(put("/bot/match", body), "newMatches", new TypeToken<List<String>>() {}.getType());
    }

    private JsonObject post(String path, JsonObject body) throws IOException, InterruptedException, CarthageException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getProperty("carthage.url") + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, path);
    }

    private JsonObject put(String path, JsonObject body) throws IOException, InterruptedException, CarthageException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getProperty("carthage.url") + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return send(request, path);
    }

    private JsonObject send(HttpRequest request, String path) throws IOException, InterruptedException, CarthageException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new CarthageException("Carthage returned status " + response.statusCode() + " for " + path);
        }

        JsonElement jsonElement = JsonParser.parseString(response.body());
        if (!jsonElement.isJsonObject()) {
            throw new CarthageException("Unexpected response from carthage for " + path + ": " + response.body());
        }
        JsonObject responseBody = jsonElement.getAsJsonObject();

        if (responseBody.has("hasError") && responseBody.get("hasError").getAsBoolean()) {
            String error = responseBody.has("error") ? responseBody.get("error").getAsString() : "unknown error";
            throw new CarthageException("Carthage reported an error for " + path + ": " + error);
        }

        return responseBody;
    }

    private String extractString(JsonObject responseBody, String field) {
        if (responseBody.has(field) && !responseBody.get(field).isJsonNull()) {
            return responseBody.get(field).getAsString();
        }
        return null;
    }

    private <T> List<T> extractList(JsonObject responseBody, String field, Type type) {
        if (responseBody.has(field) && !responseBody.get(field).isJsonNull()) {
            return gson.fromJson(responseBody.get(field), type);
        }
        return List.of();
    }
}
