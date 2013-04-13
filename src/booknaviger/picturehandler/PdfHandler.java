/*
 */

package booknaviger.picturehandler;

import booknaviger.MainInterface;
import booknaviger.exceptioninterface.InfoInterface;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;

/**
 * @author Inervo
 * Handler for album as pdf file
 */
public class PdfHandler extends AbstractImageHandler {
    
    Document pdfDocument = null;

    /**
     * Constructor of this handler. Retrieve the pdfDocument which will contain all the images
     * @param album the pdf album
     */
    public PdfHandler(File album) {
        Logger.getLogger(PdfHandler.class.getName()).entering(PdfHandler.class.getName(), "PdfHandler", album);
        pdfDocument = new Document();
        try {
            pdfDocument.setFile(album.getPath());
        } catch (PDFException | PDFSecurityException | IOException ex) {
            Logger.getLogger(PdfHandler.class.getName()).log(Level.SEVERE, "Cannot read the PDF document", ex);
            new InfoInterface(InfoInterface.ERROR, "file-read", album.getName());
        }
        Logger.getLogger(PdfHandler.class.getName()).exiting(PdfHandler.class.getName(), "PdfHandler");
    }

    @Override
    public BufferedImage getImage(int pageNumber) {
        Logger.getLogger(PdfHandler.class.getName()).entering(PdfHandler.class.getName(), "getImage", pageNumber);
        if (!isImageInRange(pageNumber)) {
            Logger.getLogger(PdfHandler.class.getName()).exiting(PdfHandler.class.getName(), "getImage", null);
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
        Logger.getLogger(PdfHandler.class.getName()).exiting(PdfHandler.class.getName(), "getImage", bufferedImage);
        return bufferedImage;
    }

    @Override
    public int getNbrOfPages() {
        Logger.getLogger(PdfHandler.class.getName()).entering(PdfHandler.class.getName(), "getNbrOfPages");
        Logger.getLogger(PdfHandler.class.getName()).exiting(PdfHandler.class.getName(), "getNbrOfPages", pdfDocument.getNumberOfPages());
        return pdfDocument.getNumberOfPages();
    }

    @Override
    public List<String> getPagesName() {
        Logger.getLogger(PdfHandler.class.getName()).entering(PdfHandler.class.getName(), "getPagesName");
        List<String> pagesTitle = new ArrayList<>(pdfDocument.getNumberOfPages());
        for (int i = 0; i < pdfDocument.getNumberOfPages(); i++) {
            pagesTitle.add("Page " + i);
        }
        Logger.getLogger(PdfHandler.class.getName()).exiting(PdfHandler.class.getName(), "getPagesName", pagesTitle);
        return pagesTitle;
    }

}
