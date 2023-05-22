import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

class ClientHandler extends Thread
{
	protected BufferedInputStream in;
	protected BufferedOutputStream out;
	protected final Socket socket;
    protected PlayerList players;
	protected PlayerLite player;	
	protected int playerNum;
	protected static int playerCount = 0;
	
	private static double totalFrames = 0;
    private static double lastFPSCheck = 0;
    private static double currentFPS = 0;

	private long t1 = 0;
	private long t2 = 0;
	private long t3 = 0;

	// Constructor
	public ClientHandler(Socket socket, InputStream in, OutputStream out, PlayerList players)
	{
		this.socket = socket;
		this.in = new BufferedInputStream(in);
		this.out = new BufferedOutputStream(out);
		this.players = players;

		playerNum = playerCount;
        playerCount++;
	}

	@Override
	public void run()
	{
		try {	
			// receive the answer from client

			player = readPlayer();
			players.addPlayer(player);

			System.out.println("Client Connected");

			ArrayList<PlayerLite> otherPlayers = players.getOtherPlayers(playerNum).getPlayers();
			sendOtherPlayers(otherPlayers);
				
		} catch (SocketException e) {
			System.out.println("Client Disconnected");
			try {
				socket.close();
			} catch (IOException e1) { e1.printStackTrace(); }
		} catch (Exception e) { e.printStackTrace(); }

		while (!socket.isClosed())
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }


			totalFrames++;
            if (System.nanoTime() > lastFPSCheck + 1000000000)
            {
                lastFPSCheck = System.nanoTime();
                currentFPS = totalFrames;
                totalFrames = 0;
				// System.out.println(currentFPS);
            }

			try {
				// receive the answer from client

				t1 = System.currentTimeMillis();

				ArrayList<PlayerLite> otherPlayers = players.getOtherPlayers(playerNum).getPlayers();
				sendOtherPlayers(otherPlayers);
				
				player = readPlayer();
				players.setPlayer(playerNum, player);
				
				t2 = System.currentTimeMillis();

				// System.out.println(t2 - t1);
				// System.out.println(player);

			} catch (SocketException e) {
				System.out.println("Client Disconnected");
				players.removePlayer(playerNum);
				try {
					socket.close();
				} catch (IOException e1) { e1.printStackTrace(); }
				break;
			} catch (Exception e) { e.printStackTrace(); }
	
		}
		
		try
		{
			// closing resources
			this.in.close();
			this.out.close();
		} catch(IOException e){
			e.printStackTrace();
		}

	}

	public PlayerLite readPlayer() throws IOException, ClassNotFoundException
	{
		
		byte[] x = new byte[4];
		byte[] y = new byte[4];
		in.read(x);
		in.read(y);
		int playerX = toInt(x);
		int playerY = toInt(y);

		System.out.println(playerX + ", " + playerY);

		return new PlayerLite(playerX, playerY);
	}

	private void sendOtherPlayers(ArrayList<PlayerLite> otherPlayers) throws IOException {
		out.write(otherPlayers.size());
		for(int i = 0; i < otherPlayers.size(); i++)
		{
			PlayerLite otherPlayer = otherPlayers.get(i);
			out.write(toByteArray(otherPlayer.x));
			out.write(toByteArray(otherPlayer.y));
		}
		out.flush();
	}

	public static byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }

    public static int toInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }

        return value;
    }
}
