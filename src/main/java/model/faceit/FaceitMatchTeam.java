package model.faceit;

import java.util.List;

public class FaceitMatchTeam {
    private String avatar;
    private String nickname;
    private String team_id;
    private String type;
    private List<FaceitPlayer> players;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTeam_id() {
        return team_id;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FaceitPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<FaceitPlayer> players) {
        this.players = players;
    }
}
