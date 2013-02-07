/*
 */

package booknaviger.picturehandler;

import booknaviger.MainInterface;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
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
    
    public static BufferedImage rotatePicture(BufferedImage originalImage, int rotationDegree) {
        if (originalImage != null) {
            BufferedImage rotatedBufferedImage;
            if(rotationDegree == 180) {
                rotatedBufferedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            } else if (rotationDegree == 90 || rotationDegree == 270) {
                rotatedBufferedImage = new BufferedImage(originalImage.getHeight(), originalImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
            } else if (rotationDegree == 0) {
                return originalImage;
            } else {
                return null;
            }
            Graphics2D g2d = rotatedBufferedImage.createGraphics();
            g2d.rotate(Math.toRadians(rotationDegree), 0, 0);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            if (rotationDegree == 180) {
                g2d.drawImage(originalImage, -originalImage.getWidth(), -originalImage.getHeight(), originalImage.getWidth(), originalImage.getHeight(), null);
            }
            else if (rotationDegree == 90) {
                g2d.drawImage(originalImage, 0, -originalImage.getHeight(), originalImage.getWidth(), originalImage.getHeight(), null);
            }
            else if (rotationDegree == 270) {
                g2d.drawImage(originalImage, -originalImage.getWidth(), 0, originalImage.getWidth(), originalImage.getHeight(), null);
            }
            g2d.dispose();
            return rotatedBufferedImage;
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
