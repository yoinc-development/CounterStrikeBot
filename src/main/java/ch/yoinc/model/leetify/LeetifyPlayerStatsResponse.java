package ch.yoinc.model.leetify;

import com.google.gson.annotations.SerializedName;

public class LeetifyPlayerStatsResponse {

    @SerializedName("steam64_id")
    public String steam64_id;

    @SerializedName("name")
    public String name;

    @SerializedName("mvps")
    public Integer mvps;

    @SerializedName("preaim")
    public Double preaim;

    @SerializedName("reaction_time")
    public Double reaction_time;

    @SerializedName("accuracy")
    public Double accuracy;

    @SerializedName("accuracy_enemy_spotted")
    public Double accuracy_enemy_spotted;

    @SerializedName("accuracy_head")
    public Double accuracy_head;

    @SerializedName("shots_fired_enemy_spotted")
    public Integer shots_fired_enemy_spotted;

    @SerializedName("shots_fired")
    public Integer shots_fired;

    @SerializedName("shots_hit_enemy_spotted")
    public Integer shots_hit_enemy_spotted;

    @SerializedName("shots_hit_friend")
    public Integer shots_hit_friend;

    @SerializedName("shots_hit_friend_head")
    public Integer shots_hit_friend_head;

    @SerializedName("shots_hit_foe")
    public Integer shots_hit_foe;

    @SerializedName("shots_hit_foe_head")
    public Integer shots_hit_foe_head;

    @SerializedName("utility_on_death_avg")
    public Double utility_on_death_avg;

    @SerializedName("he_foes_damage_avg")
    public Double he_foes_damage_avg;

    @SerializedName("he_friends_damage_avg")
    public Double he_friends_damage_avg;

    @SerializedName("he_thrown")
    public Integer he_thrown;

    @SerializedName("molotov_thrown")
    public Integer molotov_thrown;

    @SerializedName("smoke_thrown")
    public Integer smoke_thrown;

    @SerializedName("counter_strafing_shots_all")
    public Integer counter_strafing_shots_all;

    @SerializedName("counter_strafing_shots_bad")
    public Integer counter_strafing_shots_bad;

    @SerializedName("counter_strafing_shots_good")
    public Integer counter_strafing_shots_good;

    @SerializedName("counter_strafing_shots_good_ratio")
    public Double counter_strafing_shots_good_ratio;

    @SerializedName("flashbang_hit_foe")
    public Integer flashbang_hit_foe;

    @SerializedName("flashbang_leading_to_kill")
    public Integer flashbang_leading_to_kill;

    @SerializedName("flashbang_hit_foe_avg_duration")
    public Double flashbang_hit_foe_avg_duration;

    @SerializedName("flashbang_hit_friend")
    public Integer flashbang_hit_friend;

    @SerializedName("flashbang_thrown")
    public Integer flashbang_thrown;

    @SerializedName("flash_assist")
    public Integer flash_assist;

    @SerializedName("score")
    public Integer score;

    @SerializedName("initial_team_number")
    public Integer initial_team_number;

    @SerializedName("spray_accuracy")
    public Double spray_accuracy;

    @SerializedName("total_kills")
    public Integer total_kills;

    @SerializedName("total_deaths")
    public Integer total_deaths;

    @SerializedName("kd_ratio")
    public Double kd_ratio;

    @SerializedName("rounds_survived")
    public Integer rounds_survived;

    @SerializedName("rounds_survived_percentage")
    public Double rounds_survived_percentage;

    @SerializedName("dpr")
    public Double dpr;

    @SerializedName("total_assists")
    public Integer total_assists;

    @SerializedName("total_damage")
    public Integer total_damage;

    @SerializedName("leetify_rating")
    public Double leetify_rating;

    @SerializedName("ct_leetify_rating")
    public Double ct_leetify_rating;

    @SerializedName("t_leetify_rating")
    public Double t_leetify_rating;

    @SerializedName("multi1k")
    public Integer multi1k;

    @SerializedName("multi2k")
    public Integer multi2k;

    @SerializedName("multi3k")
    public Integer multi3k;

    @SerializedName("multi4k")
    public Integer multi4k;

    @SerializedName("multi5k")
    public Integer multi5k;

    @SerializedName("rounds_count")
    public Integer rounds_count;

    @SerializedName("rounds_won")
    public Integer rounds_won;

    @SerializedName("rounds_lost")
    public Integer rounds_lost;

    @SerializedName("total_hs_kills")
    public Integer total_hs_kills;

    @SerializedName("trade_kill_opportunities")
    public Integer trade_kill_opportunities;

    @SerializedName("trade_kill_attempts")
    public Integer trade_kill_attempts;

    @SerializedName("trade_kills_succeed")
    public Integer trade_kills_succeed;

    @SerializedName("trade_kill_attempts_percentage")
    public Double trade_kill_attempts_percentage;

    @SerializedName("trade_kills_success_percentage")
    public Double trade_kills_success_percentage;

    @SerializedName("trade_kill_opportunities_per_round")
    public Double trade_kill_opportunities_per_round;

    @SerializedName("traded_death_opportunities")
    public Integer traded_death_opportunities;

    @SerializedName("traded_death_attempts")
    public Integer traded_death_attempts;

    @SerializedName("traded_deaths_succeed")
    public Integer traded_deaths_succeed;

    @SerializedName("traded_death_attempts_percentage")
    public Double traded_death_attempts_percentage;

    @SerializedName("traded_deaths_success_percentage")
    public Double traded_deaths_success_percentage;

    @SerializedName("traded_deaths_opportunities_per_round")
    public Double traded_deaths_opportunities_per_round;

    public LeetifyPlayerStatsResponse() {
    }
}
