package services;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsFunService {
    DataService dataService;
    MessageService messageService;
    ResourceBundle resourceBundle;
    Map<String, String> wowList;

    public CsFunService(DataService dataService, MessageService messageService) {
        this.dataService = dataService;
        this.messageService = messageService;
        setupWowList();
    }

    public String handleWowEvent(UserContextInteractionEvent event, String locale) {
        User targetUser = event.getTarget();
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));
        String targetUserID = targetUser.getId();

        if (wowList.containsKey(targetUserID)) {
            String message = resourceBundle.getString("wow.highlightMessage").replace("%s", targetUser.getName()) + " " + wowList.get(targetUserID);
            return messageService.sendMessageInCorrectChannel(event, message, locale);
        } else if (targetUser.isBot()) {
            return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("error.cantwowabot"), locale);
        } else {
            return messageService.sendMessageInCorrectChannel(event, resourceBundle.getString("error.hasnowow"), locale);
        }
    }

    public EmbedBuilder handleSetTeamsEvent(SlashCommandInteractionEvent event, String locale) {

        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        List<VoiceChannel> allGuildVoiceChannels = event.getGuild().getVoiceChannels();
        List<Member> toShuffleList = new LinkedList<Member>();

        boolean isInVC = false;
        VoiceChannel vcToUse = null;

        for(VoiceChannel voiceChannel : allGuildVoiceChannels) {
            if(voiceChannel.getMembers().contains(event.getMember())) {
                isInVC = true;
                vcToUse = voiceChannel;
                break;
            }
        }

        if(isInVC) {
            if(vcToUse.getMembers().size() >= 2) {
                toShuffleList.addAll(vcToUse.getMembers());
                Collections.shuffle(toShuffleList);
                return messageService.sendEmbedMessageInCorrectChannel(event, buildEmbed(partitionTeams(toShuffleList, event.getOption("amountofteams"))) , locale);
            } else {
                return new EmbedBuilder().setTitle(resourceBundle.getString("error.noteamcreation"));
            }
        } else {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.notincorrectvc"));
        }
    }

    public String handleAddWowEvent(GenericCommandInteractionEvent event, String locale) {
        resourceBundle = ResourceBundle.getBundle("localization", new Locale(locale));

        String url = event.getOption("url").getAsString();
        String discordID = event.getUser().getId();

        Pattern ytPattern = Pattern.compile("(?:https\\:\\/\\/www\\.youtube\\.com\\/watch\\?v\\=)");
        Pattern dPattern = Pattern.compile("(?:https\\:\\/\\/cdn\\.discordapp\\.com\\/attachments)");

        Matcher ytMatcher = ytPattern.matcher(url);
        Matcher dMatcher = dPattern.matcher(url);

        try {
            if (ytMatcher.find() || dMatcher.find()) {
                if (wowList.containsKey(discordID)) {
                    dataService.updateWowEvent(discordID, url);
                } else {
                    dataService.addWowEvent(discordID, url);
                }
                wowList.put(discordID, url);
                return resourceBundle.getString("wow.done");
            } else {
                return resourceBundle.getString("error.invalidwow");
            }
        } catch (SQLException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] SQLException thrown: " + ex.getMessage());
            return resourceBundle.getString("error.majorerror");
        }
    }

    private EmbedBuilder buildEmbed(String teams[]) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(resourceBundle.getString("teams.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");
        for (int i = 0; i < teams.length; i++) {
            embedBuilder.addField(new MessageEmbed.Field("Team " + (i + 1), teams[i], true));
        }
        return embedBuilder;
    }

    private void setupWowList() {
        wowList = new HashMap<String, String>();
        try {
            wowList.putAll(dataService.getAllWowEntries());
        } catch (SQLException ex) {
            System.out.println("[CSBot - CsStatsService - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] SQLException thrown: " + ex.getMessage());
        }
    }

    private String[] partitionTeams(List<Member> voiceChatMember, OptionMapping amoutOfTeamsOption) {
        int amoutOfTeams = 2;

        if(amoutOfTeamsOption != null && amoutOfTeamsOption.getAsInt() >= 2) {
            amoutOfTeams = amoutOfTeamsOption.getAsInt();
        }

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
}