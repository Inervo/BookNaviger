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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * @author Inervo
 *
 */
public class ReadComponent extends JComponent {
    
    private BufferedImage readImage = null;
    private Color backgroundColor = new Color(140, 140, 140);
    /**
     * Dimension du rendu de l'image
     */
    private int drawingImageWidth = 0;
    private int drawingImageHeigh = 0;
    /**
     * Zone de début de dessin de l'image (pour centrage sur l'écran
     */
    private Dimension readInterfaceDimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    private int startXDrawingPoint = 0;
    private int startYDrawingPoint = 0;
    /**
     * Unité utilisé pour définir le zoom sur l'image
     */
    private float zoom = 1;
    /**
     * Unité du modificateur de zoom
     */
    private final float zoomModifier = 0.1F;
    /**
     * Current orientation of the image
     */
    private int currentOrientation = 0;

    public ReadComponent() {
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
        setImage(image, false, null);
    }
    
    /**
     * Dessine la nouvelle image pour le preview
     * @param image L'Image à charger
     * @param reinitializeOrientation Reinitialisation de la valeur de l'orientation actuelle de l'image
     */
    protected void setImage(final BufferedImage image, boolean reinitializeOrientation, final Dimension readInterfaceScrollPaneDimension) {
        if (reinitializeOrientation) {
            currentOrientation = 0;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (readImage != null) {
                    readImage.flush();
                }
                if (readInterfaceScrollPaneDimension != null) {
                    readInterfaceDimension = readInterfaceScrollPaneDimension;
                }
                readImage = image;
                renderImage();
                setCursor(Cursor.getDefaultCursor()); // Started in readInterface.readPageNbrImage()
            }
        });
    }
    
    private void renderImage() {
        resizeComponentToRenderImageDimension();
        backgroundColor = ImageReader.findColor(readImage);
        scrollRectToVisible(new Rectangle(0, 0));
        repaint();
    }
    
    private void resizeComponentToRenderImageDimension() {
        drawingImageWidth = (int) (readImage.getWidth() * zoom);
        drawingImageHeigh = (int) (readImage.getHeight() * zoom);
        getStartingPointToDrawCenteredImage();
        setPreferredSize(new Dimension(drawingImageWidth, drawingImageHeigh));
        revalidate();
    }
    
    private void getStartingPointToDrawCenteredImage() {
        if (readInterfaceDimension != null) {
            startXDrawingPoint = (int) ((drawingImageWidth > readInterfaceDimension.getWidth()) ? 0 : (readInterfaceDimension.getWidth() - drawingImageWidth) / 2);
            startYDrawingPoint = (int) ((drawingImageHeigh > readInterfaceDimension.getHeight()) ? 0 : (readInterfaceDimension.getHeight() - drawingImageHeigh) / 2);
        }
    }
    
    protected void rotateImage(int rotationWanted) {
        int rotationDegree = rotationWanted - currentOrientation;
        if (rotationDegree < 0) {
            rotationDegree += 360;
        }
        currentOrientation = rotationWanted;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Ended in readComponent.setImage(...)
            }
        });
        setImage(ImageReader.rotatePicture(readImage, rotationDegree));
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
            g2d.drawImage(readImage, startXDrawingPoint, startYDrawingPoint, drawingImageWidth, drawingImageHeigh, this);
        }
        
        // TODO: firstpage / endpage
        
        g2d.dispose();
    }

}
