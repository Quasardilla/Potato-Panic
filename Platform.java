import java.awt.Point;
import java.awt.Rectangle;

public class Platform {
    protected Point p1, p2;
    protected int x, y, width, height;
    protected Rectangle platform;

    /**
     * 
     * @param p1
     *      Top left point
     * @param p2
     *      Bottom right point
     */
    public Platform(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.x = (int) p1.getX();
        this.y = (int) p1.getY();
        this.width = (int) (p2.getX() - p1.getX());
        this.height = (int) (p2.getY() - p1.getY());
        platform = new Rectangle(x, y, width, height);
    }

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width; 
        this.height = height;
        this.p1 = new Point(x, y);
        this.p2 = new Point(x + width, y + height);
        platform = new Rectangle(x, y, width, height);
    }

    public boolean intersects(Rectangle other) {
        return platform.intersects(other);
    }




}
