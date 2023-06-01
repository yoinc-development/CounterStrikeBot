import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.util.Properties;

public class RetakeMessage extends ListenerAdapter {

    private String allowedRoleId;
    private String serverIp;
    private int serverPort;
    private String serverPassword;

    public RetakeMessage(Properties properties) {
        super();
        this.allowedRoleId = properties.getProperty("discord.allowedRoleId");
        this.serverIp = properties.getProperty("server.ip");
        this.serverPort = Integer.parseInt(properties.getProperty("server.port"));
        this.serverPassword = properties.getProperty("server.password");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        try {
            Role allowedRole = event.getGuild().getRoleById(allowedRoleId);
            MessageChannel channel = event.getChannel();
            String message = event.getMessage().getContentDisplay();
            Rcon rcon = new Rcon(serverIp, serverPort, serverPassword.getBytes());


            if (event.getMember().getRoles().contains(allowedRole)) {
                if (message.startsWith("changelevel")) {
                    System.out.println(event.getAuthor().getName() + ": " + message);
                    String result = rcon.command(message);
                    if(result == null || result.isEmpty()) {
                        channel.sendMessage("Level changed.").queue();
                    }
                } else {
                    channel.sendMessage("Not allowed").queue();;
                }
            }
        } catch (AuthenticationException ex) {
            System.out.println("RCON Authentication failed.");
        } catch (IOException ex) {
            System.out.println("IO Exception");
        }
    }
}
