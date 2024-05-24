import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerUDPHandler extends Thread {
    protected ServerHandler serverHandler;
    protected DatagramSocket UDPSocket;
    protected Player player;
    protected ArrayList<PlayerLite> players;
    protected Platform originPlatform;
    private final int playerLiteBufferSize = 16;
    private final int playerLiteListBufferSize = 256;
    private InetAddress serverIP;
    private int serverPort;
    private boolean close;

    public ServerUDPHandler(ServerHandler serverHandler, DatagramSocket socket, InetAddress serverIP, int serverPort, Player player, ArrayList<PlayerLite> players, Platform originPlatform) {
        this.serverHandler = serverHandler;
        this.UDPSocket = socket;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.player = player;
        this.players = players;
        this.originPlatform = originPlatform;

        System.out.println("ServerUDPHandler started");

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
        System.out.println("ServerUDPHandler running");
        while (!close) {
            try {
                Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
            if(serverHandler.gameStarted) {
                sendPlayerLite(player.genPlayerLite(originPlatform));

                byte[] data = receiveData();

                try {			
                    switch (data[0]) {
                        case 0x04:
                            readPlayerLiteList(data);
                            break;
                    
                        default:
                            break;
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        System.out.println("ServerUDPHandler closed");
    }

    public void close() {
        UDPSocket.close();
        UDPSocket.disconnect();
        close = true;
        System.out.println("UDPSocket closed");
    }

    private byte[] receiveData() {
        byte[] buffer = new byte[playerLiteListBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            UDPSocket.receive(packet);
        } catch (IOException e) { e.printStackTrace(); }
        return buffer;
    }

    /**
     * Formatted like this:
     * 0x02 | PlayerNum | 6 empty bytes | 4 bytes for x | 4 bytes for y
     * @param player
     * The player to send to the server
     */
    private void sendPlayerLite(PlayerLite player) {
        byte[] buffer = new byte[playerLiteBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverIP, serverPort);

        byte[] x = ServerHandler.toByteArray(player.x);
        byte[] y = ServerHandler.toByteArray(player.y);
        buffer[0] = 0x02;
        buffer[1] = (byte) serverHandler.playerNum;
        System.arraycopy(x, 0, buffer, 8, x.length);
        System.arraycopy(y, 0, buffer, 12, y.length);

        try {
            UDPSocket.send(packet);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void readPlayerLiteList(byte[] buffer) {
        players.clear();
        for (int i = 1; i < buffer.length - 15; i += 8) {
            byte[] playerLiteBuffer = new byte[8];
            System.arraycopy(buffer, i, playerLiteBuffer, 0, 8);
            players.add(byteArrToPlayerLite(playerLiteBuffer));
            if(i == 1)
                System.out.println("Player 1: " + byteArrToPlayerLite(playerLiteBuffer));
        }
    }
    
    private static PlayerLite byteArrToPlayerLite(byte[] arr) {
        byte[] x = new byte[4];
        byte[] y = new byte[4];

        System.arraycopy(arr, 0, x, 0, 4);
        System.arraycopy(arr, 4, y, 0, 4);

        return new PlayerLite(ServerHandler.toInt(x), ServerHandler.toInt(y));
    }

    public ArrayList<PlayerLite> getPlayers() {
        return players;
    }

}
