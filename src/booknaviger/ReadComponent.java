/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package booknaviger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Inervo
 */
public class ReadComponent extends JComponent {

    private Image readImage;
    private Image renderImage;
    private Image nextImage;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private BookNavigerReadView bnrv;
    private Color borderColor = new Color(240, 240, 240);
    private Map<RenderingHints.Key, Object> renderingHints = new HashMap<RenderingHints.Key, Object>();
    /**
     * Boolean pour savoir si c'est un pdf
     */
    private boolean imageFromPdf = false;
    protected short imagePdfZoom = 1;
    /**
     * Unité utilisé pour définir le zoom sur l'image
     */
    private float zoom = 1;
    /**
     * Unité du modificateur de zoom
     */
    private final float zoomModifier = 0.1F;
    /**
     * Adapter taille horizontale max de l'image a celle de l'écran
     */
    private boolean maxHorizon = false;
    /**
     * Adapter taille vertical max de l'image a celle de l'écran
     */
    private boolean maxVerti = false;
    /**
     * Dimension de l'image si maxHori ou maxVerti est activé
     */
    private Dimension renderSizePicture = new Dimension();
    private Dimension readSizePicture = new Dimension();
    /**
     * Alignement vertical de l'image dans sa zone de rendement
     */
    private int verticalAlignement = 0;
    private int renderVerticalAlignement = 0;
    /**
     * position des scrollbars après pour la nouvelle image
     */
    private int verticalScrollBarPosition = 0;
    private int horizontalScrollBarPosition = 0;
    /**
     * Degrés de rotation pour l'image
     */
    private int degreeOfRotation = 0;
    private boolean scrollBarPosToSet = false;

     /**
     * Constructeur qui ne sert qu'a netbeans
     */
    public ReadComponent() {
    }

    /**
     * Constructeur véritable
     * @param bnrv Instance de bnrv
     */
    public ReadComponent(BookNavigerReadView bnrv) {
        this.bnrv = bnrv;
        ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(ReadComponent.class);
        readImage = resourceMap.getImageIcon("LoadingAlbum.image").getImage();
        // TODO: loading image non centrée sur windows. PQ ?
        imageWidth = readImage.getWidth(null);
        imageHeight = readImage.getHeight(null);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    }

    /**
     * Initialise ce component avec l'image de chargement
     */
    public void initialize() {
        Thread adaptView = new Thread(new Runnable() {

            @Override
            public void run() {
                findBorderColor();
                adaptView();
            }
        });
        adaptView.start();
        try {
            adaptView.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderImage = readImage;
        renderVerticalAlignement = verticalAlignement;
        renderSizePicture = readSizePicture;
    }

    /**
     * trouve la couleur de la bordure pour l'image courante
     * @return la couleur des bordures
     */
    private Color findBorderColor() {
        int[] tempo = new int[1];

        PixelGrabber pg = new PixelGrabber(readImage, 0, 0, 1, 1, tempo, 0, 1);
        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            borderColor = new Color(240, 240, 240);
            return borderColor;
        }
        int red = (tempo[0] & 0x00ff0000) >> 16;
        int green = (tempo[0] & 0x0000ff00) >> 8;
        int blue = tempo[0] & 0x000000ff;
        borderColor = new Color(red, green, blue);
        return borderColor;
    }

