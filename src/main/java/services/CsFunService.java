package services;

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

    private EmbedBuilder buildEmbed(String teams[]) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(resourceBundle.getString("teams.title"))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch");
        for (int i = 0; i < teams.length; i++) {
            embedBuilder.addField(new MessageEmbed.Field("Team " + (i + 1), teams[i], true));
        }
        return embedBuilder;
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