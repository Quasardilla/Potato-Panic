import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * This class was made becase I had to be able
 * to send messages to all clients, and I couldn't
 * do that from the ClientHandler class, because 
 * it doesn't know if other clients exist. While
 * I could have passed a list of the clients through
 * the constructor, it would be pretty messy.
 */
class MasterClientHandler extends Thread
{
	protected ArrayList<BufferedOutputStream> outputStreams;
	protected ArrayList<ClientHandler> clientHandlers;
    protected SharedPlayers players;
    private boolean bombIntermission = false;
    private int p1Switched = -1, p2Switched = -1;
    private short gameLength = 40;
    private short intermissionLength = 5;
    private long lastBombSwitch = 0;
    private final int acceptableBombSwitchGap = 500;

	// Constructor
	public MasterClientHandler(ArrayList<BufferedOutputStream> outputStreams, ArrayList<ClientHandler> clientHandlers, SharedPlayers players)
	{
		this.outputStreams = outputStreams;
        this.clientHandlers = clientHandlers;
		this.players = players;

        players.setGameLength(gameLength);
        setName("MasterClientHandler");
	}


    /** 
     * Manages the game's timing, and collisions
     * for potato switches.
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
                    if(players.eliminatedPlayers.size() >= players.players.size() - 1) {
                        endGame();
                        bombIntermission = false;
                        continue;
                    }
                    players.setGameStarted(false);
                }

                if(bombIntermission && System.currentTimeMillis() - players.getStartTime() > intermissionLength * 1000) {
                    bombIntermission = false;
                    players.setGameLength(gameLength);
                    startGame();
                }

                ArrayList<PlayerLite> playerLites = players.getPlayers();

                /*
                 * While I have the recently switched check, I still
                 * need to make sure there is a cooldown between switches,
                 * as 3 players can form a cycle of bomb switches, which 
                 * the recently switched variables cannot protect against.
                 * I believe 500 ms is an appropriate amount of time as it
                 * it short enough for a quick switch, but long enough
                 * where it won't flood the client with switched messages
                 * when the bomb falls into a switch cycle.
                 */
                if(System.currentTimeMillis() - lastBombSwitch > acceptableBombSwitchGap) {
                    for(int i = 0; i < playerLites.size(); i++) {
                        Rectangle p1 = new Rectangle(playerLites.get(i).x, playerLites.get(i).y, 50, 50);
                        for(int j = i + 1; j < playerLites.size(); j++) {
                            Rectangle p2 = new Rectangle(playerLites.get(j).x, playerLites.get(j).y, 50, 50);
                            if(p1.intersects(p2) && players.gameStarted  && 
                            !players.isEliminated(players.getPlayerNumFromIndex(i)) && !players.isEliminated(players.getPlayerNumFromIndex(j))) {
                                /*
                                 * Oh my, a XOR operator!? This works because I 
                                 * don't want the recently switched players to 
                                 * switch again while they're still touching,
                                 * but if the player who has the bomb (who has
                                 * to be one of the recently switched numbers)
                                 * touches a player who wasnt recently switched,
                                 * the condition returns true because XOR
                                 */
                                if(i == p1Switched ^ j == p2Switched) {
                                    System.out.println("player holding bomb currently " + players.playerHoldingBomb);
                                    
                                    if(players.playerHoldingBomb == i) {
                                        potatoSwitched(j);
                                        players.playerHoldingBomb = j;
                                    }
                                    else if(players.playerHoldingBomb == j) {
                                        potatoSwitched(i);
                                        players.playerHoldingBomb = i;
                                    }
                                    p1Switched = i;
                                    p2Switched = j;

                                    lastBombSwitch = System.currentTimeMillis();

                                    System.out.println("player holding bomb after switch " + players.playerHoldingBomb);
                                }
                            }
                            else if(i == p1Switched && j == p2Switched){
                                p1Switched = players.getPlayerHoldingBomb();
                                p2Switched = players.getPlayerHoldingBomb();
                            }
                        }
                    }
                }
                

            } catch (Exception e) { e.printStackTrace(); }
		}
	}

    public void startGame() throws IOException {
        System.out.println("Game started");

        if(players.getGameStarted() || players.getPlayerInfos().size() < 2)
            return;

            bombIntermission = false;
            players.setGameStarted(true);
            
        do {
            players.setPlayerHoldingBomb((int) (Math.random() * (players.getPlayers().size() - players.getEliminatedPlayers().size())));
        } while(players.getEliminatedPlayers().contains(players.getPlayerHoldingBomb()) && players.getPlayerHoldingBomb() != -1);
        System.out.println("Player holding bomb: " + players.getPlayerHoldingBomb());
        p1Switched = players.getPlayerHoldingBomb();
        p2Switched = players.getPlayerHoldingBomb();
        players.setGameLength(gameLength);
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
        System.out.println("Game ended");

        players.reset();
		players.setGameStarted(false);

		for(BufferedOutputStream out : outputStreams) {
            if(out == null)
                continue;

			out.write(0x06);
			out.flush();
		}

        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;

			client.sendOtherPlayerInfos();
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
        System.out.println("- Player " + playerNum + " disconnected");
        outputStreams.set(playerNum, null);
        clientHandlers.set(playerNum, null);

        System.out.println("- removed output stream and clienthandler for player " + playerNum);

        for(ClientHandler client : clientHandlers) {
            if(client != null && client.isAlive())
			    client.sendDisconnectedPlayer(playerNum);
            // continue;

		}

        System.out.println("- sent disconnected player to all clients");

                
        if(players.getPlayerInfos().size() < 2) 
            players.wipe();
        else
            players.removePlayer(playerNum);
                
    }

    public void potatoSwitched(int playerNum) throws IOException {
        for(ClientHandler client : clientHandlers) {
            if(client == null)
            continue;

            client.sendPotatoSwitched(playerNum);
        }
	}

	private void potatoExploded(int playerNum) throws IOException {
        System.out.println(playerNum + " exploded");
        players.eliminatedPlayers.add(playerNum);

        for(ClientHandler client : clientHandlers) {
            if(client == null || !client.isAlive())
            continue;
            
            client.sendPotatoExploded(playerNum);
        }

        if(players.gameStarted) {
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
	}

    /**
     * This method is used to insert an array of bytes into another array of bytes.
     * 
     * @param bigArr
     * @param integratedArr
     * @param index
     * @return
     */
    public static byte[] insertByteArrays(byte[] bigArr, byte[] integratedArr, int index) {
        boolean success = true;

        for(int i = index; i < integratedArr.length; i++) {
            if(i > bigArr.length) {
                success = false;
                break;
            }
            else if(i - index < integratedArr.length)
                bigArr[i] = integratedArr[i - index];
            else {
                success = false;
                break;
            }
        }

        return success
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
