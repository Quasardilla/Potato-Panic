package Appearance;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

import Client.Sprite;

public class Mouth extends Sprite {
    Image scaledImage;

    public Mouth(Image eyes) {
        img = new ImageIcon(eyes);
        scaledImage = img.getImage().getScaledInstance(500, 500, Image.SCALE_AREA_AVERAGING);
    }

    public void draw(Graphics2D g2, int x, int y) {
        g2.drawImage(img.getImage(), x, y, null);
    }

    public void draw(Graphics2D g2, int x, int y, int w, int h) {
        // Image image = this.img.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT);
        g2.drawImage(scaledImage, x, y, w, h, null);
        // g2.drawImage(img.getImage(), x, y, w, h, null);
    }
}
