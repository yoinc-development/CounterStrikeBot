package ch.yoinc.services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

public class MessageService {

    ResourceBundle resourceBundle;
    String HOME_GUILD;
    String HOME_CHANNEL;

    public MessageService(Properties properties) {
        HOME_GUILD = properties.getProperty("discord.guildID");
        HOME_CHANNEL = properties.getProperty("discord.channelID");
    }

    public EmbedBuilder sendEmbedMessageInCorrectChannel(GenericCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        resourceBundle = ResourceBundle.getBundle("localization", Locale.of("en"));

        if(Objects.requireNonNull(event.getGuild()).getId().equals(HOME_GUILD)) {
            if(!event.getMessageChannel().getId().equals(HOME_CHANNEL)) {
                EmbedBuilder infoEmbed = new EmbedBuilder();
                infoEmbed.setTitle(resourceBundle.getString("info.messagesent"))
                        .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");
                Objects.requireNonNull(Objects.requireNonNull(event.getHook().getInteraction().getGuild()).getTextChannelById(HOME_CHANNEL)).sendMessageEmbeds(embedBuilder.build()).queue();
                return infoEmbed;
            }
        }
        return embedBuilder;
    }
}
