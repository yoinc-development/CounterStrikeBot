package services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import retakeServer.RetakeWatchdog;
import retakeServer.ServerStatus;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DiscordService {

    DataService dataService;
    Properties properties;
    RetakeService retakeService;
    JDA jda;
    ResourceBundle resourceBundle;
    MessageService messageService;

    public DiscordService(Properties properties, DataService dataService, RetakeService retakeService) {
        this.properties = properties;
        this.dataService = dataService;
        this.retakeService = retakeService;
        this.resourceBundle = ResourceBundle.getBundle("localization", new Locale("en"));
        this.messageService = new MessageService(properties);
    }

    public String getUserLocale(GenericCommandInteractionEvent event) {
        String locale = "en";
        if (event.getInteraction().getUserLocale().getLocale().equals("de")) {
            locale = "de";
        }
        return locale;
    }

    public void scheduleAllTasks(JDA jda) {
        this.jda = jda;

        TimerTask collectionTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Collection Task started at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
                runCollectionTask();
                System.out.println("Collection Task finished at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
            }
        };

        TimerTask statsTask = new TimerTask() {
            @Override
            public void run() {
                runStatsTask();
            }
        };

        TimerTask joinTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Join Task started at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
                runJoinTask();
                System.out.println("Join Task finished at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
            }
        };

        //TODO set this to daily, not hourly
        Timer timer = new Timer("Daily Collection Timer");
        long delay = 0L;
        timer.schedule(collectionTask, delay, 3600000L);
        timer.schedule(joinTask, delay, 300000L);
    }

    private void runCollectionTask() {
        for (Guild guild : jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                dataService.addUserToDatabase(member.getUser().getName(), member.getId());
            }
        }
    }

    private void runStatsTask() {

    }

    private void runJoinTask() {
        try {
            ServerStatus serverStatus = retakeService.getServerStatus();
            if (serverStatus != null) {
                if (!serverStatus.getPlayerNames().isEmpty()) {
                    if (!dataService.hasSentRetakeInvite()) {
                        EmbedBuilder embedBuilder = RetakeWatchdog.getJoinMessage(resourceBundle, getRandomPlayer(serverStatus), serverStatus.getCurrentMap());
                        ItemComponent button = Button.link(properties.getProperty("server.connectLink"), resourceBundle.getString("serverwatchdog.invite"));
                        String messageId = messageService.sendBotEmbedMessageWithAction(jda, embedBuilder, button);
                        dataService.addRetakeInvite(messageId, new Timestamp(System.currentTimeMillis()).toString());
                    }
                } else {
                    if (dataService.hasSentRetakeInvite()) {
                        String messageId = dataService.getRetakeInviteMsgId();
                        messageService.removeBotMessage(jda, messageId);
                        dataService.removeRetakeInvite();
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQLException thrown: " + ex.getMessage());
        }
    }

    private String getRandomPlayer(ServerStatus serverStatus) {
        List<String> playerNames = serverStatus.getPlayerNames();
        Random rand = new Random();
        int randIndex = rand.nextInt(playerNames.size());
        return playerNames.get(randIndex);
    }
}
