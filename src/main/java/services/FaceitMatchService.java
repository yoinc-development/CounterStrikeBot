package services;

import com.google.gson.*;
import http.ConnectionBuilder;
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
    ConnectionBuilder connectionBuilder;


    public FaceitMatchService(Properties properties, DataService dataService) {
        this.properties = properties;
        this.dataService = dataService;
        connectionBuilder = new ConnectionBuilder(properties);
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
            FaceitMatch match = connectionBuilder.fetchFaceitMatchDetails(userId);
            System.out.println(match.getMatch_id()); // placeholder
        } catch (IOException ex) {
            System.out.println("IOException thrown: " + ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException thrown: " + ex.getMessage());
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
}
