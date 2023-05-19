import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            System.out.println(clientSocket.getRemoteSocketAddress());
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        
        clientSocket.close();
    }

    public static void main(String[] args) {
        try {
            messageServer();
        } catch (IOException e) {} catch (NamingException e) {}
    }

    public static void messageServer() throws IOException, NamingException {
        Client client = new Client();
        int port = 5100;
        String ip = "rottinger.net";
        

        System.out.println("Connecting to " + ip + " on port " + port);

        // client.startConnection("rottinger.net", 5100);
        client.startConnection(ip, port);
        String response = client.sendMessage("hello server");
        System.out.println(response);
    }
}