import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    final static int PORT = 8080;

    public static class myThread extends Thread{
        private Socket socket;
        private HashMap<String,Topic> topics;


        public myThread(Socket socket, HashMap<String,Topic> topics){
            this.socket=socket;
            this.topics=topics;
        }

        public void run(){
            try (BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(
                                 socket.getOutputStream()
                         )
                 );
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                String request = "";
                String response = "";
                while(!request.equals("exit")) {
                    //int keyCode = event.getKeyCode();
                    /*Scanner sc = new Scanner(System.in);
                    if(sc.nextInt()==1){
                        System.out.println("pppp");
                    }*/
                    request = reader.readLine();
                    if (request.contains("create topic")) {
                        response = "The topic named " + createTopic(request, topics) + " created";
                    }
                    else if(request.contains("create vote" )){
                        String ans = createVote(request,topics);
                        if(ans.equals("no")){
                            response = "There is no such topic, so you can't add a vote";
                        }
                        else{
                            response = "The vote named "+ ans +" created";
                        }
                    }
                    else if(request.contains("view votes")){
                        String ans = viewVotes(request, topics);
                        if(ans.equals("no")){
                            response = "There is no such topic!";
                        }
                        else {
                            response = "Votes from this topic: " + ans;
                        }
                    }
                    else if(request.contains("view vote")){
                        String ans = viewVote(request, topics);
                        if(ans.equals("no")){
                            response = "There is no such vote!";
                        }
                        else {
                            response = "Info for the vote " + ans;
                        }
                    }
                    else if(request.equals("view")){
                        response = "List of topics: " + viewTopics(topics);
                    }
                    else if(request.contains("vote")){
                        String ans = viewOptions(request, topics);
                        if(ans.equals("no")){
                            response = "There is no such vote, so we can't accept your vote";
                        }
                        else {
                            response = "Please choose the option and write it " + ans;
                        }
                        writer.write(response);
                        writer.newLine();
                        writer.flush();
                        String vote = reader.readLine();
                        if(!vote.equals("")) {
                            response = "Your vote for " + doVote(request, vote, topics) + " accepted";
                        }
                    }
                    else if(request.contains("delete")){
                        String ans = deleteVote(request,topics);
                        if(ans.equals("no")){
                            response = "There is no such vote, so you can't delete it";
                        }
                        else if(ans.equals("wrong")){
                            response ="You can't delete this vote because it isn't yours";
                        }
                        else {
                            response = "You deleted the vote " + ans;
                        }
                    }
                    writer.write(response);
                    writer.newLine();
                    writer.flush();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        HashMap<String,Topic> topics = new HashMap<>();

        try( ServerSocket server = new ServerSocket( PORT)){
            System.out.println("Server started");
            //Topic t1 = new Topic("Pets");
            //topics.put("Pets", t1);
            HashMap<String,Integer> options = new HashMap<>();
            options.put("Tom",3);
            options.put("Garfild",2);
            options.put("Pushin",5);
            Vote v1 = new Vote("Tomas","Cat", "Finding best cat", 10,options);
            Vote v2 = new Vote("Tomas","Dog", "Finding best dog", 10,options);
            Topic t1 = new Topic("Pets");
            t1.addVote(v1);
            t1.addVote(v2);
            topics.put(t1.getTopicName(),t1);
            //loadFromFile(topics);
            //saveToFile(topics);

            Scanner sc = new Scanner(System.in);
            while(true){
                new myThread(server.accept(),topics).start();
            }



        } catch (IOException e){
            throw new RuntimeException();
        }


    }

    public static String createTopic(String line, HashMap<String,Topic> topics){
        String[] subStr = line.split("#");
        String name = subStr[1];
        //Сделать проверку на уникальность?
        topics.put(name,new Topic(name));
        return name;
    }

    public static StringBuilder viewTopics( HashMap<String,Topic> topics){
        StringBuilder ans = new StringBuilder();
        for (String t: topics.keySet()) {
            ans.append(t).append(" (votes in topic= ").append(topics.get(t).getVotesList().size()).append("); ");
        }
        return ans;
    }
    public static String viewVotes(String line,  HashMap<String,Topic> topics){
        StringBuilder ans = new StringBuilder();
        String[] subStr = line.split("#");
        String topic =subStr[1];
        if(topics.containsKey(topic)) {
            for (Vote vote: topics.get(topic).getVotesList().values()) {
                ans.append("[ Vote name: ").append(vote.getVoteName()).append(": Theme: ")
                        .append(vote.getVoteTheme()).append("; Votes:")
                        .append(vote.getOptions()).append(" ]; ");
            }
            return String.valueOf(ans);
        }
        else
            return "no";
    }

    public static String createVote(String line,  HashMap<String,Topic> topics){
        String[] subStr = line.split("#");
        String topic =subStr[1];
        if(topics.containsKey(topic)) {
            int count = Integer.parseInt(subStr[5]);
            HashMap<String, Integer> votes = new HashMap<>();
            for (int i = 6; i < 6 + count; i++) {
                votes.put(subStr[i], 0);
            }

            Vote vote = new Vote(subStr[2], subStr[3], subStr[4], count, votes);
            topics.get(topic).addVote(vote);
            return vote.getVoteName();
        }
        else
            return "no";
    }

    public static String viewVote(String line,  HashMap<String,Topic> topics){
        StringBuilder ans = new StringBuilder();
        String[] subStr = line.split("#");
        String topic =subStr[1];
        if(topics.containsKey(topic) && topics.get(topic).getVotesList().containsKey(subStr[2])) {
            Vote vote = topics.get(topic).getVotesList().get(subStr[2]);
            ans.append(vote.getVoteName()).append(": Theme: ")
                    .append(vote.getVoteTheme()).append("; Votes:")
                    .append(vote.getOptions());
            return String.valueOf(ans);
        }
        else
            return "no";
    }
    
    public static String viewOptions(String line, HashMap<String,Topic> topics){
        StringBuilder ans = new StringBuilder();
        String[] subStr = line.split("#");
        String topic =subStr[1];
        if(topics.containsKey(topic) && topics.get(topic).getVotesList().containsKey(subStr[2])) {
            Vote vote = topics.get(topic).getVotesList().get(subStr[2]);
            ans.append(vote.getOptions().keySet());
            return String.valueOf(ans);
        }
        else
            return "no";
    }

    public static String doVote(String line,String vote, HashMap<String,Topic> topics){
        String[] subStr = line.split("#");
        String topic =subStr[1];
        HashMap<String,Vote> votes = topics.get(topic).getVotesList();
        votes.get(subStr[2]).getOptions().put(vote, votes.get(subStr[2]).getOptions().get(vote)+1);
        return vote;
    }

    public static String deleteVote(String line, HashMap<String,Topic> topics){
        String[] subStr = line.split("#");
        String topic =subStr[2];
        if(topics.containsKey(topic) && topics.get(topic).getVotesList().containsKey(subStr[3])) {
            Vote vote = topics.get(topic).getVotesList().get(subStr[3]);
            if (vote.getCreator().equals(subStr[1])){
                topics.get(topic).getVotesList().remove(subStr[3]);
                return vote.getVoteName();
            }
            else{
                return "wrong";
            }
        }
        else
            return "no";
    }

    //команды сервера

    /*public static boolean keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER)
            return true;
        else
            return false;
    }*/


    public static void saveToFile(HashMap<String,Topic> topics) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("votes2.txt");
        writer.println(topics.size());
        for (Topic t: topics.values()) {
            writer.println(t.getTopicName());
            writer.println(t.getVotesList().size());
            for (Vote v: t.getVotesList().values()) {
                writer.println(v.getCreator());
                writer.println(v.getVoteName());
                writer.println(v.getVoteTheme());
                writer.println(v.getOptions().size());
                for (String key: v.getOptions().keySet()) {
                    writer.println(key +" "+ v.getOptions().get(key));
                }
            }
        }
        writer.close();
    }

    public static void loadFromFile(HashMap<String,Topic> topics) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("votes.txt"));
        int N = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < N; i++) {
            Topic topic = new Topic(sc.nextLine());
            int M = Integer.parseInt(sc.nextLine());
            ArrayList<Vote> votesList = new ArrayList<>();
            for (int j = 0; j < M; j++) {
                String creator = sc.nextLine();
                String name = sc.nextLine();
                String theme = sc.nextLine();
                int K = Integer.parseInt(sc.nextLine());
                HashMap<String,Integer> options = new HashMap<>();
                int answerNumbers =0;
                for (int k = 0; k < K; k++) {
                    String line = sc.nextLine();
                    String[] subStr = line.split(" ");
                    options.put(subStr[0], Integer.parseInt(subStr[1]));
                    answerNumbers+=Integer.parseInt(subStr[1]);
                }
                topic.addVote(new Vote(creator,name,theme,answerNumbers,options));
            }
            topics.put(topic.getTopicName(),topic);
        }
    }
}
