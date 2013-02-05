/*
 */

package booknaviger.picturehandler;

import java.awt.image.BufferedImage;

/**
 * @author Inervo
 *
 */
public abstract class AbstractImageHandler {
    
    public abstract BufferedImage getImage(int pageNumber);
    public abstract int getNbrOfPages();
    
    protected boolean isAnImage(String path) {
        if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".bmp")) {
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

}
