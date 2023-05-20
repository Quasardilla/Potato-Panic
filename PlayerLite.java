import java.awt.Graphics2D;
import java.io.Serializable;

public class PlayerLite implements Serializable{
    private static final long serialVersionUID = 1L;

    protected int x;
    protected int y;


    public PlayerLite(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2, Platform p) {
        System.out.println(this);

        g2.setColor(java.awt.Color.RED);
        // g2.fillRect(x, y, 50, 50);
        g2.fillRect((int) p.getX() + x, (int) p.getY() + y, 50, 50);
    }

    @Override
    public String toString() {
        return "PlayerLite [x=" + x + ", y=" + y + "]";
    }
}
