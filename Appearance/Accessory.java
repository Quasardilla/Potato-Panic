package Appearance;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

import Client.Player;
import Client.Sprite;

public class Accessory extends Sprite {
    /*
     * The position of the accessory on the face,
     * relative to the face's position.
     * 0, 0 is the top left corner of the face.
     */
    int offsetX;
    int offsetY;

    public Accessory(Image accessory, int width, int height, int offsetX, int offsetY) {
        this.img = new ImageIcon(accessory.getScaledInstance(width, height, Image.SCALE_DEFAULT));
        this.height = height;
        this.width = width;
        this.offsetX = offsetX;   
        this.offsetY = offsetY;
    }

    /**
     * Draw the accessory on the face.
     * @param g2
     * The graphics object to draw the accessory on
     * @param x
     * The position on the screen of the player's x
     * @param y
     * The position on the screen of the player's y
     */
    public void draw(Graphics2D g2, int x, int y) {
        g2.drawImage(img.getImage(), x + offsetX, y + offsetY, width, height, null);
    }
}
