
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.FontMetrics;

import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Appearance.Accessory;
import Appearance.Eyes;
import Appearance.Face;
import Appearance.Mouth;
import Client.*;
import Server.PlayerLite;
import UI.Button;
import UI.TextBox;
import Font.EasyFontInstaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Platformer extends JPanel implements KeyListener, MouseMotionListener, MouseListener
{
    private static final String version = "0.5.25";

    //Gets width & height of screen (which is hopefully 1080p)
    private static final int PREF_W = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int PREF_H = Toolkit.getDefaultToolkit().getScreenSize().height;
    private RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    private static int FPSCap = 80;
    private static boolean unlimited = false;
    private static double totalFrames = 0;
    private static double lastFPSCheck = 0;
    private static double currentFPS = 0;

    private final double PLAYER_VEL = 1300;
    private final double GHOST_VEL = 1900;
    private double sidewaysVelocity = PLAYER_VEL;

    private boolean left, right;
    private int[] leftKeys = {KeyEvent.VK_LEFT, KeyEvent.VK_A};
    private int[] rightKeys = {KeyEvent.VK_RIGHT, KeyEvent.VK_D};
    private int[] jumpKeys = {KeyEvent.VK_UP, KeyEvent.VK_W};

    private Font giantFont = new Font("Mochiy Pop P One", Font.PLAIN, 48);
    private Font largeFont = new Font("Mochiy Pop P One", Font.PLAIN, 32);
    private Font mediumFont = new Font("Mochiy Pop P One", Font.PLAIN, 24);
    private Font smallFont = new Font("Mochiy Pop P One", Font.PLAIN, 12);
    private FontMetrics metrics;

    private Image banner = new ImageIcon("./lib/img/PotatoPanicBanner.png").getImage().getScaledInstance(1920 / 2, 612 / 2, Image.SCALE_SMOOTH);
    private Image settings = new ImageIcon("./lib/img/Settings.png").getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
    private Image rightArrow = new ImageIcon("./lib/img/ArrowRight.png").getImage();
    private Image leftArrow = new ImageIcon("./lib/img/ArrowLeft.png").getImage();


    private ArrayList<Platform> platforms = new ArrayList<Platform>();
    private final double VERTICAL_BOUND_MAX = 4000;
    private final double VERTICAL_BOUND_MIN = -2000;
    private final double HORIZONTAL_BOUND_MAX = 3500;
    private final double HORIZONTAL_BOUND_MIN = -3500;

    private boolean titleScreen = true, showSettings, editPlayer, practicing, 
    serverList, addServer, editServer,
    connecting, connectionError, threadStarted, connThreadStarted;

    private Button playButton, practiceButton, customizeButton, customizeDoneButton,
    prevEyes, nextEyes, nextMouth, prevMouth, 
    settingsButton, doneButton, settingsDoneButton, backButton, startButton;
    private TextBox serverNameBox, serverIPBox, usernameBox, colorBox;
    private ArrayList<ServerObject> servers;
    private ArrayList<Button> serverButtons;
    private ArrayList<Button> serverOptionButtons = new ArrayList<Button>();
    private int selectedServer = -1;

    private Player player = new Player(PREF_W / 2, PREF_H / 2);
    private PlayerInfo playerInfo;
    private Face face;
    private int eyesID;
    private int mouthID;
    private ArrayList<Eyes> eyes;
    private ArrayList<Mouth> mouths;
    private ArrayList<Accessory> accessories;
    private ArrayList<PlayerLite> otherPlayers = new ArrayList<PlayerLite>();
    private ArrayList<PlayerInfo> otherPlayerInfos = new ArrayList<PlayerInfo>();
    private Client client;
    private ServerHandler t;
    private IOException connErr;
    private String errString;

    public Platformer()
    {
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        setFocusable(true);
        requestFocus();
        
        resetPlatforms();
        EasyFontInstaller.installFont("MochiyPopPOne-Regular.ttf");

        metrics = getFontMetrics(largeFont);
        int buttonWidth = 400;
        int buttonHeight = 100;
        int buttonY = PREF_H - 50 - buttonHeight;
        int spacing = (PREF_W - 100 - (4 * buttonWidth)) / 3;

        /*
         * Server buttons: add, edit, remove, connect
         */
        String str = "Add New";
        serverOptionButtons.add(new Button(50, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        str = "Edit";
        serverOptionButtons.add(new Button(50 + spacing + buttonWidth, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        str = "Remove";
        serverOptionButtons.add(new Button(50 + (spacing + buttonWidth) * 2, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        str = "Connect";
        serverOptionButtons.add(new Button(50 + (spacing + buttonWidth) * 3, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        
        int buttonX = (PREF_W / 2) - buttonWidth / 2;
        
        /*
         * Text boxes: server name, server ip, username, color
         */
        serverNameBox = new TextBox(buttonX, 200, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "New Server", Color.LIGHT_GRAY);
        serverIPBox = new TextBox(buttonX, 300, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "127.0.0.1:5100", Color.LIGHT_GRAY);
        usernameBox = new TextBox(buttonX, 550, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "Player", Color.LIGHT_GRAY);
        colorBox = new TextBox(buttonX, 625, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "#ababab", Color.LIGHT_GRAY);
        
        str = "Back";
        backButton = new Button(buttonX, PREF_H - 400, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK);
        str = "Start";
        startButton = new Button(buttonX, PREF_H - 200, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK);
        
        metrics = getFontMetrics(giantFont);

        str = "Done";
        settingsDoneButton = new Button(buttonX, PREF_H - 250, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        doneButton = new Button(buttonX, PREF_H - 400, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        customizeDoneButton = new Button(buttonX, 840, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);

        /*
         * Home Screen Buttons: Play, Practice, Settings
         */
        str = "Play";
        playButton = new Button(buttonX, 600, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        str = "Practice";
        practiceButton = new Button(buttonX, 720, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        str = "Edit Player";
        customizeButton = new Button(buttonX, 840, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        str = "";
        prevEyes = new Button((PREF_W / 32) * 22, 400, 50, 100, new ImageIcon(leftArrow));
        nextEyes = new Button((PREF_W / 32) * 28, 400, 50, 100, new ImageIcon(rightArrow));
        prevMouth = new Button((PREF_W / 32) * 22, 600, 50, 100, new ImageIcon(leftArrow));
        nextMouth = new Button((PREF_W / 32) * 28, 600, 50, 100, new ImageIcon(rightArrow));

        str = "";
        buttonWidth = 75;
        settingsButton = new Button(PREF_W - 75 - 10, 10, buttonWidth, buttonWidth, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);

        mouths = readMouths();
        eyes = readEyes();
        playerInfo = readPlayerFile();
        servers = readServerFile();

        serverButtons = generateServerButtons(50, 150, PREF_W - 100, 75, servers);
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(hints);
        metrics = g2.getFontMetrics();
        setGraphics(g2);
        
        if(t != null && t.isAlive())
            threadStarted = true;
        else
            threadStarted = false;
                
        if(t != null && !t.getDisconnectedMessage().equals("")) {
            connectionError = true;
            errString = t.getDisconnectedMessage();
            t.resetDisconnectedMessage();
        }

        g2.setColor(new Color(255, 240, 201));
        g2.fillRect(0, 0, PREF_W, PREF_H);
        
        if(titleScreen) {
            g2.setColor(Color.BLACK);
            metrics = g2.getFontMetrics();
            
            g2.drawImage(banner, (PREF_W / 2) - (banner.getWidth(null) / 2), -30, null);
            Player.drawScaled(g2, playerInfo, largeFont, (PREF_W / 2), 280, 250, 250, 5);

            g2.setColor(Color.BLACK);
            g2.setFont(mediumFont);
            g2.drawString(version, 10, PREF_H - 40);

            g2.setFont(giantFont);

            playButton.draw();
            practiceButton.draw();
            customizeButton.draw();

            if(!showSettings)
                g2.drawImage(settings, (int) settingsButton.x, (int) settingsButton.y, null);
        }

        if(practicing) {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, PREF_W, PREF_H);

            double dtime = 1/currentFPS;
            if(dtime >= 1 || dtime < 0)
            dtime = 1/FPSCap;
            
            managePlayerHorizontalSpeed(player);
            player.update(dtime);
            
            managePlatformSpeed(platforms);
            manageMapBounds(player);
            for(Platform p : platforms)
                p.update(dtime);
            
            player.checkCollisions(platforms);
            
            for(Platform p : platforms)
                p.draw(g2);    
            player.draw(g2, playerInfo, false, false);
        }

        if(editPlayer) {
            Player.drawScaled(g2, playerInfo, largeFont, (PREF_W / 2), 180, 300, 300, 7);
            int center = (int) ((nextEyes.x - prevEyes.x) / 2);

            usernameBox.draw();
            colorBox.draw();

            prevEyes.draw();
            g2.setFont(largeFont);
            metrics = g2.getFontMetrics();
            g2.setColor(Color.BLACK);
            g2.drawString("Eyes " + (eyesID + 1), (int) prevEyes.x + center - (metrics.stringWidth("Eyes " + (eyesID + 1)) / 4) , (int) prevEyes.y + (int) (prevEyes.width) + (metrics.getHeight() / 4));
            nextEyes.draw();
            prevMouth.draw();
            g2.setColor(Color.BLACK);
            g2.drawString("Mouth " + (mouthID + 1), (int) prevMouth.x + center - (metrics.stringWidth("Mouth " + (mouthID + 1)) / 4) , (int) prevMouth.y + (int) (prevMouth.width) + (metrics.getHeight() / 4));
            nextMouth.draw();

            customizeDoneButton.draw();

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(10));
        }

        if(serverList) {
            int margin = 50;
            g2.setFont(mediumFont);
            metrics = g2.getFontMetrics();
            
            for(int i = 0; i < servers.size(); i++) {
                int x = (int) serverButtons.get(i).x;
                int y = (int) serverButtons.get(i).y + metrics.getHeight();
                if(selectedServer == i)
                    serverButtons.get(i).backColor = Color.GRAY;
                else
                    serverButtons.get(i).backColor = Color.LIGHT_GRAY;
                serverButtons.get(i).draw();
                g2.setColor(Color.BLACK);
                g2.drawString(servers.get(i).getName(), x + 10, y);
                g2.drawString(servers.get(i).getIp() + ":" + servers.get(i).getPort(), x + 40, y + 30);
            }

            for(Button b : serverOptionButtons) {
                b.draw();
            }
            
            metrics = g2.getFontMetrics();
            g2.setFont(largeFont);
            g2.setColor(Color.BLACK);
            String str = "Servers";
            g2.drawString(str, (PREF_W / 2) - metrics.stringWidth(str) / 2, 100);
            g2.setStroke(new BasicStroke(10));
            g2.drawRect(margin, 100 + margin, PREF_W - (margin * 2), PREF_H - (margin * 2) - 100);
        }

        if(editServer || addServer) {
            int margin = 50;

            serverNameBox.draw();
            serverIPBox.draw();

            doneButton.draw();

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(10));
            g2.drawRect(margin, 100 + margin, PREF_W - (margin * 2), PREF_H - (margin * 2) - 100);
        }

        if(connecting) {
            connErr = null;
            g2.setColor(Color.BLACK);
            g2.setFont(mediumFont);
            metrics = g2.getFontMetrics();
            String str = "Connecting to server...";

            if(!connThreadStarted) {
                Thread connThread = new Thread(client);
                connThread.start();
                connThreadStarted = true;
            }
            IOException e = client.getErr();
            if(e == null && client.getClientSocket().isBound()) {
                t = new ServerHandler(client.getClientSocket(), client.getIn(), client.getOut(), player, playerInfo, platforms.get(0));
                t.start();
            
                threadStarted = true;
                connThreadStarted = false;
                connecting = false;
            }
            else if (e == null) {
                g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), (PREF_H / 2) - (metrics.getHeight() / 2));
            }
            else {
                connecting = false;
                connThreadStarted = false;
                connectionError = true;
                connErr = e;
            }
        }

        if(connectionError) { 
            g2.setFont(mediumFont);
            g2.setColor(new Color(0, 0, 0));
            metrics = g2.getFontMetrics();
            String str = "Connection Error:";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), (PREF_H / 2) - (metrics.getHeight() / 2));
            if(connErr != null)
                str = connErr.toString();
            else if(errString != null)
                str = errString;
            else 
                str = "Unknown Error";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), (PREF_H / 2) - (metrics.getHeight() / 2) + 50);
        }

        if(threadStarted && !t.getGameStarted() && t.getDisconnectedMessage().equals("")) {
            int margin = 50;
            g2.setFont(mediumFont);
            metrics = g2.getFontMetrics();
            g2.setColor(Color.BLACK);

            int initX = margin + 160;
            int initY = margin + 80;
            int hGap = 20;
            int vGap = 180;
            

            ArrayList<PlayerInfo> infos = t.getPlayerInfos();

            Player.drawScaled(g2, playerInfo, mediumFont, initX, initY, 200, 200, 4);


            for(int i = 1; i < infos.size() + 1; i++) {
                int x = initX + (i % 8) * 200 + (i % 8) * hGap;
                int y = initY + (i / 8) * 100 + (i / 8) * vGap;
                PlayerInfo info = infos.get(i - 1);

                if(!info.getFace().hasImages()) {
                    System.out.println("meow");
                    Face face = info.getFace();
                    face.setEyes(eyes.get(face.getEyesID()));
                    face.setMouth(mouths.get(face.getMouthID()));
                }

                Player.drawScaled(g2, info, mediumFont, x, y, 200, 200, 4);
            }   

            if(t.getPlayerInfos().size() < 1) {
                startButton.backColor = Color.GRAY;
                startButton.textColor = Color.LIGHT_GRAY;
                startButton.draw();
            }
            else {
                startButton.backColor = Color.BLACK;
                startButton.textColor = Color.WHITE;
                startButton.draw();
            }

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(5));
            g2.drawRect(margin, margin, PREF_W - (margin * 2), PREF_H - (margin * 2) - 200);
        }

        if(threadStarted && t.getGameStarted()) {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, PREF_W, PREF_H);

            double dtime = 1/currentFPS;
            if(dtime >= 1 || dtime < 0)
            dtime = 1/FPSCap;

            managePlayerHorizontalSpeed(player);
            player.update(dtime);
            
            managePlatformSpeed(platforms);
            manageMapBounds(player);
            for(Platform p : platforms)
                p.update(dtime);
            
            player.checkCollisions(platforms);
            
            for(Platform p : platforms)
                p.draw(g2);    
            
            if(t.playerHoldingBomb == 255 && !t.playerEliminated) {
                sidewaysVelocity = GHOST_VEL;
            }
            else 
                sidewaysVelocity = PLAYER_VEL;
                
            player.draw(g2, playerInfo, t.playerEliminated, (t.playerHoldingBomb == 255 && !t.playerEliminated));
            drawOtherPlayers(g2);

            metrics = g2.getFontMetrics();
            g2.setColor(Color.BLACK);
            g2.setFont(largeFont);
            String str = "" +  ((double) (t.getStartTime() + t.gameLength * 1000) - System.currentTimeMillis()) / 1000;
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10);
        }

        if(threadStarted && t.getGameEnded()) {
            gameEnded();
        }

        if(practicing || t != null) {
            g2.setColor(new Color(128, 128, 128, 128));
            g2.fillRect(0, 0, 180, 140);
            g2.setColor(Color.WHITE);
        }
        else {
            g2.setColor(Color.BLACK);
        }
        
        if(showSettings) {
            int margin = 50;
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, PREF_W, PREF_H);
            g2.setColor(new Color(255, 240, 201));
            g2.fillRect(margin, margin, PREF_W - (margin * 2), PREF_H - (margin * 2));

            settingsDoneButton.draw();

            g2.setFont(giantFont);
            g2.setColor(Color.BLACK);
            metrics = g2.getFontMetrics();
            
            String str = "Settings";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), margin + 100);

            g2.setFont(mediumFont);
            g2.setColor(Color.BLACK);
            metrics = g2.getFontMetrics();
        }
                
        g2.setFont(smallFont);

        g2.drawString("FPS: " + currentFPS, 10, 20);
        if(practicing || t != null) {
            g2.drawString("PlayerX " + (-platforms.get(0).getX() + player.getX() - ((platforms.get(0).getWidth() / 2) - player.getWidth() / 2)), 10, 40);
            g2.drawString("PlayerY " + (platforms.get(0).getY() - player.getY() - player.getHeight() + (platforms.get(0).getHeight() + 2000)), 10, 60);
        }
        if(t != null) {
            g2.drawString("PPS: " + t.getPPS(), 10, 80);
            g2.drawString("Ping: " + t.getPing(), 10, 100);
            g2.drawString("Approx Player FPS: " + t.getApproxFPS(), 10, 120);
        }

        /*
         * Limits the FPS to whatever the FPSCap is set to
         * If unlimited is set to true, the game will run as fast as possible
         */
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

        if(e.getKeyCode() == KeyEvent.VK_ESCAPE && !practicing) {
            if(showSettings) {
                if(usernameBox.isSelected || colorBox.isSelected) {
                    usernameBox.isSelected = false;
                    colorBox.isSelected = false;
                }
                else
                    showSettings = false;
                playerInfo = readPlayerFile();
            }
            else if(editServer || addServer) {
                if(serverNameBox.isSelected || serverIPBox.isSelected) {
                    serverNameBox.isSelected = false;
                    serverIPBox.isSelected = false;
                }
                else {
                    editServer = false;
                    addServer = false;
                    serverList = true;
                }
                
            }
            else if(connectionError) {
                connectionError = false;
                connErr = null;
                errString = "";
                serverList = true;
            }
            else if(serverList) {
                serverList = false;
                titleScreen = true;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE && practicing) {
            practicing = false;
            titleScreen = true;
            resetPlatforms();
        }

        if(practicing || threadStarted && t.getGameStarted()) {
            for(int i : jumpKeys)
                if(e.getKeyCode() == i)
                    player.jump();
            for(int i : leftKeys)
                if(e.getKeyCode() == i)
                    left = true;
            for(int i : rightKeys)
                if(e.getKeyCode() == i)
                    right = true;
        }

        if(editServer || addServer) {
            serverNameBox.keyTyped(e);
            serverIPBox.keyTyped(e);
        }

        if(editPlayer) {
            usernameBox.keyTyped(e);
            colorBox.keyTyped(e);

            String name = usernameBox.text;
            if(name.equals(""))
                name = "Player";
            
            if(name.length() > 16)
                name = name.substring(0, 17);
            try {
                playerInfo = new PlayerInfo(face, name, Color.decode(colorBox.text));
            }
            catch(Exception ex) {
                playerInfo = new PlayerInfo(face, usernameBox.text, Color.GRAY);
            }
            try {
                refreshPlayerFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

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
    public void keyTyped(KeyEvent e) {}

    private static void createAndShowGUI() {
        Platformer gamePanel = new Platformer();
        JFrame frame = new JFrame("Potato Panic");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(gamePanel);
        // frame.setUndecorated(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setBackground(Color.WHITE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setVisible(true);

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
    public void mousePressed(MouseEvent e) {

        if(settingsButton.mouseClick(e.getX(), e.getY()) && !showSettings)
            showSettings = true;

        if(!showSettings) {
            if(editPlayer) {
                usernameBox.mouseClick(e.getX(), e.getY());
                colorBox.mouseClick(e.getX(), e.getY());

                if(customizeDoneButton.mouseClick(e.getX(), e.getY())) {
                    editPlayer = false;
                    titleScreen = true;
                }
                if(prevEyes.mouseClick(e.getX(), e.getY())) {
                    eyesID--;
                    if(eyesID < 0)
                        eyesID = eyes.size() - 1;
                    refreshFace();
                }
                if(nextEyes.mouseClick(e.getX(), e.getY())) {
                    eyesID++;
                    if(eyesID >= eyes.size())
                        eyesID = 0;
                    refreshFace();
                }
                if(prevMouth.mouseClick(e.getX(), e.getY())) {
                    mouthID--;
                    if(mouthID < 0)
                        mouthID = mouths.size() - 1;
                    refreshFace();
                }
                if(nextMouth.mouseClick(e.getX(), e.getY())) {
                    mouthID++;
                    if(mouthID >= mouths.size())
                        mouthID = 0;
                    refreshFace();
                }
                return;
            }

            if(titleScreen) {
                if(playButton.mouseClick(e.getX(), e.getY())) {
                    serverList = true;
                    titleScreen = false;
                }
                if(practiceButton.mouseClick(e.getX(), e.getY())) {
                    practicing = true;
                    titleScreen = false;
                }
                if(customizeButton.mouseClick(e.getX(), e.getY())) {
                    editPlayer = true;
                    titleScreen = false;
                }
            }

            if(serverList) {
                for(int i = 0; i < servers.size(); i++) {
                    if(serverButtons.get(i).mouseClick(e.getX(), e.getY())) {
                        selectedServer = i;
                    }
                }
                if(serverOptionButtons.get(0).mouseClick(e.getX(), e.getY())) {
                    serverList = false;
                    addServer = true;
                    serverNameBox.text = "";
                    serverIPBox.text = "";
                }
                if(serverOptionButtons.get(1).mouseClick(e.getX(), e.getY()) && selectedServer != -1) {
                    serverList = false;
                    editServer = true;
                    System.out.println("settings set text");
                    serverNameBox.text = servers.get(selectedServer).getName();
                    serverIPBox.text = servers.get(selectedServer).getIp() + ":" + servers.get(selectedServer).getPort();
                }
                if(serverOptionButtons.get(2).mouseClick(e.getX(), e.getY()) && selectedServer != -1) {
                    servers.remove(selectedServer);
                    serverButtons.remove(selectedServer);
    
                    try {
                        refreshServerFile();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
    
                    selectedServer = -1;
                }
                if(serverOptionButtons.get(3).mouseClick(e.getX(), e.getY()) && selectedServer != -1) {
                    ServerObject server = servers.get(selectedServer);
                    client = new Client(server.getIp(), server.getPort());
                    serverList = false;
                    connecting = true;
                }
            }
    
            if(threadStarted && !t.getGameStarted()) {
                if(playButton.mouseClick(e.getX(), e.getY())) {
                    try {
                        t.sendStartGame();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                if(startButton.mouseClick(e.getX(), e.getY()))
                    try {
                        t.sendStartGame();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            }

            if(editServer || addServer) {
                serverNameBox.mouseClick(e.getX(), e.getY());
                serverIPBox.mouseClick(e.getX(), e.getY());
                if(doneButton.mouseClick(e.getX(), e.getY())) {
                    ServerObject server = null;
    
                    if(serverIPBox.text.contains(":"))
                        server = new ServerObject(serverNameBox.text, serverIPBox.text.split(":")[0], Short.parseShort(serverIPBox.text.split(":")[1]));
                    else
                        server = new ServerObject(serverNameBox.text, serverIPBox.text, (short) 5100);
    
                    if(editServer)
                        servers.set(selectedServer, server);
                    else
                        servers.add(server);
    
                    try {
                        refreshServerFile();
                        servers = readServerFile();
                        serverButtons = generateServerButtons(50, 150, PREF_W - 100, 75, servers);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
    
                    editServer = false;
                    addServer = false;
                    serverList = true;
                }
            }
        }
        else {
            if(settingsDoneButton.mouseClick(e.getX(), e.getY())) {
                String name = usernameBox.text;
                if(name.equals(""))
                    name = "Player";
                if(name.length() > 16)
                    name = name.substring(0, 17);
                try {
                    playerInfo = new PlayerInfo(face, name, Color.decode(colorBox.text));
                }
                catch(Exception ex) {
                    playerInfo = new PlayerInfo(face, usernameBox.text, Color.GRAY);
                }
                try {
                    refreshPlayerFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                showSettings = false;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private void managePlatformSpeed(ArrayList<Platform> platforms) {
        for(Platform p : platforms) {
            p.setDy(-1 * player.getDy());
            p.setDx(-1 * player.getDx());
        }
    }

    private void managePlayerHorizontalSpeed(Player player) {
        if(left)
            player.setDx(-sidewaysVelocity);
        if(right)
            player.setDx(sidewaysVelocity);
        
        if(!left && !right)
            player.setDx(0);
    }

    private void manageMapBounds(Player player) {
        double playerX = -platforms.get(0).getX() + player.getX() - ((platforms.get(0).getWidth() / 2) - player.getWidth() / 2);
        double playerY = platforms.get(0).getY() - player.getY() - player.getHeight() + (platforms.get(0).getHeight() + 2000);
        if(playerY < VERTICAL_BOUND_MIN || playerY > VERTICAL_BOUND_MAX || playerX < HORIZONTAL_BOUND_MIN || playerX > HORIZONTAL_BOUND_MAX) {
            resetPlatforms();
        }

    }

    private void drawOtherPlayers(Graphics2D g2) {
        otherPlayers = t.getPlayers();
        otherPlayerInfos = t.getPlayerInfos();

        // if(otherPlayers.size() != otherPlayerInfos.size()) {
        //     // System.out.println("there are " + otherPlayers.size() + " other players");
        //     // System.out.println("but there are " + otherPlayerInfos.size() + " other player infos");
        //     return;
        // }

        Platform originP = platforms.get(0);
        for(int i = 0; i < otherPlayers.size(); i++) {
            if(i > otherPlayerInfos.size() - 1)
                break;

            if(!otherPlayerInfos.get(i).getFace().hasImages()) {
                Face face = otherPlayerInfos.get(i).getFace();
                face.setEyes(eyes.get(face.getEyesID()));
                face.setMouth(mouths.get(face.getMouthID()));
            }

            if(t.getEliminatedPlayers().contains(i)) {
                if(t.playerEliminated)
                    otherPlayers.get(i).draw(g2, originP, otherPlayerInfos.get(i), true, false);
                continue;
            }

            try {
                otherPlayers.get(i).draw(g2, originP, otherPlayerInfos.get(i), false, i == t.playerHoldingBomb);
            } catch (Exception e) {
                System.out.println(i);
                e.printStackTrace();
            }

        }
    }

    private void setGraphics(Graphics2D g2) {
        playButton.setGraphics(g2);
        practiceButton.setGraphics(g2);
        settingsButton.setGraphics(g2);
        serverNameBox.setGraphics(g2);
        usernameBox.setGraphics(g2);
        colorBox.setGraphics(g2);
        serverIPBox.setGraphics(g2);
        doneButton.setGraphics(g2);
        settingsDoneButton.setGraphics(g2);
        backButton.setGraphics(g2);
        startButton.setGraphics(g2);
        customizeButton.setGraphics(g2);
        customizeDoneButton.setGraphics(g2);
        prevEyes.setGraphics(g2);
        nextEyes.setGraphics(g2);
        prevMouth.setGraphics(g2);
        nextMouth.setGraphics(g2);

        for(Button b : serverButtons) {
            b.setGraphics(g2);
        }
        
        for(Button b : serverOptionButtons) {
            b.setGraphics(g2);
        }
    }

    private PlayerInfo readPlayerFile() {
        File file = new File("./player.data");
        String name = "Player";
        Color color = Color.GRAY;
        eyesID = 0;
        mouthID = 0;

        try {

        if(file.createNewFile())
        {
            face = new Face(eyes.get(eyesID), mouths.get(mouthID), eyesID, mouthID);
            playerInfo = new PlayerInfo(face, "Player", Color.GRAY);
            refreshPlayerFile();
        }   
        else {
                Scanner sc = new Scanner(file);
                if(!sc.hasNextLine()) {
                    sc.close();
                    face = new Face(eyes.get(eyesID), mouths.get(mouthID), eyesID, mouthID);
                    playerInfo = new PlayerInfo(face, "Player", Color.GRAY);
                    usernameBox.text = playerInfo.getName();
                    colorBox.text = "#" + colorToHex(playerInfo.getColor());
                    refreshPlayerFile();
                    return playerInfo;
                }
                name = sc.nextLine();
    
                if(name.length() > 16)
                    name = name.substring(0, 17);
    
                    int colorNum = 0;
                try {
                    colorNum = Integer.parseInt(sc.nextLine());
                } catch (Exception e) {
                    colorNum = Color.GRAY.getRGB();
                }
                
                color = new Color(colorNum);
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    
                usernameBox.text = name;
                colorBox.text = colorToHex(color);

                playerInfo = new PlayerInfo(face, name, color);

                try {
                    eyesID = Integer.parseInt(sc.nextLine());
                    mouthID = Integer.parseInt(sc.nextLine());
                } catch (Exception e) {
                    eyesID = 0;
                    mouthID = 0;
                    refreshPlayerFile();
                }
        
                face = new Face(eyes.get(eyesID), mouths.get(mouthID), eyesID, mouthID);

                sc.close();
            } 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new PlayerInfo(face, name, color);
    }

    private void refreshPlayerFile() throws IOException {
        File file = new File("./player.data");

        try {
            FileWriter fw = new FileWriter(file);
            String str = "";
            
            str += playerInfo.getName() + "\n";
            str += playerInfo.getColor().getRGB();
            str += "\n";
            str += eyesID + "\n";
            str += mouthID + "\n";

            fw.write(str);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ServerObject> readServerFile() {
        File file = new File("./servers.data");
        ArrayList<ServerObject> servers = new ArrayList<ServerObject>();

        try {
            if(file.createNewFile()) 
                return servers; 
            else {
                
                System.out.println(file.getAbsolutePath());
                try {
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine()) {
                        String name = sc.nextLine();
                        String ip = sc.nextLine();
                        short port = Short.parseShort(sc.nextLine());
                        servers.add(new ServerObject(name, ip, port));
                    }
                    sc.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return servers;
    }

    private ArrayList<Eyes> readEyes() {
        File dir = new File("./lib/player/eyes");
        File[] files = dir.listFiles();
        sortFiles(files);
        ArrayList<Eyes> eyes = new ArrayList<Eyes>();

        for(File f : files) {
            ImageIcon img = new ImageIcon(f.getAbsolutePath());
            eyes.add(new Eyes(img.getImage()));
        }

        return eyes;
    }

    private ArrayList<Mouth> readMouths() {
        File dir = new File("./lib/player/mouth");
        File[] files = dir.listFiles();
        sortFiles(files);
        ArrayList<Mouth> mouths = new ArrayList<Mouth>();

        for(File f : files) {
            ImageIcon img = new ImageIcon(f.getAbsolutePath());
            mouths.add(new Mouth(img.getImage()));
        }

        return mouths;
    }

    // private ArrayList<Accessory> readAccessories() {
    //     File dir = new File("./lib/player/accessory");
    //     File[] files = dir.listFiles();
    //     sortFiles(files);
    //     ArrayList<Accessory> accessories = new ArrayList<Accessory>();

    //     for(File f : files) {
    //         ImageIcon img = new ImageIcon(f.getAbsolutePath());
    //         accessories.add(new Accessory(img.getImage()));
    //     }

    //     return accessories;
    // }

    private void refreshFace() {
        face = new Face(eyes.get(eyesID), mouths.get(mouthID), eyesID, mouthID);
        playerInfo.setFace(face);
    }

    private void refreshServerFile() throws IOException {
        File file = new File("./servers.data");
        try {
            FileWriter fw = new FileWriter(file);
            String str = "";
            for(int i = 0; i < servers.size(); i++) {
                str += servers.get(i).getName() + "\n";
                str += servers.get(i).getIp() + "\n";
                str += servers.get(i).getPort() + "\n";
            }

            fw.append(str);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String colorToHex(Color color) {
        String red = Integer.toHexString(color.getRed());
        String green = Integer.toHexString(color.getGreen());
        String blue = Integer.toHexString(color.getBlue());

        return "#" + (red.length() == 1 ? "0" + red : red) + (green.length() == 1 ? "0" + green : green) + (blue.length() == 1 ? "0" + blue : blue);
    }

    private ArrayList<Button> generateServerButtons(int x, int y, int width, int height, ArrayList<ServerObject> servers) {
        ArrayList<Button> buttons = new ArrayList<Button>();
        for(int i = 0; i < servers.size(); i++) {
            buttons.add(new Button(x, y + (height * i) + ((i > 0) ? 5 * i : 0), width, height, Color.LIGHT_GRAY));
        }
        return buttons;
    }

    private void resetPlatforms() {
        platforms.clear();
        //Bounding Box Platforms
        //top
        platforms.add(new Platform(-3000, -1600, 8000, 600));
        //bottom
        platforms.add(new Platform(-3000, 1000, 8000, 600));
        //left
        platforms.add(new Platform(-3000, -1500, 1200, 3000));
        //right
        platforms.add(new Platform(3800, -1500, 1200, 3000));
        
        //Center 3 Stacked Platforms
        platforms.add(new Platform(720, 650, 700, 100));
        platforms.add(new Platform(20, 300, 700, 100));
        platforms.add(new Platform(720, -50, 700, 100));
        
        //Left Floating Platform + Attached Walls
        platforms.add(new Platform(-1200, -400, 970, 100));
        platforms.add(new Platform(-300, -400, 100, 400));
        platforms.add(new Platform(-900, -300, 100, 800));

        //Right Floating Platform + Tiny Platforms
        platforms.add(new Platform(1500, -400, 1000, 100));
        //first column
        platforms.add(new Platform(2500, 300, 100, 100));
        //second column
        platforms.add(new Platform(2800, 0, 100, 100));
        platforms.add(new Platform(2800, 600, 100, 100));
        //third column
        platforms.add(new Platform(3100, 300, 100, 100));
        platforms.add(new Platform(3100, -300, 100, 100));
        //fourth column
        platforms.add(new Platform(3400, 0, 100, 100));
    }

    public void gameEnded() {
        resetPlatforms();
        t.playerEliminated = false;
    }

    public static void sortFiles(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
    }
}