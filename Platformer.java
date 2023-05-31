import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.FontMetrics;

import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Font.EasyFontInstaller;
import UI.Button;
import UI.TextBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Scanner;

public class Platformer extends JPanel implements KeyListener, MouseMotionListener, MouseListener
{
    private static final long serialVersionUID = 1L;

    //Gets width & height of screen (which is hopefully 1080p)
    private static final int PREF_W = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int PREF_H = Toolkit.getDefaultToolkit().getScreenSize().height;
    private RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    private static int FPSCap = 60;
    private static boolean unlimited = false;
    private static double totalFrames = 0;
    private static double lastFPSCheck = 0;
    private static double currentFPS = 0;

    private double sidewaysVelocity = 900;

    private boolean left, right;
    private int[] leftKeys = new int[] {KeyEvent.VK_LEFT, KeyEvent.VK_A};
    private int[] rightKeys = new int[] {KeyEvent.VK_RIGHT, KeyEvent.VK_D};
    private int[] jumpKeys = new int[] {KeyEvent.VK_UP, KeyEvent.VK_W};

    private Font giantFont = new Font("Mochiy Pop P One", Font.PLAIN, 48);
    private Font largeFont = new Font("Mochiy Pop P One", Font.PLAIN, 32);
    private Font mediumFont = new Font("Mochiy Pop P One", Font.PLAIN, 24);
    private Font smallFont = new Font("Mochiy Pop P One", Font.PLAIN, 12);
    private FontMetrics metrics;
    
    private ArrayList<Platform> platforms = new ArrayList<Platform>();

    private boolean titleScreen = true, showSettings, practicing, 
    serverList, addServer, editServer,
    connecting, connectionError, threadStarted, inServerRoom;

    private Button playButton;
    private Button practiceButton;
    private Button settingsButton;
    private TextBox serverNameBox;
    private TextBox serverIPBox;
    private TextBox usernameBox;
    private TextBox colorBox;
    private Button doneButton;
    private Button settingsDoneButton;
    private ArrayList<ServerObject> servers;
    private ArrayList<Button> serverButtons;
    private ArrayList<Button> serverOptionButtons = new ArrayList<Button>();
    private int selectedServer = -1;

    private Player player = new Player(PREF_W / 2, PREF_H / 2);
    private PlayerInfo playerInfo;
    private ArrayList<PlayerLite> otherPlayers = new ArrayList<PlayerLite>();
    private ArrayList<PlayerInfo> otherPlayerInfos = new ArrayList<PlayerInfo>();
    private Client client;
    private ServerHandler t;
    private IOException connErr;
// 


