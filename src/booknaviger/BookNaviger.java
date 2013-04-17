/*
 */

package booknaviger;

import booknaviger.exceptioninterface.ExceptionHandler;
import booknaviger.exceptioninterface.InfoInterface;
import booknaviger.macworld.MacOSXApplicationAdapter;
import booknaviger.properties.PropertiesManager;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Inervo
 *
 */
public class BookNaviger {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        ExceptionHandler.registerExceptionHandler();
        Logger.getLogger(BookNaviger.class.getName()).log(Level.INFO, "The software is now starting !");
        preInterface();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                MainInterface.getInstance().setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(BookNaviger.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                    new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
                }
                Logger.getLogger(BookNaviger.class.getName()).log(Level.INFO, "MainInterface is now showing");
                MainInterface.getInstance().changeSelectedBook(PropertiesManager.getInstance().getKey("lastSelectedSerie"), PropertiesManager.getInstance().getKey("lastSelectedAlbum")).start();
            }
        });
    }
    
    /**
     * Set the parameters that must be defined before starting the GUI
     */
    private static void preInterface() {
        Logger.getLogger(BookNaviger.class.getName()).entering(BookNaviger.class.getName(), "preInterface");
        try {
            if (MacOSXApplicationAdapter.isMac()) {
                MacOSXApplicationAdapter.setMacInterfaceAndCommands();
            }
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(BookNaviger.class.getName()).log(Level.SEVERE, "Couldn't set the system LookAndFeel", ex);
            }
            String languageWanted = PropertiesManager.getInstance().getKey("language");
            if (languageWanted != null) {
                switch (languageWanted) {
                    case "fr":
                        Locale.setDefault(Locale.FRENCH);
                        Logger.getLogger(BookNaviger.class.getName()).log(Level.INFO, "Setting language to French");
                        break;
                    case "en":
                        Locale.setDefault(Locale.ENGLISH);
                        Logger.getLogger(BookNaviger.class.getName()).log(Level.INFO, "Setting language to English");
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(BookNaviger.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
        }
        Logger.getLogger(BookNaviger.class.getName()).exiting(BookNaviger.class.getName(), "preInterface");
    }

}
