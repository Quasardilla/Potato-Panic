import Server.Server;

public class ServerRunner {
    public static void main(String[] args) {
        // Server server = new Server("192.168.201.86", 5100);
        Server server = new Server("192.168.130.150", 5100);
        System.out.println("listening for client at " + server.getHost() + " with port " + server.getPort());

        while(true) {
            server.listenForClient();
        }
    }
}
