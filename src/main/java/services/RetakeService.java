package services;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RetakeService {

    private final DateTimeFormatter LOGGED_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private String allowedRoleId;
    private String serverIp;
    private int serverPort;
    private String serverPassword;
    private int delay;
    private String allowedMaps;
    private LocalTime endTime;
    private ResourceBundle resourceBundle;

    public RetakeService(Properties properties) {
        this.allowedRoleId = properties.getProperty("discord.allowedRoleId");
        this.serverIp = properties.getProperty("server.ip");
        this.serverPort = Integer.parseInt(properties.getProperty("server.port"));
        this.serverPassword = properties.getProperty("server.password");
        this.delay = Integer.parseInt(properties.getProperty("server.delay"));
        this.allowedMaps = properties.getProperty("csgo.maps");
    }

    public String handleMapEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        return resourceBundle.getString("error.mapsdisabled");

        /*
        //rcon channel
        //Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
        //list of allowed maps to switch to set in properties
        List<String> allowedMapsList = Arrays.asList(allowedMaps.split(","));

        if (event.getMember().getRoles().contains(event.getGuild().getRoleById(allowedRoleId))) {
            if (allowedMapsList.contains(event.getOption("map").getAsString())) {
                LocalTime currentTime = LocalTime.now();
                if (endTime == null || currentTime.isAfter(endTime)) {
                    StringBuilder logMessage = new StringBuilder();
                    logMessage.append("---\n");
                    logMessage.append("Requested Time: " + currentTime.format(LOGGED_TIME) + "\n");
                    //logMessage.append(event.getMember().getNickname() + ": " +  + "\n");

                    //rcon.command("changelevel " + event.getOption("map"));
                    endTime = LocalTime.now().plusSeconds(delay);

                    logMessage.append("End Time: " + endTime.format(LOGGED_TIME) + "\n");
                    logMessage.append("---\n");

                    System.out.println(logMessage.toString());

                    return "Map gewechselt auf " + event.getOption("map");
                } else {
                    int missingTime = endTime.toSecondOfDay() - currentTime.toSecondOfDay();
                    return "Cooldown aktiv. Bitte warte noch " + missingTime + " Sekunden.";
                }
            } else {
                return "Diese Map ist nicht gültig.";
            }
        } else {
            return "Du darfst leider keine Maps wechseln. :(";
        }
         */
    }
}
