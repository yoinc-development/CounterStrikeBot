package services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import retakeServer.RankStats;

import java.io.IOException;
import java.sql.SQLException;
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
    private DataService dataService;

    public RetakeService(Properties properties, DataService dataService) {
        this.allowedRoleId = properties.getProperty("discord.allowedRoleId");
        this.serverIp = properties.getProperty("server.ip");
        this.serverPort = Integer.parseInt(properties.getProperty("server.port"));
        this.serverPassword = properties.getProperty("server.password");
        this.delay = Integer.parseInt(properties.getProperty("server.delay"));
        this.allowedMaps = properties.getProperty("csgo.maps");
        this.dataService = dataService;
    }

    public String handleMapEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        try {
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
            List<String> allowedMapsList = Arrays.asList(allowedMaps.split(","));

            if (event.getMember().getRoles().contains(event.getGuild().getRoleById(allowedRoleId))) {
                if (allowedMapsList.contains(event.getOption("map").getAsString())) {
                    LocalTime currentTime = LocalTime.now();
                    if (endTime == null || currentTime.isAfter(endTime)) {
                        rcon.command("changelevel " + event.getOption("map").getAsString());
                        endTime = LocalTime.now().plusSeconds(delay);
                        return resourceBundle.getString("map.changed").replace("%s", event.getOption("map").getAsString());
                    } else {
                        int missingTime = endTime.toSecondOfDay() - currentTime.toSecondOfDay();
                        return resourceBundle.getString("map.cooldown").replace("%s", String.valueOf(missingTime));
                    }
                } else {
                    return resourceBundle.getString("error.invalidmap");
                }
            } else {
                return resourceBundle.getString("error.mapnotallowed");
            }
        } catch (AuthenticationException ex) {
            System.out.println("AuthenticationException thrown: " + ex.getMessage());
            return resourceBundle.getString("error.majorerror");
        } catch (IOException ex) {
            System.out.println("IOException thrown: " + ex.getMessage());
            return resourceBundle.getString("error.majorerror");}
    }

    public EmbedBuilder handleStatsEvent(UserContextInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        User user = event.getTarget();
        try {
            RankStats rankStats = dataService.getRanksStatsForUsername(user.getName());
            if (rankStats != null) {
                EmbedBuilder message = RankStats.getRankStatsMessage(resourceBundle, rankStats);
                return message;
            }
        } catch (SQLException ex) {
            System.out.println("SQLException thrown: " + ex.getMessage());
        }
        return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
    }
}
