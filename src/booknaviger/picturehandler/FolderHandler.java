/*
 */

package booknaviger.picturehandler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Inervo
 *
 */
public class FolderHandler extends AbstractImageHandler {
    
    List<File>  imagefiles = new ArrayList<>();

    public FolderHandler(File album) {
        File[] allfiles = album.listFiles();
        try {
            allfiles = album.listFiles();
        } catch(SecurityException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            //new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rights", serie.toString());
            //previewComponent.setNoPreviewImage();
        }
        if (allfiles != null) {
            Arrays.sort(allfiles);
        }
        for (int i = 0; i < allfiles.length; i++) {
            if (!allfiles[i].isHidden() && isAnImage(allfiles[i].getName())) {
                imagefiles.add(allfiles[i]);
            }
        }
    }
    
    @Override
    public BufferedImage getImage(int pageNumber) {
        if (!isImageInRange(pageNumber)) {
            return null;
        }
        return new ImageReader(imagefiles.get(--pageNumber)).readImage();
    }

    @Override
    public int getNbrOfPages() {
        return imagefiles.size();
    }

    @Override
    public List<String> getPagesTitle() {
        List<String> pagesTitle = new ArrayList<>(imagefiles.size());
        for (int i = 0; i < imagefiles.size(); i++) {
            pagesTitle.add(imagefiles.get(i).getName());
        }
        return pagesTitle;
    }
}
