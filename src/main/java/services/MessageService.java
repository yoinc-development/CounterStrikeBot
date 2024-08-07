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


    public EmbedBuilder sendEmbedMessageInCorrectChannel(GenericCommandInteractionEvent event, EmbedBuilder embedBuilder, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        if(event.getGuild().getId().equals(properties.getProperty("discord.thisIsMyHome"))) {
            if(!event.getMessageChannel().getId().equals(HOME_CHANNEL)) {
                EmbedBuilder infoEmbed = new EmbedBuilder();
                infoEmbed.setTitle(resourceBundle.getString("info.messagesent"))
                        .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");
                event.getHook().getInteraction().getGuild().getTextChannelById(HOME_CHANNEL).sendMessageEmbeds(embedBuilder.build()).queue();
                return infoEmbed;
            }
        }
        return embedBuilder;
    }


    public String sendMessageInCorrectChannel(GenericCommandInteractionEvent event, String message) {
        if(event.getGuild().getId().equals(properties.getProperty("discord.thisIsMyHome"))) {
            if(!event.getMessageChannel().getId().equals(HOME_CHANNEL)) {
                event.getHook().getInteraction().getGuild().getTextChannelById(HOME_CHANNEL).sendMessage(message).queue();
                return resourceBundle.getString("info.messagesent");
            }
        }
        return message;
    }
}
