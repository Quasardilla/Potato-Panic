package UI;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

public class Button extends UIElement{
    public double width;
    public double height;
    public double xOffset;
    public double yOffset;
    public double fontSize;
    public Font font;
    public String text;
    public Color textColor;
    public Color backColor;
    public ImageIcon img;

    public Button(int x, int y, int width, int height, int xOffset, int yOffset, int fontSize, Font font, String text, Color textColor, Color backColor)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.fontSize = fontSize;
        this.font = font;
        this.font = new Font(this.font.getFontName(), Font.PLAIN, (int) this.fontSize);
        this.text = text;
        this.textColor = textColor;
        this.backColor = backColor;
    }

    public Button(int x, int y, int width, int height, Color color)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.backColor = color;
    }

    public Button(int x, int y, int width, int height, ImageIcon img)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
        backColor = new Color(0, 0, 0, 0);
    }

    @Override
    public void drawElement() 
    {
        //draw background
        g2.setColor(backColor);
        g2.fillRect((int) x, (int) y, (int) width, (int) height);
        
        if(img != null) {
            g2.drawImage(img.getImage(), (int) x, (int) y, (int) width, (int) height, null);
        }

        //draw text
        if(text == null) return;
        g2.setFont(font);
        this.font = new Font(this.font.getFontName(), Font.PLAIN, (int) this.fontSize);
        g2.setColor(textColor);
        g2.drawString(text, (int) (x + xOffset), (int) (y + yOffset));
    }

    public boolean mouseClick(double mousex, double mousey)
    {
        if (mousex > x && mousex < x + width && mousey > y && mousey < y + height) return true;
        else return false;
    }

}
