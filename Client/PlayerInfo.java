package Client;
import java.awt.Color;

import Appearance.Face;

public class PlayerInfo {
    protected Color color;
    protected String name;
    protected Face face;

    public PlayerInfo(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public PlayerInfo(Face face, String name, Color color) {
        this.face = face;
        this.name = name;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public Face getFace() {
        return face;
    }

    public void setFace(Face face) {
        this.face = face;
    }

}
