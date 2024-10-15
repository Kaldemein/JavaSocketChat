import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Client {
  private Socket socket; //подключение к клиенту, передается в конструктор от сервера
  private BufferedReader bufferedReader; //читает из потока ввода
  private BufferedWriter bufferedWriter; //пишет в выходной поток 
  private String username;
  
  public Client(Socket socket, String username) {
    try {
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.username = username;  
    } catch (IOException e) {
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public static void main(String[] args) throws UnknownHostException, IOException {
      Scanner scanner = new Scanner(System.in);
      System.out.println("Please enter your name: ");
      String username = scanner.nextLine();
      Socket socket = new Socket("localhost", 1234);
      Client client = new Client(socket, username);
      client.listenForMessages();
      client.sendMessage();
      scanner.close();
  }

  public void listenForMessages(){
    new Thread(new Runnable(){
      @Override
      public void run() {
        String messagesFromOthers;

        while(socket.isConnected()){
          try {
            messagesFromOthers = bufferedReader.readLine();
            System.out.println(messagesFromOthers);  
          } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
          }
        }
      }
    }).start();
  }

  public void sendMessage(){
    try{
      bufferedWriter.write(username);
      bufferedWriter.newLine();
      bufferedWriter.flush();
      Scanner scanner = new Scanner(System.in);
      while(socket.isConnected()){
        String messageToSend = scanner.nextLine();
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        bufferedWriter.write(timeStamp + " " + username + ": " + messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
      }
      scanner.close();
    } catch(IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
    /*
     При использовании потоков ввода/вывода, таких как InputStreamReader и OutputStreamWriter, их тоже 
     рекомендуется закрывать, но если закрыть верхний поток, 
     который их использует (например, BufferedReader или PrintWriter), 
     они закроются автоматически, так как закрытие верхнего потока приведет к 
     закрытию всех связанных с ним ресурсов.
     */
    try {
      if(bufferedReader != null){ bufferedReader.close();}
      if(bufferedWriter != null){ bufferedWriter.close();}
      if(socket != null){ socket.close();}  
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

