/*
 */

package booknaviger.picturehandler;

import booknaviger.exceptioninterface.InfoInterface;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Inervo
 * Handler for album as Folder with images files within
 */
public class FolderHandler extends AbstractImageHandler {
    
    List<File>  imagefiles = new ArrayList<>();

    /**
     * Constructor of this handler. Retrieve all the images in a list
     * @param album the folder album
     */
    public FolderHandler(File album) {
        Logger.getLogger(FolderHandler.class.getName()).entering(FolderHandler.class.getName(), "FolderHandler", album);
        File[] allfiles = album.listFiles();
        try {
            allfiles = album.listFiles();
        } catch(SecurityException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "rights", album);
        }
        if (allfiles == null) {
            return;
        }
        Arrays.sort(allfiles);
        for (File allfile : allfiles) {
            if (!allfile.isHidden() && isAnImage(allfile.getName())) {
                imagefiles.add(allfile);
            }
        }
        Logger.getLogger(FolderHandler.class.getName()).exiting(FolderHandler.class.getName(), "FolderHandler");
    }
    
    @Override
    public BufferedImage getImage(int pageNumber) {
        Logger.getLogger(FolderHandler.class.getName()).entering(FolderHandler.class.getName(), "getImage", pageNumber);
        if (!isImageInRange(pageNumber)) {
            return null;
        }
        BufferedImage bufferedImage = new ImageReader(imagefiles.get(--pageNumber)).readImage();
        Logger.getLogger(FolderHandler.class.getName()).exiting(FolderHandler.class.getName(), "getImage", bufferedImage);
        return bufferedImage;
    }

    @Override
    public int getNbrOfPages() {
        Logger.getLogger(FolderHandler.class.getName()).entering(FolderHandler.class.getName(), "getNbrOfPages");
        Logger.getLogger(FolderHandler.class.getName()).exiting(FolderHandler.class.getName(), "getNbrOfPages", imagefiles.size());
        return imagefiles.size();
    }

    @Override
    public List<String> getPagesName() {
        Logger.getLogger(FolderHandler.class.getName()).entering(FolderHandler.class.getName(), "getPagesName");
        List<String> pagesTitle = new ArrayList<>(imagefiles.size());
        for (File imagefile : imagefiles) {
            pagesTitle.add(imagefile.getName());
        }
        Logger.getLogger(FolderHandler.class.getName()).entering(FolderHandler.class.getName(), "getPagesName", pagesTitle);
        return pagesTitle;
    }
}
