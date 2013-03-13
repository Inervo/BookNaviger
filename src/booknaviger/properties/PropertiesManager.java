/*
 */
package booknaviger.properties;

import booknaviger.macworld.MacOSXApplicationAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Inervo
 */
public class PropertiesManager {
    
    private File propertiesFile = null;
    private Properties properties = new Properties();
    
    private PropertiesManager() {
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
        propertiesFile = new File(appDataDir, "booknaviger.properties");
        try {
            propertiesFile.createNewFile();
            properties.load(new FileReader(propertiesFile));
        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getKey(String keyString) {
        return properties.getProperty(keyString);
    }
    
    public void setKey(String keyString, String value) {
        properties.setProperty(keyString, value);
    }
    
    public void saveProperties() {
        try {
            properties.store(new FileWriter(propertiesFile), "Every properties is written here");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static PropertiesManager getInstance() {
        return PropertiesManagerHolder.INSTANCE;
    }
    
    private static class PropertiesManagerHolder {

        private static final PropertiesManager INSTANCE = new PropertiesManager();
    }
}
