package retakeServer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ResourceBundle;

public class RankStats {

    private final String name;
    private final int experience;
    private final int rank;
    private final int kills;
    private final int deaths;
    private final int assists;
    private final int shoots;
    private final int hits;
    private final int headshots;
    private final int roundWin;
    private final int roundLose;
    private final long playtime;
    private final long lastConnect;
    public RankStats(String name,
                     int experience,
                     int rank,
                     int kills,
                     int deaths,
                     int shoots,
                     int hits,
                     int headshots,
                     int assists,
                     int roundWin,
                     int roundLose,
                     long playtime,
                     long lastConnect) {
        this.name = name;
        this.experience = experience;
        this.rank = rank;
        this.kills = kills;
        this.deaths = deaths;
        this.shoots = shoots;
        this.hits = hits;
        this.headshots = headshots;
        this.assists = assists;
        this.roundWin = roundWin;
        this.roundLose = roundLose;
        this.playtime = playtime;
        this.lastConnect = lastConnect;
    }

    public static EmbedBuilder getRankStatsMessage(ResourceBundle resourceBundle, RankStats rankStats) {
        return new EmbedBuilder()
                .setTitle(resourceBundle.getString("stats.retakeTitle").replace("%s", rankStats.getName()))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.experience"), String.valueOf(rankStats.getExperience()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.rank"), String.valueOf(Rank.values()[rankStats.getRank()].rankDesc), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kills"), String.valueOf(rankStats.getKills()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.deaths"), String.valueOf(rankStats.getDeaths()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.assists"), String.valueOf(rankStats.getAssists()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.headshot"), String.valueOf(rankStats.getHeadshots()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.headshotPer"), String.valueOf(rankStats.getHeadshotPer()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kd"), String.valueOf(rankStats.getKd()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.roundsWon"), String.valueOf(rankStats.getRoundWin()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.roundsLost"), String.valueOf(rankStats.getRoundLose()), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.playtime"), String.valueOf(rankStats.getFormattedPlaytime()), true));
    }

    public String getName() {
        return name;
    }

    public int getExperience() {
        return experience;
    }

    public int getRank() {
        return rank;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public int getShoots() {
        return shoots;
    }

    public int getHits() {
        return hits;
    }

    public int getHeadshots() {
        return headshots;
    }

    public int getRoundWin() {
        return roundWin;
    }

    public int getRoundLose() {
        return roundLose;
    }

    public long getPlaytime() {
        return playtime;
    }

    public long getLastConnect() {
        return lastConnect;
    }

    public String getFormattedPlaytime() {
        long hours = playtime / 3600;
        long minutes = (playtime % 3600) / 60;
        long seconds = playtime % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private int getHeadshotPer() {
        if (headshots != 0) {
            return (int) Math.ceil(((double) headshots / (double) kills) * 100);
        } else {
            return 0;
        }
    }

    private double getKd() {
        if (deaths != 0) {
            return Math.round(((float) kills / deaths) * 10.0) / 10.0;
        } else {
            return 0d;
        }
    }

    enum Rank {
        SILVER_I("Silver I"),
        SILVER_II("Silver II"),
        SILVER_III("Silver III"),
        SILVER_IV("Silver IV"),
        SILVER_ELITE("Silver Elite"),
        SILVER_ELITE_MASTER("Silver Elite Master"),
        GOLD_NOVA_I("Gold Nova I"),
        GOLD_NOVA_II("Gold Nova II"),
        GOLD_NOVA_III("Gold Nova III"),
        GOLD_NOVA_MASTER("Gold Nova Master"),
        MASTER_GUARDIAN_I("Master Guardian I"),
        MASTER_GUARDIAN_II("Master Guardian II"),
        MASTER_GUARDIAN_ELITE("Master Guardian Elite"),
        DMG("DMG"),
        LEGENDARY_EAGLE("Legendary Eagle"),
        LEGENDARY_EAGLE_MASTER("Legendary Eagle Master"),
        SUPREME("Supreme"),
        THE_GLOBAL_ELITE("The Global Elite");

        private final String rankDesc;

        Rank(String rankDesc) {
            this.rankDesc = rankDesc;
        }
    }
}
