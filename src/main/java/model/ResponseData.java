package model;

import com.google.gson.annotations.SerializedName;

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

    public String returnBasicInfo(ResourceBundle resourceBundle) {
        return new StringBuilder().append(resourceBundle.getString("stats.basicInfo").replace("%s", getSteamUserInfo().getPlayers().get(0).getPersonaname())).append("\n")
                .append(resourceBundle.getString("stats.kills") + "_").append(getLongStatsForName("total_kills")).append("_\n")
                .append(resourceBundle.getString("stats.deaths") + "_").append(getLongStatsForName("total_deaths")).append("_\n")
                .append(resourceBundle.getString("stats.planted") + "_").append(getLongStatsForName("total_planted_bombs")).append("_\n")
                .append(resourceBundle.getString("stats.defused") + "_").append(getLongStatsForName("total_defused_bombs")).append("_\n")
                .append(resourceBundle.getString("stats.wins") + "_").append(getLongStatsForName("total_wins")).append("_\n")
                .append(resourceBundle.getString("stats.damage") + "_").append(getLongStatsForName("total_damage_done")).append("_").toString();
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
