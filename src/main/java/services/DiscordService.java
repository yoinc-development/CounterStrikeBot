package services;

import model.bot.GregflixEntry;
import model.bot.User;
import model.retake.RetakePlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import retakeServer.RetakeWatchdog;
import retakeServer.ServerStatus;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DiscordService {

    DataService dataService;
    Properties properties;
    RetakeService retakeService;
    JDA jda;
    ResourceBundle resourceBundle;
    MessageService messageService;

    public DiscordService(Properties properties, DataService dataService, RetakeService retakeService, MessageService messageService) {
        this.properties = properties;
        this.dataService = dataService;
        this.retakeService = retakeService;
        this.messageService = messageService;
        this.resourceBundle = ResourceBundle.getBundle("localization", new Locale("en"));
    }

    public void scheduleAllTasks(JDA jda) {
        this.jda = jda;

        TimerTask statsTask = new TimerTask() {
            @Override
            public void run() {
                runStatsTask();
            }
        };

        long weeklyReportdelay = getWeeklyReportDelay();

        //on restarts all tasks will be scheduled to start at the next full hour
        long taskDelay = getTaskDelay();

        Timer timer = new Timer("Discord Service Tasks");
        timer.schedule(collectionTask, taskDelay, 86400000L);
        timer.schedule(joinTask, taskDelay, 300000L);
        timer.schedule(weekInReviewTask, weeklyReportdelay, (7 * 24 * 60 * 60 * 1000L));
    }

    public String getUserLocale(Event event) {
        String locale = "en";
        if(event instanceof GenericCommandInteractionEvent) {
            if (((GenericCommandInteractionEvent)event).getInteraction().getUserLocale().getLocale().equals("de")) {
                locale = "de";
            }
        } else if(event instanceof ButtonInteractionEvent) {
            if(((ButtonInteractionEvent) event).getInteraction().getUserLocale().getLocale().equals("de")) {
                locale = "de";
            }
        }
        return locale;
    }

    private TimerTask collectionTask = new TimerTask() {
        @Override
        public void run() {
            System.out.println("[CSBot - DiscordService] Collection Task started at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
            runCollectionTask();
            System.out.println("[CSBot - DiscordService] Collection Task finished at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
        }
    };

    private TimerTask joinTask = new TimerTask() {
        @Override
        public void run() {
            runJoinTask();
        }
    };

    private TimerTask weekInReviewTask = new TimerTask() {
        @Override
        public void run() {
            runWeeklyInReviewTask();
        }
    };

    private static long getTaskDelay() {
        Calendar now = Calendar.getInstance();
        Calendar todayNextHour = Calendar.getInstance();

        todayNextHour.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1);
        todayNextHour.set(Calendar.MINUTE, 0);
        todayNextHour.set(Calendar.SECOND, 0);
        todayNextHour.set(Calendar.MILLISECOND, 0);

        return todayNextHour.getTimeInMillis() - now.getTimeInMillis();
    }
    private static long getWeeklyReportDelay() {
        Calendar now = Calendar.getInstance();
        Calendar nextFriday1pm = Calendar.getInstance();
        nextFriday1pm.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        nextFriday1pm.set(Calendar.HOUR_OF_DAY, 13);
        nextFriday1pm.set(Calendar.MINUTE, 0);
        nextFriday1pm.set(Calendar.SECOND, 0);
        nextFriday1pm.set(Calendar.MILLISECOND, 0);

        if (now.after(nextFriday1pm)) {
            nextFriday1pm.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return nextFriday1pm.getTimeInMillis() - now.getTimeInMillis();
    }

    private void runCollectionTask() {
        for (Guild guild : jda.getGuilds()) {
            for (Member member : guild.getMembers()) {
                dataService.addUserToDatabase(member.getUser().getName(), member.getId());
            }
        }
    }

    private void runWeeklyInReviewTask() {
        try {
            List<User> userList = dataService.getAllGregflixUsers();
            List<GregflixEntry> gregflixEntryList = dataService.getGregflixEntriesForThisWeek(new Date(new java.util.Date().getTime() - (7 * (1000 * 60 * 60 * 24))), new Date(new java.util.Date().getTime()));

            StringBuilder weeklyReportMessage = new StringBuilder();
            weeklyReportMessage.append(resourceBundle.getString("weeklyReport.introduction"));
            String movieList = resourceBundle.getString("weeklyReport.movieList");
            String seriesList = resourceBundle.getString("weeklyReport.seriesList");

            if(gregflixEntryList != null && !gregflixEntryList.isEmpty() && userList != null && !userList.isEmpty()) {
                for (GregflixEntry gregflixEntry : gregflixEntryList) {
                    if ("series".equals(gregflixEntry.getShowType())) {
                        seriesList = seriesList + "- " + gregflixEntry.getTitle() + "\n";
                    } else {
                        movieList = movieList + "- " + gregflixEntry.getTitle() + "\n";
                    }
                }
                weeklyReportMessage.append(seriesList).append("\n").append(movieList).append("\n").append(resourceBundle.getString("weeklyReport.signature"));

                for (User user : userList) {
                    jda.getUserById(user.getDiscordID()).openPrivateChannel().queue((privateChannel -> {
                        privateChannel.sendMessage(weeklyReportMessage.toString()).queue();
                    }));
                }
            }
        } catch (SQLException ex) {
            System.out.println("[CSBot - DiscordService] SQLException thrown: " + ex.getMessage());
        }
    }

    private void runStatsTask() {

    }

    private void runJoinTask() {
        try {
            ServerStatus serverStatus = retakeService.getServerStatus();
            if (serverStatus != null && !serverStatus.getPlayerNames().isEmpty()) {
                if (!dataService.hasSentRetakeInvite()) {
                    EmbedBuilder embedBuilder = RetakeWatchdog.getJoinMessage(resourceBundle, getRandomPlayer(serverStatus), serverStatus.getCurrentMap());
                    ItemComponent button = Button.link(properties.getProperty("server.connectLink"), resourceBundle.getString("serverwatchdog.invite"));
                    String messageId = messageService.sendBotEmbedMessageWithAction(jda, embedBuilder, button);
                    dataService.addRetakeInvite(messageId, new Timestamp(System.currentTimeMillis()).toString());
                }
            } else {
                if (dataService.hasSentRetakeInvite()) {
                    String messageId = dataService.getRetakeInviteMsgId();
                    messageService.removeBotMessage(jda, messageId);
                    dataService.removeRetakeInvite();
                }
            }
        } catch (SQLException ex) {
            System.out.println("[CSBot - DiscordService]SQLException thrown: " + ex.getMessage());
        }
    }

    private void runRetakeWinnerTask() {
        try {
            RetakePlayer retakePlayer = dataService.getHighestRetakeScoreAndPlayer();
            messageService.sendAssistantMessageRetake(retakePlayer, jda);
        } catch (SQLException ex) {
            System.out.println("[CSBot - DiscordService] SQLException thrown: " + ex.getMessage());
        }
    }

    private String getRandomPlayer(ServerStatus serverStatus) {
        List<String> playerNames = serverStatus.getPlayerNames();
        Random rand = new Random();
        int randIndex = rand.nextInt(playerNames.size());
        return playerNames.get(randIndex);
    }
}