    public Platformer()
    {
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        setFocusable(true);
        requestFocus();
        
        platforms.add(new Platform(-3000, 1000, 8000, 600));
        platforms.add(new Platform(-3000, -1000, 1200, 2000));
        platforms.add(new Platform(4000 - 200, -1000, 1200, 2000));
        platforms.add(new Platform(-3000, -1600, 8000, 600));
        platforms.add(new Platform(PREF_W / 2, PREF_H - 300, PREF_W / 2, 75));
        platforms.add(new Platform(0, PREF_H - 600, PREF_W / 2, 75));
        platforms.add(new Platform(PREF_W / 2, PREF_H - 900, PREF_W / 2, 75));

        EasyFontInstaller.installFont();

        metrics = getFontMetrics(largeFont);
        int buttonWidth = 400;
        int buttonHeight = 100;
        int buttonY = PREF_H - 50 - buttonHeight;
        int spacing = (PREF_W - 100 - (4 * buttonWidth)) / 3;
        String str = "Add New";
        serverOptionButtons.add(new Button(50, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        str = "Edit";
        serverOptionButtons.add(new Button(50 + spacing + buttonWidth, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        str = "Remove";
        serverOptionButtons.add(new Button(50 + (spacing + buttonWidth) * 2, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        str = "Connect";
        serverOptionButtons.add(new Button(50 + (spacing + buttonWidth) * 3, buttonY, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK));
        
        int buttonX = (PREF_W / 2) - buttonWidth / 2;
        
        serverNameBox = new TextBox(buttonX, 200, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "New Server", Color.LIGHT_GRAY);
        serverIPBox = new TextBox(buttonX, 300, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "127.0.0.1:5100", Color.LIGHT_GRAY);
        usernameBox = new TextBox(300, 200, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "Player", Color.LIGHT_GRAY);
        colorBox = new TextBox(300, 300, buttonWidth, 50, Color.GRAY, mediumFont, Color.WHITE, "#ababab", Color.LIGHT_GRAY);
        str = "Done";
        settingsDoneButton = new Button(buttonX, PREF_H - 400, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK);
        str = "Done";
        doneButton = new Button(buttonX, PREF_H - 400, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10, largeFont.getSize(), largeFont, str, Color.WHITE, Color.BLACK);

        str = "Play";
        metrics = getFontMetrics(giantFont);
        playButton = new Button(buttonX, 600, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        str = "Pratice";
        practiceButton = new Button(buttonX, 750, buttonWidth, buttonHeight, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
        str = "";
        buttonWidth = 75;
        settingsButton = new Button(PREF_W - 75 - 10, 10, buttonWidth, buttonWidth, (buttonWidth / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight(), giantFont.getSize(), giantFont, str, Color.WHITE, Color.BLACK);
    
        playerInfo = readPlayerFile();

        servers = readServerFile();
        serverButtons = generateServerButtons(50, 150, PREF_W - 100, 75, servers);
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
        metrics = g2.getFontMetrics();
        playButton.setGraphics(g2);
        practiceButton.setGraphics(g2);
        settingsButton.setGraphics(g2);
        serverNameBox.setGraphics(g2);
        usernameBox.setGraphics(g2);
        colorBox.setGraphics(g2);
        serverIPBox.setGraphics(g2);
        doneButton.setGraphics(g2);
        settingsDoneButton.setGraphics(g2);

        for(Button b : serverButtons) {
            b.setGraphics(g2);
        }

        for(Button b : serverOptionButtons) {
            b.setGraphics(g2);
        }

        // g2.drawLine(PREF_W / 2, 0, PREF_W / 2, PREF_W);

        if(titleScreen) {
            g2.setFont(giantFont);
            g2.setColor(Color.BLACK);
            metrics = g2.getFontMetrics();
            
            String str = "Potato Panic";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), 50);
            
            g2.setFont(largeFont);
            metrics = g2.getFontMetrics();

            str = playerInfo.getName();
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), 200);
            g2.setColor(Color.BLACK);
            g2.fillRect((PREF_W / 2) - 130, 245, 260, 260);
            g2.setColor(playerInfo.getColor());
            g2.fillRect((PREF_W / 2) - 125, 250, 250, 250);

            playButton.draw();
            practiceButton.draw();

            if(!showSettings)
                settingsButton.draw();
        }

        if(practicing) {
            double dtime = 1/currentFPS;
            if(dtime >= 1 || dtime < 0)
            dtime = 1/FPSCap;
            
            managePlayerHorizontalSpeed(player);
            player.update(dtime);
            
            managePlatformSpeed(platforms);
            for(Platform p : platforms)
            p.update(dtime);
            
            player.checkCollisions(platforms);
            
            for(Platform p : platforms)
                p.draw(g2);    
            player.draw(g2, playerInfo, false);
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
            g2.setFont(mediumFont);
            metrics = g2.getFontMetrics();
            String str = "Connecting to server...";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), (PREF_H / 2) - (metrics.getHeight() / 2));

            IOException e = client.startConnection();
            if(e == null) {
                t = new ServerHandler(client.getClientSocket(), client.getIn(), client.getOut(), player, playerInfo, platforms.get(0));
                t.start();
        
                threadStarted = true;
                inServerRoom = true;
            }
            else {
                connectionError = true;
                connErr = e;
            }

            connecting = false;
        }

        if(connectionError) { 
            g2.setFont(mediumFont);
            metrics = g2.getFontMetrics();
            String str = "Connection Error";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), (PREF_H / 2) - (metrics.getHeight() / 2));
            str = connErr.getMessage();
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), (PREF_H / 2) - (metrics.getHeight() / 2) + 50);
        }

        if(inServerRoom || threadStarted && !t.getGameStarted()) {

        }

