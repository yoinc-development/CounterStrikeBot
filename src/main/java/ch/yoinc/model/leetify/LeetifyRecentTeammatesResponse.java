package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyRecentTeammatesResponse {

    @SerializedName("steam64_id")
    public String steam64_id;

    @SerializedName("recent_matches_count")
    public Integer recent_matches_count;

    public LeetifyRecentTeammatesResponse() {
    }
}
