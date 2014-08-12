/*
 */

package booknaviger.inet.updater;

import booknaviger.exceptioninterface.InfoInterface;
import booknaviger.properties.PropertiesManager;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to check if a new version is available
 * @author Inervo
 */
public class Updater {
    
    String versionNumber = "";
    String downloadURLString = "";

    /**
     * Constructor. Does nothing.
     */
    public Updater() {
        Logger.getLogger(Updater.class.getName()).entering(Updater.class.getName(), "Updater");
        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "Updater");
    }
    
    /**
     * Check if a new version is available
     * @return True if a new version is available<br />false otherwise
     */
    public boolean isNewVersionAvailable() {
        Logger.getLogger(Updater.class.getName()).entering(Updater.class.getName(), "isNewVersionAvailable");
        PropertiesManager.getInstance().setKey("lastUpdateCheck", DateFormat.getDateInstance().format(new Date()));
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(ResourceBundle.getBundle("booknaviger/resources/Application").getString("appUpdateFeedURL")).openStream());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.WARNING, "Can't parse the update feed URL", ex);
            if (ex instanceof ParserConfigurationException || ex instanceof SAXException) {
                new InfoInterface(InfoInterface.InfoLevel.WARNING, "update-parseerror");
            }
        }
        if (doc == null) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isNewVersionAvailable", false);
            return false;
        }
        NodeList entryNodeList = doc.getElementsByTagName("entry");
        for (int i = 0; i < entryNodeList.getLength(); i++) {
            if (i > 10) {
                Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isNewVersionAvailable", false);
                return false;
            }
            NodeList entryNodeListChild = entryNodeList.item(i).getChildNodes();
            for (int j = 0; j < entryNodeListChild.getLength(); j++) {
                if (entryNodeListChild.item(j).getNodeName().equals("link")) {
                    downloadURLString = "https://github.com" + entryNodeListChild.item(j).getAttributes().getNamedItem("href").getTextContent();
                }
                if (entryNodeListChild.item(j).getNodeName().equals("title")) {
                    String version = getVersionFromFileName(entryNodeListChild.item(j).getTextContent().trim());
                    if (version == null) {
                        continue;
                    }
                    if (isVersionNumberGreaterThanCurrent(version)) {
                        versionNumber = version;
                        Logger.getLogger(Updater.class.getName()).log(Level.INFO, "A new version ({0}) is available", version);
                        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isNewVersionAvailable", true);
                        return true;
                    }
                }
            }
        }
        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isNewVersionAvailable", false);
        return false;
    }
    
    /**
     * Parse the executable file name and extract the version number (as 1.12.2 for example)
     * @param fileName The filename of the executable
     * @return String of the version number
     */
    private String getVersionFromFileName(String fileName) {
        Logger.getLogger(Updater.class.getName()).entering(Updater.class.getName(), "getVersionFromFileName", fileName);
        Pattern pattern;
        pattern = Pattern.compile("([\\d\\.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            Logger.getLogger(Updater.class.getName()).log(Level.CONFIG, "Version parsed = {0}", matcher.group(1));
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "getVersionFromFileName", matcher.group(1));
            return matcher.group(1);
        }
        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "getVersionFromFileName", null);
        return null;
    }
    
    /**
     * Compare the given version number to the actual running application version number
     * @param versionNumber the version to compare to the current
     * @return true if the given version is greater than current<br />false otherwise
     */
    private boolean isVersionNumberGreaterThanCurrent(String versionNumber) {
        Logger.getLogger(Updater.class.getName()).entering(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", versionNumber);
        int currentMajor;
        int versionCheckedMajor;
        int currentMinor;
        int versionCheckedMinor;
        int currentBuild;
        int versionCheckedBuild;
        String currentVersionNumber = ResourceBundle.getBundle("booknaviger/resources/Application").getString("appVersion");
        if (versionNumber == null || currentVersionNumber == null) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
            return false;
        }
        StringTokenizer currentVersionStringTokenizer = new StringTokenizer(currentVersionNumber, ".");
        StringTokenizer versionNumberStringTokenizer = new StringTokenizer(versionNumber, ".");
        if (!currentVersionStringTokenizer.hasMoreTokens() || !versionNumberStringTokenizer.hasMoreTokens()) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
            return false;
        }
        currentMajor = Integer.parseInt(currentVersionStringTokenizer.nextToken());
        versionCheckedMajor = Integer.parseInt(versionNumberStringTokenizer.nextToken());
        if (versionCheckedMajor > currentMajor) {
            Logger.getLogger(Updater.class.getName()).log(Level.CONFIG, "Version number is greater than current");
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", true);
            return true;
        }
        if (currentMajor > versionCheckedMajor) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
            return false;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() && versionNumberStringTokenizer.hasMoreTokens()) {
            Logger.getLogger(Updater.class.getName()).log(Level.CONFIG, "Version number is greater than current");
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", true);
            return true;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() || !versionNumberStringTokenizer.hasMoreTokens()) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
            return false;
        }
        currentMinor = Integer.parseInt(currentVersionStringTokenizer.nextToken());
        versionCheckedMinor = Integer.parseInt(versionNumberStringTokenizer.nextToken());
        if (versionCheckedMinor > currentMinor) {
            Logger.getLogger(Updater.class.getName()).log(Level.CONFIG, "Version number is greater than current");
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", true);
            return true;
        }
        if (currentMinor > versionCheckedMinor) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
            return false;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() && versionNumberStringTokenizer.hasMoreTokens()) {
            Logger.getLogger(Updater.class.getName()).log(Level.CONFIG, "Version number is greater than current");
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", true);
            return true;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() || !versionNumberStringTokenizer.hasMoreTokens()) {
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
            return false;
        }
        currentBuild = Integer.parseInt(currentVersionStringTokenizer.nextToken());
        versionCheckedBuild = Integer.parseInt(versionNumberStringTokenizer.nextToken());
        if (versionCheckedBuild > currentBuild) {
            Logger.getLogger(Updater.class.getName()).log(Level.CONFIG, "Version number is greater than current");
            Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", true);
            return true;
        }
        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "isVersionNumberGreaterThanCurrent", false);
        return false;
    }

    /**
     * Get the version number as string
     * @return The string of the version number
     */
    public String getVersionNumber() {
        Logger.getLogger(Updater.class.getName()).entering(Updater.class.getName(), "getVersionNumber");
        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "getVersionNumber", versionNumber);
        return versionNumber;
    }
    
    /**
     * Get the download url for the last parsed version number (so the most recent scanned)
     * @return The download url string
     */
    public String getDownloadURLString() {
        Logger.getLogger(Updater.class.getName()).entering(Updater.class.getName(), "getVersionNumber");
        Logger.getLogger(Updater.class.getName()).exiting(Updater.class.getName(), "getVersionNumber", downloadURLString);
        return downloadURLString;
    }
    
}
