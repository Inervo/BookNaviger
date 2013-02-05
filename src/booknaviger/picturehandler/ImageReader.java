/*
 */

package booknaviger.picturehandler;

import booknaviger.MainInterface;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * @author Inervo
 *
 */
public class ImageReader {
    
    BufferedImage image = null;
    Object imageObject = null;
    
    public ImageReader(Image image) {
        imageObject = image;
    }

    public ImageReader(File imageFile) {
        imageObject = imageFile;
    }

    public ImageReader(InputStream imageIS) {
        imageObject = imageIS;
    }
    
    public BufferedImage convertImageToBufferedImage() {
        if (imageObject.getClass().getName().toLowerCase().contains("image")) {
            Image tampon = (Image) imageObject;
            image = new BufferedImage(tampon.getWidth(null), tampon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(tampon, 0, 0, tampon.getWidth(null), tampon.getHeight(null), null);
            g2d.dispose();
            return image;
        }
        return null;
    }
    
    public BufferedImage readImage() {
//        System.out.println(imageObject.getClass().getName());
        if (imageObject.getClass().getName().equals("java.io.File")) {
            if (((File)imageObject).getName().toLowerCase().endsWith(".jpg") || ((File)imageObject).getName().toLowerCase().endsWith(".jpeg") || ((File)imageObject).getName().toLowerCase().endsWith(".gif") || ((File)imageObject).getName().toLowerCase().endsWith(".png")) {
            readWithFileToolkit();
            } else {
                readWithFileImageIO();
            }
            return image;
        }
        if (imageObject.getClass().getName().equals("java.util.zip.ZipFile$ZipFileInflaterInputStream")) {
            readWithInputStreamImageIO();
            return image;
        }
        if (imageObject.getClass().getName().equals("java.io.PipedInputStream")) {
            readWithInputStreamImageIO();
            return image;
        }
        
        return null;
    }
    
    private void readWithFileImageIO() {
        try {
            image = ImageIO.read((File)imageObject);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            //new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imagePath.toString());
            //previewComponent.setNoPreviewImage();
        }
    }
    
    private void readWithFileToolkit() {
        Image tampon = Toolkit.getDefaultToolkit().createImage(((File)imageObject).toString());
        MediaTracker mt = new MediaTracker(MainInterface.getPreviewComponent());
        mt.addImage(tampon, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            //Logger.getLogger(PreviewComponent.class.getName()).log(Level.SEVERE, "wait for image loading interrupted", ex);
        }
        image = new ImageReader(tampon).convertImageToBufferedImage();
    }

    private void readWithInputStreamImageIO() {
        try {
            image = ImageIO.read((InputStream)imageObject);
            ((InputStream)imageObject).close();
        } catch (IOException ex) {
            Logger.getLogger(ImageReader.class.getName()).log(Level.SEVERE, null, ex);
            //new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imagePath.toString());
            //previewComponent.setNoPreviewImage();
        }
    }
    
    

}
