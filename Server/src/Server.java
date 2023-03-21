import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    public static class myThread extends Thread{
        private Socket socket;
        HashMap<String,Topic> topics;


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
                while (true) {

                    //int keyCode = event.getKeyCode();
                    /*Scanner sc = new Scanner(System.in);
                    if(sc.nextInt()==1){
                        System.out.println("pppp");
                    }*/
                    String request = null;
                    request = reader.readLine();
                    String response = "";
                    if (request.contains("create topic")) {
                        response = "Раздел с названием " + createTopic(request, topics) + " создан";
                    }
                    else if(request.contains("create vote" )){
                        String ans = createVote(request,topics);
                        if(ans.equals("no")){
                            response = "Такого раздела не существует. Добавление не выполнено.";
                        }
                        else{
                            response = "Голосование с названием "+ ans +" создано";
                        }
                    }
                    else if(request.contains("view vote")){
                        String ans = viewVotes(request, topics);
                        if(ans.equals("no")){
                            response = "Такого голосования не существует!";
                        }
                        else {
                            response = "Информация по голосованию " + ans;
                        }
                    }
                    else if(request.equals("view")){
                        response = "Список разделов: " + viewTopics(topics);
                    }
                    else if(request.contains("vote")){
                        String ans = viewOptions(request, topics);
                        if(ans.equals("no")){
                            response = "Такого голосования не существует! Ваш голос не принят";
                        }
                        else {
                            response = "Выберете понравившийся вариант ответа и напишите его:" + ans;
                        }
                        writer.write(response);
                        writer.newLine();
                        writer.flush();
                        String vote = reader.readLine();
                        if(!vote.equals("")) {
                            response = "Ваш голос за " + doVote(request, vote, topics) + " принят";
                        }
                    }
                    else if(request.contains("delete")){
                        String ans = deleteVote(request,topics);
                        if(ans.equals("no")){
                            response = "Такого голосования не существует! Вы не можете его удалить";
                        }
                        else if(ans.equals("wrong")){
                            response ="Вы не можете удалить это голосование, так как не вы его создали";
                        }
                        else {
                            response = "Удаление голосования " + ans + " выполнено";
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

        try( ServerSocket server = new ServerSocket( 8000);){
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
            ans.append(t).append(" (голосований в разделе: ").append(topics.get(t).getVotesList().size()).append("); ");
        }
        return ans;
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

    public static String viewVotes(String line,  HashMap<String,Topic> topics){
        StringBuilder ans = new StringBuilder();
        String[] subStr = line.split("#");
        String topic =subStr[1];
        if(topics.containsKey(topic) && topics.get(topic).getVotesList().containsKey(subStr[2])) {
            Vote vote = topics.get(topic).getVotesList().get(subStr[2]);
            ans.append(vote.getVoteName()).append(": Тема: ")
                    .append(vote.getVoteTheme()).append("; Голоса:")
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

    /*public static int findTopicByName(ArrayList<Topic> topics, String topicName){
        for (int i = 0; i < topics.size() ; i++) {
            if(topics.get(i).getTopicName().equals(topicName))
                return i;
        }
        return -1;
    }*/

    /*public static int findVoteByName(ArrayList<Vote> votes, String voteName){
        for (int i = 0; i < votes.size() ; i++) {
            if(votes.get(i).getVoteName().equals(voteName))
                return i;
        }
        return -1;
    }*/

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
