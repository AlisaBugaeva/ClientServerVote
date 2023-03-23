import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    final static String HOST = "localhost";
    final static int PORT = 8080;
    final static String CHOOSING_COMMAND ="Please chose and write the command:\n"+
            "'create topic' - to create new topic\n"+
            "'view' - to see all topics\n"+
            "'view votes' - to see votes in the topic\n"+
            "'create vote' - to create new vote\n"+
            "'view vote' - to see the results of th vote\n"+
            "'vote' - to vote\n"+
            "'delete' - to delete the vote\n"+
            "'exit' - to finish voting\n";

    public static void main(String[] args) {
        try(
                Socket socket = new Socket(HOST, PORT);
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()
                        )
                );
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));
        ){

            String choose = reader.readLine();
            if(choose.equals("1")){
                workInClientMode(writer,reader);
            }
            else {
                String fileName = reader.readLine();
                workInServerMode(writer,reader,fileName);
            }


       } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void workInClientMode(BufferedWriter writer, BufferedReader reader) throws IOException {
        System.out.println("Connected to server!");
        System.out.println("");

        System.out.println("Welcome to the voting app");
        System.out.println("");
        System.out.println("Please sign in by writing 'login'");

        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();
        String u ="";
        String n;
        String request ="";
        String response;

        while(!command.equals("exit")){
            if(command.equals("login")){
                System.out.println("Enter username");
                u = sc.nextLine();
                System.out.println("You logged in as "+ u);

                System.out.println("");
                System.out.println(CHOOSING_COMMAND);
                command= sc.nextLine();
            }
            else if(!u.equals("")){
                if(command.equals("create topic")){
                    System.out.println("Enter the topic name");
                    n=sc.nextLine();
                    request = "create topic#" + n;
                }
                else if(command.equals("view")){
                    request= "view";
                }
                else if(command.equals("create vote")){
                    StringBuilder ans = new StringBuilder();
                    ans.append("create vote#");
                    System.out.println("Enter the topic name, in which you want to add a vote");
                    ans.append(sc.nextLine()).append("#");
                    ans.append(u).append("#");
                    System.out.println("Enter the vote name");
                    ans.append(sc.nextLine()).append("#");
                    System.out.println("Enter the vote theme");
                    ans.append(sc.nextLine()).append("#");
                    System.out.println("Enter the number of answer options");
                    String count = sc.nextLine();
                    ans.append(count);
                    System.out.println("Enter answer options using 'Enter'");
                    for (int i = 0; i < Integer.parseInt(count); i++) {
                        ans.append("#").append(sc.nextLine());
                    }
                    request= String.valueOf(ans);
                }
                else if(command.equals("view vote")){
                    System.out.println("Enter the topic name");
                    String topic = sc.nextLine();
                    System.out.println("Enter the vote name");
                    String voteName = sc.nextLine();
                    request = "view vote#" + topic+"#"+ voteName;

                }
                else if(command.equals("view votes")){
                    System.out.println("Enter the topic name");
                    String topic = sc.nextLine();
                    request = "view votes#" + topic;

                }
                else if(command.equals("vote")){
                    System.out.println("Enter the topic name");
                    String topic = sc.nextLine();
                    System.out.println("Enter the vote name");
                    String voteName = sc.nextLine();
                    request = "vote#" + topic+"#"+ voteName;
                    writer.write(request);
                    writer.newLine();
                    writer.flush();
                    response = reader.readLine();
                    if(response.equals("There is no such vote, so we can't accept your vote")){
                        request ="";
                    }
                    else{
                        System.out.println(response);
                        request = sc.nextLine();
                    }
                }
                else if(command.equals("delete")){
                    System.out.println("Enter the topic name");
                    String topic = sc.nextLine();
                    System.out.println("Enter the vote name");
                    String voteName = sc.nextLine();
                    request = "delete#" + u + "#" + topic+"#"+ voteName;
                }
                else{
                    System.out.println("You entered an invalid command");
                    request="";
                }

                writer.write(request);
                writer.newLine();
                writer.flush();
                response = reader.readLine();
                System.out.println(response);

                System.out.println("");
                System.out.println(CHOOSING_COMMAND);
                command= sc.nextLine();
            }
            else{
                System.out.println("To vote you should sign in by writing 'login'");
            }


        }
        request="exit";
        writer.write(request);
        writer.newLine();
        writer.flush();
        u="";

    }

    public static void workInServerMode(BufferedWriter writer, BufferedReader reader, String fileName) throws IOException {

        Scanner sc = new Scanner(new File(fileName));
        String command = sc.nextLine();
        String u ="";
        String n;
        String request ="";
        String response;

        while(!command.equals("exit")){
            if(command.equals("login")){
                u = sc.nextLine();

                command= sc.nextLine();
            }
            else if(!u.equals("")){
                if(command.equals("create topic")){
                    n=sc.nextLine();
                    request = "create topic#" + n;
                }
                else if(command.equals("view")){
                    request= "view";
                }
                else if(command.equals("create vote")){
                    StringBuilder ans = new StringBuilder();
                    ans.append("create vote#");
                    ans.append(sc.nextLine()).append("#");
                    ans.append(u).append("#");
                    ans.append(sc.nextLine()).append("#");
                    ans.append(sc.nextLine()).append("#");
                    String count = sc.nextLine();
                    ans.append(count);
                    for (int i = 0; i < Integer.parseInt(count); i++) {
                        ans.append("#").append(sc.nextLine());
                    }
                    request= String.valueOf(ans);
                }
                else if(command.equals("view vote")){
                    String topic = sc.nextLine();
                    String voteName = sc.nextLine();
                    request = "view vote#" + topic+"#"+ voteName;

                }
                else if(command.equals("view votes")){
                    String topic = sc.nextLine();
                    request = "view votes#" + topic;

                }
                else if(command.equals("vote")){
                    String topic = sc.nextLine();
                    String voteName = sc.nextLine();
                    request = "vote#" + topic+"#"+ voteName;
                    writer.write(request);
                    writer.newLine();
                    writer.flush();
                    response = reader.readLine();
                    if(response.equals("There is no such vote, so we can't accept your vote")){
                        request ="";
                    }
                    else{
                        request = sc.nextLine();
                    }
                }
                else if(command.equals("delete")){
                    String topic = sc.nextLine();
                    String voteName = sc.nextLine();
                    request = "delete#" + u + "#" + topic+"#"+ voteName;
                }
                else{
                    request="wrong command";
                }

                writer.write(request);
                writer.newLine();
                writer.flush();
                //response = reader.readLine();

                command= sc.nextLine();
            }
            else{
                request="wrong command";
            }
        }
        request="exit";
        writer.write(request);
        writer.newLine();
        writer.flush();
        u="";
    }
}
