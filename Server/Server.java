package Server;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    protected String host;
    protected int port;

    protected ServerSocket serverSocket;
    protected Socket clientSocket;
    protected DatagramSocket clientUDPSocket;
    protected InputStream in;
    protected OutputStream out;
    protected ArrayList<BufferedOutputStream> outputStreams = new ArrayList<BufferedOutputStream>();
    protected ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
    private SharedPlayers players = new SharedPlayers();
    private MasterClientHandler sharedThread = new MasterClientHandler(outputStreams, clientHandlers, players);

    public Server(int port) {
        this("localhost", port);
    }

    public Server(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));
            clientUDPSocket = new DatagramSocket(port);
        } catch (IOException e) {}
        sharedThread.start();
    }

    public void listenForClient() {
        try {
            clientSocket = serverSocket.accept(); 
            // System.out.println(clientSocket.getInetAddress().getHostAddress());
            out = clientSocket.getOutputStream();
            outputStreams.add(new BufferedOutputStream(out));
            in = clientSocket.getInputStream();

            ClientHandler t = new ClientHandler(clientSocket, clientUDPSocket, in, out, sharedThread, players);
            clientHandlers.add(t);

            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}