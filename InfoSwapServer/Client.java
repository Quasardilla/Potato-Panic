package InfoSwapServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Scanner;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

public class Client {
    private static Socket clientSocket;
    private static DataOutputStream out;
    private static DataInputStream in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            System.out.println(clientSocket.getRemoteSocketAddress());
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sendMessage(String msg) throws IOException {
        out.writeUTF(msg);
        String resp = in.readUTF();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        
        clientSocket.close();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        initConnection();

        String response = "";

        while(!response.equals("hello client")) {
            
            
            String input = sc.nextLine();
            System.out.println("Sending " + input);

            try {
                response = sendMessage(input);
                response = sendMessage(input + "extra");
                response = sendMessage(input + "extra1");
                response = sendMessage(input + "extra2");
            } catch (IOException e) {}

            if(response == null) {
                System.out.println("Socket closed");
                return;
            }

            System.out.println("Received " + response);

        }
    }

    public static void initConnection() {
        Client client = new Client();
        int port = 5100;
        String ip = "localhost";

        System.out.println("Connecting to " + ip + " on port " + port);
        client.startConnection(ip, port);
    }
}