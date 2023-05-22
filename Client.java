import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private static Socket clientSocket;
    protected static BufferedInputStream in;
	protected static BufferedOutputStream out;
    protected String ip;
    protected int port;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    public void startConnection() {
        try {
            clientSocket = new Socket(ip, port);
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            in = new BufferedInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        
        clientSocket.close();
    }

    public BufferedInputStream getIn() {
        return in;
    }

    public BufferedOutputStream getOut() {
        return out;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}