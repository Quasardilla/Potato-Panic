import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientUDPHandler extends Thread {
    protected DatagramSocket UDPSocket;
    protected ClientHandler clientHandler;
    protected SharedPlayers players;
    private int playerNum;
    private int playerLiteBufferSize = 16;
    private int playerLiteListBufferSize = 256;
    private InetAddress ClientIP;
    private int ClientPort;

    public ClientUDPHandler(ClientHandler clientHandler, DatagramSocket socket, SharedPlayers players, int playerNum) {
        this.UDPSocket = socket;
        this.players = players;
        this.playerNum = playerNum;
        this.clientHandler = clientHandler;

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
        while (true) {
            try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
            byte[] data = receiveData();

			try {			
                switch (data[0]) {
                    case 0x02:
                        players.setPlayer(playerNum, bufferToPlayerLite(data));
                        /*
                        * "recentlySwitched" exists because when the potato switches,
                        * it delays the player from having their position registered
                        * for ~10 ms, the same amount of time the thread sleeps for.
                        * This time adds up as the potato switches throughout the game, 
                        * so instead of sending the other player's positions, which 
                        * would prompt the client to send their position 
                        * (upholding the delay), I simply catch up the thread by just 
                        * reading the player's position.
                        */
                        if(!clientHandler.recentlySwitched) {
                            sendOtherPlayers(players.getOtherPlayers(playerNum));
                        } else
                            clientHandler.recentlySwitched = false;

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

		int playerX = MasterClientHandler.toInt(x);
		int playerY = MasterClientHandler.toInt(y);

		return new PlayerLite(playerX, playerY);
	} 


	private void sendOtherPlayers(ArrayList<PlayerLite> otherPlayers) throws IOException {
        byte[] buffer = new byte[playerLiteListBufferSize];
        buffer[0] = 0x04;
		for(int i = 0; i < otherPlayers.size(); i++)
		{
			PlayerLite otherPlayer = otherPlayers.get(i);
			out.write(MasterClientHandler.toByteArray(otherPlayer.x));
			out.write(MasterClientHandler.toByteArray(otherPlayer.y));
		}
		out.flush();
	}

}