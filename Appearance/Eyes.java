package Appearance;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

import Client.Sprite;

public class Eyes extends Sprite {

    public Eyes(Image eyes) {
        img = new ImageIcon(eyes);
    }

    public void draw(Graphics2D g2, int x, int y) {
        g2.drawImage(img.getImage(), x, y, null);
    }
    
    public void draw(Graphics2D g2, int x, int y, int w, int h) {
        g2.drawImage(img.getImage(), x, y, w, h, null);
    }
}
