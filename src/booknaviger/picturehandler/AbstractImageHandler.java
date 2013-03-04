/*
 */

package booknaviger.picturehandler;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Inervo
 *
 */
public abstract class AbstractImageHandler {
    
    private BufferedImage preloadedImage = null;
    private boolean preloadingInProgress = false;
    
    public abstract int getNbrOfPages();
    public abstract BufferedImage getImage(int pageNumber);
    public abstract List<String> getPagesTitle();
    
    protected boolean isAnImage(String path) {
        if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg") || path.toLowerCase().endsWith(".gif") || path.toLowerCase().endsWith(".png") || path.toLowerCase().endsWith(".bmp")) {
            return true;
        }
        return false;
    }
    
    public boolean isImageInRange(int pageNumber) {
        pageNumber--; // is now pageIndex
        if (pageNumber >= getNbrOfPages() || pageNumber < 0) {
            return false;
        }
        return true;
    }

    public BufferedImage getPreloadedImage() {
        while (preloadingInProgress) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(AbstractImageHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return preloadedImage;
    }
    
    public synchronized void preloadNextImage(final int nextPageNumber, final boolean dualPageReadMode) {
        preloadingInProgress = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (preloadedImage != null) {
                    preloadedImage.flush();
                    preloadedImage = null;
                }
                if (dualPageReadMode) {
                    preloadedImage = ImageReader.combine2Images(getImage(nextPageNumber+1), getImage(nextPageNumber+2));
                } else {
                    preloadedImage = getImage(nextPageNumber);
                }
                preloadingInProgress = false;
            }
        }).start();
     }

}
