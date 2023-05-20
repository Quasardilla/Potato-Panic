import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;

public class Sprite {
    protected int width, height;
    protected double x, y, dx = 0, dy = 0;
    protected double dtime = 1;
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

    public double getX() {
        return x;
    }

    public double getY() {
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

    public void setX(double x) {
        this.x = x;
        updateCenter();
    }

    public void setY(double y) {
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

    protected void updateCenter() {
        center = new Point((int) x + (width / 2), (int) y + (height / 2));
    }

    public void update(double deltaTime) {
        dtime = deltaTime;

        x += ((dx * dtime) % 1 > 0.4) ? Math.ceil(dx * dtime) : Math.floor(dx * dtime);
        y += ((dy * dtime) % 1 > 0.4) ? Math.ceil(dy * dtime) : Math.floor(dy * dtime);

        updateCenter();
    }

    public void draw(Graphics2D g2) {
        if(img != null)
            g2.drawImage(img.getImage(), (int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)), (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)), width, height, null);
        else
            g2.fillRect((int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)), (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)), width, height);

        
    }

}
