/*
 */

package booknaviger.picturehandler;

import booknaviger.exceptioninterface.InfoInterface;
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
 * Handler for album as zip file
 */
public class ZipHandler extends AbstractImageHandler {
    
    ZipFile zipFile = null;
    List<ZipEntry> imageEntries = new ArrayList<>();

    /**
     * Constructor of this handler. Retrieve all the images in a list
     * @param album the zip album
     */
    public ZipHandler(File album) {
        Logger.getLogger(ZipHandler.class.getName()).entering(ZipHandler.class.getName(), "ZipHandler", album);
        try {
            zipFile = new ZipFile(album, Charset.forName("IBM437"));
        } catch (IOException ex) {
            Logger.getLogger(ZipHandler.class.getName()).log(Level.SEVERE, "Cannot read the ZIP archive", ex);
            if (!ex.getMessage().equals("zip file is empty")) {
                new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", album.getName());
            }
            Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "ZipHandler");
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
        Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "ZipHandler");
    }

    @Override
    public BufferedImage getImage(int pageNumber) {
        Logger.getLogger(ZipHandler.class.getName()).entering(ZipHandler.class.getName(), "getImage", pageNumber);
        if (!isImageInRange(pageNumber)) {
            Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "getImage", null);
            return null;
        }
        try {
            BufferedImage bufferedImage = new ImageReader(zipFile.getInputStream(imageEntries.get(--pageNumber))).readImage();
            Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "getImage", bufferedImage);
            return bufferedImage;
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot read the image from zip entry", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", imageEntries.get(pageNumber).getName());
        }
        Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "getImage", null);
        return null;
    }

    @Override
    public int getNbrOfPages() {
        Logger.getLogger(ZipHandler.class.getName()).entering(ZipHandler.class.getName(), "getNbrOfPages");
        Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "getNbrOfPages", imageEntries.size());
        return imageEntries.size();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        Logger.getLogger(ZipHandler.class.getName()).entering(ZipHandler.class.getName(), "finalize");
        zipFile.close();
        super.finalize();
        Logger.getLogger(ZipHandler.class.getName()).exiting(ZipHandler.class.getName(), "finalize");
    }

    @Override
    public List<String> getPagesName() {
        Logger.getLogger(ZipHandler.class.getName()).entering(ZipHandler.class.getName(), "getPagesName");
        List<String> pagesTitle = new ArrayList<>(imageEntries.size());
        for (int i = 0; i < imageEntries.size(); i++) {
            pagesTitle.add(imageEntries.get(i).getName());
        }
        Logger.getLogger(ZipHandler.class.getName()).entering(ZipHandler.class.getName(), "getPagesName", pagesTitle);
        return pagesTitle;
    }

}
