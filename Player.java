import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Player extends Sprite {

    private double gravity = 0.0028, velocity = -1;
    private boolean left, right;
    private int[] leftKeys;
    private int[] rightKeys;
    protected boolean isJumping, isGrounded = true;

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

    public void update() {
        if(isJumping) {
            super.dy += gravity;
        }

        super.x += (super.dx % 1 > 0.4) ? Math.ceil(super.dx) : Math.floor(super.dx);
        super.y += (super.dy % 1 > 0.4) ? Math.ceil(super.dy) : Math.floor(super.dy);
    }

    public void jump() {
        // if(!isGrounded)
        //     return;
        super.dy = velocity;
        isJumping = true;
        isGrounded = false;
    }

    public void keyPressed(int key) {
        for(int i : leftKeys) {
            if(key == i) {
                left = true;
                super.dx = -1;
            }
        }
    }

    public void checkCollisions(ArrayList<Platform> platforms) {
        for(Platform p : platforms) {
            if(p.intersects(this)) {
                int side = p.intersectionSide(this);
                System.out.println(side);

                switch (side) {
                    case 1: //top
                        System.out.println("hit top of other sprite");
                        isJumping = false;
                        isGrounded = true;
                        super.dy = 0;
                        super.y = p.getY() - super.height;
                        break;
                    case 2: //left
                        System.out.println("hit left of other sprite");
                        super.dx = 0;
                        super.x = p.getX() + p.getWidth();
                        break;
                    case 3: //right
                        System.out.println("hit right of other sprite");
                        super.dx = 0;
                        super.x = p.getX() - super.width;
                        break;
                    case 4: //bottom
                        System.out.println("hit bottom of other sprite");
                        isJumping = true;
                        isGrounded = false;
                        super.dy = 0;
                        super.y = p.getY() + p.getHeight();
                        break;
                    default:
                        System.out.println("Error: Invalid side (dear god)");
                        super.dy = 0;
                        super.y = p.getY() - super.height;
                        break;
                }
            }

        }
        
    }
    
}
