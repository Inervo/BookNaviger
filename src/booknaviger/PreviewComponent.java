/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package booknaviger;

import booknaviger.picturehandler.ImageReader;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * This class is for the preview of an album
 * @author Inervo
 */
public final class PreviewComponent extends JComponent {

    private BufferedImage previewImage;
    private int imageWidth;
    private int imageHeight;
    private int newWidth;
    private int newHeight;
    private int statusToolBarHeigh;

    /**
     * Constructor. Set a no preview image by default
     */
    public PreviewComponent() {
        Logger.getLogger(PreviewComponent.class.getName()).entering(PreviewComponent.class.getName(), "PreviewComponent");
        setNoPreviewImage();
        Logger.getLogger(PreviewComponent.class.getName()).exiting(PreviewComponent.class.getName(), "PreviewComponent");
    }

    /**
     * Set the heigh of the statusToolBar.<br />This is used to calculate the picture size ratio.
     * @param statusToolBarHeigh The status toolbar heigh
     */
    protected void setStatusToolBarHeigh(int statusToolBarHeigh) {
        Logger.getLogger(PreviewComponent.class.getName()).entering(PreviewComponent.class.getName(), "setStatusToolBarHeigh", statusToolBarHeigh);
        this.statusToolBarHeigh = statusToolBarHeigh;
        Logger.getLogger(PreviewComponent.class.getName()).log(Level.CONFIG, "StatusToolBar heigh set to {0}", statusToolBarHeigh);
        Logger.getLogger(PreviewComponent.class.getName()).exiting(PreviewComponent.class.getName(), "setStatusToolBarHeigh");
    }

    /**
     * Define the image to preview
     * @param image The image to preview
     */
    protected synchronized void setImage(final BufferedImage image) {
        Logger.getLogger(PreviewComponent.class.getName()).entering(PreviewComponent.class.getName(), "setImage", image);
        final int width = image.getWidth();
        final int height = image.getHeight();
        SwingUtilities.invokeLater(() -> {
            if (previewImage != null) {
                previewImage.flush();
            }
            Logger.getLogger(PreviewComponent.class.getName()).log(Level.INFO, "A new preview image is being set");
            previewImage = image;
            imageWidth = width;
            imageHeight = height;
            refresh();
            Logger.getLogger(PreviewComponent.class.getName()).exiting(PreviewComponent.class.getName(), "setImage");
        });
    }

    /**
     * Set a default image when no image is available
     */
    protected void setNoPreviewImage() {
        Logger.getLogger(PreviewComponent.class.getName()).entering(PreviewComponent.class.getName(), "setNoPreviewImage");
        Logger.getLogger(PreviewComponent.class.getName()).log(Level.INFO, "Image \"no preview image\" is being set");
        Image image = new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/PreviewComponent").getString("no-preview_image"))).getImage();
        setImage(new ImageReader(image).convertImageToBufferedImage());
        Logger.getLogger(PreviewComponent.class.getName()).exiting(PreviewComponent.class.getName(), "setNoPreviewImage");
    }
    
    /**
     * Refresh the picture size, and the component size, and repaint the component
     */
    protected void refresh() {
        Logger.getLogger(PreviewComponent.class.getName()).entering(PreviewComponent.class.getName(), "refresh");
        int width = getParent().getWidth();
        int height = getParent().getHeight() - statusToolBarHeigh;
        int maxWidth = (width / 5) + (width / 2);

        if (previewImage == null) {
            return;
        }
        Logger.getLogger(PreviewComponent.class.getName()).log(Level.FINER, "Refreshing the previewComponent");
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
        
        this.setPreferredSize(new Dimension(newWidth, getHeight()));
        repaint();
        Logger.getLogger(PreviewComponent.class.getName()).exiting(PreviewComponent.class.getName(), "refresh");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Logger.getLogger(PreviewComponent.class.getName()).entering(PreviewComponent.class.getName(), "paintComponent");
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.drawImage(previewImage, 0, ((this.getHeight() - newHeight) / 2), newWidth, newHeight, this);
        g2d.dispose();
        revalidate();
        Logger.getLogger(PreviewComponent.class.getName()).exiting(PreviewComponent.class.getName(), "paintComponent");
    }
}
