import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Server {
    private final static int PORT = 8080;
    final static String CHOOSING_COMMAND ="Please chose and write the command:\n"+
            "'load' - to load client's commands from file\n"+
            "'save' - to save information about votes to file\n"+
            "'exit' - to stop server\n";

    public static void main(String[] args) {
        HashMap<String,Topic> topics = new HashMap<>();

        try(ServerSocket server = new ServerSocket(PORT)){
            System.out.println("Server started!");

            //начальные данные
            HashMap<String,Integer> options = new HashMap<>();
            options.put("Tom",3);
            options.put("Garfild",2);
            options.put("Pushin",5);
            HashMap<String,Integer> options2 = new HashMap<>();
            options2.put("Sharik",1);
            options2.put("Puch",2);
            options2.put("Lucky",4);
            Vote v1 = new Vote("Tomas","Cat", "Finding best cat", 10,options);
            Vote v2 = new Vote("Tomas","Dog", "Finding best dog", 10,options2);
            Topic t1 = new Topic("Pets");
            t1.addVote(v1);
            t1.addVote(v2);
            topics.put(t1.getTopicName(),t1);
            //

            System.out.println("How would you like to start? Enter the number");
            System.out.println("1 - in client mode");
            System.out.println("2 - in server mode");
            Scanner sc = new Scanner(System.in);
            int modeNumber = sc.nextInt();
            if(modeNumber == 1){
                while(true){
                    new myThread(server.accept(),topics,"1").start();
                }
            }
            else if(modeNumber == 2){
                List<myThread> threads = new ArrayList<>();
                System.out.println(CHOOSING_COMMAND);
                sc.nextLine();
                String command = sc.nextLine();
                while (!command.equals("exit")) {
                    if (command.equals("load")) {
                        System.out.println("How many clients would you like to make?");
                        int count = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Enter file names using 'Enter'");
                        String[] fileNames = new String[count];
                        for (int i = 0; i < count; i++) {
                            fileNames[i]= sc.nextLine();
                        }
                        for (int i = 0; i < count; i++) {
                            loadFromFile(topics,fileNames[i],threads,server);
                        }
                    } else if (command.equals("save")){
                        System.out.println("Enter the file name");
                        String fileName = sc.nextLine();
                        saveToFile(topics, fileName);
                    }
                    System.out.println(CHOOSING_COMMAND);
                    command = sc.nextLine();
                }

            }

        } catch (IOException e){
            throw new RuntimeException();
        }
    }

    public static class myThread extends Thread{
        private Socket socket;
        private HashMap<String,Topic> topics;
        private String choose;
        private String fileName;


        public myThread(Socket socket, HashMap<String,Topic> topics, String choose){
            this.socket = socket;
            this.topics = topics;
            this.choose = choose;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void run(){
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream()
                    )
            );
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(
                                 socket.getInputStream()))
            ) {
                boolean flag = true;
                String request = "";
                String response =  choose;
                writer.write(response);
                writer.newLine();
                writer.flush();
                if(choose.equals("2")){
                    response = fileName;
                    writer.write(response);
                    writer.newLine();
                    writer.flush();
                }

                while(!request.equals("exit")) {
                    request = reader.readLine();
                    if (request.contains("create topic")) {
                        String ans = createTopic(request, topics);
                        if(ans.equals("already exists")){
                            response = "This topic is "+ ans +"Please make another one";
                            flag = false;
                        }
                        else{
                            response = "The topic named " + ans + " created";
                        }
                    }
                    else if(request.contains("create vote" )){
                        String ans = createVote(request,topics);
                        if(ans.equals("no")){
                            response = "There is no such topic, so you can't add a vote";
                            flag = false;
                        }
                        else if(ans.equals("already exists")){
                            response = "This vote is "+ ans +". Please make another one";
                            flag = false;
                        }
                        else{
                            response = "The vote named "+ ans +" created";
                        }
                    }
                    else if(request.contains("view votes")){
                        String ans = viewVotes(request, topics);
                        if(ans.equals("no")){
                            response = "There is no such topic!";
                            flag = false;
                        }
                        else {
                            response = "Votes from this topic: " + ans;
                        }
                    }
                    else if(request.contains("view vote")){
                        String ans = viewVote(request, topics);
                        if(ans.equals("no")){
                            response = "There is no such vote!";
                            flag = false;
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
                            flag = false;
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
                            flag = false;
                        }
                        else if(ans.equals("wrong")){
                            response ="You can't delete this vote because it isn't yours";
                            flag = false;
                        }
                        else {
                            response = "You deleted the vote " + ans;
                        }
                    }
                    else if(request.equals("wrong command")){
                        flag = false;
                    }
                    writer.write(response);
                    writer.newLine();
                    writer.flush();
                }
                if(flag){
                    System.out.println("The file '"+fileName+"' was executed. Everything is OK!");
                    System.out.println("");
                }
                else{
                    System.out.println("The file '"+ fileName +"' was executed, but some commands were written incorrectly, so they weren't executed... ");
                    System.out.println("");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //команды клиента
    public static String createTopic(String line, HashMap<String,Topic> topics){
        String[] subStr = line.split("#");
        String name = subStr[1];
        if(topics.containsKey(name)){
            return "already exists";
        }
        else {
            topics.put(name, new Topic(name));
            return name;
        }
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

            if(topics.get(topic).getVotesList().containsKey(subStr[3])){
                return "already exists";
            }
            else{
                Vote vote = new Vote(subStr[2], subStr[3], subStr[4], count, votes);
                topics.get(topic).addVote(vote);
                return vote.getVoteName();
            }
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
    public static void saveToFile(HashMap<String,Topic> topics, String fileName) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
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
        System.out.println("Information was saved to file '"+ fileName+"'");
        System.out.println("");
    }

    public static void loadFromFile(HashMap<String,Topic> topics,String fileName,List<myThread> threads, ServerSocket server) throws IOException {
        threads.add(new myThread(server.accept(),topics, "2"));
        myThread thisThread = threads.get(threads.size()-1);
        thisThread.setFileName(fileName);
        thisThread.start();
    }
}
