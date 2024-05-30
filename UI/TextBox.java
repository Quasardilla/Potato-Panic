package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TextBox extends UIElement implements KeyListener
{
    public int width, height;
    public Color backgroundColor, fontColor, defaultTextColor;
    public Font font;
    public String defaultText;
    public boolean isSelected;
    public String text;
    public int cursorPos;
    
    public TextBox(double x, double y, int width, int height, Color backgroundColor, Font font, Color fontColor, String defaultText, Color defaultTextColor)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.backgroundColor = backgroundColor;
        this.font = font;
        this.fontColor = fontColor;
        this.defaultText = defaultText;
        this.defaultTextColor = defaultTextColor;
    }

    @Override
    public void drawElement() 
    {
        //draw background
        g2.setColor(backgroundColor);
        g2.fillRect((int) x, (int) y, width, height);
        
        //draw text
        g2.setColor(defaultTextColor);
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();

        if(isSelected) {
            int distToCursor = metrics.stringWidth(text.substring(0, cursorPos));
            g2.setStroke(new BasicStroke(3));
            g2.drawLine((int) x + distToCursor + 5, (int) y + 5, (int) x + distToCursor + 5, (int) y + height - 5);
        }
        
        if(text == null || text.equals(""))
            g2.drawString(defaultText, (int) x + 5, (int) y + (height / 2) + (metrics.getHeight() / 2) - 5);
        else {
            g2.setColor(fontColor);
            g2.drawString(text, (int) x + 5, (int) y + (height / 2) + (metrics.getHeight() / 2) - 5);
        }
        
    }
    
    public void mouseClick(double mousex, double mousey)
    {
        if (mousex > x && mousex < x + width && mousey > y && mousey < y + height) {
            isSelected = true;
            cursorPos = text.length();
        }
        else 
            isSelected = false;
    }

    @Override
    public void keyTyped(KeyEvent e) 
    {        
        if((e.getKeyCode() != KeyEvent.VK_RIGHT && e.getKeyCode() != KeyEvent.VK_LEFT && e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN)  
        && (e.isActionKey() || e.getKeyChar() == KeyEvent.CHAR_UNDEFINED))
            return;


        if(isSelected) {
            if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                //Split the if statement so it would absorb backspace and delete, even if the text is empty
                if(text.length() > 0) {
                    text = text.substring(0, cursorPos - 1) + text.substring(cursorPos);
                    cursorPos--;
                }
            }
            else if(e.getKeyCode() == KeyEvent.VK_ENTER)
                isSelected = false;
            else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                if(cursorPos > 0)
                    cursorPos--;
            }
            else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                if(cursorPos < text.length())
                    cursorPos++;
            }
            else if(e.getKeyCode() == KeyEvent.VK_UP) {
                //Move the cursor to the beginning of the text
                cursorPos = 0;
            }
            else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                //Move the cursor to the end of the text
                cursorPos = text.length();
            }
            else {
                text = text.substring(0, cursorPos) + e.getKeyChar() + text.substring(cursorPos);
                cursorPos++;
            }
        }

    }

    @Override
    public void keyPressed(KeyEvent e) 
    {
        
    }

    @Override
    public void keyReleased(KeyEvent e) 
    {
        
    }
}
