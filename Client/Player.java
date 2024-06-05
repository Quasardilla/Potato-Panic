package Client;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import Appearance.Face;
import Server.PlayerLite;

public class Player extends Sprite {

    private double gravity = 3400, velocity = -1800;
    private int jumpCount = 0;
    protected boolean isJumping = true, isGrounded, wallSliding;
    private Font font = new Font("Mochiy Pop P One", Font.PLAIN, 24);

    public Player() {
        super();
    }

    public Player(int x, int y) {
        super(x, y);
    }

    public Player(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Player(int x, int y, ImageIcon img) {
        super(x, y, img);
    }

    public Player(int x, int y, int width, int height, ImageIcon img) {
        super(x, y, width, height, img);
    }

    public void update(double deltaTime) {
        dtime = deltaTime;

        if(wallSliding) {
            if(dy < 190)
                dy += ((gravity * dtime * 2) % 1 > 0.4) ? Math.ceil(gravity * dtime) : Math.floor(gravity * dtime);
            else if(dy > 190)
                dy -= ((gravity * dtime * 2) % 1 > 0.4) ? Math.ceil(gravity * dtime) : Math.floor(gravity * dtime);
        }
        else if(!isGrounded) {
            dy += ((gravity * dtime) % 1 > 0.4) ? Math.ceil(gravity * dtime) : Math.floor(gravity * dtime);
        }

        super.updateCenter();
    }

    public void draw(Graphics2D g2, PlayerInfo info, boolean eliminated) {
        g2.setFont(font);
        int margin = 3;
        FontMetrics metrics = g2.getFontMetrics();
    
        if(eliminated) {
            //Semi-transparent background for name
            g2.setColor(new Color(0, 0, 0, 75));
            g2.fillRect((int) (x + width / 2) - (metrics.stringWidth(info.getName()) / 2) - 5, (int) y - metrics.getHeight() - 22, metrics.stringWidth(info.getName()) + 10, metrics.getHeight() + 4);
            
            //Name
            g2.setColor(Color.WHITE);
            g2.drawString(info.getName(), (int) (x + width / 2) - metrics.stringWidth(info.getName()) / 2, (int) y - 30);
            
            //Player
            g2.setColor(new Color(info.getColor().getRed(), info.getColor().getGreen(), info.getColor().getBlue(), 100));
            g2.fillRect((int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)), (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)), width, height);
        }
        else {
            //Semi-transparent background for name
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect((int) (x + width / 2) - (metrics.stringWidth(info.getName()) / 2) - 5, (int) y - metrics.getHeight() - 10, metrics.stringWidth(info.getName()) + 10, metrics.getHeight() + 4);
            
            //Name
            g2.setColor(Color.WHITE);
            g2.drawString(info.getName(), (int) (x + width / 2) - metrics.stringWidth(info.getName()) / 2, (int) y - 16);
            
            //Player Outline
            g2.setColor(Color.BLACK);
            g2.fillRect((int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)) - margin, (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)) - margin, width + margin * 2, height + margin * 2);

            //Player
            g2.setColor(info.getColor());
            g2.fillRect((int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)), (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)), width, height);
        }

        info.getFace().draw(g2, (int)((x % 1 > 0.4) ? Math.ceil(x) : Math.floor(x)), (int) ((y % 1 > 0.4) ? Math.ceil(y) : Math.floor(y)), width, height);
    }

    /**
     * 
     * @param g2
     * Graphics2D object
     * @param info
     * PlayerInfo used for color and name
     * @param font
     * Font & font size used to draw name
     * @param face
     * Eyes & Mouth for player
     * @param x
     * Centered on X
     * @param margin
     * The thickness of the outline
     */
    public static void drawScaled(Graphics2D g2, PlayerInfo info, Font font, int x, int y, int width, int height, int margin) {
        String str = info.getName();
        g2.setColor(Color.BLACK);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();

        //Name
        g2.drawString(str, x - (metrics.stringWidth(str) / 2), y - 40);

        //Player Outline
        g2.fillRect(x - (width / 2) - margin, y - margin, width + (margin * 2), height + (margin * 2));

        //Player
        g2.setColor(info.getColor());
        g2.fillRect(x - (width / 2), y, width, height);

        //Face
        info.getFace().draw(g2, (int) x - (width / 2), (int) y, width, height);
    }

    public void jump() {
        if(jumpCount > 1)
            return;
        
        jumpCount++;
        super.dy = velocity;
        isJumping = true;
        isGrounded = false;
    }

    public PlayerLite genPlayerLite(Platform p) {
        return new PlayerLite((int) (x - p.getX()), (int) (y - p.getY()));
    }

    public boolean findFloorCollision(ArrayList<Platform> platforms) {
        for(Platform p : platforms) {
            if(p.intersects(new Player((int) (x), (int) (y), width, height)))
                return true;
        }

        return false;
    }

    public boolean findWallCollision(ArrayList<Platform> platforms) {
        if(dx == 0)
            return false;
        

        for(Platform p : platforms) {
            if(p.intersects(new Player((int) (x + 5), (int) (y), width, height)))
                return true;
        }

        return false;
    }

    public void checkCollisions(ArrayList<Platform> platforms) {
        int count = 0;

        for(Platform p : platforms) {
            if(p.intersects(this)) {
                int side = p.intersectionSide(this);
                count++;

                switch (side) {
                    case 1: //top
                        // System.out.println("hit top of other sprite");
                        isJumping = false;
                        isGrounded = true;
                        jumpCount = 0;
                        dy = 0;
                        adjustPlatforms(platforms, 0, y - (p.getY() - height));
                        break;
                    case 2: //left
                        // System.out.println("hit left of other sprite"); 
                        dx = 0;
                        jumpCount = 1;
                        wallSliding = true;
                        adjustPlatforms(platforms, x - (p.getX() + p.getWidth()), 0);
                        break;
                    case 3: //right
                        // System.out.println("hit right of other sprite");
                        dx = 0;
                        wallSliding = true;
                        jumpCount = 1;
                        adjustPlatforms(platforms, x - (p.getX() - width), 0);
                        break;
                    case 4: //bottom
                        // System.out.println("hit bottom of other sprite");
                        dy = 0;
                        adjustPlatforms(platforms, 0, y - (p.getY() + p.getHeight()));
                        break;
                    default:
                        System.out.println("Error: Invalid side (dear god)");
                        dy = 0;
                        adjustPlatforms(platforms, 0, y - (p.getY() - height));
                        break;
                }
            }
        }

        if(count == 0) {
            if (findFloorCollision(platforms)) {
                isGrounded = true;
                System.out.println("found floor collision");
            }
            else {
                wallSliding = false;
                isGrounded = false;
            }
        }
        
    }

    private void adjustPlatforms(ArrayList<Platform> platforms, double xAdjustment, double yAdjustment) {
        for(Platform p : platforms) {
            p.setX(p.getX() + xAdjustment);
            p.setY(p.getY() + yAdjustment);
        }
    }
    
}
