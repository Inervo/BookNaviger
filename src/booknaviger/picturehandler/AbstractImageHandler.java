/*
 */

package booknaviger.picturehandler;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Inervo
 * Abstract handler for the albums. 
 * @see FolderHandler
 * @see PdfHandler
 * @see RarHandler
 * @see ZipHandler
 */
public abstract class AbstractImageHandler {
    
    private BufferedImage preloadedImage = null;
    private boolean preloadingInProgress = false;
    
    /**
     * Get the number of pages for the current album
     * @return The number of pages
     */
    public abstract int getNbrOfPages();
    /**
     * Get the image for the album page specified
     * @param pageNumber the page to get the image from
     * @return the image for the page specified
     */
    public abstract BufferedImage getImage(int pageNumber);
    /**
     * get the list of the pages name (the filename of the file usualy)
     * @return a List of String of the pagesName
     */
    public abstract List<String> getPagesName();
    
    /**
     * Check of the specified path points to an image
     * @param path the path to the file which must be tested
     * @return true if it's an image<br /> false otherwise
     */
    protected boolean isAnImage(String path) {
        Logger.getLogger(AbstractImageHandler.class.getName()).entering(AbstractImageHandler.class.getName(), "isAnImage", path);
        if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg") || path.toLowerCase().endsWith(".gif") || path.toLowerCase().endsWith(".png") || path.toLowerCase().endsWith(".bmp")) {
            Logger.getLogger(AbstractImageHandler.class.getName()).exiting(AbstractImageHandler.class.getName(), "isAnImage", true);
            return true;
        }
        Logger.getLogger(AbstractImageHandler.class.getName()).exiting(AbstractImageHandler.class.getName(), "isAnImage", false);
        return false;
    }
    
    /**
     * Check if the pageNumber given is within the range of pages the album of this handler have
     * @param pageNumber the page number to check (first page is pageNumber=1)
     * @return true if it's within range<br />false otherwise
     */
    public boolean isImageInRange(int pageNumber) {
        Logger.getLogger(AbstractImageHandler.class.getName()).entering(AbstractImageHandler.class.getName(), "isImageInRange", pageNumber);
        pageNumber--; // is now pageIndex
        if (pageNumber >= getNbrOfPages() || pageNumber < 0) {
            Logger.getLogger(AbstractImageHandler.class.getName()).exiting(AbstractImageHandler.class.getName(), "isImageInRange", true);
            return false;
        }
        Logger.getLogger(AbstractImageHandler.class.getName()).exiting(AbstractImageHandler.class.getName(), "isImageInRange", true);
        return true;
    }

    /**
     * Get the Image which have been preloaded
     * @return The preloaded Image
     */
    public BufferedImage getPreloadedImage() {
        Logger.getLogger(AbstractImageHandler.class.getName()).entering(AbstractImageHandler.class.getName(), "getPreloadedImage");
        while (preloadingInProgress) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(AbstractImageHandler.class.getName()).log(Level.SEVERE, "The sleep for waiting the preload of the image has been interrupted", ex);
            }
        }
        Logger.getLogger(AbstractImageHandler.class.getName()).log(Level.CONFIG, "Retreive the preloaded image");
        Logger.getLogger(AbstractImageHandler.class.getName()).exiting(AbstractImageHandler.class.getName(), "getPreloadedImage", preloadedImage);
        return preloadedImage;
    }
    
    /**
     * Preload a given page
     * @param nextPageNumber the page number to preload
     * @param dualPageReadMode Set to true if the dual mode page is activated (2 images to preload, and then combine)
     */
    public synchronized void preloadNextImage(final int nextPageNumber, final boolean dualPageReadMode) {
        Logger.getLogger(AbstractImageHandler.class.getName()).entering(AbstractImageHandler.class.getName(), "preloadNextImage", new Object[] {nextPageNumber, dualPageReadMode});
        preloadingInProgress = true;
        new Thread(() -> {
            if (preloadedImage != null) {
                preloadedImage.flush();
                preloadedImage = null;
            }
            Logger.getLogger(AbstractImageHandler.class.getName()).log(Level.CONFIG, "Preload page {0}", nextPageNumber);
            if (dualPageReadMode) {
                preloadedImage = ImageReader.combine2Images(getImage(nextPageNumber+1), getImage(nextPageNumber+2));
            } else {
                preloadedImage = getImage(nextPageNumber);
            }
            preloadingInProgress = false;
            Logger.getLogger(AbstractImageHandler.class.getName()).exiting(AbstractImageHandler.class.getName(), "preloadNextImage");
        }).start();
     }

}
