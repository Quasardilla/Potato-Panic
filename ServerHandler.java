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

class ServerHandler extends Thread
{
	protected final BufferedInputStream in;
	protected final BufferedOutputStream out;
	protected final Socket socket;
    protected PlayerList players;
	protected Player player;	
    protected Platform platform;
	protected int playerNum;
	protected static int playerCount = 0;
	
	private static double totalFrames = 0;
    private static double lastFPSCheck = 0;
    private static double currentFPS = 0;

	private int ping;

    private long t1;
    private long t2;    

	// Constructor
	public ServerHandler(Socket socket, BufferedInputStream in, BufferedOutputStream out, Player player, PlayerList players, Platform originPlatform)
	{
        
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.player = player;
        this.players = players;
        this.platform = originPlatform;
	}

	@Override
	public void run()
	{
		try {	
            sendPlayer(player.genPlayerLite(platform));
            players = readPlayers();
		} catch (SocketException e) {
			System.out.println("Client Disconnected");
			try {
				socket.close();
			} catch (IOException e1) { e1.printStackTrace(); }
		} catch (Exception e) { e.printStackTrace(); }

		while (!socket.isClosed())
		{
            totalFrames++;
            if (System.nanoTime() > lastFPSCheck + 1000000000)
            {
                lastFPSCheck = System.nanoTime();
                currentFPS = totalFrames;
                totalFrames = 0;
            }

            t1 = System.currentTimeMillis();
            
            try {	
                sendPlayer(player.genPlayerLite(platform));
                players = readPlayers();
            } catch (SocketException e) {
                System.out.println("Server Disconnected");
                try {
                    socket.close();
                } catch (IOException e1) { e1.printStackTrace(); }
            } catch (Exception e) { e.printStackTrace(); }
            
            t2 = System.currentTimeMillis();

            ping = (int) (t2 - t1);
            // System.out.println("Ping: " + ping + "ms");

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

    public void sendPlayer(PlayerLite player) throws IOException, ClassNotFoundException {
        // System.out.println(player);
        byte[] x = toByteArray(player.x);
        byte[] y = toByteArray(player.y);
        out.write(x);
        out.write(y);
        out.flush();
    }

    public PlayerList readPlayers() throws IOException, ClassNotFoundException {
        int size = in.read();
        PlayerList players = new PlayerList();
        for(int i = 0; i < size; i++)
        {
            byte[] x = new byte[4];
            byte[] y = new byte[4];
            in.read(x);
            in.read(y);
            int playerX = toInt(x);
            int playerY = toInt(y);
            
            players.addPlayer(new PlayerLite(playerX, playerY));
        }

        return players;
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

    public PlayerList getPlayers() {
        return players;
    }

	public int getPing() {
		return ping;
	}

	public int getPPS() {
		return (int) currentFPS;
	}
}
