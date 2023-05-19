import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.awt.event.MouseListener;

public class Platformer extends JPanel implements KeyListener, MouseMotionListener, MouseListener
{
    private static final long serialVersionUID = 1L;
    private static final int PREF_W = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int PREF_H = Toolkit.getDefaultToolkit().getScreenSize().height;
    private RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    private static int FPSCap = 60;
    // private static int FPSCap = 120;
    // private static int FPSCap = 5;
    private static boolean unlimited = false;
    private static double totalFrames = 0;
    private static double lastFPSCheck = 0;
    private static double currentFPS = 0;

    private double sidewaysVelocity = 10;

    private boolean left, right;
    private int[] leftKeys = new int[] {KeyEvent.VK_LEFT, KeyEvent.VK_A};
    private int[] rightKeys = new int[] {KeyEvent.VK_RIGHT, KeyEvent.VK_D};

    private ArrayList<Platform> platforms = new ArrayList<Platform>();

    private Player playerOne = new Player(PREF_W / 2, PREF_H - 70);


    public Platformer()
    {
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        setFocusable(true);
        requestFocus();

        platforms.add(new Platform(0, PREF_H - 10, PREF_W, 200));
        // platforms.add(new Platform(PREF_W / 2, PREF_H - 300, PREF_W, 75));
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        //keep this for program to work
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(hints);

        
        double dtime = 1/currentFPS;
        if(dtime >= 1 || dtime < 0)
        dtime = 1/FPSCap;
        
        for(Platform p : platforms)
            p.update(dtime);
        playerOne.update(dtime);

        managePlatformSpeed(platforms);
        playerOne.checkCollisions(platforms);
        
        for(Platform p : platforms)
            p.draw(g2);    
        playerOne.draw(g2);


        //keep this for program to work
        if (!unlimited)
        {
            totalFrames++;
            if (System.nanoTime() > lastFPSCheck + 1000000000)
            {
                lastFPSCheck = System.nanoTime();
                currentFPS = totalFrames;
                totalFrames = 0;
            }

            long millis = System.currentTimeMillis();
            try
            {
            Thread.sleep((long) ((1000/FPSCap) - millis % (1000/FPSCap)));
            this.repaint();
            return;
            } catch (InterruptedException e) {System.out.println(e);}
        }
        else
        {
            totalFrames++;
            if (System.nanoTime() > lastFPSCheck + 1000000000)
            {
                lastFPSCheck = System.nanoTime();
                currentFPS = totalFrames;
                totalFrames = 0;
            }
            this.repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_UP)
            playerOne.jump();
        for(int i : leftKeys)
            if(e.getKeyCode() == i)
                left = true;
        for(int i : rightKeys)
            if(e.getKeyCode() == i)
                right = true;

    }

    @Override
    public void keyReleased(KeyEvent e){
        for(int i : leftKeys)
            if(e.getKeyCode() == i)
                left = false;
        for(int i : rightKeys)
            if(e.getKeyCode() == i)
                right = false;
    }

    @Override
    public void keyTyped(KeyEvent e){}

    private static void createAndShowGUI() {
        Platformer gamePanel = new Platformer();
        JFrame frame = new JFrame("My Frame");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(gamePanel);
        frame.setUndecorated(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.WHITE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setVisible(true);
        System.out.println(frame.getInsets());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public void managePlatformSpeed(ArrayList<Platform> platforms) {
        
        for(Platform p : platforms) {
            p.setDx(-1 * playerOne.getDy());

            if(left)
                p.setDx(sidewaysVelocity);

            if(right)
                p.setDx(-sidewaysVelocity);
            
            if(!left && !right)
                p.setDx(0);
        }
    }
}