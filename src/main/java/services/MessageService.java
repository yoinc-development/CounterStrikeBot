package services;

import http.ConnectionBuilder;
import model.retake.RetakePlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class MessageService {

    ResourceBundle resourceBundle;
    Properties properties;
    String HOME_CHANNEL = "901976174484418600";
    ConnectionBuilder connectionBuilder;

    public MessageService(Properties properties) {
        this.properties = properties;
        this.connectionBuilder = new ConnectionBuilder(properties);
    }

    public void sendAssistantMessageRetake(RetakePlayer retakePlayer, JDA jda) {
        Guild homeGuild = jda.getGuildById(properties.getProperty("discord.thisIsMyHome"));
        TextChannel textChannel = homeGuild.getTextChannelById(HOME_CHANNEL);
        try {
            textChannel.sendMessage(connectionBuilder.fetchAssistantRetakeMessage(retakePlayer)).queue();
        } catch (InterruptedException ex) {
            System.out.println("[CSBot - MessageService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] InterruptedException thrown: " + ex);
            //TODO send localized message instead of assistant message
        } catch (IOException ex) {
            System.out.println("[CSBot - MessageService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] IOException thrown: " + ex);
            //TODO send localized message instead of assistant message
        }
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


    public String sendMessageInCorrectChannel(GenericCommandInteractionEvent event, String message, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        if(event.getGuild().getId().equals(properties.getProperty("discord.thisIsMyHome"))) {
            if(!event.getMessageChannel().getId().equals(HOME_CHANNEL)) {
                event.getHook().getInteraction().getGuild().getTextChannelById(HOME_CHANNEL).sendMessage(message).queue();
                return resourceBundle.getString("info.messagesent");
            }
        }
        return message;
    }

    public void sendPrivateMessageToUser(JDA jda, String message, String discordID) {
        jda.getUserById(discordID).openPrivateChannel().queue((privateChannel -> {
            privateChannel.sendMessage(message).queue();
        }));
    }

    public String sendBotEmbedMessageWithAction(JDA jda, EmbedBuilder embedBuilder, ItemComponent itemComponent) {
        TextChannel tc = jda.getGuildById(properties.getProperty("discord.thisIsMyHome"))
                .getTextChannelById(HOME_CHANNEL);

        return tc.sendMessageEmbeds(embedBuilder.build()).addActionRow(itemComponent).complete().getId();
    }

    public void removeBotMessage(JDA jda, String messageId) {
        TextChannel tc = jda.getGuildById(properties.getProperty("discord.thisIsMyHome"))
                .getTextChannelById(HOME_CHANNEL);
        tc.deleteMessageById(messageId).queue();
    }
}
