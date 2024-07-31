package services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordService {

    DataService dataService;
    Properties properties;

    JDA jda;

    public DiscordService(Properties properties, DataService dataService) {
        this.properties = properties;
        this.dataService = dataService;
    }

    public String getUserLocale(GenericCommandInteractionEvent event) {
        String locale = "en";
        if(event.getInteraction().getUserLocale().getLocale().equals("de")) {
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
                scheduleCollectionTask();
                System.out.println("Collection Task finished at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
            }
        };

        //TODO set this to daily, not hourly
        Timer timer = new Timer("Daily Collection Timer");
        long delay = 5000L;
        timer.schedule(collectionTask, delay, 3600000L);
    }

    private void scheduleCollectionTask() {
        for(Guild guild : jda.getGuilds()) {
            for(Member member : guild.getMembers()) {
                try {
                    dataService.addUserToDatabase(member.getUser().getName(), member.getId());
                } catch (SQLException ex) {
                    System.out.println("SQLException thrown: " + ex.getMessage());
                }
            }
        }
    }
}
