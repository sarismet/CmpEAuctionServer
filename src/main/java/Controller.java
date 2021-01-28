import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Controller {
    Repository  repository;
    public Controller(int port){
         this.repository = new Repository();
         initServer(port);
    }

    private void initServer(int port) {
        try{
            final ServerSocket server = new ServerSocket(port);
            while (true) {
                System.out.println("Listening for connection on port 8000 ....");
                final Socket client = server.accept();
                System.out.println("client is accepted");
                Main.USER_COUNT++;
                ClientHandler ch = new ClientHandler(client,this.repository,Main.USER_COUNT);
                ch.start();
            }
        }
        catch (IOException e){
            System.out.println("Exception -> "+e);
        }
    }
}
