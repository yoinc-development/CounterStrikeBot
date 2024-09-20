package model.bot;

public class User {
    private int user_id;
    private String username;
    private String steamID;
    private String faceitID;
    private String discordID;
    private boolean hasGregflix;

    public User(int user_id, String username, String steamID, String faceitID, String discordID, boolean hasGregflix) {
        this.user_id = user_id;
        this.username = username;
        this.steamID = steamID;
        this.faceitID = faceitID;
        this.discordID = discordID;
        this.hasGregflix = hasGregflix;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSteamID() {
        return steamID;
    }

    public void setSteamID(String steamID) {
        this.steamID = steamID;
    }

    public String getFaceitID() {
        return faceitID;
    }

    public void setFaceitID(String faceitID) {
        this.faceitID = faceitID;
    }

    public String getDiscordID() {
        return discordID;
    }

    public void setDiscordID(String discordID) {
        this.discordID = discordID;
    }

    public boolean isHasGregflix() {
        return hasGregflix;
    }

    public void setHasGregflix(boolean hasGregflix) {
        this.hasGregflix = hasGregflix;
    }
}
