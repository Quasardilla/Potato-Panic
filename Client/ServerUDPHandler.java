package Client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import Server.PlayerLite;

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

    private static double totalPings = 0;
    private static double playerFPSPings = 0;
    private static double lastPPSCheck = 0;
    private static double currentPPS = 0;
    private static double playerFPS = 0;

	private int ping;
    private long t1;
    private long t2; 

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
     *  0x00 - PlayerLite List (For client, player positions)
     *  </pre>
     */
    @Override
    public void run() {
        System.out.println("ServerUDPHandler running");
        while (!close) {
            if (System.nanoTime() > lastPPSCheck + 1000000000)
            {
                lastPPSCheck = System.nanoTime();
                currentPPS = totalPings;
                playerFPS = playerFPSPings;
                totalPings = 0;
                playerFPSPings = 0;
            }

            t1 = System.currentTimeMillis();

            try {
                Thread.sleep(5);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
            if(serverHandler.gameStarted) {
                sendPlayerLite(player.genPlayerLite(originPlatform));

                byte[] data = receiveData();

                try {			
                    switch (data[0]) {
                        case 0x00:
                            readPlayerLiteList(data);
                            break;
                    
                        default:
                            break;
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        t2 = System.currentTimeMillis();

        ping = (int) (t2 - t1);

        System.out.println("ServerUDPHandler closed");
    }

    public void close() {
        UDPSocket.close();
        UDPSocket.disconnect();
        close = true;
        System.out.println("UDPSocket closed");
    }

    private byte[] receiveData() {
        totalPings++;
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
        totalPings++;
        byte[] buffer = new byte[playerLiteBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverIP, serverPort);

        byte[] x = ServerHandler.toByteArray(player.getX());
        byte[] y = ServerHandler.toByteArray(player.getY());
        buffer[0] = 0x02;
        buffer[1] = (byte) serverHandler.playerNum;
        System.arraycopy(x, 0, buffer, 8, x.length);
        System.arraycopy(y, 0, buffer, 12, y.length);

        try {
            UDPSocket.send(packet);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void readPlayerLiteList(byte[] buffer) {
        if(buffer[1] != serverHandler.playerNum) {
            return;
        }
        playerFPSPings++;

        players.clear();
        for (int i = 16; i < buffer.length; i += 8) {
            byte[] playerLiteBuffer = new byte[8];
            System.arraycopy(buffer, i, playerLiteBuffer, 0, 8);
            players.add(byteArrToPlayerLite(playerLiteBuffer));
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

    public int getPing() {
		return ping;
	}

	public int getPPS() {
		return (int) currentPPS;
	}

    public int getApproxFPS() {
        return (int) playerFPS;
    }

}
