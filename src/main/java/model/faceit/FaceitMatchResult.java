package model.faceit;

public class FaceitMatchResult {
    private String winner;
    private FaceitMatchResultScore score;

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public FaceitMatchResultScore getScore() {
        return score;
    }

    public void setScore(FaceitMatchResultScore score) {
        this.score = score;
    }
}
