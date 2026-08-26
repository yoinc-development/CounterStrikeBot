package ch.yoinc.model.internal;

import com.google.gson.annotations.SerializedName;

public class InternalUser {

    @SerializedName("userID")
    public Integer userID;

    @SerializedName("username")
    public String username;

    @SerializedName("discordID")
    public String discordID;

    @SerializedName("steamID")
    public String steamID;

    public InternalUser() {
    }
}
