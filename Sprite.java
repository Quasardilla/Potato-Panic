import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;

public class Sprite {
    protected int x, y, width, height;
    protected double dx = 0, dy = 0;
    protected Point center;
    protected ImageIcon img;

    public Sprite() {
        this.x = 0;
        this.y = 0;
        this.width = 50;
        this.height = 50;
        this.img = null;

        updateCenter();
    }

    public Sprite(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.img = null;
        
        updateCenter();
    }

    public Sprite(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = null;

        updateCenter();
    }

    public Sprite(int x, int y, ImageIcon img) {
        this.x = x;
        this.y = y;
        this.width = img.getIconWidth();
        this.height = img.getIconHeight();
        this.img = img;

        updateCenter();
    }

    public Sprite(int x, int y, int width, int height, ImageIcon img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;

        updateCenter();
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Point getCenter() {
        return center;
    }

    public void setX(int x) {
        this.x = x;
        updateCenter();
    }

    public void setY(int y) {
        this.y = y;
        updateCenter();
    }
    
    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void updateCenter() {
        center = new Point(x + (width / 2), y + (height / 2));
    }

    public void update() {
        x += dx;
        y += dy;

        updateCenter();
    }

    public void draw(Graphics2D g2) {
        if(img != null)
            g2.drawImage(img.getImage(), x, y, width, height, null);
        else
            g2.fillRect(x, y, width, height);
        
    }

}
