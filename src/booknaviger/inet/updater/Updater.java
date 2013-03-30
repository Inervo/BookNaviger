/*
 */

package booknaviger.inet.updater;

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
 * @author Inervo
 *
 */
public class Updater {
    
    String versionNumber = "";
    String downloadURLString = "";

    public Updater() {
    }
    
    public boolean isNewVersionAvailable() {
        PropertiesManager.getInstance().setKey("lastUpdateCheck", DateFormat.getDateInstance().format(new Date()));
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new URL("http://code.google.com/feeds/p/booknaviger/downloads/basic").openStream());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (doc == null) {
            return false;
        }
        NodeList entryNodeList = doc.getElementsByTagName("entry");
        for (int i = 0; i < entryNodeList.getLength(); i++) {
            if (i > 10) {
                return false;
            }
            NodeList entryNodeListChild = entryNodeList.item(i).getChildNodes();
            for (int j = 0; j < entryNodeListChild.getLength(); j++) {
                if (entryNodeListChild.item(j).getNodeName().equals("link")) {
                    downloadURLString = entryNodeListChild.item(j).getAttributes().getNamedItem("href").getTextContent();
                }
                if (entryNodeListChild.item(j).getNodeName().equals("title")) {
                    String version = getVersionFromFileName(entryNodeListChild.item(j).getTextContent().trim());
                    if (version == null) {
                        continue;
                    }
                    if (isVersionNumberGreaterThanCurrent(version)) {
                        versionNumber = version;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private String getVersionFromFileName(String fileName) {
        Pattern pattern;
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            pattern = Pattern.compile("^BookNaviger_mac[a-zA-Z]+_([\\d_]+).*", Pattern.CASE_INSENSITIVE);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            pattern = Pattern.compile("^BookNaviger_windows_([\\d_]+).*", Pattern.CASE_INSENSITIVE);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("linux") || System.getProperty("os.name").toLowerCase().startsWith("unix")) {
            pattern = Pattern.compile("^BookNaviger_unix_([\\d_]+).*", Pattern.CASE_INSENSITIVE);
        } else {
            return null;
        }
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private boolean isVersionNumberGreaterThanCurrent(String versionNumber) {
        int currentMajor;
        int versionCheckedMajor;
        int currentMinor;
        int versionCheckedMinor;
        int currentBuild;
        int versionCheckedBuild;
        
        String currentVersionNumber = ResourceBundle.getBundle("booknaviger/resources/Application").getString("appVersion");

        if (versionNumber == null || currentVersionNumber == null) {
            return false;
        }
        StringTokenizer currentVersionStringTokenizer = new StringTokenizer(currentVersionNumber, ".");
        StringTokenizer versionNumberStringTokenizer = new StringTokenizer(versionNumber, "_");
        if (!currentVersionStringTokenizer.hasMoreTokens() || !versionNumberStringTokenizer.hasMoreTokens()) {
            return false;
        }
        currentMajor = Integer.parseInt(currentVersionStringTokenizer.nextToken());
        versionCheckedMajor = Integer.parseInt(versionNumberStringTokenizer.nextToken());
        if (versionCheckedMajor > currentMajor) {
            return true;
        }
        if (currentMajor > versionCheckedMajor) {
            return false;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() && versionNumberStringTokenizer.hasMoreTokens()) {
            return true;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() || !versionNumberStringTokenizer.hasMoreTokens()) {
            return false;
        }
        currentMinor = Integer.parseInt(currentVersionStringTokenizer.nextToken());
        versionCheckedMinor = Integer.parseInt(versionNumberStringTokenizer.nextToken());
        if (versionCheckedMinor > currentMinor) {
            return true;
        }
        if (currentMinor > versionCheckedMinor) {
            return false;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() && versionNumberStringTokenizer.hasMoreTokens()) {
            return true;
        }
        if (!currentVersionStringTokenizer.hasMoreTokens() || !versionNumberStringTokenizer.hasMoreTokens()) {
            return false;
        }
        currentBuild = Integer.parseInt(currentVersionStringTokenizer.nextToken());
        versionCheckedBuild = Integer.parseInt(versionNumberStringTokenizer.nextToken());
        if (versionCheckedBuild > currentBuild) {
            return true;
        }
        return false;
    }

    public String getVersionNumber() {
        return versionNumber;
    }
    
    public String getDownloadURLString() {
        return downloadURLString;
    }

}
