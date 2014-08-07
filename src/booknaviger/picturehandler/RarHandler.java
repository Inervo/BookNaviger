/*
 */

package booknaviger.picturehandler;

import booknaviger.exceptioninterface.InfoInterface;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Inervo
 * Handler for album as rar file
 */
public class RarHandler extends AbstractImageHandler {
    Archive archive = null;
    List<FileHeader> compressedFilesHeaders = new ArrayList<>();

    /**
     * Constructor of this handler. Retrieve all the images in a list
     * @param album the rar album
     */
    public RarHandler(File album) {
        Logger.getLogger(RarHandler.class.getName()).entering(RarHandler.class.getName(), "RarHandler", album);
        try {
            archive = new Archive(album);
        } catch (RarException | IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Cannot read the RAR archive", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", album.getName());
            Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "RarHandler");
            return;
        }
        if (archive.isEncrypted()) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "RAR archive encrypted");
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", album.getName());
            Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "RarHandler");
            return;
        }
        List<FileHeader> fh = archive.getFileHeaders();
        Collections.sort(fh, (FileHeader o1, FileHeader o2) -> o1.getFileNameString().compareTo(o2.getFileNameString()));
        ListIterator<FileHeader> it = fh.listIterator();
        while (it.hasNext()) {
            FileHeader currentEntry = it.next();
            if (!currentEntry.isDirectory() && isAnImage(currentEntry.getFileNameString())) {
                compressedFilesHeaders.add(currentEntry);
            }
        }
        Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "RarHandler");
    }

    @Override
    public BufferedImage getImage(int pageNumber) {
        Logger.getLogger(RarHandler.class.getName()).entering(RarHandler.class.getName(), "getImage", pageNumber);
        if (!isImageInRange(pageNumber)) {
            Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "getImage", null);
            return null;
        }
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = null;
        try {
            pos = new PipedOutputStream(pis);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Can't get the output stream from input stream", ex);
        }
        new ExtractFileFromRar(compressedFilesHeaders.get(--pageNumber), pos).start();
        BufferedImage bufferedImage = new ImageReader(pis).readImage();
        Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "getImage", bufferedImage);
        return bufferedImage;
    }

    @Override
    public List<String> getPagesName() {
        Logger.getLogger(RarHandler.class.getName()).entering(RarHandler.class.getName(), "getPagesName");
        List<String> pagesTitle = new ArrayList<>(compressedFilesHeaders.size());
        compressedFilesHeaders.stream().forEach((compressedFilesHeader) -> {
            pagesTitle.add(compressedFilesHeader.getFileNameString());
        });
        Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "getPagesName", pagesTitle);
        return pagesTitle;
    }
    
    /**
     * This class is made to extract and read the image from the archive to an output stream in a different thread
     * so that the RarHandler class can read the output stream in input stream
     */
    private class ExtractFileFromRar extends Thread {
        Archive a = null;
        FileHeader fh = null;
        OutputStream os = null;

        /**
         * Constructor to set the needed parameters
         * @param fh the file header of the archive we want to read and extract data from
         * @param os the output stream to extract the data
         */
        public ExtractFileFromRar(FileHeader fh, OutputStream os) {
            Logger.getLogger(ExtractFileFromRar.class.getName()).entering(ExtractFileFromRar.class.getName(), "ExtractFileFromRar", new Object[] {fh, os});
            this.a = archive;
            this.fh = fh;
            this.os = os;
            Logger.getLogger(ExtractFileFromRar.class.getName()).exiting(ExtractFileFromRar.class.getName(), "ExtractFileFromRar");
        }

        @Override
        public void run() {
            Logger.getLogger(ExtractFileFromRar.class.getName()).entering(ExtractFileFromRar.class.getName(), "run");
            try {
                a.extractFile(fh, os);
            } catch (RarException ex) {
                Logger.getLogger(RarHandler.class.getName()).log(Level.SEVERE, "Cannot extract the file from the RAR", ex);
                new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", fh.getFileNameString());
            } finally {
                try {
                    os.close();
                } catch (IOException ex) {
                    Logger.getLogger(RarHandler.class.getName()).log(Level.SEVERE, "Cannot close the OutputStream", ex);
                }
            }
            Logger.getLogger(ExtractFileFromRar.class.getName()).exiting(ExtractFileFromRar.class.getName(), "run");
        }
        
    }

    @Override
    public int getNbrOfPages() {
        Logger.getLogger(RarHandler.class.getName()).entering(RarHandler.class.getName(), "getNbrOfPages");
        Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "getNbrOfPages", compressedFilesHeaders.size());
        return compressedFilesHeaders.size();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        Logger.getLogger(RarHandler.class.getName()).entering(RarHandler.class.getName(), "finalize");
        archive.close();
        super.finalize();
        Logger.getLogger(RarHandler.class.getName()).exiting(RarHandler.class.getName(), "finalize");
    }

}
