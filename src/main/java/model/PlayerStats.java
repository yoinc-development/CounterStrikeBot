package model;

import java.util.List;

public class PlayerStats {
    private long steamID;
    private List<SingleStat> stats;

    public long getSteamID() {
        return steamID;
    }

    public void setSteamID(long steamID) {
        this.steamID = steamID;
    }

    public List<SingleStat> getStats() {
        return stats;
    }

    public void setStats(List<SingleStat> stats) {
        this.stats = stats;
    }
}
