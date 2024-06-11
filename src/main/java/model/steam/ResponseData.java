package model.steam;

import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ResourceBundle;

public class ResponseData {

    @SerializedName("response")
    private SteamUserInfo steamUserInfo;
    private PlayerStats playerstats;

    public PlayerStats getPlayerstats() {
        return playerstats;
    }

    public void setPlayerstats(PlayerStats playerstats) {
        this.playerstats = playerstats;
    }

    public SteamUserInfo getSteamUserInfo() {
        return steamUserInfo;
    }

    public void setSteamUserInfo(SteamUserInfo steamUserInfo) {
        this.steamUserInfo = steamUserInfo;
    }

    public EmbedBuilder returnBasicInfo(ResourceBundle resourceBundle) {

        return new EmbedBuilder()
                .setTitle(resourceBundle.getString("stats.title").replace("%s", getSteamUserInfo().getPlayers().get(0).getPersonaname()))
                .setAuthor(resourceBundle.getString("stats.author"), "https://www.yoinc.ch")
                .setImage(getSteamUserInfo().getPlayers().get(0).getAvatarmedium())
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.kills"), String.valueOf(getLongStatsForName("total_kills")), true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.deaths"),String.valueOf(getLongStatsForName("total_deaths")),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.wins"),String.valueOf(getLongStatsForName("total_wins")),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.planted"),String.valueOf(getLongStatsForName("total_planted_bombs")),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.defused"),String.valueOf(getLongStatsForName("total_defused_bombs")),true))
                .addField(new MessageEmbed.Field(resourceBundle.getString("stats.damage"),String.valueOf(getLongStatsForName("total_damage_done")),true));
    }

    public long getLongStatsForName(String name) {
        for(SingleStat stat : getPlayerstats().getStats()) {
            if(stat.getName().equals(name)) {
                return stat.getValue();
            }
        }
        return 0l;
    }
}
