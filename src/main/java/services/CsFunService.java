package services;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsFunService {

    private Properties properties;
    ResourceBundle resourceBundle;

    Map<String, String> wowList;

    public CsFunService(Properties properties) {
        this.properties = properties;
        wowList = new HashMap<String, String>();
        wowList.put("jay_th", "https://cdn.discordapp.com/attachments/449281855175393280/1221510017354563674/loud.mov");
        wowList.put("vi24ra", "https://cdn.discordapp.com/attachments/288367861515419649/1167948820525621248/Dropshot.mp4");
        wowList.put("aatha", "https://cdn.discordapp.com/attachments/844510835241910303/1225082807110336653/Me_Is_Sorry_Janes.mp4");
    }

    public String handleWowEvent(UserContextInteractionEvent event, String locale) {

        String dedicatedChannel = properties.getProperty("discord.dedicatedChannel");
        User targetUser = event.getTarget();
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String targetUserName = targetUser.getName();

        if("CSBot".equals(targetUserName)) {
            //teehee.
            return sendMessageInCorrectChannel(event,dedicatedChannel, "https://www.youtube.com/watch?v=2qTHmSyqrok");
        } else if(targetUser.isBot()) {
            return sendMessageInCorrectChannel(event, dedicatedChannel, resourceBundle.getString("error.cantwowabot"));
        } else {
            if(wowList.containsKey(targetUserName)) {
                return sendMessageInCorrectChannel(event, dedicatedChannel, wowList.get(targetUserName));
            } else {
                return sendMessageInCorrectChannel(event, dedicatedChannel, resourceBundle.getString("error.hasnowow"));
            }
        }
    }

    private String sendMessageInCorrectChannel(GenericCommandInteractionEvent event, String dedicatedChannel, String originalMessage) {
        if(dedicatedChannel.equals(event.getMessageChannel().getId())) {
            return originalMessage;
        } else {
            event.getHook().getInteraction().getGuild().getTextChannelById(dedicatedChannel).sendMessage(originalMessage).queue();
            return resourceBundle.getString("wow.messageSent");
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

        if(ytMatcher.find() || dMatcher.find()) {
            wowList.put(user, url);
            return resourceBundle.getString("wow.done");
        } else {
            return resourceBundle.getString("error.invalidwow");
        }
    }
}