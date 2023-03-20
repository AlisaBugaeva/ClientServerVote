import java.util.HashMap;

public class Vote {
    private User creator;
    private String voteName;
    private String voteTheme;
    private int answersNumber;
    private HashMap<String,Integer> options;

    public Vote(User creator, String voteName, String voteTheme, int answersNumber, HashMap<String, Integer> options) {
        this.creator = creator;
        this.voteName = voteName;
        this.voteTheme = voteTheme;
        this.answersNumber = answersNumber;
        this.options = options;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getVoteName() {
        return voteName;
    }

    public void setVoteName(String voteName) {
        this.voteName = voteName;
    }

    public String getVoteTheme() {
        return voteTheme;
    }

    public void setVoteTheme(String voteTheme) {
        this.voteTheme = voteTheme;
    }

    public int getAnswersNumber() {
        return answersNumber;
    }

    public void setAnswersNumber(int answersNumber) {
        this.answersNumber = answersNumber;
    }

    public HashMap<String, Integer> getOptions() {
        return options;
    }

    public void setOptions(HashMap<String, Integer> options) {
        this.options = options;
    }
}
