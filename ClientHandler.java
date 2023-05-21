import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

class ClientHandler extends Thread
{
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected final Socket socket;
    protected PlayerList players;
	Object player;	
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
		try {
            this.in = new ObjectInputStream(in);
            this.out = new ObjectOutputStream(out);
		} catch (IOException e) {}
		this.players = players;

		playerNum = playerCount;
        playerCount++;
	}

	@Override
	public void run()
	{
		try {	
			// receive the answer from client
			player = in.readObject();
			players.addPlayer((PlayerLite) player);
			System.out.println("Client Connected");
			out.writeObject(players.getOtherPlayers(playerNum));
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
				// System.out.println(currentFPS);
            }

			try {
				// receive the answer from client

				t1 = System.currentTimeMillis();
				
				out.reset();
				out.writeObject(players.getOtherPlayers(playerNum));
				
				player = in.readObject();
				System.out.println(player);
				players.setPlayer(playerNum, (PlayerLite) player);
				
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
}
