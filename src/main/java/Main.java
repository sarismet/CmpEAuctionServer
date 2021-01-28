import java.net.ServerSocket;
import java.util.UUID;

public class Main {

    public static int USER_COUNT = -1;
    public static void main(String[] args) {
        System.out.println("USER COUNT is "+USER_COUNT);
        Controller controller = new Controller(8000);

    }
}


