/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package booknaviger;

//import booknaviger.errorhandler.KnownErrorBox;
import booknaviger.picturehandler.ImageReader;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
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
     * Initialisation du composant
     */
    public PreviewComponent() {
        setNoPreviewImage();
    }

    /**
     *
     * @param statusToolBarHeigh
     */
    protected void setStatusToolBarHeigh(int statusToolBarHeigh) {
        this.statusToolBarHeigh = statusToolBarHeigh;
    }

    /**
     * Dessine la nouvelle image pour le preview
     * @param image L'Image à charger
     */
    protected void setImage(final BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (previewImage != null) {
                    previewImage.flush();
                }
                previewImage = image;
                imageWidth = width;
                imageHeight = height;
                refresh();
            }
        });
    }

    /**
     *
     */
    protected void setNoPreviewImage() {
        Image image = new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/PreviewComponent").getString("no-preview_image"))).getImage();
        setImage(new ImageReader(image).convertImageToBufferedImage());
    }
    
    /**
     *
     */
    protected void refresh() {
        int width = getParent().getWidth();
        int height = getParent().getHeight() - statusToolBarHeigh;
        int maxWidth = (width / 5) + (width / 2);

        if (previewImage == null) {
            return;
        }
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
    }

    @Override
    protected void paintComponent(Graphics g) {
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
    }
}
