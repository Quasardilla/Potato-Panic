import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable {
    private static Socket clientSocket = new Socket();
    private final int TIMEOUT = 15;
    protected static BufferedInputStream in;
	protected static BufferedOutputStream out;
    protected String ip;
    protected int port;
    public IOException error;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    @Override
    public void run() {
        error = startConnection();
    }

    public IOException startConnection() {
        try {
            clientSocket.connect(new InetSocketAddress(ip, port), TIMEOUT * 1000);
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            in = new BufferedInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            return e;
        }
        return null;

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

    public IOException getErr() {
        return error;
    }
}