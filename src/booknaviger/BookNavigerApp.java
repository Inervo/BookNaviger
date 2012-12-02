/*
 * BookNavigerApp.java
 */

package booknaviger;

import booknaviger.macworld.MacOSXApplicationAdapter;
import booknaviger.errorhandler.ErrorOutputStream;
import booknaviger.errorhandler.KnownErrorBox;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class BookNavigerApp extends SingleFrameApplication {

    private static boolean IS_MAC = false;
    /**
     * Instance de bnv
     */
    protected BookNavigerView bnv = null;
    /**
     * Fichier de propriétés contenant les series / albums / pages lus, sélectionnés
     */
    protected Properties p = new Properties();
    private File propertiesFile = null;
    static protected String language = null;

    /**
     * Informe si le système hôte est un Mac OS X
     * @return true si mac, false sinon
     */
    public static boolean IS_MAC() {
        return IS_MAC;
    }

    /**
     * Retourne l'instance de BookNavigerView
     * @return l'instance de bnv
     */
    public BookNavigerView getBnv() {
        return bnv;
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (IS_MAC) {
                    new MacOSXApplicationAdapter(getApplication());
                }
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ResourceMap resourceMap = Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getResourceMap(BookNavigerApp.class);
                    String userHome = System.getProperty("user.home");
                    String appName = resourceMap.getString("Application.name");
                    File appDataDir = null;
                    if (IS_MAC()) {
                        appDataDir = new File(userHome, "Library" + File.separatorChar + "Application Support"+ File.separatorChar + appName);
                    } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                        appDataDir = new File(System.getenv("APPDATA"), appName);
                    } else {
                        appDataDir = new File(userHome, "." + appName);
                    }
                    if (!appDataDir.exists())
                        appDataDir.mkdirs();
                    propertiesFile = new File(appDataDir, "booknaviger.props");
                    p.load(new FileReader(propertiesFile));
                } catch (IOException ex) {}
                String language = p.getProperty("Language");
                if (language != null) {
                    Locale.setDefault(new Locale(language.substring(0, 2), language.substring(3, 5)));
                    BookNavigerApp.language = Locale.getDefault().toString();
                }
                StaticWorld.registerErrorDialogWindow();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            bnv = new BookNavigerView(getApplication());
                            show(bnv);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                bnv.loadAndApplyProperties();
                p.clear();
            }
        }).start();
    }

    /**
     * Executé à l'arrêt du programme (plus spécifiquement, de la partie graphique)
     */
    @Override
    protected void shutdown() {
        class QuitRunnable extends Thread {
            public boolean errorWritingProperties = false;
            public boolean errorClosingErrorFile = false;

            @Override
            public void run() {
                try {
                    saveProperties();
                } catch (IOException ex) {
                    errorWritingProperties = true;
                }
                try {
                    ErrorOutputStream.getErrorFileOS().close();
                } catch (IOException ex) {
                    errorClosingErrorFile = true;
                }
            }
        }
        QuitRunnable quitRunnable = new QuitRunnable();
        quitRunnable.start();
        try {
            quitRunnable.join(20000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BookNavigerApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (quitRunnable.errorWritingProperties)
            new KnownErrorBox(null, KnownErrorBox.ERROR_LOGO, "Error_Saving_Properties", propertiesFile.toString());
        if (quitRunnable.errorClosingErrorFile)
            new KnownErrorBox(null, KnownErrorBox.ERROR_LOGO, "Error_Closing_ErrorLog_File");
        super.shutdown();
    }

    /**
     * Sauvegarde des propriétés de series, albums, basedir, pages, etc etc
     */
    private void saveProperties() throws IOException {
        saveProfiles();
        if (bnv.serie != null)
            p.put("CurrentSerieSelected", bnv.serie.getName());
        if (bnv.album != null)
            p.put("CurrentAlbumSelected", bnv.album.getName());
        if (bnv.lastReadedProfile != null)
            p.put("LastReadedProfile", bnv.lastReadedProfile);
        if (bnv.lastReadedSerie != null)
            p.put("LastReadedSerie", bnv.lastReadedSerie);
        if (bnv.lastReadedAlbum != null)
            p.put("LastReadedAlbum", bnv.lastReadedAlbum);
        p.put("LastReadedPage", bnv.lastReadedPage.toString());
        p.put("AutoCheckUpdates", (bnv.checkUpdatesCheckBoxMenuItem.isSelected()) ? "true" : "false");
        if (bnv.lastUpdateCheck != null)
            p.put("LastUpdatesChecked", DateFormat.getDateInstance().format(bnv.lastUpdateCheck));
        if (language != null)
            p.put("Language", language);
        p.store(new FileWriter(propertiesFile), null);
    }

    private void saveProfiles() {
        String profiles = "";
        for (int i = 0; i < bnv.profiles.length; i++) {
            profiles = profiles.concat(bnv.profiles[i][0]).concat(",");
            profiles = (bnv.profiles[i][1] == null) ? profiles.concat("") : profiles.concat(bnv.profiles[i][1]);
            profiles = profiles.concat(";");
        }
        p.put("Profiles", profiles);
        p.put("CurrentProfile", ((Short)bnv.currentProfile).toString());
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     * @param root
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of BookNavigerApp
     */
    public static BookNavigerApp getApplication() {
        return Application.getInstance(BookNavigerApp.class);
    }

    /**
     * Main method launching the application.
     * @param args 
     */
    public static void main(String[] args) {
        IS_MAC = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
        if (IS_MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "BookNaviger");
        }
        launch(BookNavigerApp.class, args);
    }
}
