import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetakeMessage extends ListenerAdapter {

    private final String CHANGELEVEL_PATTERN = "(changelevel )(de_{1}[a-zA-Z]+)";
    private final String FORCEWINNERS_PATTERN = "(forcewinners)";
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
    public void onMessageReceived(MessageReceivedEvent event) {
        try {

            //origin channel of message
            MessageChannel channel = event.getChannel();

/*
            //this has never really worked.
            if(!hasTimerStarted) {
                startCongratulateTask(channel);
                hasTimerStarted = true;
            }
 */
            //message object
            Message message = event.getMessage();
            //role object obtained via id set in properties
            Role allowedRole = event.getGuild().getRoleById(allowedRoleId);
            //rcon channel
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
            //excepted message pattern
            Pattern changelevelPattern = Pattern.compile(CHANGELEVEL_PATTERN);
            Pattern forceWinnersPattern = Pattern.compile(FORCEWINNERS_PATTERN);
            //list of allowed maps to switch to set in properties
            List<String> allowedMapsList = Arrays.asList(allowedMaps.split(","));

            if (event.getMember().getRoles().contains(allowedRole)) {
                Matcher changelevelMatcher = changelevelPattern.matcher(message.getContentDisplay());
                Matcher forceWinnersMatcher = forceWinnersPattern.matcher(message.getContentDisplay());
                if (changelevelMatcher.matches()) {
                    String requestedMap = changelevelMatcher.group(2);
                    if (allowedMapsList.contains(requestedMap)) {
                        LocalTime currentTime = LocalTime.now();
                        if (endTime == null || currentTime.isAfter(endTime)) {
                            StringBuilder logMessage = new StringBuilder();
                            logMessage.append("---\n");
                            logMessage.append("Requested Time: " + currentTime.format(LOGGED_TIME) + "\n");
                            logMessage.append(event.getAuthor().getName() + ": " + message.getContentDisplay() + "\n");

                            rcon.command(message.getContentDisplay());
                            endTime = LocalTime.now().plusSeconds(delay);

                            logMessage.append("End Time: " + endTime.format(LOGGED_TIME) + "\n");
                            logMessage.append("---\n");

                            System.out.println(logMessage.toString());

                            channel.addReactionById(message.getId(), "U+1F504").queue();
                            channel.sendMessage("Map gewechselt.").queue();
                        } else {
                            int missingTime = endTime.toSecondOfDay() - currentTime.toSecondOfDay();
                            channel.addReactionById(message.getId(), "U+26A0").queue();
                            channel.sendMessage("Cooldown aktiv. Bitte warte noch " + missingTime + " Sekunden.").queue();
                        }
                    }
                }
                if(forceWinnersMatcher.matches()) {
                    ConsoleUpdate consoleUpdate = new ConsoleUpdate(properties, channel);
                    consoleUpdate.congratulateStreakWinners();
                }
            }
        } catch (AuthenticationException ex) {
            System.out.println("RCON Authentication failed.");
        } catch (IOException ex) {
            System.out.println("IO Exception");
        }
    }

    private void startCongratulateTask(MessageChannel channel) {
        TimerTask hourSchedule = new TimerTask () {
            @Override
            public void run () {
                ConsoleUpdate consoleUpdate = new ConsoleUpdate(properties, channel);
                consoleUpdate.congratulateStreakWinners();
            }
        };
        //checks every hour
        new Timer().schedule(hourSchedule, 1000, 3600000);
    }
}
