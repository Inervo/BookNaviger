/*
 */

package booknaviger.readinterface;

import booknaviger.picturehandler.ImageReader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * @author Inervo
 *
 */
public class ReadComponent extends JComponent {
    
    private BufferedImage readImage;
    private Color backgroundColor = new Color(140, 140, 140);

    public ReadComponent() {
        setLoadingImage();
    }
    
    /**
     * Dessine la nouvelle image pour le preview
     * @param image L'Image à charger
     */
    public void setImage(final BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (readImage != null) {
                    readImage.flush();
                }
                readImage = image;
//                imageWidth = width;
//                imageHeight = height;
//                test de redimensionnement du component
                setPreferredSize(new Dimension(width, height));
//                revalidate();
//                fin du test
                forceRepaint();
            }
        });
    }
    
    /**
     * Force le composant à se faire repeindre
     */
    private void forceRepaint() {
//        paintComponent(getGraphics());
//        repaint();
    }
    
    private void setLoadingImage() {
        Image image = new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/ReadComponent").getString("loading_image"))).getImage();
        setImage(new ImageReader(image).convertImageToBufferedImage());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (readImage == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(backgroundColor);
        
        g2d.fillRect(0, 0, this.getBounds().width + 1, this.getBounds().height + 1);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.drawImage(readImage, 0, 0, readImage.getWidth(), readImage.getHeight(), this);
        g2d.dispose();
    }

}
