package messages;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import retakeServer.ConsoleUpdate;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RetakeMessage extends ListenerAdapter {
    private final DateTimeFormatter LOGGED_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Properties properties;
    private String allowedRoleId;
    private String serverIp;
    private int serverPort;
    private String serverPassword;
    private int delay;
    private String allowedMaps;
    private LocalTime endTime;
    private boolean hasTimerStarted = false;

    public RetakeMessage(Properties properties) {
        super();

        this.properties = properties;

        this.allowedRoleId = properties.getProperty("discord.allowedRoleId");
        this.serverIp = properties.getProperty("server.ip");
        this.serverPort = Integer.parseInt(properties.getProperty("server.port"));
        this.serverPassword = properties.getProperty("server.password");
        this.delay = Integer.parseInt(properties.getProperty("server.delay"));
        this.allowedMaps = properties.getProperty("csgo.maps");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        try {
            //role object obtained via id set in properties
            Role allowedRole = event.getGuild().getRoleById(allowedRoleId);
            //rcon channel
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
            //list of allowed maps to switch to set in properties
            List<String> allowedMapsList = Arrays.asList(allowedMaps.split(","));

            if (event.getMember().getRoles().contains(allowedRole)) {

                if ("changelevel".equals(event.getName())) {
                    if (allowedMapsList.contains(event.getOption("map").getAsString())) {
                        LocalTime currentTime = LocalTime.now();
                        if (endTime == null || currentTime.isAfter(endTime)) {
                            StringBuilder logMessage = new StringBuilder();
                            logMessage.append("---\n");
                            logMessage.append("Requested Time: " + currentTime.format(LOGGED_TIME) + "\n");
                            //logMessage.append(event.getMember().getNickname() + ": " +  + "\n");

                            rcon.command("changelevel " + event.getOption("map"));
                            endTime = LocalTime.now().plusSeconds(delay);

                            logMessage.append("End Time: " + endTime.format(LOGGED_TIME) + "\n");
                            logMessage.append("---\n");

                            System.out.println(logMessage.toString());

                            event.reply("Map gewechselt auf " + event.getOption("map")).queue();
                        } else {
                            int missingTime = endTime.toSecondOfDay() - currentTime.toSecondOfDay();
                            event.reply("Cooldown aktiv. Bitte warte noch " + missingTime + " Sekunden.").queue();
                        }
                    } else {
                        event.reply("Diese Map ist nicht gültig.").queue();
                    }
                } else {
                    event.reply("Du darfst leider keine Maps wechseln. :(").queue();
                }
            }
        } catch (IOException ex) {
            event.reply("Leider ist etwas kaputt gegangen. :(").queue();
        } catch (AuthenticationException ex) {
            event.reply("Leider lief etwas beim Server schief. :(").queue();
        }
    }

    private void startCongratulateTask(MessageChannel channel) {
        TimerTask hourSchedule = new TimerTask() {
            @Override
            public void run() {
                ConsoleUpdate consoleUpdate = new ConsoleUpdate(properties, channel);
                consoleUpdate.congratulateStreakWinners();
            }
        };
        //checks every hour
        new Timer().schedule(hourSchedule, 1000, 3600000);
    }
}
