package ch.yoinc.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ch.yoinc.services.*;
import ch.yoinc.tasks.*;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class CounterStrikeBotListener extends ListenerAdapter {

    private final CsFunService csFunService;
    private final CsStatsService csStatsService;
    private final DataService dataService;
    private final DiscordService discordService;
    private final TaskScheduler taskScheduler;

    public CounterStrikeBotListener(Properties properties) {
        dataService = new DataService(properties);
        csStatsService = new CsStatsService(properties, dataService);
        csFunService = new CsFunService(new MessageService(properties));
        discordService = new DiscordService();
        taskScheduler = new TaskScheduler(properties);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            if (event.getGuild() != null && event.getGuild().getMembers().contains(event.getMember())) {
                if ("stats".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csStatsService.handleStatsEvent(event).build()).queue();
                }

                if ("compare".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csStatsService.handleCompareEvent(event).build()).queue();
                }

                if ("teams".equals(event.getName())) {
                    event.deferReply().queue();
                    event.getHook().sendMessageEmbeds(csFunService.handleSetTeamsEvent(event).build()).queue();
                }
            }
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {

    }

    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();
        dataService.setBotID(jda.getSelfUser().getId());
        CompletableFuture.runAsync(() -> taskScheduler.startAllTasks(jda));
    }
}
