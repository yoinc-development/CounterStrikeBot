package ch.yoinc.services;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.*;

public class CsFunService {
    MessageService messageService;
    ResourceBundle resourceBundle;

    public CsFunService(MessageService messageService) {
        this.messageService = messageService;
    }

    public EmbedBuilder handleSetTeamsEvent(SlashCommandInteractionEvent event, String locale) {

        resourceBundle = ResourceBundle.getBundle("localization", Locale.of(locale));

        List<VoiceChannel> allGuildVoiceChannels = Objects.requireNonNull(event.getGuild()).getVoiceChannels();

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
                List<Member> toShuffleList = new LinkedList<>(vcToUse.getMembers());
                Collections.shuffle(toShuffleList);
                return messageService.sendEmbedMessageInCorrectChannel(event, buildEmbed(partitionTeams(toShuffleList, event.getOption("amountofteams"))) , locale);
            } else {
                return new EmbedBuilder().setTitle(resourceBundle.getString("error.noteamcreation"));
            }
        } else {
            return new EmbedBuilder().setTitle(resourceBundle.getString("error.notincorrectvc"));
        }
    }

    private EmbedBuilder buildEmbed(String[] teams) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(resourceBundle.getString("teams.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");
        for (int i = 0; i < teams.length; i++) {
            embedBuilder.addField(new MessageEmbed.Field("Team " + (i + 1), teams[i], true));
        }
        return embedBuilder;
    }

    private String[] partitionTeams(List<Member> voiceChatMember, OptionMapping amountOfTeamsOption) {
        int amountOfTeams = 2;

        if(amountOfTeamsOption != null && amountOfTeamsOption.getAsInt() >= 2) {
            amountOfTeams = amountOfTeamsOption.getAsInt();
        }

        String[] result = new String[amountOfTeams];

        int teamSize = Math.round((float) voiceChatMember.size() / amountOfTeams);
        List<List<Member>> partitionedList = Lists.partition(voiceChatMember, teamSize);
        for (int i = 0; i < partitionedList.size(); i++) {
            result[i] = returnStringOfMembers(partitionedList.get(i));
        }
        return result;
    }

    private String returnStringOfMembers(List<Member> partitionedVoiceChatMembers) {
        StringBuilder builder = new StringBuilder();

        for (Member member : partitionedVoiceChatMembers) {
            builder.append(member.getUser().getName()).append("\n");
        }
        return builder.toString();
    }
}