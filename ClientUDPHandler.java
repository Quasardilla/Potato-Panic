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
    private int playerNum;
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
     *  0x02 - PlayerLite Info (For server, player position)
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
                        players.setPlayer(playerNum, bufferToPlayerLite(data));
                        sendOtherPlayers(players.getOtherPlayers(playerNum));
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

    private PlayerLite bufferToPlayerLite(byte[] buffer) throws IOException, ClassNotFoundException
	{
        byte[] x = new byte[4];
        byte[] y = new byte[4];
		for(int i = 1 ; i < 5; i++)
            x[i - 1] = buffer[i];
            
		for(int i = 5 ; i < 9; i++)
            y[i - 5] = buffer[i];

		int playerX = MasterClientHandler.toInt(x);
		int playerY = MasterClientHandler.toInt(y);

        System.out.println(new PlayerLite(playerX, playerY));

		return new PlayerLite(playerX, playerY);
	} 


	private void sendOtherPlayers(ArrayList<PlayerLite> otherPlayers) throws IOException {
        byte[] buffer = new byte[playerLiteListBufferSize];
        buffer[0] = 0x04;
		for(int i = 1; i < otherPlayers.size(); i++)
		{
			PlayerLite otherPlayer = otherPlayers.get(i);
            byte[] x = MasterClientHandler.toByteArray(otherPlayer.x);
            byte[] y = MasterClientHandler.toByteArray(otherPlayer.y);
            System.arraycopy(x, 0, buffer, i * 8, x.length);
            System.arraycopy(y, 0, buffer, (i * 8) + 4, y.length);
		}
        // for(int i = 0; i < buffer.length; i++)
        //     System.out.print(buffer[i] + " ");

        DatagramPacket otherPlayersPacket = new DatagramPacket(buffer, buffer.length, ClientIP, ClientPort);
		UDPSocket.send(otherPlayersPacket);
	}

}
