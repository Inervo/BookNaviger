/*
 */

package booknaviger.picturehandler;

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
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Inervo
 *
 */
public class RarHandler extends AbstractImageHandler {
    Archive archive = null;
    List<FileHeader> compressedFilesHeaders = new ArrayList<>();

    public RarHandler(File album) {
        try {
            archive = new Archive(album);
        } catch (RarException | IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
//            previewComponent.setNoPreviewImage();
            return;
        }
        List<FileHeader> fh = archive.getFileHeaders();
        Collections.sort(fh, new Comparator<FileHeader>() {

                @Override
                public int compare(FileHeader o1, FileHeader o2) {
                    return o1.getFileNameString().compareTo(o2.getFileNameString());
                }
            });
        ListIterator<FileHeader> it = fh.listIterator();
        while (it.hasNext()) {
            FileHeader currentEntry = it.next();
            if (!currentEntry.isDirectory() && isAnImage(currentEntry.getFileNameString())) {
                compressedFilesHeaders.add(currentEntry);
            }
        }
    }

    @Override
    public BufferedImage getImage(int pageNumber) {
        pageNumber--; // is now pageIndex
        if (compressedFilesHeaders.isEmpty() || pageNumber >= (compressedFilesHeaders.size()) || pageNumber < 0) {
            return null;
        }
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = null;
        try {
            pos = new PipedOutputStream(pis);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        new ExtractFileFromRar(compressedFilesHeaders.get(pageNumber), pos).start();
        return new ImageReader(pis).readImage();
    }
    
    private class ExtractFileFromRar extends Thread {
        Archive a = null;
        FileHeader fh = null;
        OutputStream os = null;

        public ExtractFileFromRar(FileHeader fh, OutputStream os) {
            this.a = archive;
            this.fh = fh;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                a.extractFile(fh, os);
            } catch (RarException ex) {
                Logger.getLogger(RarHandler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    os.close();
                } catch (IOException ex) {
                    Logger.getLogger(RarHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }

    @Override
    public int getNbrOfPages() {
        return compressedFilesHeaders.size();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        archive.close();
        super.finalize();
    }
    
    

}
