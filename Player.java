import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Player extends Sprite {

    private double gravity = 800, velocity = -800;
    protected boolean isJumping = true, isGrounded;

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
        super.dtime = deltaTime;
        if(!isGrounded) {
            super.dy += gravity * dtime;
        }

        x += dx * deltaTime;  
        y += dy * deltaTime;

        super.updateCenter();
    }

    public void jump() {
        // if(!isGrounded)
        //     return;
        super.dy = velocity;
        isJumping = true;
        isGrounded = false;
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
                        dy = 0;
                        y = p.getY() - height;
                        break;
                    case 2: //left
                        // System.out.println("hit left of other sprite");
                        dx = 0;
                        x = p.getX() + p.getWidth();
                        break;
                    case 3: //right
                        // System.out.println("hit right of other sprite");
                        dx = 0;
                        x = p.getX() - width;
                        break;
                    case 4: //bottom
                        // System.out.println("hit bottom of other sprite");
                        dy = 0;
                        y = p.getY() + p.getHeight();
                        break;
                    default:
                        System.out.println("Error: Invalid side (dear god)");
                        dy = 0;
                        y = p.getY() - height;
                        break;
                }
            }
        }

        if(count == 0) {
            isGrounded = false;
        }
        
    }
    
}
