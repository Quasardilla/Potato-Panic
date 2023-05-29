import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class MasterClientHandler extends Thread
{
	protected ArrayList<BufferedOutputStream> outputStreams;
	protected ArrayList<ClientHandler> clientHandlers;
    protected SharedPlayers players;
    private boolean bombIntermission = false;
    private short gameLength = 20;
    private short intermissionLength = 5;

	// Constructor
	public MasterClientHandler(ArrayList<BufferedOutputStream> outputStreams, ArrayList<ClientHandler> clientHandlers, SharedPlayers players)
	{
		this.outputStreams = outputStreams;
        this.clientHandlers = clientHandlers;
		this.players = players;

        players.setGameLength(gameLength);
	}


    /** 
     *  <pre>     
     *Message Types: 
     *  0x00 - Player Join (For server, playerInfo like name, color, etc.)
     *  0x01 - Player Leaves (For client, player index of who left)
     *  0x02 - PlayerLite Info (For server, player position)
     *  0x03 - PlayerInfo (For client, names, colors, etc.)
     *  0x04 - PlayerList Info (For client, all players but current player)
     *  0x05 - Start Game (For server, which will then send 0x05 to all clients, along with unix time stamp of when game started, and how long the game will last)
     *  0x06 - End Game (For clients)
     *  0x07 - Potato switches to new player (For client, player index of who is now holding potato)
     *  0x08 - Potato explodes (For client, playerLite of who exploded, and unix time stamp of when the game will start again)
     *  </pre>
     */
	@Override
	public void run()
	{
		while (true)
		{ 
            try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			
            try {
                if((players.getGameStarted() && !bombIntermission) && System.currentTimeMillis() - players.getStartTime() > players.getGameLength() * 1000) {
                    bombIntermission = true;
                    players.setGameLength(intermissionLength);
                    potatoExploded(players.playerHoldingBomb);
                    if(players.eliminatedPlayers.size() == players.players.size() - 1) {
                        endGame();
                        continue;
                    }
                    players.setGameStarted(false);
                }
                if(bombIntermission && System.currentTimeMillis() - players.getStartTime() > 5000) {
                    bombIntermission = false;
                    players.setGameLength(gameLength);
                    startGame();
                }

            } catch (Exception e) { e.printStackTrace(); }
		}
	}

    public void startGame() throws IOException {
        if(players.getGameStarted())
            return;

		players.setGameStarted(true);
		players.setPlayerHoldingBomb((int) (Math.random() * (players.getPlayers().size())));
		players.setStartTime(System.currentTimeMillis());
		for(BufferedOutputStream out : outputStreams) {
            if(out == null)
            continue;

			out.write(0x05);
			out.write(toByteArray(players.getStartTime()));
            out.write(toByteArray(players.getGameLength()));
			out.flush();
		}

        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;

            client.sendPotatoSwitched(players.getPlayerHoldingBomb());
        }

    }

    public void endGame() throws IOException {
        players.reset();
        if(!players.getGameStarted())
        return;

		players.setGameStarted(false);
		for(BufferedOutputStream out : outputStreams) {
            if(out == null)
                continue;

			out.write(0x06);
			out.flush();
		}
    }

    
    public void playerJoined() throws IOException {
        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;

			client.sendOtherPlayerInfos();
		}
    }

    public void playerDisconnected(int playerNum) throws IOException {
        outputStreams.set(playerNum, null);
        clientHandlers.set(playerNum, null);

        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;

			client.sendDisconnectedPlayer(playerNum);
		}

        System.out.println("Player " + playerNum + " disconnected");
    }

    public void potatoSwitched(int playerNum) throws IOException {
        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;

            client.sendPotatoSwitched(playerNum);
        }
	}

	private void potatoExploded(int playerNum) throws IOException {
        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;
            
            client.sendPotatoExploded(playerNum);
        }
        players.setStartTime(System.currentTimeMillis());

        for(BufferedOutputStream out : outputStreams) {
            if(out == null)
            continue;

			out.write(0x05);
			out.write(toByteArray(players.getStartTime()));
            out.write(toByteArray(players.getGameLength()));
			out.flush();
		}
	}

    public static byte[] toByteArray(short value) {
        return new byte[] {
			(byte) ((value >> 8) & 0xff),
			(byte) ((value >> 0) & 0xff),
		};
    }

    public static byte[] toByteArray(int value) {
        return new byte[] {
			(byte) ((value >> 24) & 0xff),
			(byte) ((value >> 16) & 0xff),
			(byte) ((value >> 8) & 0xff),
			(byte) ((value >> 0) & 0xff),
		};
    }

	public static byte[] toByteArray(long value) {
        return new byte[] {
			(byte) ((value >> 56) & 0xff),
			(byte) ((value >> 48) & 0xff),
			(byte) ((value >> 40) & 0xff),
			(byte) ((value >> 32) & 0xff),
			(byte) ((value >> 24) & 0xff),
			(byte) ((value >> 16) & 0xff),
			(byte) ((value >> 8) & 0xff),
			(byte) ((value >> 0) & 0xff),
		};
	}

	public static byte[] toByteArray(Color value) {
		int color = value.getRGB();

        return new byte[] {
                (byte)(color >> 16),
                (byte)(color >> 8),
                (byte) color};
    }

    public static byte[] toByteArray(String value) {
        char[] chars = value.toCharArray();
		byte[] bytes = new byte[chars.length];
		for(int i = 0; i < chars.length; i++)
			bytes[i] = (byte)chars[i];

		return bytes;
    }

    public static int toInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }

        return value;
    }
}
