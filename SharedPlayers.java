import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.awt.Color;

public class SharedPlayers implements Serializable{
    private static final long serialVersionUID = 1L;

    protected ArrayList<PlayerLite> players = new ArrayList<PlayerLite>();
    protected ArrayList<PlayerInfo> playerInfos = new ArrayList<PlayerInfo>();
    protected ArrayList<Integer> playerIndicies = new ArrayList<Integer>();
    protected ArrayList<Integer> eliminatedPlayers = new ArrayList<Integer>();
    protected int playerHoldingBomb = -1;
    protected boolean gameStarted = false;
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
            if(i != index && !eliminatedPlayers.contains(i))
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

    public synchronized int removePlayer(int playerNum) {
        int index = playerIndicies.get(playerNum);
        players.remove(index);
        playerInfos.remove(index);

        for(int i = index + 1; i < playerIndicies.size(); i++) {
            playerIndicies.set(i, playerIndicies.get(i) - 1);
        }

        if(eliminatedPlayers.contains(index))
            eliminatedPlayers.remove(eliminatedPlayers.indexOf(index));

        return index;
    }

    public synchronized void eliminatePlayer(int index) {
        eliminatedPlayers.add(index);
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

    public boolean isEliminated(int playerNum) {
        int index = playerIndicies.get(playerNum);
        return eliminatedPlayers.contains(index);
    }
}
