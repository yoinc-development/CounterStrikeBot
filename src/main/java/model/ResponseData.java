package model;

import com.google.gson.annotations.SerializedName;

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

    public String returnBasicInfo() {
        return new StringBuilder().append("__Stats für ").append(getSteamUserInfo().getPlayers().get(0).getPersonaname()).append(":__\n")
                .append("Kills: _").append(returnLongStatsForName("total_kills")).append("_\n")
                .append("Deaths: _").append(returnLongStatsForName("total_deaths")).append("_\n")
                .append("Bomben geplant: _").append(returnLongStatsForName("total_planted_bombs")).append("_\n")
                .append("Bomben defused: _").append(returnLongStatsForName("total_defused_bombs")).append("_\n")
                .append("Totale Wins: _").append(returnLongStatsForName("total_wins")).append("_\n")
                .append("Gesamter Schaden: _").append(returnLongStatsForName("total_damage_done")).append("_").toString();
    }

    private long returnLongStatsForName(String name) {
        for(SingleStat stat : getPlayerstats().getStats()) {
            if(stat.getName().equals(name)) {
                return stat.getValue();
            }
        }
        return 0l;
    }
}
