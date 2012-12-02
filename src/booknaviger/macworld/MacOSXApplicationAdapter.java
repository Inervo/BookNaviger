/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package booknaviger.macworld;

import booknaviger.BookNavigerApp;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.AppReOpenedEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.AppReOpenedListener;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import java.awt.SystemTray;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Inervo
 */
public class MacOSXApplicationAdapter {

    BookNavigerApp bna = null;

    /**
     * Constructeur de l'adapteur de l'application pour Mac OS X
     * @param bna instance de BookNavigerApp
     */
    public MacOSXApplicationAdapter(final BookNavigerApp bna) {
        this.bna = bna;
        com.apple.eawt.Application.getApplication().setAboutHandler(new AboutHandler() {

            /**
             * Handle sur le menu about
             * @param ae Event
             */
            @Override
            public void handleAbout(AboutEvent ae) {
                bna.getBnv().showAboutBox();
            }
        });
        com.apple.eawt.Application.getApplication().setQuitHandler(new QuitHandler() {

            /**
             * Handle sur le quit
             * @param ae Event
             */
            @Override
            public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                bna.exit();
            }
        });
        com.apple.eawt.Application.getApplication().addAppEventListener(new AppReOpenedListener() {

            /**
             * Handle sur le fait de cliquer sur le logo de l'appli
             * @param ae Event
             */
            @Override
            public void appReOpened(AppReOpenedEvent aroe) {
                if (bna == null)
                    return;
                if (bna.getBnv() == null)
                    return;
                if (bna.getBnv().getFrame().isDisplayable() && !bna.getBnv().getFrame().isVisible())
                    bna.getBnv().getFrame().setVisible(true);
                if (bna.getBnv().getReadView() == null)
                    return;
                if (bna.getBnv().getReadView().isDisplayable() && !bna.getBnv().getReadView().isVisible()) {
                    bna.getBnv().getReadView().setVisible(true);
                    SystemTray sysTray = SystemTray.getSystemTray();
                    sysTray.remove(sysTray.getTrayIcons()[0]);
                }
            }
        });
        ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap();
        com.apple.eawt.Application.getApplication().setDockIconImage(resourceMap.getImageIcon("Application.logo").getImage());
    }
}
