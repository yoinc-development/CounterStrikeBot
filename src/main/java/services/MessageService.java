package services;

import http.ConnectionBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class MessageService {

    ResourceBundle resourceBundle;
    Properties properties;
    String HOME_GUILD;
    String HOME_CHANNEL;

    public MessageService(Properties properties) {
        this.properties = properties;
        HOME_GUILD = properties.getProperty("discord.guildID");
        HOME_CHANNEL = properties.getProperty("discord.channelID");
    }

    public EmbedBuilder sendEmbedMessageInCorrectChannel(GenericCommandInteractionEvent event, EmbedBuilder embedBuilder, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        if(event.getGuild().getId().equals(HOME_GUILD)) {
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
}
