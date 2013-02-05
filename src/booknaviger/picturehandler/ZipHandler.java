/*
 */

package booknaviger.picturehandler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Inervo
 *
 */
public class ZipHandler extends AbstractImageHandler {
    
    ZipFile zipFile = null;
    List<ZipEntry> imageEntries = new ArrayList<>();

    public ZipHandler(File album) {
        try {
            zipFile = new ZipFile(album, Charset.forName("IBM437"));
        } catch (IOException ex) {
            Logger.getLogger(ZipHandler.class.getName()).log(Level.SEVERE, null, ex);
//            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Zip", album.toString());
//            previewComponent.setNoPreviewImage();
            return;
        }
        
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        List<? extends ZipEntry> entries = Collections.list(zipEntries);
        Collections.sort(entries, new Comparator<ZipEntry>() {

            @Override
            public int compare(ZipEntry o1, ZipEntry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        ListIterator<? extends ZipEntry> it = entries.listIterator();
        while(it.hasNext()) {
            ZipEntry currentEntry = it.next();
            if (!currentEntry.isDirectory() && isAnImage(currentEntry.getName())) {
                imageEntries.add(currentEntry);
            }
        }
    }

    @Override
    public BufferedImage getImage(int pageNumber) {
        if (!isImageInRange(pageNumber)) {
            return null;
        }
        try {
            return new ImageReader(zipFile.getInputStream(imageEntries.get(--pageNumber))).readImage();
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Unsupported_Zip_Compression", currentEntry.getName());
//            previewComponent.setNoPreviewImage();
        }
        return null;
    }

    @Override
    public int getNbrOfPages() {
        return imageEntries.size();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        zipFile.close();
        super.finalize();
    }

}
