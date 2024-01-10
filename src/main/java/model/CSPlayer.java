package model;

public class CSPlayer {

    private String nickname;
    private PlayerStats playerstats;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public PlayerStats getPlayerstats() {
        return playerstats;
    }

    public void setPlayerstats(PlayerStats playerstats) {
        this.playerstats = playerstats;
    }

    public String returnBasicInfo() {

        long total_kills = returnStatsForName("total_kills");

        StringBuilder returnValue = new StringBuilder();
        returnValue.append("_Stats für " + getNickname() + ":_\n");
        returnValue.append("Insgesamte Kills: " +  total_kills);

        return returnValue.toString();
    }

    private long returnStatsForName(String name) {
        for(Stats stat : getPlayerstats().getStats()) {
            if(stat.getName().equals(name)) {
                return stat.getValue();
            }
        }
        return 0l;
    }
}
