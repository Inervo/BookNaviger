/*
 */

package booknaviger.osbasics;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;

/**
 * @author Inervo
 * Basic operation related to the OS
 */
public class OSBasics {
    
    /**
     * Open an URL in the default browser
     * @param URIString the URI string to navigate to
     */
    public static void openURI(String URIString) {
        Logger.getLogger(OSBasics.class.getName()).entering(OSBasics.class.getName(), "openURI", URIString);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                Logger.getLogger(OSBasics.class.getName()).log(Level.INFO, "Open the URL \"{0}\" in the default browser", URIString);
                desktop.browse(new URI(URIString));
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(OSBasics.class.getName()).log(Level.SEVERE, "The URL can't be accessed", ex);
                }
            } else {
                Logger.getLogger(OSBasics.class.getName()).log(Level.WARNING, "No default browser found");
            }
        }
        Logger.getLogger(OSBasics.class.getName()).exiting(OSBasics.class.getName(), "openURI");
    }
    
    /**
     * Open the default file manager to a file / folder
     * @param fileString The file / folder to open
     */
    public static void openFile(String fileString) {
        Logger.getLogger(OSBasics.class.getName()).entering(OSBasics.class.getName(), "openFile", fileString);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    Logger.getLogger(OSBasics.class.getName()).log(Level.INFO, "Open the file \"{0}\" in the default browser", fileString);
                    desktop.open(new File(fileString));
                } catch (IOException ex) {
                    Logger.getLogger(OSBasics.class.getName()).log(Level.SEVERE, "The specified file / folder can't be accessed", ex);
                }
            } else {
                Logger.getLogger(OSBasics.class.getName()).log(Level.WARNING, "No default file manager found");
            }
        }
        Logger.getLogger(OSBasics.class.getName()).exiting(OSBasics.class.getName(), "openFile");
    }
    
    /**
     * Retreive the application data folder
     * @return The path to the folder
     */
    public static String getAppDataDir() {
        Logger.getLogger(OSBasics.class.getName()).entering(OSBasics.class.getName(), "getAppDataDir");
        File appDataDir;
        if (isMac()) {
            appDataDir = new File(System.getProperty("user.home"), "Library" + File.separatorChar + "Application Support"+ File.separatorChar + ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            appDataDir = new File(System.getenv("APPDATA"), ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        } else {
            appDataDir = new File(System.getProperty("user.home"), "." + ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
        }
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }
        Logger.getLogger(OSBasics.class.getName()).log(Level.CONFIG, "Appdir is \"{0}\"", appDataDir.toString());
        Logger.getLogger(OSBasics.class.getName()).exiting(OSBasics.class.getName(), "getAppDataDir", appDataDir.toString());
        return appDataDir.toString();
    }
    
    /**
     * Retreive the home folder
     * @return The path to the folder
     */
    public static String getHomeDir() {
        Logger.getLogger(OSBasics.class.getName()).entering(OSBasics.class.getName(), "getHomeDir");
        String folder = System.getProperty("user.home");
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            folder = FileSystemView.getFileSystemView().getDefaultDirectory().toString();
        }
        Logger.getLogger(OSBasics.class.getName()).log(Level.CONFIG, "HomeDir is \"{0}\"", folder);
        Logger.getLogger(OSBasics.class.getName()).exiting(OSBasics.class.getName(), "getHomeDir", folder);
        return folder;
    }

    /**
     * Check if this instance is running on a Mac OS X
     * @return true if it's a mac<br />false otherwise
     */
    public static boolean isMac() {
        Logger.getLogger(OSBasics.class.getName()).entering(OSBasics.class.getName(), "isMac");
        Logger.getLogger(OSBasics.class.getName()).exiting(OSBasics.class.getName(), "isMac", System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }

}
