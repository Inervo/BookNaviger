/*
 */

package booknaviger.picturehandler;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;

/**
 * @author Inervo
 *
 */
public class PdfHandler extends AbstractImageHandler {
    
    Document pdfDocument = null;

    public PdfHandler(File album) {
        pdfDocument = new Document();
        try {
            pdfDocument.setFile(album.getPath());
        } catch (PDFException | PDFSecurityException | IOException ex) {
            Logger.getLogger(PdfHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public BufferedImage getImage(int pageNumber) {
        if (!isImageInRange(pageNumber)) {
            return null;
        }
        BufferedImage bufferedImage;
        try {
            bufferedImage = (BufferedImage) pdfDocument.getPageImages(--pageNumber).get(0);
        }
        catch (ClassCastException ex) {
            Image image = (Image) pdfDocument.getPageImages(pageNumber).get(0);
            bufferedImage = new ImageReader(image).convertImageToBufferedImage();
        }
        return bufferedImage;
    }

    @Override
    public int getNbrOfPages() {
        return pdfDocument.getNumberOfPages();
    }

}
