import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {
    protected String host;
    protected int port;

    protected ServerSocket serverSocket;
    protected Socket clientSocket;
    protected InputStream in;
    protected OutputStream out;
    protected ArrayList<BufferedOutputStream> outputStreams = new ArrayList<BufferedOutputStream>();
    protected ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
    private SharedPlayers players = new SharedPlayers();
    private MasterClientHandler sharedThread = new MasterClientHandler(outputStreams, clientHandlers, players);

    public Server(int port) {
        this.host = "localhost";
        this.port = port;
        
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {}
        sharedThread.start();
    }

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {}
        sharedThread.start();
    }

    public void listenForClient() {
        try {
            clientSocket = serverSocket.accept();
            out = clientSocket.getOutputStream();
            outputStreams.add(new BufferedOutputStream(out));
            in = clientSocket.getInputStream();

            ClientHandler t = new ClientHandler(clientSocket, in, out, sharedThread, players);
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

}