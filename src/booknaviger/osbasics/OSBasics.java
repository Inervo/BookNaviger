/*
 */

package booknaviger.osbasics;

import booknaviger.macworld.MacOSXApplicationAdapter;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Inervo
 *
 */
public class OSBasics {

    /**
     *
     */
    public OSBasics() {
    }
    
    /**
     *
     * @param URIString
     */
    public static void openURI(String URIString) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(URIString));
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(OSBasics.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     *
     * @param URIString
     */
    public static void openFile(String fileString) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(new File(fileString));
                } catch (IOException ex) {
                    Logger.getLogger(OSBasics.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     *
     * @param URIString
     */
    public static String getAppDataDir() {
        File appDataDir;
        if (MacOSXApplicationAdapter.isMac()) {
            appDataDir = new File(System.getProperty("user.home"), "Library" + File.separatorChar + "Application Support"+ File.separatorChar + ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            appDataDir = new File(System.getenv("APPDATA"), ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        } else {
            appDataDir = new File(System.getProperty("user.home"), "." + ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        }
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }
        return appDataDir.toString();
    }

}
