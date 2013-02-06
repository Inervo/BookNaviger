/*
 */

package booknaviger.readinterface;

import booknaviger.picturehandler.ImageReader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * @author Inervo
 *
 */
public class ReadComponent extends JComponent {
    
    private ReadInterface readInterface = null;
    private BufferedImage readImage;
    private Color backgroundColor = new Color(140, 140, 140);
    /**
     * Dimension du rendu de l'image
     */
    private int drawingImageWidth = 0;
    private int drawingImageHeigh = 0;
    /**
     * Unité utilisé pour définir le zoom sur l'image
     */
    private float zoom = 1;
    /**
     * Unité du modificateur de zoom
     */
    private final float zoomModifier = 0.1F;

    public ReadComponent() {
    }
    
    protected void initializeComponent(ReadInterface readInterface) {
        this.readInterface = readInterface;
        setLoadingImage();
    }
    
    private void setLoadingImage() {
        Image image = new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/ReadComponent").getString("loading_image"))).getImage();
        setImage(new ImageReader(image).convertImageToBufferedImage());
    }
    
    /**
     * Dessine la nouvelle image pour le preview
     * @param image L'Image à charger
     */
    protected void setImage(final BufferedImage image) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (readImage != null) {
                    readImage.flush();
                }
                readImage = image;
                renderImage();
                setCursor(Cursor.getDefaultCursor()); // Started in readInterface.readPageNbrImage()
            }
        });
    }
    
    private void renderImage() {
        resizeComponentToRenderImageDimension();
        setBackgroundColor();
        readInterface.getReadInterfaceScrollPane().setBackground(backgroundColor);
        scrollRectToVisible(new Rectangle(0, 0));
        repaint();
        readInterface.getReadInterfaceScrollPane().repaint();
    }
    
    private void resizeComponentToRenderImageDimension() {
        calculateRenderImageSize();
        setPreferredSize(new Dimension(drawingImageWidth, drawingImageHeigh));
        revalidate();
    }
    
    protected void calculateRenderImageSize() {
        // drawnImage on paintComponent seems to take care of the scale. To verify
        drawingImageWidth = (int) (readImage.getWidth() * zoom);
        drawingImageHeigh = (int) (readImage.getHeight() * zoom);
    }
    
    /**
     * trouve la couleur de la bordure pour l'image courante
     * @return la couleur des bordures
     */
    private void setBackgroundColor() {
        int[] tempo = new int[1];

        PixelGrabber pg = new PixelGrabber(readImage, 0, 0, 1, 1, tempo, 0, 1);
        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            backgroundColor = new Color(140, 140, 140);
        }
        int alpha = (tempo[0] & 0xff000000) >> 24;
        int red = (tempo[0] & 0x00ff0000) >> 16;
        int green = (tempo[0] & 0x0000ff00) >> 8;
        int blue = tempo[0] & 0x000000ff;
        if (alpha != 0) {
            backgroundColor = new Color(red, green, blue);
        } else {
            backgroundColor = new Color(140, 140, 140);
        }
    }
    
    protected void zoomIn() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                zoom += zoomModifier;
                resizeComponentToRenderImageDimension();
                repaint();
            }
        });
    }
    
    protected void zoomOut() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                zoom -= zoomModifier;
                if (zoom < zoomModifier) {
                    zoom = zoomModifier;
                }
                resizeComponentToRenderImageDimension();
                repaint();
            }
        });
    }
    
    protected void normalZoom() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                zoom = 1;
                resizeComponentToRenderImageDimension();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, this.getBounds().width + 1, this.getBounds().height + 1);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        
        if (readImage != null) {
            g2d.drawImage(readImage, 0, 0, drawingImageWidth, drawingImageHeigh, this); // TODO: center this
        }
        
        // TODO: firstpage / endpage
        
        g2d.dispose();
    }

}
