import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerUDPHandler extends Thread {
    protected DatagramSocket UDPSocket;
    private int byteBufferSize = 1024;
    private InetAddress ClientIP;
    private int ClientPort;

    public ServerUDPHandler(DatagramSocket socket, SharedPlayers players, int playerNum) {
        this.UDPSocket = socket;

        setName("ServerUDPHandler-" + playerNum);
    }

    /** 
     *  <pre>     
     *Message Types: 
     *  0x02 - PlayerLite Info (For server, player position)
     *  </pre>
     */
    @Override
    public void run() {
        while (true) {
            try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
            byte[] data = receiveData();

			try {			
                switch (data[0]) {
                    case 0x02:
                        
                        break;
                
                    default:
                        break;
                }
			} catch (Exception e) { e.printStackTrace(); }
        }
    }

    private byte[] receiveData() {
        byte[] buffer = new byte[byteBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            UDPSocket.receive(packet);
        } catch (IOException e) { e.printStackTrace(); }
        ClientIP = packet.getAddress();
        ClientPort = packet.getPort();
        return buffer;
    }

}
