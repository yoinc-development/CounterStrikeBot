package services;

import http.ConnectionBuilder;
import model.omdb.OMDBMovieResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class GregflixService {

    private MessageService messageService;
    ConnectionBuilder connectionBuilder;
    DataService dataService;
    ResourceBundle resourceBundle;

    public GregflixService(Properties properties, MessageService messageService, DataService dataService) {
        this.messageService = messageService;
        this.dataService = dataService;
        connectionBuilder = new ConnectionBuilder(properties);
    }

    public String handleButtonEvent(ButtonInteractionEvent buttonInteractionEvent, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String buttonId = buttonInteractionEvent.getButton().getId();

        if ("falseItem".equals(buttonId)) {
            buttonInteractionEvent.getMessage().delete().queue();
            return resourceBundle.getString("gregflix.cancel");
        } else {
            buttonInteractionEvent.getMessage().delete().queue();
            try {
                dataService.addGregflixEntry(buttonId.split("--")[1], buttonId.split("--")[2]);
                messageService.contactGreg(buttonInteractionEvent.getButton().getId(), dataService.getDiscordIdForUsername("jay_th"), buttonInteractionEvent.getJDA());
                return resourceBundle.getString("gregflix.confirm");
            } catch (SQLException ex) {
                return resourceBundle.getString("error.majorerror");
            }
        }
    }

    public void handleGregflixEvent(MessageReceivedEvent event, String locale, PrivateChannel privateChannel) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        try {
            if(dataService.hasGregflix(privateChannel.getUser().getName(), privateChannel.getUser().getId())) {
                OMDBMovieResponse omdbMovieResponse = connectionBuilder.fetchMovieDetails(event.getMessage().getContentDisplay());
                EmbedBuilder embedBuilder = new EmbedBuilder();

                if ("True".equals(omdbMovieResponse.getResponse())) {
                    if (!dataService.doesGregflixEntryExist(omdbMovieResponse)) {
                        embedBuilder.setTitle(omdbMovieResponse.getTitle());
                        embedBuilder.setDescription(resourceBundle.getString("gregflix.description"));
                        embedBuilder.addField(new MessageEmbed.Field("Type", omdbMovieResponse.getType(), true));
                        if ("series".equals(omdbMovieResponse.getType())) {
                            embedBuilder.addField(new MessageEmbed.Field("Total Seasons", Integer.toString(omdbMovieResponse.getTotalSeasons()), false));
                        } else {
                            embedBuilder.addField(new MessageEmbed.Field("Runtime", omdbMovieResponse.getRuntime(), false));
                        }
                        embedBuilder.addField(new MessageEmbed.Field("Genre", omdbMovieResponse.getGenre(), false));
                        embedBuilder.addField(new MessageEmbed.Field("IMDB ID", omdbMovieResponse.getImdbID(), false));
                        embedBuilder.setImage(omdbMovieResponse.getPoster());
                        messageService.sendGregflixEmbedMessage(privateChannel, embedBuilder, locale, false, omdbMovieResponse.getTitle(), omdbMovieResponse.getImdbID());
                    } else {
                        messageService.sendGregflixEmbedMessage(privateChannel, new EmbedBuilder().setTitle(resourceBundle.getString("info.movieexists")).addField("IMDB ID", omdbMovieResponse.getImdbID(), false), locale, true, null, null);
                    }
                } else {
                    embedBuilder.setTitle(resourceBundle.getString("info.nomoviefound"));
                }
            }
        } catch (IOException ex) {
            messageService.sendGregflixEmbedMessage(privateChannel, new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror")), locale, true, null, null);
        } catch (InterruptedException ex) {
            messageService.sendGregflixEmbedMessage(privateChannel, new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror")), locale, true, null, null);
        } catch (SQLException ex) {
            messageService.sendGregflixEmbedMessage(privateChannel, new EmbedBuilder().setTitle(resourceBundle.getString("error.majorerror")), locale, true, null, null);
        }
    }

    public void handleGregflixReactionEvent(MessageReactionAddEvent messageReactionAddEvent, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        try {
            if (dataService.isGreg(messageReactionAddEvent.getUser().getId())) {
                messageReactionAddEvent.getChannel().retrieveMessageById(messageReactionAddEvent.getMessageId()).queue((message -> {
                    try {
                        String[] splitMessage = message.getContentDisplay().split("--");
                        dataService.updateGregflixEntry(splitMessage[2]);
                        messageService.sendPrivateMessageToUser(messageReactionAddEvent.getJDA(), resourceBundle.getString("gregflix.requestedDone").replace("%s", splitMessage[1]), dataService.getDiscordIdForUsername(splitMessage[0]));
                    } catch (SQLException ex) {
                        messageReactionAddEvent.getChannel().sendMessage("error in sending private message").queue();
                    }
                }));
            }
        } catch (SQLException ex) {
            messageReactionAddEvent.getChannel().sendMessage("error").queue();
        }
    }
}