import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{
  
  public static List<ClientHandler> clientHandlers = new ArrayList<>(); //хранит в себе объекты clientHandler каждого клиента, чтобы отправлять данные каждому клиенту в цикле.
  private Socket socket; //подключение к клиенту, передается в конструктор от сервера
  private BufferedReader bufferedReader; //читает из потока ввода
  private BufferedWriter bufferedWriter; //пишет в выходной поток 
  private String clientUsername;

  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      //socket.getOutputStream() - сырой поток байтов выходящих через сокет
      //OutputStreamWriter() - класс для преобразования потока байтов в поток символов
      //BufferedReader() добавляется поверх OutputStreamReader для улучшения производительности при чтении данных. Он буферизирует (накапливает) символы и позволяет читать данные большими блоками (например, целыми строками).
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
      this.clientUsername = bufferedReader.readLine();
      clientHandlers.add(this);
      broadcastMessage("SERVER: " + clientUsername +" has entered the chat.");  
    } catch (IOException e) {
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  @Override
  public void run() {
    String messageFromClient;
    while(socket.isConnected()){
      try {
        messageFromClient = bufferedReader.readLine(); //считываем сообщение клиента
        broadcastMessage(messageFromClient);           //передаем его всем клиентам
      } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);
      }
    }
  }
  
  public void broadcastMessage(String message){                   //проходимся по всем клиентам, кроме текущего
    for(ClientHandler clientHandler : clientHandlers){              
      try {
        if(!clientHandler.clientUsername.equals(clientUsername)){
          clientHandler.bufferedWriter.write(message);           //записываем в поток вывода
          clientHandler.bufferedWriter.newLine();                
          clientHandler.bufferedWriter.flush();                  
        }  
      } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);
      }
    }
  }

  public void removeClientHandler(){
    clientHandlers.remove(this);
    broadcastMessage("SERVER: " + clientUsername + "has left the chat.");
  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
    /*
     При использовании потоков ввода/вывода, таких как InputStreamReader и OutputStreamWriter, их тоже 
     рекомендуется закрывать, но если закрыть верхний поток, 
     который их использует (например, BufferedReader или PrintWriter), 
     они закроются автоматически, так как закрытие верхнего потока приведет к 
     закрытию всех связанных с ним ресурсов.
     */
    removeClientHandler();
    try {
      if(bufferedReader != null){ bufferedReader.close();}
      if(bufferedWriter != null){ bufferedWriter.close();}
      if(socket != null){ socket.close();}  
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
