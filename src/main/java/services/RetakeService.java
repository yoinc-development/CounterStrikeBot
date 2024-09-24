package services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import model.retake.RankStats;
import retakeServer.ServerStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RetakeService {

    private String allowedRoleId;
    private String serverIp;
    private int serverPort;
    private String serverPassword;
    private int delay;
    private String allowedMaps;
    private LocalTime endTime;
    private ResourceBundle resourceBundle;
    private DataService dataService;
    MessageService messageService;

    public RetakeService(Properties properties, DataService dataService, MessageService messageService) {
        this.allowedRoleId = properties.getProperty("discord.allowedRoleId");
        this.serverIp = properties.getProperty("server.ip");
        this.serverPort = Integer.parseInt(properties.getProperty("server.port"));
        this.serverPassword = properties.getProperty("server.password");
        this.delay = Integer.parseInt(properties.getProperty("server.delay"));
        this.allowedMaps = properties.getProperty("csgo.maps");
        this.dataService = dataService;
        this.messageService = messageService;
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
                        return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("map.changed").replace("%s", event.getOption("map").getAsString()), locale);
                    } else {
                        int missingTime = endTime.toSecondOfDay() - currentTime.toSecondOfDay();
                        return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("map.cooldown").replace("%s", String.valueOf(missingTime)), locale);
                    }
                } else {
                    return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("error.invalidmap"), locale);
                }
            } else {
                return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("error.mapnotallowed"), locale);
            }
        } catch (AuthenticationException ex) {
            System.out.println("[CSBot - RetakeService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] AuthenticationException thrown: " + ex.getMessage());
            return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("error.majorerror"), locale);
        } catch (IOException ex) {
            System.out.println("[CSBot - RetakeService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] IOException thrown: " + ex.getMessage());
            return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("error.majorerror"), locale);
        }
    }

    public EmbedBuilder handleStatsEvent(UserContextInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        User user = event.getTarget();
        try {
            RankStats rankStats = dataService.getRanksStatsForUsername(user.getName());
            if (rankStats != null) {
                return messageService.sendEmbedMessageInCorrectChannel(event, RankStats.getRankStatsMessage(resourceBundle, rankStats), locale);
            }
        } catch (SQLException ex) {
            System.out.println("[CSBot - RetakeService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] SQLException thrown: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            System.out.println("[CSBot - RetakeService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] NumberFormatException thrown: " + ex.getMessage());
        }
        return new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror"));
    }

    public EmbedBuilder handleStatusEvent(SlashCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        ServerStatus serverStatus = getServerStatus();
        if (serverStatus != null) {
            EmbedBuilder statusMessage = serverStatus.getStatusMessage(resourceBundle);
            return messageService.sendEmbedMessageInCorrectChannel(event, statusMessage, locale);
        } else {
            return ServerStatus.getInactiveStatusMessage(resourceBundle);
        }
    }

    public ServerStatus getServerStatus() {
        try {
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
            String status = rcon.command("status");
            rcon.disconnect();
            return new ServerStatus(status);
        } catch (AuthenticationException | IOException e) {
            return null;
        }
    }
}
