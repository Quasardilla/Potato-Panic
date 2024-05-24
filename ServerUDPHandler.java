import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerUDPHandler extends Thread {
    protected DatagramSocket UDPSocket;
    protected ArrayList<PlayerLite> players;
    private final int playerLiteBufferSize = 16;
    private final int playerLiteListBufferSize = 256;
    private InetAddress serverIP;
    private int serverPort;

    public ServerUDPHandler(DatagramSocket socket, InetAddress serverIP, int serverPort, ArrayList<PlayerLite> players) {
        this.UDPSocket = socket;
        this.serverIP = serverIP;
        this.serverPort = serverPort;

        setName("ServerUDPHandler");
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
                    case 0x04:
                        sendPlayerLite();
                        break;
                
                    default:
                        break;
                }
			} catch (Exception e) { e.printStackTrace(); }
        }
    }

    private byte[] receiveData() {
        byte[] buffer = new byte[playerLiteListBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            UDPSocket.receive(packet);
        } catch (IOException e) { e.printStackTrace(); }
        return buffer;
    }

    private void sendPlayerLite() {
        byte[] buffer = new byte[playerLiteBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverIP, serverPort);
        try {
            UDPSocket.send(packet);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void readPlayerLiteList() {
        byte[] buffer = new byte[playerLiteListBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            UDPSocket.receive(packet);
        } catch (IOException e) { e.printStackTrace(); }

        for (int i = 1; i < buffer.length; i += playerLiteBufferSize) {
            int playerX = ServerHandler.toInt(ServerHandler.extractByteArray(buffer, i * 8, (i * 8) + 4));
            int playerY = ServerHandler.toInt(ServerHandler.extractByteArray(buffer, (i * 8) + 4, (i * 8) + 8));
            System.out.println("Other Player Pos");
            System.out.println("Player X: " + playerX + " Player Y: " + playerY);
            PlayerLite player = new PlayerLite(playerX, playerY);
            players.add(player);
        }
    }

}
