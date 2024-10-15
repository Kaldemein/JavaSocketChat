import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server{
  /*ждем клиента 
    когда клиент подключается, создаем поток для обработки клиента  
  */
  private ServerSocket serverSocket;

  public Server(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void startServer(){ //держит сервер запущенным
    try{
      System.out.println("Server on port " + serverSocket.getLocalPort() + " is runnings...");
      while(!serverSocket.isClosed()){ 
        /*Метод accept() заставляет сервер ждать до тех пор, пока клиент не попытается установить соединение. Как только клиент подключится, метод вернет объект Socket, который представляет это соединение 
         
        Класс ServerSocket обычно используется в многопоточных приложениях, где каждый клиент обрабатывается в отдельном потоке, что позволяет серверу обслуживать нескольких клиентов одновременно.
        */
        Socket socket = serverSocket.accept();    
        System.out.println("A new client has connected!");
        ClientHandler clientHandler = new ClientHandler(socket);  
        
        Thread thread = new Thread(clientHandler);
        thread.start();
      }
    } catch(IOException e){
      e.printStackTrace();
    }
  }


  public void closeServerSocket() throws IOException{
    if(!serverSocket.isClosed()){
      serverSocket.close();
    }
  }

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(1234);
    Server server = new Server(serverSocket);
    server.startServer();
  }
  
}  