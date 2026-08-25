package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyRecentTeammatesResponse {

    @SerializedName("steam64_id")
    private String steam64_id;

    @SerializedName("recent_matches_count")
    private Integer recent_matches_count;

    public LeetifyRecentTeammatesResponse() {
    }
}
