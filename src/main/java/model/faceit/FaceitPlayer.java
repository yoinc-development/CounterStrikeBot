package model.faceit;

public class FaceitPlayer {
    private String avatar;
    private String faceit_url;
    private String game_player_id;
    private String game_player_name;
    private String nickname;
    private String player_id;
    private int skill_level;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFaceit_url() {
        return faceit_url;
    }

    public void setFaceit_url(String faceit_url) {
        this.faceit_url = faceit_url;
    }

    public String getGame_player_id() {
        return game_player_id;
    }

    public void setGame_player_id(String game_player_id) {
        this.game_player_id = game_player_id;
    }

    public String getGame_player_name() {
        return game_player_name;
    }

    public void setGame_player_name(String game_player_name) {
        this.game_player_name = game_player_name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public int getSkill_level() {
        return skill_level;
    }

    public void setSkill_level(int skill_level) {
        this.skill_level = skill_level;
    }
}
