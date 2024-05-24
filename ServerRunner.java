public class ServerRunner {
    public static void main(String[] args) {
        Server server = new Server("192.168.201.86", 5100);
        // Server server = new Server("localhost", 5100);
        System.out.println("listening for client at " + server.host + " with port " + server.port);

        while(true) {
            server.listenForClient();
        }
    }
}
