import java.util.HashMap;

public class Topic {
    private String topicName;
    private HashMap<String,Vote> votesList;
    private int count;

    public Topic(String topicName) {
        this.topicName = topicName;
        this.votesList = new HashMap<>();
        this.count= 0;
    }

    public void addVote(Vote vote){
        votesList.put(vote.getVoteName(),vote);
        count++;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public HashMap<String,Vote> getVotesList() {
        return votesList;
    }

    public void setVotesList(HashMap<String,Vote> votesList) {
        this.votesList = votesList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
