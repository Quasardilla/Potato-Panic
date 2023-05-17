import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Player extends Sprite {

    private double gravity = 0.0006, velocity = -8;
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
            super.dy -= gravity;
        }

        super.x += super.dx;
        super.y += super.dy;
        System.out.println(super.y);
        System.out.println(super.dy);
    }

    public void jump() {
        System.out.println("Jumping!");

        super.dy = velocity;
        isJumping = true;
        isGrounded = false;
    }

    public void checkCollisions(ArrayList<Platform> platforms) {
        for(Platform p : platforms) {
            if(p.intersects(new Rectangle((int) (super.x + super.dx), (int) (super.y + super.dy), super.width, super.height))) {
                isJumping = false;
                isGrounded = true;
                super.dy = 0;
            }

        }
        
    }
    
}
