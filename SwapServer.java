public class SwapServer {
    public static void main(String[] args) {
        Server server = new Server("127.0.0.1", 5100);
        System.out.println("listening for client at " + server.host + " with port " + server.port);

        while(true)
            server.listenForClient();
    }
}
