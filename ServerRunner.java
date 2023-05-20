public class ServerRunner {
    public static void main(String[] args) {
        Server server = new Server(5100);
        System.out.println("listening for client at " + server.host + " with port " + server.port);

        while(true) {
            server.listenForClient();
            System.out.println("client connected");
        }
    }
}
