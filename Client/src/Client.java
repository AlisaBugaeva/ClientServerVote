import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        try(
                Socket socket = new Socket("127.0.0.1", 8000);
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream()
                        )
                );
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));
        ){
            System.out.println("Соединение с сервером установлено!");

            System.out.println("Добро пожаловать в приложение для голосования");
            System.out.println("Пожалуйста, выполните вход, введя команду 'login'");

            Scanner sc = new Scanner(System.in);
            String command = sc.nextLine();
            String u ="";
            String n;
            String request ="";
            String response;

            while(!command.equals("exit")){
                /*if (command.equals("exit")){
                    writer.close();
                    reader.close();
                    socket.close();
                }*/
                if(command.equals("login")){
                    System.out.println("Введите имя пользователя");
                    u = sc.nextLine();
                    System.out.println("Вход для пользователя "+ u+ " выполнен");

                    System.out.println("Введите команду");
                    command= sc.nextLine();
                }
                else if(!u.equals("")){
                    if(command.equals("create topic")){
                        System.out.println("Введите название раздела");
                        n=sc.nextLine();
                        request = "create topic#" + n;
                    }
                    else if(command.equals("view")){
                        request= "view";
                    }
                    else if(command.equals("create vote")){
                        StringBuilder ans = new StringBuilder();
                        ans.append("create vote#");
                        System.out.println("Введите название темы, в которую хотите добавить голосование");
                        ans.append(sc.nextLine()).append("#");
                        ans.append(u).append("#");
                        System.out.println("Введите название голосования");
                        ans.append(sc.nextLine()).append("#");
                        System.out.println("Введите тему голосования");
                        ans.append(sc.nextLine()).append("#");
                        System.out.println("Введите количество вариантов ответа");
                        String count = sc.nextLine();
                        ans.append(count);
                        System.out.println("Введите варианты ответов через Enter");
                        for (int i = 0; i < Integer.parseInt(count); i++) {
                            ans.append("#").append(sc.nextLine());
                        }
                        request= String.valueOf(ans);
                    }
                    else if(command.equals("view vote")){
                        System.out.println("Введите название раздела");
                        String topic = sc.nextLine();
                        System.out.println("Введите название голосования");
                        String voteName = sc.nextLine();
                        request = "view vote#" + topic+"#"+ voteName;

                    }
                    else if(command.equals("vote")){
                        System.out.println("Введите название раздела");
                        String topic = sc.nextLine();
                        System.out.println("Введите название голосования");
                        String voteName = sc.nextLine();
                        request = "vote#" + topic+"#"+ voteName;
                        writer.write(request);
                        writer.newLine();
                        writer.flush();
                        response = reader.readLine();
                        if(response.equals("Такого голосования не существует! Ваш голос не принят")){
                            request ="";
                        }
                        else{
                            System.out.println(response);
                            request = sc.nextLine();
                        }
                    }
                    else if(command.equals("delete")){
                        System.out.println("Введите название раздела");
                        String topic = sc.nextLine();
                        System.out.println("Введите название голосования");
                        String voteName = sc.nextLine();
                        request = "delete#" + u + "#" + topic+"#"+ voteName;
                    }
                    else{
                        System.out.println("Вы ввели неверную команду");
                        request="";
                    }

                    writer.write(request);
                    writer.newLine();
                    writer.flush();
                    response = reader.readLine();
                    System.out.println(response);

                    System.out.println("Введите команду");
                    command= sc.nextLine();
                }
                else{
                    System.out.println("Чтобы участвовать в голосованиях, " +
                            "пожалуйста, введите имя пользователя через команду 'login'");
                }


            }
            u="";

       } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
