/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package booknaviger.errorhandler;

import booknaviger.BookNavigerApp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Inervo
 */
public class ErrorOutputStream extends OutputStream {

    private ErrorBox eb = null;
    private static FileOutputStream errorFileOS = null;
    private PrintStream stdErr = System.err;
    private Thread showErrorBox = null;

    /**
     * Constructeur de l'outputstream
     * @param eb Instance d'ErrorBox vers laquelle envoyer les messages d'erreurs
     * @param errorFileString Fichier vers lequel sauver les erreurs
     */
    public ErrorOutputStream(ErrorBox eb, String errorFileString) {
        this.eb = eb;
        try {
            ResourceMap resourceMap = Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getResourceMap(ErrorOutputStream.class);
            String userHome = System.getProperty("user.home");
            String appName = resourceMap.getString("Application.name");
            File appDataDir = null;
            if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
                appDataDir = new File(userHome, "Library" + File.separatorChar + "Application Support"+ File.separatorChar + appName);
            } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                appDataDir = new File(System.getenv("APPDATA"), appName);
            } else {
                appDataDir = new File(userHome, "." + appName);
            }
            if (!appDataDir.exists())
                appDataDir.mkdirs();
            errorFileOS = new FileOutputStream(new File(appDataDir, errorFileString), true);
        } catch (FileNotFoundException ex) {
            new KnownErrorBox(null, KnownErrorBox.ERROR_LOGO, "Error_Saving_ErrorLog_File", errorFileString);
        }
    }

    /**
     * Retourne l'outputstream sur le fichier d'erreur
     * @return l'outputstream vers le fichier d'erreur
     */
    public static FileOutputStream getErrorFileOS() {
        return errorFileOS;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        write(new byte[] {(byte)b}, 0, 1);
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        String newText = new String(b, off, len);
        if (showErrorBox != null && showErrorBox.isAlive()) {
            while (!eb.getErrorTextArea().isShowing())
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ErrorOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        eb.getErrorTextArea().setText(eb.getErrorTextArea().getText() + newText);
        stdErr.print(newText);
        stdErr.flush();
        errorFileOS.write(b, off, len);
        errorFileOS.flush();
        if (showErrorBox == null || !showErrorBox.isAlive()) {
            showErrorBox = new Thread() {

                @Override
                public void run() {
                    eb.setVisible(true);
                }
            };
            SwingUtilities.invokeLater(showErrorBox);
        }
    }

}
