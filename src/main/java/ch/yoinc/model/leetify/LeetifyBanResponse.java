package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class LeetifyBanResponse {

    @SerializedName("platform")
    public String platform;

    @SerializedName("platform_nickname")
    public String platform_nickname;

    @SerializedName("banned_since")
    public Instant banned_since;

    public LeetifyBanResponse() {
    }

}
