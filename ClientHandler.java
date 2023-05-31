import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

class ClientHandler extends Thread
{
	protected BufferedInputStream in;
	protected BufferedOutputStream out;
	protected MasterClientHandler sharedThread;
	protected final Socket socket;
    protected SharedPlayers players;
	protected PlayerLite player;	
	protected int playerNum;
	protected static int playerCount = 0;
	protected int acknowledgedPlayers = 0;

	private boolean recentlySwitched = false;

	// Constructor
	public ClientHandler(Socket socket, InputStream in, OutputStream out, MasterClientHandler sharedThread, SharedPlayers players)
	{
		this.socket = socket;
		this.in = new BufferedInputStream(in);
		this.out = new BufferedOutputStream(out);
		this.sharedThread = sharedThread;
		this.players = players;

		playerNum = playerCount;
		acknowledgedPlayers = playerNum;
        playerCount++;
	}


    /** 
     *  <pre>     
     *Message Types: 
     *  0x00 - Player Join (For server, playerInfo like name, color, etc.)
     *  0x01 - Player Leaves (For client, player index of who left)
     *  0x02 - PlayerLite Info (For server, player position)
     *  0x03 - PlayerInfo (For client, names, colors, etc.)
     *  0x04 - PlayerList Info (For client, all players but current player)
     *  0x05 - Start Game (For server, which will then send 0x05 to all clients, along with unix time stamp of when game started)
     *  0x06 - End Game (For clients)
     *  0x07 - Potato switches to new player (For client, player index of who is now holding potato)
     *  0x08 - Potato explodes (For client, player index of who exploded, and unix time stamp of when the game will start again)
     *  0x09 - Player Eliminated (For client, only sent when client is eliminated / spectating)
     *  </pre>
     */
	@Override
	public void run()
	{
		System.out.println("Player " + playerNum + " Connected");

		while (!socket.isClosed())
		{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
			try {
				int type = in.read();
                switch(type) {
                    case 0x00:
                        players.addPlayer(readPlayerInfo()); 
						if(players.getGameStarted()) {
							players.eliminatedPlayers.add(playerNum);
							System.out.println("adding player as spectator");
							sendStartGame();
							out.write(0x09);
						}
						sharedThread.playerJoined();
                        break;
                    case 0x02:
						players.setPlayer(playerNum, readPlayer());
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
						if(!recentlySwitched) {
							sendOtherPlayers(players.getOtherPlayers(playerNum));
						} else
							recentlySwitched = false;

                        break;
                    case 0x05:
                        sharedThread.startGame();
                        break;
                    default:
                        System.err.println("An invalid message was recieved from player " + playerNum + ".");
						System.out.println(type);
						sharedThread.playerDisconnected(players.getPlayerIndicies().get(playerNum));
                        close();
                        break;
                }
			} catch (SocketException e) {
				try {
					sharedThread.playerDisconnected(players.getPlayerIndicies().get(playerNum));
				} catch (IOException e1) {}
				close();
			} catch (Exception e) { e.printStackTrace(); }
		}

		return;

	}

	private void close() {
        try {
            socket.close();
            this.in.close();
            this.out.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

	public PlayerLite readPlayer() throws IOException, ClassNotFoundException
	{
		byte[] x = new byte[4];
		byte[] y = new byte[4];
		in.read(x);
		in.read(y);
		int playerX = MasterClientHandler.toInt(x);
		int playerY = MasterClientHandler.toInt(y);

		return new PlayerLite(playerX, playerY);
	}

	public PlayerInfo readPlayerInfo() throws IOException, ClassNotFoundException
	{
		int size = in.read();
		String name = "";

		for(int i = 0; i < size; i++)
		{
			name += (char)in.read();
		}

		
		int R = in.read();
		int G = in.read();
		int B = in.read();
		Color color = new Color(R, G, B);
		System.out.println(name + ", " + color);
		return new PlayerInfo(name, color);
	}

	public void sendStartGame() throws IOException {
		out.write(0x05);
		out.write(MasterClientHandler.toByteArray(players.getStartTime()));
		out.write(MasterClientHandler.toByteArray(players.getGameLength()));
		out.flush();
		sendPotatoSwitched(players.playerHoldingBomb);
    }

	private void sendOtherPlayers(ArrayList<PlayerLite> otherPlayers) throws IOException {

		out.write(0x04);
		out.write(otherPlayers.size());
		for(int i = 0; i < otherPlayers.size(); i++)
		{
			PlayerLite otherPlayer = otherPlayers.get(i);
			out.write(MasterClientHandler.toByteArray(otherPlayer.x));
			out.write(MasterClientHandler.toByteArray(otherPlayer.y));
		}
		out.flush();
	}

	public void sendOtherPlayerInfos() throws IOException {
		ArrayList<PlayerInfo> otherPlayers = players.getOtherPlayerInfos(playerNum);

		System.out.println(otherPlayers.size());

		out.write(0x03);
		out.write(otherPlayers.size());
		for(int i = 0; i < otherPlayers.size(); i++)
		{
			PlayerInfo otherPlayer = otherPlayers.get(i);
			out.write(otherPlayer.name.length());
			out.write(MasterClientHandler.toByteArray(otherPlayer.name));
			out.write(MasterClientHandler.toByteArray(otherPlayer.color));
		}
		out.flush();
	}

	public void sendDisconnectedPlayer(int playerNum) throws IOException {
		int otherIndex = players.getPlayerIndicies().get(playerNum);
		int thisIndex = players.getPlayerIndicies().get(this.playerNum);
		if(otherIndex < thisIndex) {
			thisIndex--;
		}
		out.write(0x01);
		out.write(thisIndex);
		out.flush();
	}

	public void sendPotatoSwitched(int playerNum) throws IOException {
		int otherIndex = players.getPlayerIndicies().get(playerNum);
		int thisIndex = players.getPlayerIndicies().get(this.playerNum);
		
		if(otherIndex < thisIndex) {
			thisIndex--;
		}
		else if(otherIndex == thisIndex)
			thisIndex = 255;

		out.write(0x07);
		out.write(thisIndex);
		out.flush();

		recentlySwitched = true;
	}

	public void sendPotatoExploded(int playerNum) throws IOException {
		int otherIndex = players.getPlayerIndicies().get(playerNum);
		int thisIndex = players.getPlayerIndicies().get(this.playerNum);

		if(otherIndex > thisIndex) {
			thisIndex--;
		}
		else if(otherIndex == thisIndex)
			thisIndex = 255; 

		out.write(0x08);
		out.write(thisIndex);
		out.flush();

		recentlySwitched = true;
	}
}
