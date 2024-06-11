package services;

import spark.Request;

import java.util.Properties;
import java.util.ResourceBundle;

public class FaceitMatchService {
    private Properties properties;
    ResourceBundle resourceBundle;

    public FaceitMatchService(Properties properties) {
        this.properties = properties;
    }

    public void receiveMatchUpdate(Request request) {
        System.out.println(request.body());
    }

    public void handleFaceitMatchUpdateEvent() {
        //TODO expand
    }
}
