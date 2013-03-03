/*
 */

package booknaviger;

import booknaviger.macworld.MacOSXApplicationAdapter;
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
        preInterface();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainInterface.getInstance().setVisible(true);
            }
        });
    }
    
    private static void preInterface() {
        if (MacOSXApplicationAdapter.isMac()) {
            MacOSXApplicationAdapter.setMacInterfaceAndCommands();
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
