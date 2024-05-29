package Particle;

import java.awt.Color;
import java.awt.Graphics2D;

public class SquareParticle extends Particle {
    private int width;
    private int height;
    private Color color;
    /** In degrees */
    private int rotation;
    private int rotationSpeed;

    public SquareParticle(int x, int y, int width, int height, Color color, int rotation) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.color = color;
        this.rotation = rotation;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(color);
        rotation += rotationSpeed;
        g2.rotate(Math.toRadians(rotation), getX() + (width / 2), getY() + (height / 2));
        g2.drawRect(getY(), getX(), width, height);
        g2.rotate(-Math.toRadians(rotation), getX() + (width / 2), getY() + (height / 2));
    }

    @Override
    public String toString() {
        return "SquareParticle [width=" + width + ", height=" + height + ", color=" + color + ", rotation=" + rotation + "]";
    }
    
}
