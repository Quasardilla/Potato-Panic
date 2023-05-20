package InfoSwapServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    protected String host;
    protected int port;

    protected ServerSocket serverSocket;
    protected Socket clientSocket;
    protected DataInputStream in;
    protected DataOutputStream out;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {}
    }

    public void listenForClient() {
        try {
            clientSocket = serverSocket.accept();
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            Thread t = new ClientHandler(clientSocket, clientSocket.getInputStream(), clientSocket.getOutputStream());

            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String listenForInput() throws IOException {
        String input = "";
        try {
            input = in.readUTF();
        } catch (SocketException e) {
            System.out.println("Client Disconnected");
            stop();
            return "LOST CONNECTION";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    public void send(String msg) {
        try {
            out.writeUTF(msg);
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