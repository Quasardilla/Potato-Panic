import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PlayerList implements Serializable{
    private static final long serialVersionUID = 1L;

    protected ArrayList<PlayerLite> players = new ArrayList<PlayerLite>();
    protected ArrayList<Integer> playerIndicies = new ArrayList<Integer>();

    public PlayerList() {}

    public synchronized void addPlayer(PlayerLite player) {
        players.add(player);
        playerIndicies.add(players.size() - 1);
    }

    public PlayerLite getPlayer(int playerNum) {
        int index = playerIndicies.get(playerNum);
        System.out.println(index);
        return players.get(index);
    }

    public ArrayList<PlayerLite> getPlayers() {
        return players;
    }

    public PlayerList getOtherPlayers(int playerNum) {
        PlayerList otherPlayers = new PlayerList();
        int index = playerIndicies.get(playerNum);

        for(int i = 0; i < players.size(); i++) {
            if(i != index)
                otherPlayers.addPlayer(players.get(i));
        }

        return otherPlayers;
    }

    public synchronized void removePlayer(int playerNum) {
        int index = playerIndicies.get(playerNum);
        players.remove(index);
        for(int i = index + 1; i < playerIndicies.size(); i++) {
            playerIndicies.set(i, playerIndicies.get(i) - 1);
        }

    }

    public void setPlayer(int playerNum, PlayerLite player) {
        int index = playerIndicies.get(playerNum);
        players.set(index, player);
    }
}
