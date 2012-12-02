/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package booknaviger;

import booknaviger.errorhandler.KnownErrorBox;
import java.awt.Dimension;
import java.awt.Graphics;
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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;

/**
 *
 * @author Inervo
 */
public class PreviewComponent extends JComponent {

    private Image previewImage;
    private int imageWidth;
    private int imageHeight;
    private BookNavigerView bnv;

    /**
     * Initialisation du composant
     */
    public PreviewComponent() {
        setNoPreviewImage();
    }

    public void setBnv(BookNavigerView bnv) {
        this.bnv = bnv;
    }

    /**
     * Chargement de la nouvelle image pour le preview via un toolkit
     * @param image L'Image à charger
     */
    public void setImage(final Image image) {
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, this);
        g2d.dispose();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (previewImage != null)
                    previewImage.flush();
                previewImage = bi;
                imageWidth = width;
                imageHeight = height;
                forceRepaint();
            }
        });
    }

    /**
     * Chargement de la nouvelle image pour le preview via un toolkit
     * @param imagePath Le File correspondant à l'image à charger
     * @throws IOException
     */
    public void setNewImage(File imagePath) {
        Image tampon = Toolkit.getDefaultToolkit().createImage(imagePath.toString());
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(tampon, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(PreviewComponent.class.getName()).log(Level.SEVERE, "wait for image loading interrupted", ex);
        }
        if (tampon.getWidth(null) == -1 && tampon.getHeight(null) == -1) {
            new KnownErrorBox(null, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imagePath.toString());
            setNoPreviewImage();
            return;
        }
        setImage(tampon);
    }

    /**
     * Chargement de la nouvelle image pour le preview via un ImageIO.read
     * @param imagePath Le File correspondant à l'image à charger
     * @throws IOException
     */
    public void setNewImageIO(File imagePath) throws IOException {
        Image tampon = ImageIO.read(imagePath).getScaledInstance(-1, -1, 1);
        setImage(tampon);
    }

    /**
     * Chargement de la nouvelle image pour le preview via un ImageIO.read, puis ferme l'inputStream
     * @param imageStream Le flux correspondant à l'image à charger
     * @throws IOException
     */
    public void setNewImageStream(InputStream imageStream) throws IOException {
        Image tampon = ImageIO.read(imageStream).getScaledInstance(-1, -1, 1);
        imageStream.close();
        setImage(tampon);
    }

    /**
     * Force le composant à se faire repeindre
     */
    public void forceRepaint() {
        paintComponent(getGraphics());
    }

    public final void setNoPreviewImage() {
        setImage(Application.getInstance(BookNavigerApp.class).getContext().getResourceMap(PreviewComponent.class).getImageIcon("NoPreview.imageIcon").getImage());
    }

    @Override
    public void paintComponent(Graphics g) {
        int width = getParent().getWidth();
        int height = getParent().getHeight() - bnv.mainToolBar.getHeight();
        int maxWidth = (width / 5) + (width / 2);
        int newWidth = 0;
        int newHeight = 0;

        if (previewImage == null)
            return;

        if (imageWidth > (maxWidth)) {
            newWidth = maxWidth;
            float scale = (float) newWidth / (float) imageWidth;
            newHeight = (int) ((float) imageHeight * scale);
            if ((imageHeight * ((float) newWidth / (float) imageWidth)) > (height)) {
                newHeight = height;
                scale = (float) newHeight / (float) imageHeight;
                newWidth = (int) ((float) imageWidth * scale);
            }
        } else {
            newHeight = height;
            float scale = (float) newHeight / (float) imageHeight;
            newWidth = (int) ((float) imageWidth * scale);
            if ((imageWidth * ((float) newHeight / (float) imageHeight)) > (maxWidth)) {
                newWidth = maxWidth;
                scale = (float) newWidth / (float) imageWidth;
                newHeight = (int) ((float) imageHeight * scale);
            }
        }

        /*if (imageHeight > height) {
            newHeight = height;
            float scale = (float) newHeight / (float) imageHeight;
            newWidth = (int) ((float) imageWidth * scale);
        }
        if (imageWidth > maxWidth) {
            newWidth = maxWidth;
            float scale = (float) newWidth / (float) imageWidth;
            newHeight = (int) ((float) imageHeight * scale);
        }
        if (newHeight > height) {
            float scale = (float) height / (float) newHeight;
            newHeight = height;
            newWidth = (int) ((float) newWidth * scale);
        }
        if (newHeight == 0) {
            newHeight = height;
            float scale = (float) newHeight / (float) imageHeight;
            newWidth = (int) ((float) imageWidth * scale);
        }
        if (newWidth > maxWidth) {
            newWidth = maxWidth;
            float scale = (float) newWidth / (float) imageWidth;
            newHeight = (int) ((float) imageHeight * scale);
        }*/

        this.setPreferredSize(new Dimension(newWidth, newHeight));
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.clearRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.drawImage(previewImage, 0, ((this.getHeight() - newHeight) / 2), newWidth, newHeight, this);
        g2d.dispose();
        revalidate();
    }
}
