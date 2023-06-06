import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

class ServerHandler extends Thread
{
	protected final BufferedInputStream in;
	protected final BufferedOutputStream out;
	protected final Socket socket;
    protected ArrayList<PlayerLite> players = new ArrayList<PlayerLite>();
    protected ArrayList<PlayerInfo> playerInfos = new ArrayList<PlayerInfo>();
    protected ArrayList<Integer> eliminatedPlayers = new ArrayList<Integer>();
	protected Player player;	
	protected PlayerInfo playerInfo;	
    protected int playerHoldingBomb;
    protected long startTime;
    protected short gameLength;
    protected boolean gameStarted = false, playerEliminated = false;
    protected Platform platform;
	protected int playerNum;
	protected static int playerCount = 0;
	
	private static double totalPings = 0;
    private static double lastPPSCheck = 0;
    private static double currentPPS = 0;

	private int ping;

    private long t1;
    private long t2;    

	// Constructor
	public ServerHandler(Socket socket, BufferedInputStream in, BufferedOutputStream out, Player player, PlayerInfo playerInfo, Platform originPlatform)
	{
        
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.player = player;
        this.playerInfo = playerInfo;
        this.platform = originPlatform;
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
        try {
            sendPlayerInfo(playerInfo);
        } catch (Exception e) {}

		while (!socket.isClosed())
		{
            totalPings++;
            if (System.nanoTime() > lastPPSCheck + 1000000000)
            {
                lastPPSCheck = System.nanoTime();
                currentPPS = totalPings;
                totalPings = 0;
            }

            t1 = System.currentTimeMillis();
            
            try {
                int type = in.read();

                switch(type) {
                    case 0x01:
                        removePlayer();
                        break;
                    case 0x03:
                        System.out.println("Reading player infos");
                        playerInfos = readPlayerInfos();
                        System.out.println("There are " + playerInfos.size() + " other players");
                        break;
                    case 0x04:
                        players = readPlayers();
                        break;
                    case 0x05:
                        startTime = readStartTime();
                        gameLength = readGameLength();
                        gameStarted = true;
                        System.out.println("Game started at " + startTime);
                        System.out.println("Game length: " + gameLength);
                        break;
                    case 0x06:
                        gameStarted = false;
                        // sendPlayerInfo(playerInfo);
                        eliminatedPlayers.clear();
                        playerEliminated = false;
                        System.out.println("Game ended");
                        break;
                    case 0x07:
                        playerHoldingBomb = readPlayerIndex();
                        System.out.println("Player holding bomb: " + playerHoldingBomb);
                        break;
                    case 0x08:
                        playerHoldingBomb = readPlayerIndex();
                        if(playerHoldingBomb != 255) {
                            eliminatedPlayers.add(playerHoldingBomb);
                            playerHoldingBomb = -1;
                        }
                        else
                            playerEliminated = true;
                        System.out.println("Player that exploded: " + playerHoldingBomb);
                        // startTime = readStartTime();
                        break;
                    case 0x09:
                        System.out.println("Player eliminated");
                        playerEliminated = true;
                        break;
                    default:
                        // System.err.println("A fatal error has occured.");
                        // System.out.println("Type: " + type);
                        // for(int i = 0; i < 20; i++)
                        //     System.out.println(in.read());
                        close();
                        System.out.println("irregular type: " + type);
                        break;
                }

                if(gameStarted) {
                    sendPlayer(player.genPlayerLite(platform));
                }

            } catch (SocketException e) {
                System.out.println("Server Disconnected");
                close();
            } catch (Exception e) { e.printStackTrace(); }
            
            t2 = System.currentTimeMillis();

            ping = (int) (t2 - t1);
		}

        System.out.println("ServerHandler closed");
        gameStarted = false;

	}

    private void close() {
        try {
            socket.close();
            this.in.close();
            this.out.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void sendPlayer(PlayerLite player) throws IOException, ClassNotFoundException {
        out.write((byte) 0x02);
        byte[] x = toByteArray(player.x);
        byte[] y = toByteArray(player.y);
        out.write(x);
        out.write(y);
        out.flush();
    }

    public void sendPlayerInfo(PlayerInfo player) throws IOException, ClassNotFoundException {
        System.out.println("sending player info");

        out.write((byte) 0x00);
        byte[] name = player.name.getBytes();
        out.write(name.length);
        out.write(name);
        out.write(player.color.getRed());
        out.write(player.color.getGreen());
        out.write(player.color.getBlue());
        out.flush();
    }

    public void sendStartGame() throws IOException {
        System.out.println("Sending start game");

        out.write((byte) 0x05);
        out.flush();
    }

    public void removePlayer() throws IOException, ClassNotFoundException {
        int index = in.read();
        playerInfos.remove(index);

        for(int i = 0; i < eliminatedPlayers.size(); i++)
        {
            if(index < eliminatedPlayers.get(i))
                eliminatedPlayers.set(i, eliminatedPlayers.get(i) - 1);
        }
    }

    public ArrayList<PlayerLite> readPlayers() throws IOException, ClassNotFoundException {

        int size = in.read();

        ArrayList<PlayerLite> players = new ArrayList<PlayerLite>();
        for(int i = 0; i < size; i++)
        {
            byte[] x = new byte[4];
            byte[] y = new byte[4];
            in.read(x);
            in.read(y);
            int playerX = toInt(x);
            int playerY = toInt(y);

            players.add(new PlayerLite(playerX, playerY));
        }

        return players;
    }

    public ArrayList<PlayerInfo> readPlayerInfos() throws IOException, ClassNotFoundException {
        int size = in.read();
        ArrayList<PlayerInfo> players = new ArrayList<PlayerInfo>();
        for(int i = 0; i < size; i++)
        {
            int nameSize = in.read();
            String name = "";

            for(int j = 0; j < nameSize; j++)
            {
                name += (char)in.read();
            }
    
            int R = in.read();
            int G = in.read();
            int B = in.read();
            Color color = new Color(R, G, B);
            
            players.add(new PlayerInfo(name, color));
        }

        return players;
    }

    public long readStartTime() throws IOException, ClassNotFoundException {
        byte[] time = new byte[8];
        in.read(time);
        return toLong(time);
    }

    public short readGameLength() throws IOException, ClassNotFoundException {
        byte[] length = new byte[2];
        in.read(length);

        return toShort(length);
    }

    public int readPlayerIndex() throws IOException, ClassNotFoundException {
        return in.read();
    }

    public static byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }

    public static short toShort(byte[] bytes) {
        short value = 0;
        for (byte b : bytes) {
            value = (short) ((value << 8) + (b & 0xFF));
        }

        return value;
    }

    public static int toInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }

        return value;
    }

    public static long toLong(byte[] bytes) {
        long value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }

        return value;
    }


    public ArrayList<PlayerLite> getPlayers() {
        return players;
    }

    public ArrayList<PlayerInfo> getPlayerInfos() {
        return playerInfos;
    }

    public ArrayList<Integer> getEliminatedPlayers() {
        return eliminatedPlayers;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean getGameStarted() {
        return gameStarted;
    }

	public int getPing() {
		return ping;
	}

	public int getPPS() {
		return (int) currentPPS;
	}
}
