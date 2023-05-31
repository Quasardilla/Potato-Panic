import java.io.Serializable;
import java.util.ArrayList;

public class SharedPlayers implements Serializable{
    private static final long serialVersionUID = 1L;

    protected ArrayList<PlayerLite> players = new ArrayList<PlayerLite>();
    protected ArrayList<PlayerInfo> playerInfos = new ArrayList<PlayerInfo>();
    protected ArrayList<Integer> playerIndicies = new ArrayList<Integer>();
    protected ArrayList<Integer> eliminatedPlayers = new ArrayList<Integer>();
    protected int playerHoldingBomb = -1;
    protected boolean gameStarted = false;
    protected short gameLength = -1;
    protected long startTime = -1;

    public SharedPlayers() {}

    public synchronized void addPlayer(PlayerInfo player) {
        playerInfos.add(player);
        players.add(new PlayerLite(0, 0));
        playerIndicies.add(players.size() - 1);

        System.out.println("Player " + (players.size() - 1) + " added");

    }

    public synchronized void addPlayer(PlayerLite player) {
        players.add(new PlayerLite(0, 0));
        playerIndicies.add(players.size() - 1);
    }

    public PlayerLite getPlayer(int playerNum) {
        int index = playerIndicies.get(playerNum);
        return players.get(index);
    }

    public ArrayList<PlayerLite> getPlayers() {
        return players;
    }

    public ArrayList<Integer> getEliminatedPlayers() {
        return eliminatedPlayers;
    }

    public ArrayList<Integer> getPlayerIndicies() {
        return playerIndicies;
    }

    public ArrayList<PlayerLite> getOtherPlayers(int playerNum) {
        ArrayList<PlayerLite> otherPlayers = new ArrayList<PlayerLite>();
        int index = playerIndicies.get(playerNum);

        for(int i = 0; i < players.size(); i++) {
            if(i != index)
                otherPlayers.add(players.get(i));
        }

        return otherPlayers;
    }

    public ArrayList<PlayerInfo> getOtherPlayerInfos(int playerNum) {
        ArrayList<PlayerInfo> otherPlayers = new ArrayList<PlayerInfo>();
        int index = playerIndicies.get(playerNum);

        for(int i = 0; i < playerInfos.size(); i++) {
            if(i != index)
                otherPlayers.add(playerInfos.get(i));
        }

        return otherPlayers;
    }

    public boolean getGameStarted() {
        return gameStarted;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getPlayerHoldingBomb() {
        return playerHoldingBomb;
    }

    public short getGameLength() {
        return gameLength;
    }

    public synchronized int removePlayer(int playerNum) {
        int index = playerIndicies.get(playerNum);
        if(gameStarted)
            players.remove(index);
        playerInfos.remove(index);

        for(int i = index + 1; i < playerIndicies.size(); i++) {
            playerIndicies.set(i, playerIndicies.get(i) - 1);
        }

        if(eliminatedPlayers.contains(index))
            eliminatedPlayers.remove(eliminatedPlayers.indexOf(index));

        return index;
    }

    public void setPlayer(int playerNum, PlayerLite player) {
        int index = playerIndicies.get(playerNum);
        players.set(index, player);
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setPlayerHoldingBomb(int playerHoldingBomb) {
        this.playerHoldingBomb = playerHoldingBomb;
    }

    public void setGameLength(short gameLength) {
        this.gameLength = gameLength;
    }

    public boolean isEliminated(int playerNum) {
        int index = playerIndicies.get(playerNum);
        return eliminatedPlayers.contains(index);
    }

    public int getPlayerNumFromIndex(int index) {
        return playerIndicies.indexOf(index);
    }

    public void reset() {
        eliminatedPlayers.clear();
        // playerInfos.clear();
        // players.clear();

        playerHoldingBomb = -1;
        gameStarted = false;
        gameLength = -1;
        startTime = -1;
    }

    public void wipe() {
        System.out.println("Wiping players");

        eliminatedPlayers.clear();
        playerInfos.clear();
        players.clear();
        playerIndicies.clear();

        playerHoldingBomb = -1;
        gameStarted = false;
        gameLength = -1;
        startTime = -1;
    }
}
