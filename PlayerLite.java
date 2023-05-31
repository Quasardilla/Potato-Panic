import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.Serializable;

public class PlayerLite implements Serializable{
    private static final long serialVersionUID = 1L;
    private Font font = new Font("Mochiy Pop P One", Font.PLAIN, 24);

    protected int x;
    protected int y;


    public PlayerLite(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2, Platform p, PlayerInfo info, boolean ghost) {
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
    
        int localizedX = (int) (x + p.getX());
        int localizedY = (int) (y + p.getY());

        //Semi-transparent background for name
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect((int) (localizedX + 50 / 2) - (metrics.stringWidth(info.getName()) / 2) - 5, (int) localizedY - metrics.getHeight() - 2, 
        metrics.stringWidth(info.getName()) + 10, metrics.getHeight() + 4);
        
        //Name
        g2.setColor(Color.WHITE);
        g2.drawString(info.getName(), (int) (localizedX + 50 / 2) - metrics.stringWidth(info.getName()) / 2, (int) localizedY - 10);
        
        //Player
        if(ghost)
            g2.setColor(new Color(info.getColor().getRed(), info.getColor().getGreen(), info.getColor().getBlue(), 100));
        else
            g2.setColor(info.getColor());
        g2.fillRect(localizedX, localizedY, 50, 50);
    }

    @Override
    public String toString() {
        return "PlayerLite [x=" + x + ", y=" + y + "]";
    }
}
