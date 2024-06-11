package services;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsFunService {

    private Properties properties;
    private DataService dataService;
    ResourceBundle resourceBundle;

    Map<String, String> wowList;

    public CsFunService(Properties properties) {
        this.properties = properties;
        setupWowList();
    }

    public String handleWowEvent(UserContextInteractionEvent event, String locale) {

        String dedicatedChannel = properties.getProperty("discord.dedicatedChannel");
        User targetUser = event.getTarget();
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String targetUserName = targetUser.getName();

        if(wowList.containsKey(targetUserName)) {
            String message = resourceBundle.getString("wow.highlightMessage").replace("%s", targetUserName) + " " + wowList.get(targetUserName);
            return sendMessageInCorrectChannel(event, dedicatedChannel, message);
        } else if(targetUser.isBot()) {
            return sendMessageInCorrectChannel(event, dedicatedChannel, resourceBundle.getString("error.cantwowabot"));
        } else {
            return sendMessageInCorrectChannel(event, dedicatedChannel, resourceBundle.getString("error.hasnowow"));
        }
    }

    private void setupWowList() {
        wowList = new HashMap<String, String>();
        try {
            dataService = new DataService(properties);
            dataService.setupConnection();
            wowList = dataService.returnAllWowEntries();
        } catch (SQLException exception) {
            System.out.println("Exception thrown.");
        }
    }

    private String sendMessageInCorrectChannel(GenericCommandInteractionEvent event, String dedicatedChannel, String message) {
        if(dedicatedChannel.equals(event.getMessageChannel().getId())) {
            return message;
        } else {
            TextChannel dedicatedTextChannel = event.getHook().getInteraction().getGuild().getTextChannelById(dedicatedChannel);

            //this means no dedicated channel was found for this ID. either no dedicated channel was set or it doesn't exist on this server.
            //either way, this means that the event is going to be returned in the current active channel. that's a bit messy but hey,
            //if that's what they want..?
            if(dedicatedTextChannel == null) {
                return message;
            } else {
                dedicatedTextChannel.sendMessage(message).queue();
                return resourceBundle.getString("wow.messageSent");
            }
        }
    }

    public String handleAddWowEvent(GenericCommandInteractionEvent event, String locale) {

        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        String url = event.getOption("url").getAsString();
        String user = event.getUser().getName();

        Pattern ytPattern = Pattern.compile("(?:https\\:\\/\\/www\\.youtube\\.com\\/watch\\?v\\=)");
        Pattern dPattern = Pattern.compile("(?:https\\:\\/\\/cdn\\.discordapp\\.com\\/attachments)");

        Matcher ytMatcher = ytPattern.matcher(url);
        Matcher dMatcher = dPattern.matcher(url);

        try {
            if (ytMatcher.find() || dMatcher.find()) {
                dataService.addWowEvent(user, url);
                wowList.put(user, url);
                return resourceBundle.getString("wow.done");
            } else {
                return resourceBundle.getString("error.invalidwow");
            }
        } catch (SQLException ex) {
            return resourceBundle.getString("error.majorerror");
        }
    }
}