package model.faceit;

import java.util.List;

public class FaceitMatch {

    private String competition_id;
    private String competition_name;
    private String competition_type;
    private String faceit_url;
    private int finished_at;
    private String game_id;
    private String game_mode;
    private String match_id;
    private String match_type;
    private int max_players;
    private String organizer_id;
    private String region;
    private int started_at;
    private String status;
    private int teams_size;
    private List<String> playing_players;
    private FaceitMatchResult results;
    private FaceitMatchTeams teams;

    public String getCompetition_id() {
        return competition_id;
    }

    public void setCompetition_id(String competition_id) {
        this.competition_id = competition_id;
    }

    public String getCompetition_name() {
        return competition_name;
    }

    public void setCompetition_name(String competition_name) {
        this.competition_name = competition_name;
    }

    public String getCompetition_type() {
        return competition_type;
    }

    public void setCompetition_type(String competition_type) {
        this.competition_type = competition_type;
    }

    public String getFaceit_url() {
        return faceit_url;
    }

    public void setFaceit_url(String faceit_url) {
        this.faceit_url = faceit_url;
    }

    public int getFinished_at() {
        return finished_at;
    }

    public void setFinished_at(int finished_at) {
        this.finished_at = finished_at;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public String getGame_mode() {
        return game_mode;
    }

    public void setGame_mode(String game_mode) {
        this.game_mode = game_mode;
    }

    public String getMatch_id() {
        return match_id;
    }

    public void setMatch_id(String match_id) {
        this.match_id = match_id;
    }

    public String getMatch_type() {
        return match_type;
    }

    public void setMatch_type(String match_type) {
        this.match_type = match_type;
    }

    public int getMax_players() {
        return max_players;
    }

    public void setMax_players(int max_players) {
        this.max_players = max_players;
    }

    public String getOrganizer_id() {
        return organizer_id;
    }

    public void setOrganizer_id(String organizer_id) {
        this.organizer_id = organizer_id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getStarted_at() {
        return started_at;
    }

    public void setStarted_at(int started_at) {
        this.started_at = started_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTeams_size() {
        return teams_size;
    }

    public void setTeams_size(int teams_size) {
        this.teams_size = teams_size;
    }

    public List<String> getPlaying_players() {
        return playing_players;
    }

    public void setPlaying_players(List<String> playing_players) {
        this.playing_players = playing_players;
    }

    public FaceitMatchResult getResults() {
        return results;
    }

    public void setResults(FaceitMatchResult results) {
        this.results = results;
    }

    public FaceitMatchTeams getTeams() {
        return teams;
    }

    public void setTeams(FaceitMatchTeams teams) {
        this.teams = teams;
    }
}
