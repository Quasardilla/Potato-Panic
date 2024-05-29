package Particle;

import java.awt.Graphics2D;

/**
 * Particle
 */
public abstract class Particle {
    private int x;
    private int y;

    public Particle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void draw(Graphics2D g2);
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Particle [x=" + x + ", y=" + y + "]";
    }
}