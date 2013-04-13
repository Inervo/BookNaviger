/*
 */
package booknaviger.properties;

import booknaviger.exceptioninterface.InfoInterface;
import booknaviger.osbasics.OSBasics;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class the manage the properties of this software
 * @author Inervo
 */
public class PropertiesManager {
    
    private File propertiesFile = null;
    private Properties properties = new Properties();
    
    /**
     * Constructor to set the properties file path, and load it.
     */
    private PropertiesManager() {
        Logger.getLogger(PropertiesManager.class.getName()).entering(PropertiesManager.class.getName(), "PropertiesManager");
        Logger.getLogger(PropertiesManager.class.getName()).log(Level.INFO, "Loading the properties");
        File appDataDir = new File(OSBasics.getAppDataDir());
        propertiesFile = new File(appDataDir, "booknaviger.properties");
        try {
            propertiesFile.createNewFile();
            properties.load(new FileReader(propertiesFile));
        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, "The properties couldn't be loaded", ex);
            new InfoInterface(InfoInterface.ERROR, "properties-load", propertiesFile.toString());
        }
        Logger.getLogger(PropertiesManager.class.getName()).exiting(PropertiesManager.class.getName(), "PropertiesManager");
    }
    
    /**
     * Get the Value of a key
     * @param keyString The keystring of the value
     * @return The value for the searched key
     */
    public String getKey(String keyString) {
        Logger.getLogger(PropertiesManager.class.getName()).entering(PropertiesManager.class.getName(), "getKey", keyString);
        Logger.getLogger(PropertiesManager.class.getName()).exiting(PropertiesManager.class.getName(), "getKey", properties.getProperty(keyString));
        Logger.getLogger(PropertiesManager.class.getName()).log(Level.CONFIG, "Getting the key \"{0}\"", keyString);
        return properties.getProperty(keyString);
    }
    
    /**
     * Set a key to a new value
     * @param keyString The key to set the value to
     * @param value The value for the key
     */
    public void setKey(String keyString, String value) {
        Logger.getLogger(PropertiesManager.class.getName()).entering(PropertiesManager.class.getName(), "setKey", new Object[] {keyString, value});
        Logger.getLogger(PropertiesManager.class.getName()).log(Level.CONFIG, "Setting the key \"{0}\" to value \"{1}\"", new Object[] {keyString, value});
        properties.setProperty(keyString, value);
        Logger.getLogger(PropertiesManager.class.getName()).exiting(PropertiesManager.class.getName(), "setKey");
    }
    
    /**
     * Delete a key
     * @param keyString The key string to remove
     */
    public void removeKey(String keyString) {
        Logger.getLogger(PropertiesManager.class.getName()).entering(PropertiesManager.class.getName(), "removeKey", keyString);
        Logger.getLogger(PropertiesManager.class.getName()).log(Level.CONFIG, "Removing the key \"{0}\"", keyString);
        properties.remove(keyString);
        Logger.getLogger(PropertiesManager.class.getName()).exiting(PropertiesManager.class.getName(), "removeKey");
    }
    
    /**
     * Save the properties to the file
     */
    public void saveProperties() {
        Logger.getLogger(PropertiesManager.class.getName()).entering(PropertiesManager.class.getName(), "saveProperties");
        Logger.getLogger(PropertiesManager.class.getName()).log(Level.INFO, "Saving the properties");
        try {
            properties.store(new FileWriter(propertiesFile), "Every properties is written here");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, "Couldn't save the properties", ex);
            new InfoInterface(InfoInterface.ERROR, "properties-save", propertiesFile.toString());
        }
        Logger.getLogger(PropertiesManager.class.getName()).exiting(PropertiesManager.class.getName(), "saveProperties");
    }
    
    /**
     * Get the unique instance of this class
     * @return The unique instance of this class
     */
    public static PropertiesManager getInstance() {
        Logger.getLogger(PropertiesManager.class.getName()).entering(PropertiesManager.class.getName(), "getInstance");
        Logger.getLogger(PropertiesManager.class.getName()).exiting(PropertiesManager.class.getName(), "getInstance", PropertiesManagerHolder.INSTANCE);
        return PropertiesManagerHolder.INSTANCE;
    }
    
    /**
     * Holder of the unique instance of <code>PropertiesManager</code>.
     */
    private static class PropertiesManagerHolder {

        private static final PropertiesManager INSTANCE = new PropertiesManager();
    }
}