   synchronized private void postChangeImage() {
        imageWidth = readImage.getWidth(null);
        imageHeight = readImage.getHeight(null);
        final Image imageToRender = rotatePicture();
        adaptView();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (renderImage != null)
                    renderImage.flush();
                renderImage = imageToRender;
                renderVerticalAlignement = verticalAlignement;
                renderSizePicture = readSizePicture;
                scrollBarPosToSet = true;
                bnrv.readScrollPane.repaint();
            }
        });
    }

    private Image rotatePicture() {
        BufferedImage bi = null;
        if (degreeOfRotation == 0 || degreeOfRotation == 180)
            bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        else
            bi = new BufferedImage(imageHeight, imageWidth, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.rotate(Math.toRadians(degreeOfRotation), 0, 0);
        g2d.setRenderingHints(renderingHints);
        if (degreeOfRotation == 180)
            g2d.drawImage(readImage, -imageWidth, -imageHeight, imageWidth, imageHeight, this);
        else if (degreeOfRotation == 90)
            g2d.drawImage(readImage, 0, -imageHeight, imageWidth, imageHeight, this);
        else if (degreeOfRotation == -90)
            g2d.drawImage(readImage, -imageWidth, 0, imageWidth, imageHeight, this);
        else
            g2d.drawImage(readImage, 0, 0, imageWidth, imageHeight, this);
        g2d.dispose();
        imageWidth = bi.getWidth(null);
        imageHeight = bi.getHeight(null);
        return bi;
    }

    private Image combine2Images(Image image1, Image image2) {
        int image1Width = image1.getWidth(null);
        int image1Height = image1.getHeight(null);
        int image2Width = image2.getWidth(null);
        int image2Height = image2.getHeight(null);
        int width = image1Width + image2Width;
        int height = (image1Height < image2Height) ? image2Height : image1Height;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHints(renderingHints);
        g2d.setColor(findBorderColor());
        g2d.fillRect(0, 0, width, height);
        g2d.drawImage(image1, 0, 0, image1Width, image1Height, this);
        g2d.drawImage(image2, image1Width, 0, image2Width, image2Height, this);
        g2d.dispose();
        return bi;
    }

    /**
     * Position des scrollbars après chargement de l'image
     * @param horizontalScrollBar position horizontale
     * @param verticalScrollBar position verticale
     */
    public void setScrollBarPosition(final int horizontalScrollBar, final int verticalScrollBar) {
        horizontalScrollBarPosition = horizontalScrollBar;
        verticalScrollBarPosition = verticalScrollBar;
    }

    /**
     * Chargement de la nouvelle image
     * @param image L'Image à charger
     */
    public void setImage(Image image) {
        if (readImage != null)
            readImage.flush();
        readImage = image;
        activateImage();
    }

    /**
     * Chargement des nouvelles images
     * @param image L'Image à charger
     * @param image2 La deuxième image à charger
     */
    public void setImage(Image image, Image image2) {
        setImage(combine2Images(image, image2));
    }

    public Image getNoImage() {
        return Application.getInstance(BookNavigerApp.class).getContext().getResourceMap(ReadComponent.class).getImageIcon("NoImage.imageIcon").getImage();
    }

    /**
     * Préchargement de la nouvelle image
     * @param image L'Image à charger
     */
    public void setPreImage(Image image) {
        if (nextImage != null)
            nextImage.flush();
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, this);
        g2d.dispose();
        nextImage = bi;
    }

    public void activatePreImage() {
        setImage(nextImage);
    }

    public void activateImage() {
        findBorderColor();
        postChangeImage();
    }

    /**
     * Préchargement des nouvelles images
     * @param image L'Image à charger
     * @param image2 La deuxième image à charger
     */
    public void setPreImage(Image image, Image image2) {
        setPreImage(combine2Images(image, image2));
    }

    /**
     * Adapte la bordure a la taille de l'image, et la taille de ce composant
     */
    synchronized private void adaptView() {
        int parentWidth = bnrv.getWidth() - bnrv.readScrollPane.getVerticalScrollBar().getWidth();
        if (maxHorizon || maxVerti) {
            calculatePictureSize();
        }
        else {
            readSizePicture = new Dimension((int) ((imageWidth / imagePdfZoom) * zoom), (int) ((imageHeight / imagePdfZoom) * zoom));
        }
        bnrv.setBorderParameters(borderColor, new Dimension((readSizePicture.width > parentWidth) ? 0 : ((parentWidth - readSizePicture.width) / 2), readSizePicture.height));
        verticalAlignement = (readSizePicture.height > bnrv.getHeight()) ? 0 : ((bnrv.getHeight() - readSizePicture.height) / 2);
        setPreferredSize(readSizePicture);
        revalidate();
    }

    /**
     * calcul la taille de rendu de l'image pour que la largeur et/ou la hauteur ne dépassent pas la taille du composant
     */
    private void calculatePictureSize() {
        int parentWidth = bnrv.getWidth();
        int parentHeight = bnrv.getHeight();
        int renderHeight = (int) ((imageHeight / imagePdfZoom) * zoom);
        int renderWidth = (int) ((imageWidth / imagePdfZoom) * zoom);
        int newHeight = renderHeight;
        int newWidth = renderWidth;
        float pictureRatio = (float) imageWidth / (float) imageHeight;
        float parentRatio = (float) parentWidth / (float) parentHeight;

        if (pictureRatio > parentRatio) {
            parentHeight -= bnrv.verticalScrollBar.getWidth();
        } else //if (pictureRatio < parentRatio)
        {
            parentWidth -= bnrv.horizontalScrollBar.getHeight();
        }
//        else
//            ratio image identique a celui de la fenetre (que faire ?)

        if (renderWidth > (parentWidth) && maxHorizon) {
            newWidth = parentWidth;
            float scale = (float) newWidth / (float) renderWidth;
            newHeight = (int) ((float) renderHeight * scale);
            if ((renderHeight * ((float) newWidth / (float) renderWidth)) > (parentHeight) && maxVerti) {
                newHeight = parentHeight;
                scale = (float) newHeight / (float) renderHeight;
                newWidth = (int) ((float) renderWidth * scale);
            }
        } else if (renderHeight > (parentHeight) && maxVerti) {
            newHeight = parentHeight;
            float scale = (float) newHeight / (float) renderHeight;
            newWidth = (int) ((float) renderWidth * scale);
            if ((renderWidth * ((float) newHeight / (float) renderHeight)) > (parentWidth) && maxHorizon) {
                newWidth = parentWidth;
                scale = (float) newWidth / (float) renderWidth;
                newHeight = (int) ((float) renderHeight * scale);
            }
        }
        readSizePicture = new Dimension(newWidth, newHeight);
    }

    /**
     * Défini si l'image provient d'un pdf (pour le zoom)
     * @param imageFromPdf true si oui, false sinon
     */
    public void setImageFromPdf(boolean imageFromPdf)
    {
      this.imageFromPdf = imageFromPdf;
    }

    /**
     * Réinitialise le zoom à la taille par défaut
     */
    public void setDefaultZoom() {
        bnrv.waitingCursor(true);
        zoom = 1;
        if (imageFromPdf && imagePdfZoom != -1) {
            imagePdfZoom = 1;
            bnrv.displayPage(bnrv.getCurrentPage());
            return;
        }
        Thread adaptView = new Thread(new Runnable() {

            @Override
            public void run() {
                adaptView();
            }
        });
        adaptView.start();
        try {
            adaptView.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderVerticalAlignement = verticalAlignement;
        renderSizePicture = readSizePicture;
        repaint();
        bnrv.waitingCursor(false);
    }

    /**
     * Agrandit l'image
     */
    public void setZoomBigger() {
        bnrv.waitingCursor(true);
        zoom += zoomModifier;
        if (imageFromPdf && zoom >= ((float) imagePdfZoom + 0.5)) {
            imagePdfZoom++;
            bnrv.displayPage(bnrv.getCurrentPage());
            return;
        }
        Thread adaptView = new Thread(new Runnable() {

            @Override
            public void run() {
                adaptView();
            }
        });
        adaptView.start();
        try {
            adaptView.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderVerticalAlignement = verticalAlignement;
        renderSizePicture = readSizePicture;
        repaint();
        bnrv.waitingCursor(false);
    }

    /**
     * Réduit l'image
     */
    public void setZoomSmaller() {
        bnrv.waitingCursor(true);
        if (zoom <= zoomModifier) {
            bnrv.waitingCursor(false);
            return;
        }
        zoom -= zoomModifier;
        if (imageFromPdf && zoom < ((float) imagePdfZoom - 0.5)) {
            if (imagePdfZoom > 1) {
                imagePdfZoom--;
                bnrv.displayPage(bnrv.getCurrentPage());
                return;
            }
        }
        Thread adaptView = new Thread(new Runnable() {

            @Override
            public void run() {
                adaptView();
            }
        });
        adaptView.start();
        try {
            adaptView.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderVerticalAlignement = verticalAlignement;
        renderSizePicture = readSizePicture;
        repaint();
        bnrv.waitingCursor(false);
    }

    /**
     * tourne l'image à 90°
     */
    public void rotate90() {
        bnrv.waitingCursor(true);
        degreeOfRotation = 90;
        postChangeImage();
        bnrv.waitingCursor(false);
    }

    /**
     * tourne l'image à -90°
     */
    public void rotateMinus90() {
        bnrv.waitingCursor(true);
        degreeOfRotation = -90;
        postChangeImage();
        bnrv.waitingCursor(false);
    }

    /**
     * tourne l'image à 180°
     */
    public void rotate180() {
        bnrv.waitingCursor(true);
        degreeOfRotation = 180;
        postChangeImage();
        bnrv.waitingCursor(false);
    }

    /**
     * tourne l'image à sa position initiale
     */
    public void rotateOriginal() {
        bnrv.waitingCursor(true);
        degreeOfRotation = 0;
        postChangeImage();
        bnrv.waitingCursor(false);
    }

    /**
     * Tourne l'image d'1/4 de tour de le sens horaire
     */
    public void rotateCW() {
        if (degreeOfRotation == 0) {
            rotate90();
            bnrv.rotation90CWRadioButtonMenuItem.setSelected(true);
        } else if (degreeOfRotation == 90) {
            rotate180();
            bnrv.rotation180RadioButtonMenuItem.setSelected(true);
        } else if (degreeOfRotation == 180) {
            rotateMinus90();
            bnrv.rotation90CCWRadioButtonMenuItem.setSelected(true);
        } else if (degreeOfRotation == -90) {
            rotateOriginal();
            bnrv.rotationInitialRadioButtonMenuItem.setSelected(true);
        }
    }

    /**
     * Tourne l'image d'1/4 de tour de le sens antihoraire
     */
    public void rotateCCW() {
        if (degreeOfRotation == 0) {
            rotateMinus90();
            bnrv.rotation90CCWRadioButtonMenuItem.setSelected(true);
        } else if (degreeOfRotation == 90) {
            rotateOriginal();
            bnrv.rotationInitialRadioButtonMenuItem.setSelected(true);
        } else if (degreeOfRotation == 180) {
            rotate90();
            bnrv.rotation90CWRadioButtonMenuItem.setSelected(true);
        }else if (degreeOfRotation == -90) {
            rotate180();
            bnrv.rotation180RadioButtonMenuItem.setSelected(true);
        }
    }

    /**
     * activer / désactiver l'adaptation horizontale de l'image si supérieur a celle du composant
     * @param maxHorizon valeur false / true de l'activation
     */
    public void setMaxHorizon(boolean maxHorizon) {
        bnrv.waitingCursor(true);
        this.maxHorizon = maxHorizon;
        Thread adaptView = new Thread(new Runnable() {

            @Override
            public void run() {
                adaptView();
            }
        });
        adaptView.start();
        try {
            adaptView.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderVerticalAlignement = verticalAlignement;
        renderSizePicture = readSizePicture;
        repaint();
        bnrv.waitingCursor(false);
    }

    /**
     * activer / désactiver l'adaptation vertical de l'image si supérieur a celle du composant
     * @param maxVerti valeur false / true de l'activation
     */
    public void setMaxVerti(boolean maxVerti) {
        bnrv.waitingCursor(true);
        this.maxVerti = maxVerti;
        Thread adaptView = new Thread(new Runnable() {

            @Override
            public void run() {
                adaptView();
            }
        });
        adaptView.start();
        try {
            adaptView.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderVerticalAlignement = verticalAlignement;
        renderSizePicture = readSizePicture;
        repaint();
        bnrv.waitingCursor(false);
    }

    @Override
    public void repaint() {
        super.repaint();
        if (scrollBarPosToSet) {
            scrollBarPosToSet = false;
            bnrv.horizontalScrollBar.setValue(horizontalScrollBarPosition);
            bnrv.verticalScrollBar.setValue(verticalScrollBarPosition);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(borderColor);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2d.setRenderingHints(renderingHints);
        g2d.drawImage(renderImage, 0, renderVerticalAlignement, renderSizePicture.width, renderSizePicture.height, this);
        g2d.dispose();
        if (scrollBarPosToSet)
            repaint();
    }
}
