import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetakeMessage extends ListenerAdapter {

    private final String CHANGELEVEL_PATTERN = "(changelevel )(de_{1}[a-zA-Z]+)";
    private final DateTimeFormatter LOGGED_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String allowedRoleId;
    private String serverIp;
    private int serverPort;
    private String serverPassword;
    private int delay;
    private String allowedMaps;

    private LocalDateTime endTime;

    public RetakeMessage(Properties properties) {
        super();
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
            //message object
            Message message = event.getMessage();
            //role object obtained via id set in properties
            Role allowedRole = event.getGuild().getRoleById(allowedRoleId);
            //rcon channel
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
            //excepted message pattern
            Pattern changelevelPattern = Pattern.compile(CHANGELEVEL_PATTERN);
            //list of allowed maps to switch to set in properties
            List<String> allowedMapsList = Arrays.asList(allowedMaps.split(","));

            if (event.getMember().getRoles().contains(allowedRole)) {
                Matcher changelevelMatcher = changelevelPattern.matcher(message.getContentDisplay());
                if (changelevelMatcher.matches()) {
                    String requestedMap = changelevelMatcher.group(2);
                    if (allowedMapsList.contains(requestedMap)) {
                        LocalDateTime currentTime = LocalDateTime.now();
                        if (endTime == null || currentTime.isAfter(endTime)) {
                            StringBuilder logMessage = new StringBuilder();
                            logMessage.append("---\n");
                            logMessage.append("Requested Time: " + currentTime.format(LOGGED_TIME) + "\n");
                            logMessage.append(event.getAuthor().getName() + ": " + message.getContentDisplay() + "\n");

                            rcon.command(message.getContentDisplay());
                            endTime = LocalDateTime.now().plusSeconds(delay);

                            logMessage.append("End Time: " + endTime.format(LOGGED_TIME) + "\n");
                            logMessage.append("---\n");

                            System.out.println(logMessage.toString());

                            channel.addReactionById(message.getId(), "U+1F504").queue();
                            channel.sendMessage("Map gewechselt.").queue();
                        } else {
                            channel.addReactionById(message.getId(), "U+26A0").queue();
                            channel.sendMessage("Cooldown aktiv. Bitte warte " + delay + " Sekunden.").queue();
                        }
                    }
                }
            }
        } catch (AuthenticationException ex) {
            System.out.println("RCON Authentication failed.");
        } catch (IOException ex) {
            System.out.println("IO Exception");
        }
    }
}
