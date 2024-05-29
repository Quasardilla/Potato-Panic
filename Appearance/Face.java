package Appearance;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class Face {
    private Eyes eyes;
    private int eyesID;
    private Mouth mouth;
    private int mouthID;
    private ArrayList<Accessory> accessories;

    public Face(Eyes eyes, Mouth mouth, int eyesID, int mouthID) {
        this.eyes = eyes;
        this.eyesID = eyesID;
        this.mouth = mouth;
        this.mouthID = mouthID;
        this.accessories = new ArrayList<Accessory>();
    }

    public void addAccessory(Accessory accessory) {
        accessories.add(accessory);
    }

    public void removeAccessory(Accessory accessory) {
        accessories.remove(accessory);
    }

    public void draw(Graphics2D g2, int x, int y) {
        eyes.draw(g2, x, y);
        mouth.draw(g2, x, y);

        for(Accessory accessory : accessories) {
            accessory.draw(g2, x, y);
        }
    }

    public void draw(Graphics2D g2, int x, int y, int width, int height) {
        eyes.draw(g2, x, y, width, height);
        mouth.draw(g2, x, y, width, height);

        for(Accessory accessory : accessories) {
            accessory.draw(g2, x, y);
        }
    }

    public int getEyesID() {
        return eyesID;
    }

    public int getMouthID() {
        return mouthID;
    }
}
