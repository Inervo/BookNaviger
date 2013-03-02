/*
 */

package booknaviger.picturehandler;

import booknaviger.MainInterface;
import booknaviger.exceptioninterface.ExceptionHandler;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
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
        ExceptionHandler.registerExceptionHandler(ImageReader.class.getName());
        imageObject = image;
    }

    public ImageReader(File imageFile) {
        ExceptionHandler.registerExceptionHandler(ImageReader.class.getName());
        imageObject = imageFile;
    }

    public ImageReader(InputStream imageIS) {
        ExceptionHandler.registerExceptionHandler(ImageReader.class.getName());
        imageObject = imageIS;
    }
    
    public BufferedImage convertImageToBufferedImage() {
        if (imageObject.getClass().getName().toLowerCase().contains("image")) {
            Image tampon = (Image) imageObject;
            if (image != null) {
                image.flush();
            }
            image = new BufferedImage(tampon.getWidth(null), tampon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(tampon, 0, 0, tampon.getWidth(null), tampon.getHeight(null), null);
            g2d.dispose();
            return image;
        }
        return null;
    }
    
    public BufferedImage readImage() {
        Logger.getLogger(ImageReader.class.getName()).log(Level.FINE, "Read the image. Type : {0}", imageObject.getClass().getName());
        if (image != null) {
            image.flush();
        }
        if (imageObject instanceof File) {
            if (((File)imageObject).getName().toLowerCase().endsWith(".jpg") || ((File)imageObject).getName().toLowerCase().endsWith(".jpeg") || ((File)imageObject).getName().toLowerCase().endsWith(".gif") || ((File)imageObject).getName().toLowerCase().endsWith(".png")) {
                readWithFileToolkit();
            } else {
                readWithFileImageIO();
            }
            return image;
        }
        if (imageObject instanceof java.util.zip.InflaterInputStream) {
            readWithInputStreamImageIO();
            return image;
        }
        if (imageObject instanceof java.io.PipedInputStream) {
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
            originalImage.flush();
            return rotatedBufferedImage;
        }
        return null;
    }
    
    public static BufferedImage combine2Images(BufferedImage imageLeft, BufferedImage imageRight) {
        if (imageLeft == null || imageRight == null) {
            return null;
        }
        int totalWidth = imageLeft.getWidth() + imageRight.getWidth();
        int totalHeigh = (imageLeft.getHeight() < imageRight.getHeight()) ? imageRight.getHeight() : imageLeft.getHeight();
        BufferedImage imageCombined = new BufferedImage(totalWidth, totalHeigh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imageCombined.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setColor(findColor(imageLeft));
        g2d.fillRect(0, 0, totalWidth, totalHeigh);
        g2d.drawImage(imageLeft, 0, 0, imageLeft.getWidth(), imageLeft.getHeight(), null);
        g2d.drawImage(imageRight, imageLeft.getWidth(), 0, imageRight.getWidth(), imageRight.getHeight(), null);
        g2d.dispose();
        imageRight.flush();
        imageLeft.flush();
        return imageCombined;
    }
    
    /**
     * trouve la couleur de la bordure pour l'image passée en paramètre
     * @param image l'image sur laquelle trouver la couleur du premier pixel
     * @return la couleur trouvée
     */
    public static Color findColor(BufferedImage image) {
        int[] tempo = new int[1];
        Color color;

        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, tempo, 0, 1);
        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            color = new Color(140, 140, 140);
            return color;
        }
        int alpha = (tempo[0] & 0xff000000) >> 24;
        int red = (tempo[0] & 0x00ff0000) >> 16;
        int green = (tempo[0] & 0x0000ff00) >> 8;
        int blue = tempo[0] & 0x000000ff;
        if (alpha != 0) {
            color = new Color(red, green, blue);
        } else {
            color = new Color(140, 140, 140);
        }
        return color;
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
