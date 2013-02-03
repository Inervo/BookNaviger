/*
 */

package booknaviger.inet;

import booknaviger.MainInterface;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Inervo
 *
 */
public class InetBasics {

    public InetBasics() {
    }
    
    public static void openURI(String URIString) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(URIString));
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
