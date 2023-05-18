import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class Platform extends Sprite{
    protected Point p1, p2;
    protected Rectangle platform;

    /**
     * 
     * @param p1
     *      Top left point
     * @param p2
     *      Bottom right point
     */
    public Platform(Point p1, Point p2) {
        super((int) p1.getX(), (int) p1.getY(), (int) (p2.getX() - p1.getX()), (int) (p2.getY() - p1.getY()));
        this.p1 = p1;
        this.p2 = p2;
        platform = new Rectangle((int) super.x, (int) super.y, super.width, super.height);
    }
    
    public Platform(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.p1 = new Point(x, y);
        this.p2 = new Point(x + width, y + height);
        platform = new Rectangle(x, y, width, height);
    }

    public boolean intersects(Sprite other) {
        Rectangle otherRect = new Rectangle((int) other.x, (int) other.y, other.getWidth(), other.getHeight());

        return platform.intersects(otherRect);
    }

    public int intersectionSide(Sprite other) {
        double otherY = -1 * (other.center.getY() - center.getY());
        double otherX = -1 * (other.center.getX() - center.getX());
        double topLeftSlope = (y - center.getY()) / (x - center.getX());
        double topRightSlope = (y - center.getY()) / ((x + width) - center.getX());
        
        if(otherY > topLeftSlope * otherX) {
            if(otherY > topRightSlope * otherX)
                return 1; //top
            else
                return 2; //left
        }
        else {
            if(otherY > topRightSlope * otherX)
                return 3; //right
            else
                return 4; //bottom
        }
    }

    public void update(double deltaTime) {
        super.dtime = deltaTime;

        x += ((dx * dtime) % 1 > 0.4) ? Math.ceil(dx) : Math.floor(dx);
        y += ((dy * dtime) % 1 > 0.4) ? Math.ceil(dy) : Math.floor(dy);

        System.out.println(x);


        platform = new Rectangle((int) super.x, (int) super.y, super.width, super.height);

        super.updateCenter();
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect((int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)), (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)), width, height);
    }

}
