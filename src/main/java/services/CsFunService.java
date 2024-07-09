package services;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsFunService {

    private Properties properties;
    private DataService dataService;
    ResourceBundle resourceBundle;

    String dedicatedChannel;
    Map<String, String> wowList;

    public CsFunService(Properties properties) {
        dedicatedChannel = properties.getProperty("discord.dedicatedChannel");
        this.properties = properties;
        setupWowList();
    }

    public String handleWowEvent(UserContextInteractionEvent event, String locale) {

        User targetUser = event.getTarget();
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String targetUserName = targetUser.getName();

        if (wowList.containsKey(targetUserName)) {
            String message = resourceBundle.getString("wow.highlightMessage").replace("%s", targetUserName) + " " + wowList.get(targetUserName);
            return sendMessageInCorrectChannel(event, message);
        } else if (targetUser.isBot()) {
            return sendMessageInCorrectChannel(event, resourceBundle.getString("error.cantwowabot"));
        } else {
            return sendMessageInCorrectChannel(event, resourceBundle.getString("error.hasnowow"));
        }
    }

    public EmbedBuilder handleSetTeamsEvent(SlashCommandInteractionEvent event, String locale) {
        VoiceChannel channel = event.getGuild().getVoiceChannelById(properties.getProperty("discord.dedicatedVoiceChannel"));
        List<Member> toShuffleList = new LinkedList<Member>();

        if(channel.getMembers().contains(event.getMember()) && channel.getMembers().size() >= 2) {
            toShuffleList.addAll(channel.getMembers());
            Collections.shuffle(toShuffleList);
            return sendEmbedMessageInCorrectChannel(event, toShuffleList, locale);
        }
        return sendEmbedMessageInCorrectChannel(event, null, locale);
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

    private EmbedBuilder sendEmbedMessageInCorrectChannel(GenericCommandInteractionEvent event, List<Member> voiceChatMembers, String locale) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        String[] teams;

        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        embedBuilder.setTitle(resourceBundle.getString("teams.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");

        if(CollectionUtils.isEmpty(voiceChatMembers)) {
            embedBuilder.setTitle(resourceBundle.getString("error.noteamcreation"));
        } else {
            if(event.getOption("amountofteams") == null) {
                teams = partitionTeams(voiceChatMembers, 2);
                for (int i = 0; i < teams.length; i++) {
                    embedBuilder.addField(new MessageEmbed.Field("Team " + (i + 1), teams[i], true));
                }
            } else if(event.getOption("amountofteams").getAsInt() == 0 || event.getOption("amountofteams").getAsInt() == 1) {
                embedBuilder.setTitle(resourceBundle.getString("error.noteamcreation"));
            } else {
                teams = partitionTeams(voiceChatMembers, event.getOption("amountofteams").getAsInt());
                for (int i = 0; i < teams.length; i++) {
                    embedBuilder.addField(new MessageEmbed.Field("Team " + (i + 1), teams[i], true));
                }
            }
        }
        return embedBuilder;
    }

    private String[] partitionTeams(List<Member> voiceChatMember, int amoutOfTeams) {
        String[] result = new String[amoutOfTeams];

        int teamSize = Math.round(voiceChatMember.size() / amoutOfTeams);
        List<List<Member>> partitionedList = Lists.partition(voiceChatMember, teamSize);

            for (int i = 0; i < partitionedList.size(); i++) {
                result[i] = returnStringOfMembers(partitionedList.get(i));
            }
        return result;
    }

    private String returnStringOfMembers(List<Member> partitionedVoiceChatMembers) {

        StringBuilder builder = new StringBuilder();

        for (Member member : partitionedVoiceChatMembers) {
            builder.append(member.getUser().getName() + "\n");
        }

        return builder.toString();
    }

    private String sendMessageInCorrectChannel(GenericCommandInteractionEvent event, String message) {
        if (event.getMessageChannel().getId().equals(dedicatedChannel)) {
            return message;
        } else {
            TextChannel dedicatedTextChannel = event.getHook().getInteraction().getGuild().getTextChannelById(dedicatedChannel);

            //this means no dedicated channel was found for this ID. either no dedicated channel was set or it doesn't exist on this server.
            //either way, this means that the event is going to be returned in the current active channel. that's a bit messy but hey,
            //if that's what they want..?
            if (dedicatedTextChannel == null) {
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
                if (wowList.containsKey(user)) {
                    dataService.updateWowEvent(user, url);
                } else {
                    dataService.addWowEvent(user, url);
                }
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