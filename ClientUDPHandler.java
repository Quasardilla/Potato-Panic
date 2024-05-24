import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientUDPHandler extends Thread {
    protected DatagramSocket UDPSocket;
    protected ClientHandler clientHandler;
    protected SharedPlayers players;
    private final int playerNum;
    private final int playerLiteBufferSize = 16;
    private final int playerLiteListBufferSize = 256;
    private InetAddress ClientIP;
    private int ClientPort;

    public ClientUDPHandler(ClientHandler clientHandler, DatagramSocket socket, SharedPlayers players, int playerNum) {
        this.UDPSocket = socket;
        this.players = players;
        this.playerNum = playerNum;
        this.clientHandler = clientHandler;

        System.out.println("ClientUDPHandler-" + playerNum + " created");

        setName("ClientUDPHandler-" + playerNum);
    }

    /** 
     *  <pre>     
     *Message Types: 
     *  0x02 - PlayerLite (For Server, player position)
     *  </pre>
     */
    @Override
    public void run() {
        System.out.println("ClientUDPHandler-" + playerNum + " started");

        while (true) {
            try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
            byte[] data = receiveData();

			try {			
                switch (data[0]) {
                    case 0x02:
                        players.setPlayer(data[1], bufferToPlayerLite(data));
                        // System.out.println(players.getOtherPlayers(playerNum).toString());
                        sendOtherPlayers(players.getOtherPlayers(playerNum));
                        // sendOtherPlayers(players.players);
                        break;
                
                    default:
                        break;
                }
			} catch (Exception e) { e.printStackTrace(); }
        }
    }

    private byte[] receiveData() {
        byte[] buffer = new byte[playerLiteBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            UDPSocket.receive(packet);
        } catch (IOException e) { e.printStackTrace(); }
        ClientIP = packet.getAddress();
        ClientPort = packet.getPort();
        return buffer;
    }

    /**
     * 
     * @param buffer
     * Formatted like this:
     * 0x02 | PlayerNum | 6 empty bytes | 4 bytes for x | 4 bytes for y
     * @return
     * PlayerLite with the x and y from buffer
     */
    private PlayerLite bufferToPlayerLite(byte[] buffer)
	{
        byte[] x = new byte[4];
        byte[] y = new byte[4];
		for(int i = 8 ; i < 12; i++)
            x[i - 8] = buffer[i];
            
		for(int i = 12; i < 16; i++)
            y[i - 12] = buffer[i];

		int playerX = MasterClientHandler.toInt(x);
		int playerY = MasterClientHandler.toInt(y);

        // System.out.println(new PlayerLite(playerX, playerY));

		return new PlayerLite(playerX, playerY);
	} 


	private void sendOtherPlayers(ArrayList<PlayerLite> otherPlayers) throws IOException {
        System.out.println("Player " + playerNum + ": " + players.getPlayer(playerNum).toString());
        System.out.println("Player " + playerNum + "'s arr: " + otherPlayers.toString());
        byte[] buffer = new byte[playerLiteListBufferSize];
        buffer[0] = 0x00;
        buffer[1] = (byte) playerNum;
		for(int i = 0; i < otherPlayers.size(); i++)
		{
			PlayerLite otherPlayer = otherPlayers.get(i);
            byte[] x = MasterClientHandler.toByteArray(otherPlayer.x);
            byte[] y = MasterClientHandler.toByteArray(otherPlayer.y);
            System.arraycopy(x, 0, buffer, ((i + 2) * 8), x.length);
            System.arraycopy(y, 0, buffer, ((i + 2) * 8) + 4, y.length);
		}
        DatagramPacket otherPlayersPacket = new DatagramPacket(buffer, buffer.length, ClientIP, ClientPort);
		UDPSocket.send(otherPlayersPacket);
	}

}
