package services;

import com.google.gson.*;
import model.faceit.FaceitMatch;
import net.dv8tion.jda.api.entities.Guild;
import spark.Request;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class FaceitMatchService {
    private Properties properties;
    ResourceBundle resourceBundle;
    DataService dataService;

    public FaceitMatchService(Properties properties, DataService dataService) {
        this.properties = properties;
        this.dataService = dataService;
    }

    public void handleFaceitMatchStartEvent(Request request, List<Guild> allGuilds) {
        System.out.println("Faceit Match Start // Attributes:");
        for(String string : request.attributes()) {
            System.out.println(string);
        }
        System.out.println("-------");
        System.out.println("Body:");
        System.out.println(request.body());
        System.out.println("-------");
        String userId = request.params("user");
        try {
            FaceitMatch match = fetchMatchDetails(userId);
            System.out.println(match.getMatch_id()); // placeholder
        } catch (IOException | InterruptedException e) {
            System.out.println("Error while fetching Match Info...");
        }
    }

    public void handleFaceitMatchEndEvent(Request request, List<Guild> allGuilds) {
        System.out.println("Faceit Match End // Attributes:");
        for(String string : request.attributes()) {
            System.out.println(string);
        }
        System.out.println("-------");
        System.out.println("Body:");
        System.out.println(request.body());
        System.out.println("-------");

        //TODO use the data service
    }

    private FaceitMatch fetchMatchDetails(String userId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String matchId = fetchMatchId(client, userId);

        HttpRequest request;
        FaceitMatch responseData;

        request = HttpRequest.newBuilder()
                .uri(URI.create("https://open.faceit.com/data/v4/matches/" + matchId))
                .header("Authorization", "Bearer " + properties.getProperty("faceit.api"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        responseData = new Gson().fromJson(response.body(), FaceitMatch.class);

        return responseData;
    }

    private String fetchMatchId(HttpClient client, String userId) throws IOException, InterruptedException {
        HttpRequest request;
        String id = null;

        request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.faceit.com/api/match/v1/matches/groupByState?userId=" + userId))
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
