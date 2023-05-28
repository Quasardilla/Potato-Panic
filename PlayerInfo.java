import java.awt.Color;

public class PlayerInfo {
    protected Color color;
    protected String name;

    public PlayerInfo(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

}
