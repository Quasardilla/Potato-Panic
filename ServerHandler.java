import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

class ServerHandler extends Thread
{
	protected final ObjectInputStream in;
	protected final ObjectOutputStream out;
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

	// Constructor
	public ServerHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out, Player player, PlayerList players, Platform originPlatform)
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
            sendObject(player.genPlayerLite(platform));
            players = (PlayerList) readObject();
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

            long t1 = System.currentTimeMillis();

            try {	
                sendObject(player.genPlayerLite(platform));
                players = (PlayerList) readObject();
            } catch (SocketException e) {
                System.out.println("Client Disconnected");
                try {
                    socket.close();
                } catch (IOException e1) { e1.printStackTrace(); }
            } catch (Exception e) { e.printStackTrace(); }
            
            long t2 = System.currentTimeMillis();

            ping = (int) (t2 - t1);

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

    public void sendObject(Object obj) throws IOException, ClassNotFoundException {
        out.writeObject(obj);
        out.reset();
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        return in.readObject();
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
