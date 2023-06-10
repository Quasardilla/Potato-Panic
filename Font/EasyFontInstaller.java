package font;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.awt.FontFormatException;

public class EasyFontInstaller{
    
    private static File file = new File("./Font");

    public static void installFont() 
    {
        for (int i = 0; i < file.list().length; i++)
        {
            if(!file.list()[i].equals("EasyFontInstaller.java"))
            {
                File fontFile = new File("./font/" + file.list()[i]);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                } catch (FontFormatException e) {} catch (IOException e) {}
            }
        }

        // printAll();
    }
    public static void installFont(String fontfilename) 
    {
        File fontFile = new File("./font/" + fontfilename);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void printAll()
    {
        for(String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            System.out.println(s);
    }
}
