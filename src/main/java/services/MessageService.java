package services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class MessageService {

    ResourceBundle resourceBundle;
    Properties properties;
    String HOME_CHANNEL = "855156874184491062";

    public MessageService(Properties properties) {
        this.properties = properties;
    }


    public EmbedBuilder sendEmbedMessageInCorrectChannel(GenericCommandInteractionEvent event, String[] teams, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(resourceBundle.getString("teams.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");
        for (int i = 0; i < teams.length; i++) {
            embedBuilder.addField(new MessageEmbed.Field("Team " + (i + 1), teams[i], true));
        }
        return embedBuilder;
    }


    public String sendMessageInCorrectChannel(GenericCommandInteractionEvent event, String message) {
        if(event.getGuild().getId().equals(properties.getProperty("discord.thisIsMyHome"))) {
            if(!event.getMessageChannel().getId().equals(HOME_CHANNEL)) {
                //TODO: Getting null for .getTextChannelById(HOME_CHANNEL) when sending /map in csgo-stuff
                event.getHook().getInteraction().getGuild().getTextChannelById(HOME_CHANNEL).sendMessage(message).queue();
                return resourceBundle.getString("info.messagesent");
            }
        }
        return message;
    }
}
