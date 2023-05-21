import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Player extends Sprite {

    private double gravity = 2800, velocity = -1400;
    private int jumpCount = 0;
    protected boolean isJumping = true, isGrounded, wallSliding;

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
            if(p.intersects(new Player((int) (x), (int) (y + height + 5), width, height)))
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
                        // System.out.println("Error: Invalid side (dear god)");
                        dy = 0;
                        adjustPlatforms(platforms, 0, y - (p.getY() - height));
                        break;
                }
            }
        }

        if(count == 0) {
            if (findFloorCollision(platforms))
                isGrounded = true;
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
