/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package booknaviger.macworld;

import booknaviger.MainInterface;
import com.apple.mrj.MRJApplicationUtils;
import java.awt.Image;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used for everything Mac OS X related
 * @author Inervo
 */
public class MacOSXApplicationAdapter {


    /**
     * Constructor to the adapter for Mac OS X
     * @param mainInterface instance (which could still be in construction) of mainInterface
     */
    @SuppressWarnings("deprecation")
    public MacOSXApplicationAdapter(final MainInterface mainInterface) {
            Logger.getLogger(MacOSXApplicationAdapter.class.getName()).entering(MacOSXApplicationAdapter.class.getName(), "MacOSXApplicationAdapter", mainInterface);
            Logger.getLogger(MacOSXApplicationAdapter.class.getName()).log(Level.INFO, "Binding to the Mac OS X handlers");
        try {
            setFullScreenMode(mainInterface);
            
            // Dock icon
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            Method getApplicationMethod = applicationClass.getMethod("getApplication");
            Method method3 = applicationClass.getMethod("setDockIconImage", new Class[] {Image.class});
            method3.invoke(getApplicationMethod.invoke(applicationClass), new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/Application").getString("appLogoIcon"))).getImage());
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(MacOSXApplicationAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        MRJApplicationUtils.registerQuitHandler(() -> {
            MainInterface.getInstance().exit();
        });
        MRJApplicationUtils.registerAboutHandler(() -> {
            MainInterface.getInstance().openAboutDialog();
        });
        Logger.getLogger(MacOSXApplicationAdapter.class.getName()).exiting(MacOSXApplicationAdapter.class.getName(), "MacOSXApplicationAdapter");
    }
    
    public static void setFullScreenMode(Window window) {
        try {
            // Fullscreen
            Class<?> fullScreenUtilitiesClass = Class.forName("com.apple.eawt.FullScreenUtilities");
            Method setWindowCanFullScreenMethod = fullScreenUtilitiesClass.getMethod("setWindowCanFullScreen", new Class[] {Window.class, Boolean.TYPE});
            setWindowCanFullScreenMethod.invoke(fullScreenUtilitiesClass, window, true);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(MacOSXApplicationAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Set the properties for the mac interface
     */
    public static void setMacInterfaceAndCommands() {
        Logger.getLogger(MacOSXApplicationAdapter.class.getName()).entering(MacOSXApplicationAdapter.class.getName(), "setMacInterfaceAndCommands");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "BookNaviger"); // <--Useless / --> -Xcode:name="BookNaviger"
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        Logger.getLogger(MacOSXApplicationAdapter.class.getName()).entering(MacOSXApplicationAdapter.class.getName(), "setMacInterfaceAndCommands");
    }
}
