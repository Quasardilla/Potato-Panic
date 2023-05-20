import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private static Socket clientSocket;
    protected static ObjectInputStream in;
	protected static ObjectOutputStream out;
    protected String ip;
    protected int port;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    public void startConnection() {
        try {
            clientSocket = new Socket(ip, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object sendObject(Object obj) throws IOException, ClassNotFoundException {
        out.writeObject(obj);
        Object resp = in.readObject();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        
        clientSocket.close();
    }
}