        if(threadStarted && t.getGameStarted()) {
            double dtime = 1/currentFPS;
            if(dtime >= 1 || dtime < 0)
            dtime = 1/FPSCap;
            
            managePlayerHorizontalSpeed(player);
            player.update(dtime);
            
            managePlatformSpeed(platforms);
            for(Platform p : platforms)
            p.update(dtime);
            
            player.checkCollisions(platforms);
            
            for(Platform p : platforms)
                p.draw(g2);    
            
            if(t.playerHoldingBomb == 255 && !t.playerEliminated) {
                g2.setColor(Color.RED);
                g2.fillRect((int) player.getX() - 5, (int) player.getY() - 5, 60, 60);
            }
            player.draw(g2, playerInfo, t.playerEliminated);
            drawOtherPlayers(g2);

            metrics = g2.getFontMetrics();
            g2.setColor(Color.BLACK);
            g2.setFont(largeFont);
            String str = "" +  ((double) (t.getStartTime() + t.gameLength * 1000) - System.currentTimeMillis()) / 1000;
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), metrics.getHeight() + 10);
        }

        g2.setFont(smallFont);
        g2.setColor(Color.BLACK);

        g2.drawString("FPS: " + currentFPS, 10, 20);
        if(t != null) {
            g2.drawString("PPS: " + t.getPPS(), 10, 40);
            g2.drawString("Ping: " + t.getPing(), 10, 60);
        }

        if(showSettings) {
            int margin = 50;
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, PREF_W, PREF_H);
            g2.setColor(Color.WHITE);
            g2.fillRect(margin, margin, PREF_W - (margin * 2), PREF_H - (margin * 2));

            settingsButton.draw();
            settingsDoneButton.draw();
            usernameBox.draw();
            colorBox.draw();

            g2.setFont(largeFont);
            g2.setColor(Color.BLACK);
            metrics = g2.getFontMetrics();
            
            String str = "Settings";
            g2.drawString(str, (PREF_W / 2) - (metrics.stringWidth(str) / 2), margin + 50);

            g2.setFont(mediumFont);
            g2.setColor(Color.BLACK);
            metrics = g2.getFontMetrics();
        }

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

        if(e.getKeyCode() == KeyEvent.VK_ESCAPE && t == null && !practicing) {
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

        if(practicing || t != null) {
            if(e.getKeyCode() == KeyEvent.VK_S)
                try {
                    t.sendStartGame();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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

        if(showSettings) {
            usernameBox.keyTyped(e);
            colorBox.keyTyped(e);
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
    public void keyTyped(KeyEvent e){
    }

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

        if(settingsButton.mouseClick(e.getX(), e.getY()))
            showSettings = !showSettings;

        if(!showSettings) {
            if(titleScreen) {
                if(playButton.mouseClick(e.getX(), e.getY())) {
                    serverList = true;
                    titleScreen = false;
                }
                if(practiceButton.mouseClick(e.getX(), e.getY())) {
                    practicing = true;
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
            usernameBox.mouseClick(e.getX(), e.getY());
            colorBox.mouseClick(e.getX(), e.getY());
            if(settingsDoneButton.mouseClick(e.getX(), e.getY())) {
                String name = usernameBox.text;
                if(name.equals(""))
                    name = "Player";
                if(name.length() > 16)
                    name = name.substring(0, 17);
                try {
                    playerInfo = new PlayerInfo(name, Color.decode(colorBox.text));
                }
                catch(Exception ex) {
                    playerInfo = new PlayerInfo(usernameBox.text, Color.GRAY);
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

    private void drawOtherPlayers(Graphics2D g2) {
        otherPlayers = t.getPlayers();
        otherPlayerInfos = t.getPlayerInfos();

        if(otherPlayers.size() != otherPlayerInfos.size()) {
            System.out.println("there are " + otherPlayers.size() + " other players");
            System.out.println("but there are " + otherPlayerInfos.size() + " other player infos");
            return;
        }

        Platform originP = platforms.get(0);
        for(int i = 0; i < otherPlayers.size(); i++) {
            if(t.getEliminatedPlayers().contains(i)) {
                if(t.playerEliminated)
                    otherPlayers.get(i).draw(g2, originP, otherPlayerInfos.get(i), true);
                continue;
            }
            
            g2.setColor(Color.RED);
            if(i == t.playerHoldingBomb) {
                g2.fillRect(otherPlayers.get(i).x + (int) originP.getX() - 5, otherPlayers.get(i).y + (int) originP.getY() - 5, 60, 60);
            }

            otherPlayers.get(i).draw(g2, originP, otherPlayerInfos.get(i), false);

        }
    }

    private PlayerInfo readPlayerFile() {
        File file = new File("./player.data");
        String name = "Player";
        Color color = Color.GRAY;

        try {
            Scanner sc = new Scanner(file);
            if(!sc.hasNextLine()) {
                sc.close();
                playerInfo = new PlayerInfo("Player", Color.GRAY);
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
    
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new PlayerInfo(name, color);
    }

    private void refreshPlayerFile() throws IOException {
        File file = new File("./player.data");

        try {
            FileWriter fw = new FileWriter(file);
            String str = "";
            
            str += playerInfo.getName() + "\n";
            str += playerInfo.getColor().getRGB();

            fw.append(str);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ServerObject> readServerFile() {
        File file = new File("./servers.data");
        ArrayList<ServerObject> servers = new ArrayList<ServerObject>();

        
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

        return servers;
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

    private String colorToHex(Color color) {
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
        platforms.add(new Platform(-3000, 1000, 8000, 600));
        platforms.add(new Platform(-3000, -1000, 1200, 2000));
        platforms.add(new Platform(4000 - 200, -1000, 1200, 2000));
        platforms.add(new Platform(-3000, -1600, 8000, 600));
        platforms.add(new Platform(PREF_W / 2, PREF_H - 300, PREF_W / 2, 75));
        platforms.add(new Platform(0, PREF_H - 600, PREF_W / 2, 75));
        platforms.add(new Platform(PREF_W / 2, PREF_H - 900, PREF_W / 2, 75));
    }
}