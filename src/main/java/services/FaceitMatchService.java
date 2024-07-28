package services;

import net.dv8tion.jda.api.entities.Guild;
import spark.Request;

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
