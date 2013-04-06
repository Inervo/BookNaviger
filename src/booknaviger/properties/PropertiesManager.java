/*
 */
package booknaviger.properties;

import booknaviger.osbasics.OSBasics;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
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
        File appDataDir = new File(OSBasics.getAppDataDir());
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
    
    public void removeKey(String keyString) {
        properties.remove(keyString);
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
