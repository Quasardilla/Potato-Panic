import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
        long t1 = System.currentTimeMillis();
        out.writeObject(obj);
        out.reset();
        Object resp = in.readObject();
        long t2 = System.currentTimeMillis();
        System.out.println("Ping (ms): " + (t2 - t1));
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        
        clientSocket.close();
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}