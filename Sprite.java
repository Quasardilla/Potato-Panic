import java.awt.Graphics2D;

import javax.swing.ImageIcon;

public class Sprite {
    protected int x, y, width, height;
    protected double dx = 0, dy = 0;
    protected ImageIcon img;

    public Sprite() {
        this.x = 0;
        this.y = 0;
        this.width = 50;
        this.height = 50;
        this.img = null;
    }

    public Sprite(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.img = null;
    }

    public Sprite(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = null;
    }

    public Sprite(int x, int y, ImageIcon img) {
        this.x = x;
        this.y = y;
        this.width = img.getIconWidth();
        this.height = img.getIconHeight();
        this.img = img;
    }

    public Sprite(int x, int y, int width, int height, ImageIcon img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics2D g2) {
        if(img != null)
            g2.drawImage(img.getImage(), x, y, width, height, null);
        else
            g2.fillRect(x, y, width, height);
        
    }

}
