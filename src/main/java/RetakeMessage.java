import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RetakeMessage extends ListenerAdapter {

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
            MessageChannel channel = event.getChannel();
            Role allowedRole = event.getGuild().getRoleById(allowedRoleId);
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());
            TextChannel textChannel = event.getTextChannel();
            Message message = event.getMessage();
            List<String> allowedMapsList = Arrays.asList(allowedMaps.split(","));

            if (event.getMember().getRoles().contains(allowedRole)) {

                LocalDateTime currentTime = LocalDateTime.now();
                System.out.println("Start Time: " + currentTime);
                System.out.println(event.getAuthor().getName() + ": " + message.getContentDisplay());

                if (message.getContentDisplay().startsWith("changelevel")) {
                    if (endTime == null || currentTime.isAfter(endTime)) {
                        String[] splitMessage = message.getContentDisplay().split(" ");
                        if (splitMessage.length == 2) {
                            if (allowedMapsList.contains(splitMessage[1])) {
                                String result = rcon.command(message.getContentDisplay());
                                if (result == null || result.isEmpty()) {
                                    endTime = LocalDateTime.now().plusSeconds(delay);
                                    System.out.println("End Time: " + endTime);
                                    textChannel.addReactionById(message.getId(), "U+1F504").queue();
                                    channel.sendMessage("Map gewechselt.").queue();
                                }
                            }
                        }
                    } else {
                        channel.sendMessage("Cooldown aktiv. Bitte warte " + delay + " Sekunden.").queue();
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
