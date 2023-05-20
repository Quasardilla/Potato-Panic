package InfoSwapServer;

import java.io.IOException;

public class SwapServer {
    public static void main(String[] args) {
        Server server = new Server("localhost", 5100);
        System.out.println("listening for client at " + server.host + " with port " + server.port);

        while(true)
            server.listenForClient();
    }
}
