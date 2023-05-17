package ElevensLab.Thomas_Final;

public class Player extends Sprite {

    private double gravity = 0.06, velocity = 4;
    protected boolean isJumping, isGrounded = true;
    protected int x, y;
    protected double dx, dy;

    public Player(int x, int y) {
        super(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void jump() {
        dy = velocity;
        dy -= gravity;
        isJumping = true;
        isGrounded = false;
    }

    public void update() {
        x += dx;
        y += dy;
    }
    
}
