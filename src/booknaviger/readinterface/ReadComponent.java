/*
 */

package booknaviger.readinterface;

import booknaviger.picturehandler.ImageReader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * This class is the component used to render the images in the {@link ReadInterface}
 * @author Inervo
 */
public class ReadComponent extends JComponent {
    
    private BufferedImage readImage = null;
    private Color backgroundColor = new Color(0, 0, 0);
    /**
     * Dimension du rendu de l'image
     */
    private int drawingImageWidth = 0;
    private int drawingImageHeigh = 0;
    /**
     * Zone de début de dessin de l'image (pour centrage sur l'écran
     */
    private JScrollPane readInterfaceScrollPane = null;
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
    /**
     * Fit to screen
     */
    private boolean fitToScreenVertically = false;
    private boolean fitToScreenHorizontally = false;
    /**
     * First & last page reached
     */
    private boolean firstPageReached = false;
    private boolean lastPageReached = false;

    /**
     * Constructor. Set the loading image
     */
    public ReadComponent() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "ReadComponent");
        setLoadingImage();
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "ReadComponent");
    }
    
    /**
     * The image to render is set to the loading image
     */
    private void setLoadingImage() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "setLoadingImage");
        Image image = new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/ReadComponent").getString("loading_image"))).getImage();
        setImage(new ImageReader(image).convertImageToBufferedImage());
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "setLoadingImage");
    }
    
    /**
     * Set the image to render to the image set in parameters
     * @param image The picture to render
     */
    protected void setImage(final BufferedImage image) {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "setImage", image);
        setImage(image, false, null);
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "setImage");
    }
    
    /**
     * Set the image to render to the image set in parameters
     * @param image The picture to render
     * @param reinitializeOrientation Reinitialize the orientation to 0° if set at true<br />No changes if set to false
     * @param readInterfaceScroll The scrollPane of the {@link ReadInterface} for calculating the ratio and image size
     */
    protected void setImage(final BufferedImage image, boolean reinitializeOrientation, final JScrollPane readInterfaceScroll) {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "setImage", new Object[] {image, reinitializeOrientation, readInterfaceScroll});
        Logger.getLogger(ReadComponent.class.getName()).log(Level.CONFIG, "A new image is being set to rendered");
        if (reinitializeOrientation) {
            currentOrientation = 0;
        }
        firstPageReached = false;
        lastPageReached = false;
        SwingUtilities.invokeLater(() -> {
            if (readImage != null) {
                readImage.flush();
            }
            readInterfaceScrollPane = readInterfaceScroll;
            readImage = image;
            image.flush();
            renderImage();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Started in readInterface.readPageNbrImage()
            Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "setImage");
        });
    }
    
    /**
     * Resize the image with zoom parameters, find the border color, reinitialize the showing part of the image
     * on the scroll pane, and repaint the component
     */
    private void renderImage() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "renderImage");
        resizeComponentToRenderImageDimension();
        backgroundColor = ImageReader.findColor(readImage);
        scrollRectToVisible(new Rectangle(0, 0));
        repaint();
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "renderImage");
    }
    
    /**
     * Resize the image with zoom parameters, hard zoom value and the fit to vertical / horizontal size, then resize the component
     * to the same size that of the picture, revalidate the tree, and search for the starting point to draw the image
     * (if the image is smaller than the rendering bounds)
     */
    private void resizeComponentToRenderImageDimension() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "resizeComponentToRenderImageDimension");
        drawingImageWidth = (int) (readImage.getWidth() * zoom);
        drawingImageHeigh = (int) (readImage.getHeight() * zoom);
        checkScreenFit();
        setPreferredSize(new Dimension(drawingImageWidth, drawingImageHeigh));
        revalidate();
        getStartingPointToDrawCenteredImage();
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "resizeComponentToRenderImageDimension");
    }
    
    /**
     * Search the point to start drawing the image if the image is smaller vertically or horizontally
     * that the parents size so that the image is centered
     */
    private void getStartingPointToDrawCenteredImage() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "getStartingPointToDrawCenteredImage");
        if (readInterfaceScrollPane != null) {
            readInterfaceScrollPane.validate();
            if (drawingImageHeigh > readInterfaceScrollPane.getHeight()) {
                startXDrawingPoint = (int) ((drawingImageWidth > readInterfaceScrollPane.getWidth() - readInterfaceScrollPane.getVerticalScrollBar().getWidth()) ? 0 : (readInterfaceScrollPane.getWidth() - drawingImageWidth - readInterfaceScrollPane.getVerticalScrollBar().getWidth()) / 2);
            } else {
                startXDrawingPoint = (int) ((drawingImageWidth > readInterfaceScrollPane.getWidth()) ? 0 : (readInterfaceScrollPane.getWidth() - drawingImageWidth) / 2);
            }
            if (drawingImageWidth > readInterfaceScrollPane.getWidth()) {
                startYDrawingPoint = (int) ((drawingImageHeigh > readInterfaceScrollPane.getHeight() - readInterfaceScrollPane.getHorizontalScrollBar().getHeight()) ? 0 : (readInterfaceScrollPane.getHeight() - drawingImageHeigh - readInterfaceScrollPane.getHorizontalScrollBar().getHeight()) / 2);
            } else {
                startYDrawingPoint = (int) ((drawingImageHeigh > readInterfaceScrollPane.getHeight()) ? 0 : (readInterfaceScrollPane.getHeight() - drawingImageHeigh) / 2);
            }
        } else {
            startXDrawingPoint = (int) ((drawingImageWidth > Toolkit.getDefaultToolkit().getScreenSize().getWidth()) ? 0 : (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - drawingImageWidth) / 2);
            startYDrawingPoint = (int) ((drawingImageHeigh > Toolkit.getDefaultToolkit().getScreenSize().getHeight()) ? 0 : (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - drawingImageHeigh) / 2);
        }
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "getStartingPointToDrawCenteredImage");
    }
    
    /**
     * Verify if a "fit to vertical/horizontal" has been activated, and calculate new picture size
     */
    private void checkScreenFit() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "checkScreenFit");
        float ratio = 0;
        if (fitToScreenHorizontally && drawingImageWidth > readInterfaceScrollPane.getWidth()) {
            ratio = (float)readInterfaceScrollPane.getWidth() / (float)drawingImageWidth;
            drawingImageWidth = (int) readInterfaceScrollPane.getWidth();
            drawingImageHeigh = (int) (drawingImageHeigh * ratio);
        }
        if (fitToScreenVertically && drawingImageHeigh > readInterfaceScrollPane.getHeight()) {
            ratio = (float)readInterfaceScrollPane.getHeight() / (float)drawingImageHeigh;
            drawingImageHeigh = (int) readInterfaceScrollPane.getHeight();
            drawingImageWidth = (int) (drawingImageWidth * ratio);
        }
        if ((fitToScreenHorizontally || fitToScreenVertically) && ratio != 0) {
            if (drawingImageHeigh > readInterfaceScrollPane.getHeight()) {
                drawingImageWidth -= readInterfaceScrollPane.getVerticalScrollBar().getWidth();
                drawingImageHeigh -= (readInterfaceScrollPane.getVerticalScrollBar().getWidth() * ratio);
            }
            if (drawingImageWidth > readInterfaceScrollPane.getWidth()) {
                drawingImageHeigh -= readInterfaceScrollPane.getHorizontalScrollBar().getHeight();
                drawingImageWidth -= (readInterfaceScrollPane.getHorizontalScrollBar().getHeight() * ratio);
            }
        }
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "checkScreenFit");
    }
    
    /**
     * Rotate the picture with the wanted rotation (in degree)
     * @param rotationWanted The rotation wanted (in degree) (from 0°)
     */
    public void rotateImage(int rotationWanted) {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "rotateImage", rotationWanted);
        int rotationDegree = rotationWanted - currentOrientation;
        if (rotationDegree < 0) {
            rotationDegree += 360;
        } else if (rotationDegree >=360) {
            rotationDegree -= 360;
        }
        currentOrientation = rotationWanted;
        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        });
        setImage(ImageReader.rotatePicture(readImage, rotationDegree));
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "rotateImage");
    }
    
    /**
     * zoom in the picture
     */
    public void zoomIn() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "zoomIn");
        SwingUtilities.invokeLater(() -> {
            zoom += zoomModifier;
            resizeComponentToRenderImageDimension();
            repaint();
            Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "zoomIn");
        });
    }
    
    /**
     * zoom out the picture
     */
    public void zoomOut() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "zoomOut");
        SwingUtilities.invokeLater(() -> {
            zoom -= zoomModifier;
            if (zoom < zoomModifier) {
                zoom = zoomModifier;
            }
            resizeComponentToRenderImageDimension();
            repaint();
            Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "zoomout");
        });
    }
    
    /**
     * Set the zoom value
     * @param zoomValue the value to add to the zoom value
     */
    public void setZoomValue(double zoomValue) {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "setZoomValue", zoomValue);
        SwingUtilities.invokeLater(() -> {
            zoom = zoom * (float)zoomValue;
            if (zoom < zoomModifier) {
                zoom = zoomModifier;
            }
            resizeComponentToRenderImageDimension();
            repaint();
            Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "setZoomValue");
        });
    }
    
    /**
     * set the zoom factor to 1:1
     */
    public void normalZoom() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "normalZoom");
        SwingUtilities.invokeLater(() -> {
            zoom = 1;
            resizeComponentToRenderImageDimension();
            repaint();
            Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "normalZoom");
        });
    }
    
    /**
     * Switch the "fit to screen vertically" parameters
     */
    protected void changeFitToScreenVertically() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "changeFitToScreenVertically");
        this.fitToScreenVertically = !fitToScreenVertically;
        renderImage();
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "changeFitToScreenVertically");
    }

    /**
     * Switch the "fit to screen horizontally" parameters
     */
    protected void changeFitToScreenHorizontally() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "changeFitToScreenHorizontally");
        this.fitToScreenHorizontally = !fitToScreenHorizontally;
        renderImage();
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "changeFitToScreenHorizontally");
    }

    /**
     * Set the info about the 1st page reached must be shown
     */
    public void setFirstPageReached() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "setFirstPageReached");
        this.firstPageReached = true;
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "setFirstPageReached");
    }

    /**
     * Set the info about the last page reached must be shown
     */
    public void setLastPageReached() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "setLastPageReached");
        this.lastPageReached = true;
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "setLastPageReached");
    }

    /**
     * get the current rotation of the component
     * @return the value of the current rotation
     */
    public int getCurrentOrientation() {
        Logger.getLogger(ReadComponent.class.getName()).entering(ReadComponent.class.getName(), "getCurrentOrientation");
        Logger.getLogger(ReadComponent.class.getName()).exiting(ReadComponent.class.getName(), "getCurrentOrientation", currentOrientation);
        return currentOrientation;
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
        
        if (firstPageReached || lastPageReached) {
            Font font = new Font(g2d.getFont().getName(), Font.BOLD, 15);
            FontRenderContext frc = new FontRenderContext(null, false, false);
            g2d.setFont(font);
            String text;
            if (firstPageReached) {
                text = java.util.ResourceBundle.getBundle("booknaviger/resources/ReadComponent").getString("firstPage.text");
            } else {
                text = java.util.ResourceBundle.getBundle("booknaviger/resources/ReadComponent").getString("lastPage.text");
            }
            int fontWidth = (int) font.getStringBounds(text, frc).getWidth();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(5, 5, fontWidth + 10, 20);
            g2d.fillRect(this.getWidth() - fontWidth - 15, 5, fontWidth + 10, 20);
            g2d.fillRect(5, this.getHeight() - 25, fontWidth + 10, 20);
            g2d.fillRect(this.getWidth() - fontWidth - 15, this.getHeight() - 25, fontWidth + 10, 20);
            g2d.setColor(Color.YELLOW);
            g2d.drawString(text, 10, 20);
            g2d.drawString(text, this.getWidth() - fontWidth - 10, 20);
            g2d.drawString(text, 10, this.getHeight() - 10);
            g2d.drawString(text, this.getWidth() - fontWidth - 10, this.getHeight() - 10);
        }
        
        g2d.dispose();
    }

}
