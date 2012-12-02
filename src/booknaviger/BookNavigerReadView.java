/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BookNavigerReadView.java
 *
 * Created on 7 oct. 2009, 18:07:34
 */
package booknaviger;

import booknaviger.errorhandler.KnownErrorBox;
import booknaviger.macworld.TrackPadAdapter;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Application;

/**
 *
 * @author Inervo
 */
public class BookNavigerReadView extends javax.swing.JFrame {

    private File album = null;
    private File[] imageFiles = null;
    private ZipFile zipFile = null;
    private ZipArchiveEntry[] zipArchiveEntries = null;
    private Archive rarFile = null;
    private FileHeader[] rarArchiveEntries = null;
    private PDFFile pdfFile = null;
    private RandomAccessFile pdfraf = null;
    private FileChannel pdfchannel = null;
    private PDFPage[] pdfPages = null;
    private boolean[] imageFilesToolkit = null;
    private int currentPage = 0;
    private int numberOfPage = 0;
    private BookNavigerView bnv = null;
    /**
     * ScrollBar horizontale
     */
    protected JScrollBar horizontalScrollBar = null;
    /**
     * Scrollbar verticale
     */
    protected JScrollBar verticalScrollBar = null;
    private short downHit = 0;
    private short upHit = 0;
    private short rightHit = 0;
    private short leftHit = 0;
    private int previousHorizontalScrollBarPosition = 0;
    private boolean previousPageAsked = false;
    private boolean mouseWheel = false;
    private BookNavigerApp bna = null;
    private TrackPadAdapter tpa = null;
    /**
     * Unité utilisé lors du défilement avec les flêches de direction du clavier
     */
    private final short scrollUnit = 20;
    /**
     * Modifieur du pas de défilement (1 par défaut, modifié par le pas de la molette)
     */
    private short modifier = 1;
    /**
     * Image de curseur invisible
     */
    private final static Cursor invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(1, 1, new int[1 * 1], 0, 1)), new Point(0, 0), "invisibleCursor");
    /**
     * Chargement de deux pages
     */
    private boolean doublePages = false;
    /**
     * Listener de la molette de la souris
     */
    private MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            readScrollPaneMouseWheelMoved(e);
        }
    };
    /**
     * Listener des touches du clavier
     */
    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {
            readKeyPressed(evt);
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent evt) {
            readKeyReleased(evt);
        }
    };
    /**
     * Listener de page précédente pour la souris
     */
    private MouseAdapter previousPageMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            previousPageMousePressed(e);
        }
    };
    /**
     * Listener de page précédente pour la souris
     */
    private MouseAdapter nextPageMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            nextPageMousePressed(e);
        }
    };
    /**
     * Thread de chargement de l'image suivante
     */
    private Thread imageLoader = null;

    /** Creates new form BookNavigerReadView
     * @param album Le File que cette frame doit gérer
     * @param bnv instance de bnv
     */
    public BookNavigerReadView(File album, BookNavigerView bnv) {
        initComponents();
        this.album = album;
        this.bnv = bnv;
        this.setExtendedState(MAXIMIZED_BOTH);
        readScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        readComponent.initialize();
        this.setVisible(true);
        this.requestFocus();
        waitingCursor(true);
        bna = BookNavigerApp.getApplication();
        if (BookNavigerApp.IS_MAC()) {
            tpa = new TrackPadAdapter(this);
        }
        addKeyListener(keyAdapter);
    }

    /**
     * Initialize cette frame
     * @param startingPage page à laquelle débuter la lecture
     */
    public void initialize(int startingPage) {
        class StartReading extends Thread {
            int startingPage = 1;

            public StartReading(int startingPage) {
                this.startingPage = startingPage;
            }

            @Override
            public void run() {
                getImageFilesFromAlbum();
                displayPage(startingPage);
            }
        }
        new StartReading(startingPage).start();
    }

    /**
     * Liste les images lisibles dans le dossier d'album défini
     */
    private void getImageFilesFromAlbumDirectory() {
        File[] allfiles = album.listFiles();
        if (allfiles == null) {
            return;
        }
        Arrays.sort(allfiles);
        imageFiles = new File[allfiles.length];
        imageFilesToolkit = new boolean[allfiles.length];
        for (int i = 0, j = 0; i < allfiles.length; i++) {
            if (!allfiles[i].isHidden()) {
                imageFilesToolkit[j] = false;
                if (StaticWorld.typeSupportToolkit(allfiles[i].getName())) {
                    imageFilesToolkit[j] = true;
                    imageFiles[j] = allfiles[i];
                    numberOfPage = ++j;
                }
                if (StaticWorld.typeSupportImageIO(allfiles[i].getName())) {
                    imageFiles[j] = allfiles[i];
                    numberOfPage = ++j;
                }
            }
        }
    }

    /**
     * Liste les images lisibles dans l'album zip/cbz défini
     */
    @SuppressWarnings("unchecked")
    private void getImageFilesFromAlbumZip() throws IOException {
        zipFile = new ZipFile(album, "IBM437");
        Enumeration zipEntries = zipFile.getEntries();
        List<ZipArchiveEntry> zipEntriesList = Collections.list(zipEntries);
        ListIterator it = zipEntriesList.listIterator();
        while (it.hasNext()) {
            ZipArchiveEntry currentEntry = (ZipArchiveEntry) it.next();
            if (currentEntry.isDirectory() || !StaticWorld.typeIsImage(currentEntry.getName())) {
                it.remove();
            }
        }
        Collections.sort(zipEntriesList, new Comparator<ZipArchiveEntry>() {

            @Override
            public int compare(ZipArchiveEntry o1, ZipArchiveEntry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        zipArchiveEntries = new ZipArchiveEntry[zipEntriesList.size()];
        numberOfPage = zipEntriesList.size();
        zipArchiveEntries = zipEntriesList.toArray(zipArchiveEntries);
    }

    /**
     * Liste les images lisibles dans l'album rar/cbr défini
     */
    private void getImageFilesFromAlbumRar() throws IOException {
        try {
            Logger.getLogger(Archive.class.getName()).setFilter(new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    if (record.getMessage().equals("exception in archive constructor maybe file is encrypted or currupt")) {
                        new KnownErrorBox(getThis(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
                        return false;
                    }
                    return true;
                }
            });
            rarFile = new Archive(album);
        } catch (RarException ex) {
            new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
        }
        List<FileHeader> rarEntriesList = rarFile.getFileHeaders();
        ListIterator it = rarEntriesList.listIterator();
        while (it.hasNext()) {
            FileHeader currentEntry = (FileHeader) it.next();
            if (currentEntry.isDirectory() || !StaticWorld.typeIsImage(currentEntry.getFileNameString())) {
                it.remove();
            }
        }
        Collections.sort(rarEntriesList, new Comparator<FileHeader>() {

            @Override
            public int compare(FileHeader o1, FileHeader o2) {
                return o1.getFileNameString().compareTo(o2.getFileNameString());
            }
        });
        rarArchiveEntries = new FileHeader[rarEntriesList.size()];
        numberOfPage = rarEntriesList.size();
        rarArchiveEntries = rarEntriesList.toArray(rarArchiveEntries);
    }

    /**
     * Liste les images lisibles dans l'album rar/cbr défini
     */
    @SuppressWarnings("unchecked")
    private void getImageFilesFromAlbumPdf() throws IOException {
        pdfraf = new RandomAccessFile(album, "r");
        pdfchannel = pdfraf.getChannel();
        ByteBuffer buf = pdfchannel.map(FileChannel.MapMode.READ_ONLY, 0, pdfchannel.size());
        pdfFile = new PDFFile(buf);
        numberOfPage = pdfFile.getNumPages();
        pdfPages = new PDFPage[numberOfPage];
        for (int i = 0; i < numberOfPage; i++) {
            pdfPages[i] = pdfFile.getPage(i+1);

        }
    }

    /**
     * Liste les images lisibles dans l'album défini
     */
    private void getImageFilesFromAlbum() {
        if (album.isDirectory()) {
            getImageFilesFromAlbumDirectory();
        }
        if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
            try {
                getImageFilesFromAlbumZip();
            } catch (IOException ex) {
                new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Read_Zip", album.toString());
            }
        }
        if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
            try {
                getImageFilesFromAlbumRar();
            } catch (IOException ex) {
                new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
            }
        }
        if (album.getName().toLowerCase().endsWith(".pdf")) {
            try {
                getImageFilesFromAlbumPdf();
            } catch (IOException ex) {
                new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Read_Pdf", album.toString());
            }
            readComponent.setImageFromPdf(true);
        }
    }

    @Override
    public void dispose() {
        try {
            if (zipFile != null)
                zipFile.close();
            if (rarFile != null)
                rarFile.close();
            if (pdfFile != null) {
                pdfchannel.close();
                pdfraf.close();
            }
        } catch (IOException ex) {
            new KnownErrorBox(this, KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
        }
        bnv.lastReadedPage = currentPage;
        super.dispose();
    }

    /**
     * Défini tous les parametres pour la bordure d'une bd, à savoir la taille
     * et la couleur
     * @param c la couleur de la bordure
     * @param d les dimensions de celle-ci
     */
    protected void setBorderParameters(final Color c, final Dimension d) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                rightBorderReadComponent.setPreferredSize(d);
                rightBorderReadComponent.setNewBackgroundColor(c);
                leftBorderReadComponent.setPreferredSize(d);
                leftBorderReadComponent.setNewBackgroundColor(c);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PopupMenu = new javax.swing.JPopupMenu() {

            @Override
            protected void firePopupMenuWillBecomeInvisible() {
                super.firePopupMenuWillBecomeInvisible();
                disableCursor(false);
            }

            @Override
            protected void firePopupMenuWillBecomeVisible() {
                super.firePopupMenuWillBecomeVisible();
                disableCursor(true);
            }
        };
        navigateMenu = new javax.swing.JMenu();
        previousPageMenuItem = new javax.swing.JMenuItem();
        nextPageMenuItem = new javax.swing.JMenuItem();
        navigateMenuSeparator1 = new javax.swing.JSeparator();
        tenPageBeforeMenuItem = new javax.swing.JMenuItem();
        tenPageAfterMenuItem = new javax.swing.JMenuItem();
        navigateMenuSeparator2 = new javax.swing.JSeparator();
        firstPageMenuItem = new javax.swing.JMenuItem();
        lastPageMenuItem = new javax.swing.JMenuItem();
        navigateMenuSeparator3 = new javax.swing.JSeparator();
        showListMenuItem = new javax.swing.JMenuItem();
        displayMenu = new javax.swing.JMenu();
        zoomMenu = new javax.swing.JMenu();
        defaultZoomMenuItem = new javax.swing.JMenuItem();
        decreaseZoomMenuItem = new javax.swing.JMenuItem();
        increaseZoomMenuItem = new javax.swing.JMenuItem();
        zoomMenuSeparator1 = new javax.swing.JSeparator();
        horizontalMaxZoomCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        verticalMaxZoomCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        rotateMenu = new javax.swing.JMenu();
        rotationInitialRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rotation90CWRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rotation180RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rotation90CCWRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        doublePagesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        minimizeMenuItem = new javax.swing.JMenuItem();
        exitViewerMenuItem = new javax.swing.JMenuItem();
        rotationButtonGroup = new javax.swing.ButtonGroup();
        readScrollPane = new javax.swing.JScrollPane();
        readPanel = new booknaviger.ReadPanel();
        leftBorderReadComponent = new booknaviger.BorderReadComponent();
        readComponent = new booknaviger.ReadComponent(this);
        rightBorderReadComponent = new booknaviger.BorderReadComponent();

        PopupMenu.setName("PopupMenu"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getResourceMap(BookNavigerReadView.class);
        navigateMenu.setIcon(resourceMap.getIcon("navigateMenu.icon")); // NOI18N
        navigateMenu.setText(resourceMap.getString("navigateMenu.text")); // NOI18N
        navigateMenu.setName("navigateMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getActionMap(BookNavigerReadView.class, this);
        previousPageMenuItem.setAction(actionMap.get("goPreviousPage")); // NOI18N
        previousPageMenuItem.setIcon(resourceMap.getIcon("previousPageMenuItem.icon")); // NOI18N
        previousPageMenuItem.setText(resourceMap.getString("previousPageMenuItem.text")); // NOI18N
        previousPageMenuItem.setName("previousPageMenuItem"); // NOI18N
        navigateMenu.add(previousPageMenuItem);

        nextPageMenuItem.setAction(actionMap.get("goNextPage")); // NOI18N
        nextPageMenuItem.setIcon(resourceMap.getIcon("nextPageMenuItem.icon")); // NOI18N
        nextPageMenuItem.setText(resourceMap.getString("nextPageMenuItem.text")); // NOI18N
        nextPageMenuItem.setName("nextPageMenuItem"); // NOI18N
        navigateMenu.add(nextPageMenuItem);

        navigateMenuSeparator1.setName("navigateMenuSeparator1"); // NOI18N
        navigateMenu.add(navigateMenuSeparator1);

        tenPageBeforeMenuItem.setAction(actionMap.get("go10PagesBefore")); // NOI18N
        tenPageBeforeMenuItem.setIcon(resourceMap.getIcon("tenPageBeforeMenuItem.icon")); // NOI18N
        tenPageBeforeMenuItem.setText(resourceMap.getString("tenPageBeforeMenuItem.text")); // NOI18N
        tenPageBeforeMenuItem.setName("tenPageBeforeMenuItem"); // NOI18N
        navigateMenu.add(tenPageBeforeMenuItem);

        tenPageAfterMenuItem.setAction(actionMap.get("go10PagesAfter")); // NOI18N
        tenPageAfterMenuItem.setIcon(resourceMap.getIcon("tenPageAfterMenuItem.icon")); // NOI18N
        tenPageAfterMenuItem.setText(resourceMap.getString("tenPageAfterMenuItem.text")); // NOI18N
        tenPageAfterMenuItem.setName("tenPageAfterMenuItem"); // NOI18N
        navigateMenu.add(tenPageAfterMenuItem);

        navigateMenuSeparator2.setName("navigateMenuSeparator2"); // NOI18N
        navigateMenu.add(navigateMenuSeparator2);

        firstPageMenuItem.setAction(actionMap.get("goFirstPage")); // NOI18N
        firstPageMenuItem.setIcon(resourceMap.getIcon("firstPageMenuItem.icon")); // NOI18N
        firstPageMenuItem.setText(resourceMap.getString("firstPageMenuItem.text")); // NOI18N
        firstPageMenuItem.setName("firstPageMenuItem"); // NOI18N
        navigateMenu.add(firstPageMenuItem);

        lastPageMenuItem.setAction(actionMap.get("goLastPage")); // NOI18N
        lastPageMenuItem.setIcon(resourceMap.getIcon("lastPageMenuItem.icon")); // NOI18N
        lastPageMenuItem.setText(resourceMap.getString("lastPageMenuItem.text")); // NOI18N
        lastPageMenuItem.setName("lastPageMenuItem"); // NOI18N
        navigateMenu.add(lastPageMenuItem);

        navigateMenuSeparator3.setName("navigateMenuSeparator3"); // NOI18N
        navigateMenu.add(navigateMenuSeparator3);

        showListMenuItem.setAction(actionMap.get("displayPageSelector")); // NOI18N
        showListMenuItem.setIcon(resourceMap.getIcon("showListMenuItem.icon")); // NOI18N
        showListMenuItem.setText(resourceMap.getString("showListMenuItem.text")); // NOI18N
        showListMenuItem.setName("showListMenuItem"); // NOI18N
        navigateMenu.add(showListMenuItem);

        PopupMenu.add(navigateMenu);

        displayMenu.setIcon(resourceMap.getIcon("displayMenu.icon")); // NOI18N
        displayMenu.setText(resourceMap.getString("displayMenu.text")); // NOI18N
        displayMenu.setName("displayMenu"); // NOI18N

        zoomMenu.setIcon(resourceMap.getIcon("zoomMenu.icon")); // NOI18N
        zoomMenu.setText(resourceMap.getString("zoomMenu.text")); // NOI18N
        zoomMenu.setName("zoomMenu"); // NOI18N

        defaultZoomMenuItem.setAction(actionMap.get("setDefaultZoom")); // NOI18N
        defaultZoomMenuItem.setIcon(resourceMap.getIcon("defaultZoomMenuItem.icon")); // NOI18N
        defaultZoomMenuItem.setText(resourceMap.getString("defaultZoomMenuItem.text")); // NOI18N
        defaultZoomMenuItem.setName("defaultZoomMenuItem"); // NOI18N
        zoomMenu.add(defaultZoomMenuItem);

        decreaseZoomMenuItem.setAction(actionMap.get("decreaseZoom")); // NOI18N
        decreaseZoomMenuItem.setIcon(resourceMap.getIcon("decreaseZoomMenuItem.icon")); // NOI18N
        decreaseZoomMenuItem.setText(resourceMap.getString("decreaseZoomMenuItem.text")); // NOI18N
        decreaseZoomMenuItem.setName("decreaseZoomMenuItem"); // NOI18N
        zoomMenu.add(decreaseZoomMenuItem);

        increaseZoomMenuItem.setAction(actionMap.get("increaseZoom")); // NOI18N
        increaseZoomMenuItem.setIcon(resourceMap.getIcon("increaseZoomMenuItem.icon")); // NOI18N
        increaseZoomMenuItem.setText(resourceMap.getString("increaseZoomMenuItem.text")); // NOI18N
        increaseZoomMenuItem.setName("increaseZoomMenuItem"); // NOI18N
        zoomMenu.add(increaseZoomMenuItem);

        zoomMenuSeparator1.setName("zoomMenuSeparator1"); // NOI18N
        zoomMenu.add(zoomMenuSeparator1);

        horizontalMaxZoomCheckBoxMenuItem.setAction(actionMap.get("horizontalMaxZoomChange")); // NOI18N
        horizontalMaxZoomCheckBoxMenuItem.setText(resourceMap.getString("horizontalMaxZoomCheckBoxMenuItem.text")); // NOI18N
        horizontalMaxZoomCheckBoxMenuItem.setToolTipText(resourceMap.getString("horizontalMaxZoomCheckBoxMenuItem.toolTipText")); // NOI18N
        horizontalMaxZoomCheckBoxMenuItem.setIcon(resourceMap.getIcon("horizontalMaxZoomCheckBoxMenuItem.icon")); // NOI18N
        horizontalMaxZoomCheckBoxMenuItem.setName("horizontalMaxZoomCheckBoxMenuItem"); // NOI18N
        zoomMenu.add(horizontalMaxZoomCheckBoxMenuItem);

        verticalMaxZoomCheckBoxMenuItem.setAction(actionMap.get("verticalMaxZoomChange")); // NOI18N
        verticalMaxZoomCheckBoxMenuItem.setText(resourceMap.getString("verticalMaxZoomCheckBoxMenuItem.text")); // NOI18N
        verticalMaxZoomCheckBoxMenuItem.setToolTipText(resourceMap.getString("verticalMaxZoomCheckBoxMenuItem.toolTipText")); // NOI18N
        verticalMaxZoomCheckBoxMenuItem.setIcon(resourceMap.getIcon("verticalMaxZoomCheckBoxMenuItem.icon")); // NOI18N
        verticalMaxZoomCheckBoxMenuItem.setName("verticalMaxZoomCheckBoxMenuItem"); // NOI18N
        zoomMenu.add(verticalMaxZoomCheckBoxMenuItem);

        displayMenu.add(zoomMenu);

        rotateMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/menuicons/rotation.png"))); // NOI18N
        rotateMenu.setText(resourceMap.getString("rotateMenu.text")); // NOI18N
        rotateMenu.setName("rotateMenu"); // NOI18N

        rotationInitialRadioButtonMenuItem.setAction(actionMap.get("rotationInitial")); // NOI18N
        rotationButtonGroup.add(rotationInitialRadioButtonMenuItem);
        rotationInitialRadioButtonMenuItem.setSelected(true);
        rotationInitialRadioButtonMenuItem.setIcon(resourceMap.getIcon("rotationInitialRadioButtonMenuItem.icon")); // NOI18N
        rotationInitialRadioButtonMenuItem.setName("rotationInitialRadioButtonMenuItem"); // NOI18N
        rotateMenu.add(rotationInitialRadioButtonMenuItem);

        rotation90CWRadioButtonMenuItem.setAction(actionMap.get("rotation90CW")); // NOI18N
        rotationButtonGroup.add(rotation90CWRadioButtonMenuItem);
        rotation90CWRadioButtonMenuItem.setText(resourceMap.getString("rotation90CWRadioButtonMenuItem.text")); // NOI18N
        rotation90CWRadioButtonMenuItem.setIcon(resourceMap.getIcon("rotation90CWRadioButtonMenuItem.icon")); // NOI18N
        rotation90CWRadioButtonMenuItem.setName("rotation90CWRadioButtonMenuItem"); // NOI18N
        rotateMenu.add(rotation90CWRadioButtonMenuItem);

        rotation180RadioButtonMenuItem.setAction(actionMap.get("rotation180")); // NOI18N
        rotationButtonGroup.add(rotation180RadioButtonMenuItem);
        rotation180RadioButtonMenuItem.setIcon(resourceMap.getIcon("rotation180RadioButtonMenuItem.icon")); // NOI18N
        rotation180RadioButtonMenuItem.setName("rotation180RadioButtonMenuItem"); // NOI18N
        rotateMenu.add(rotation180RadioButtonMenuItem);

        rotation90CCWRadioButtonMenuItem.setAction(actionMap.get("rotation90CCW")); // NOI18N
        rotationButtonGroup.add(rotation90CCWRadioButtonMenuItem);
        rotation90CCWRadioButtonMenuItem.setText(resourceMap.getString("rotation90CCWRadioButtonMenuItem.text")); // NOI18N
        rotation90CCWRadioButtonMenuItem.setIcon(resourceMap.getIcon("rotation90CCWRadioButtonMenuItem.icon")); // NOI18N
        rotation90CCWRadioButtonMenuItem.setName("rotation90CCWRadioButtonMenuItem"); // NOI18N
        rotateMenu.add(rotation90CCWRadioButtonMenuItem);

        displayMenu.add(rotateMenu);

        doublePagesCheckBoxMenuItem.setAction(actionMap.get("activateDoublePages")); // NOI18N
        doublePagesCheckBoxMenuItem.setText(resourceMap.getString("doublePagesCheckBoxMenuItem.text")); // NOI18N
        doublePagesCheckBoxMenuItem.setToolTipText(resourceMap.getString("doublePagesCheckBoxMenuItem.toolTipText")); // NOI18N
        doublePagesCheckBoxMenuItem.setIcon(resourceMap.getIcon("doublePagesCheckBoxMenuItem.icon")); // NOI18N
        doublePagesCheckBoxMenuItem.setName("doublePagesCheckBoxMenuItem"); // NOI18N
        displayMenu.add(doublePagesCheckBoxMenuItem);

        PopupMenu.add(displayMenu);

        minimizeMenuItem.setAction(actionMap.get("reduceToSystray")); // NOI18N
        minimizeMenuItem.setIcon(resourceMap.getIcon("minimizeMenuItem.icon")); // NOI18N
        minimizeMenuItem.setText(resourceMap.getString("minimizeMenuItem.text")); // NOI18N
        minimizeMenuItem.setName("minimizeMenuItem"); // NOI18N
        PopupMenu.add(minimizeMenuItem);

        exitViewerMenuItem.setAction(actionMap.get("exitViewer")); // NOI18N
        exitViewerMenuItem.setIcon(resourceMap.getIcon("exitViewerMenuItem.icon")); // NOI18N
        exitViewerMenuItem.setText(resourceMap.getString("exitViewerMenuItem.text")); // NOI18N
        exitViewerMenuItem.setName("exitViewerMenuItem"); // NOI18N
        PopupMenu.add(exitViewerMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("ReadingFrame"); // NOI18N
        setUndecorated(true);

        horizontalScrollBar = readScrollPane.getHorizontalScrollBar();
        verticalScrollBar = readScrollPane.getVerticalScrollBar();
        readScrollPane.setAlignmentX(0.0F);
        readScrollPane.setAlignmentY(0.0F);
        readScrollPane.setComponentPopupMenu(PopupMenu);
        readScrollPane.setName("readScrollPane"); // NOI18N
        readScrollPane.setPreferredSize(new java.awt.Dimension(480, 344));
        readScrollPane.setWheelScrollingEnabled(false);
        readScrollPane.addMouseWheelListener(mouseWheelListener);

        readPanel.setAlignmentX(0.0F);
        readPanel.setInheritsPopupMenu(true);
        readPanel.setName("readPanel"); // NOI18N
        readPanel.setLayout(new javax.swing.BoxLayout(readPanel, javax.swing.BoxLayout.LINE_AXIS));

        leftBorderReadComponent.setToolTipText(resourceMap.getString("leftBorderReadComponent.toolTipText")); // NOI18N
        leftBorderReadComponent.setAlignmentX(0.0F);
        leftBorderReadComponent.setAlignmentY(0.0F);
        leftBorderReadComponent.setDoubleBuffered(true);
        leftBorderReadComponent.setInheritsPopupMenu(true);
        leftBorderReadComponent.setName("leftBorderReadComponent"); // NOI18N

        javax.swing.GroupLayout leftBorderReadComponentLayout = new javax.swing.GroupLayout(leftBorderReadComponent);
        leftBorderReadComponent.setLayout(leftBorderReadComponentLayout);
        leftBorderReadComponentLayout.setHorizontalGroup(
            leftBorderReadComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        leftBorderReadComponentLayout.setVerticalGroup(
            leftBorderReadComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 344, Short.MAX_VALUE)
        );

        leftBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftBorderReadComponent.addMouseListener(previousPageMouseAdapter);
        readPanel.add(leftBorderReadComponent);

        readComponent.setAlignmentX(0.0F);
        readComponent.setAlignmentY(0.0F);
        readComponent.setDoubleBuffered(true);
        readComponent.setInheritsPopupMenu(true);
        readComponent.setName("readComponent"); // NOI18N

        javax.swing.GroupLayout readComponentLayout = new javax.swing.GroupLayout(readComponent);
        readComponent.setLayout(readComponentLayout);
        readComponentLayout.setHorizontalGroup(
            readComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        readComponentLayout.setVerticalGroup(
            readComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 344, Short.MAX_VALUE)
        );

        readComponent.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(1, 1, new int[1 * 1], 0, 1)), new Point(0, 0), "invisibleCursor"));
        readComponent.addMouseListener(nextPageMouseAdapter);
        readPanel.add(readComponent);

        rightBorderReadComponent.setToolTipText(resourceMap.getString("rightBorderReadComponent.toolTipText")); // NOI18N
        rightBorderReadComponent.setAlignmentX(0.0F);
        rightBorderReadComponent.setAlignmentY(0.0F);
        rightBorderReadComponent.setDoubleBuffered(true);
        rightBorderReadComponent.setInheritsPopupMenu(true);
        rightBorderReadComponent.setName("rightBorderReadComponent"); // NOI18N

        javax.swing.GroupLayout rightBorderReadComponentLayout = new javax.swing.GroupLayout(rightBorderReadComponent);
        rightBorderReadComponent.setLayout(rightBorderReadComponentLayout);
        rightBorderReadComponentLayout.setHorizontalGroup(
            rightBorderReadComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );
        rightBorderReadComponentLayout.setVerticalGroup(
            rightBorderReadComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 344, Short.MAX_VALUE)
        );

        rightBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightBorderReadComponent.addMouseListener(nextPageMouseAdapter);
        readPanel.add(rightBorderReadComponent);

        readScrollPane.setViewportView(readPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(readScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(readScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Afficher la page spécifiée
     * @param page page à afficher
     */
    @SuppressWarnings("deprecation")
    protected void displayPage(int page) {
        readPanel.showFirstPage = false;
        readPanel.showLastPage = false;
        if (page < 1 || page > numberOfPage) {
            if (currentPage == 1) {
                readPanel.showFirstPage = true;
                readPanel.repaint();
                return;
            }
            if ( currentPage == numberOfPage) {
                readPanel.showLastPage = true;
                readPanel.repaint();
                return;
            }
            if (page < 1) {
                page = 1;
            } else {
                page = numberOfPage;
            }
        }
        if (imageLoader != null) {
            if ((page == currentPage + 1 && !doublePages) || (page == currentPage + 2 && doublePages)) {
                loadingPicture(true);
                currentPage = page;
                reinitHits();
                if (imageLoader.isAlive()) {
                    try {
                        imageLoader.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BookNavigerReadView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                readComponent.activatePreImage();
                loadingPicture(false);
                setImageLoader();
                imageLoader.start();
                return;
            }
            imageLoader.stop();
        }
        if (doublePages && page == numberOfPage) {
            doublePages = false;
            doublePagesCheckBoxMenuItem.setSelected(false);
        }
        loadingPicture(true);
        currentPage = page;
        reinitHits();
        final int memoryHorizontalScrollBarPosition = previousHorizontalScrollBarPosition;
        previousHorizontalScrollBarPosition = horizontalScrollBar.getValue();
        Image[] tampon = readImage(page - 1);
        if (previousPageAsked) {
            readComponent.setScrollBarPosition(memoryHorizontalScrollBarPosition, verticalScrollBar.getMaximum());
            previousPageAsked = false;
        } else {
            readComponent.setScrollBarPosition(0, 0);
        }
        if (tampon == null) {
            readComponent.setImage(readComponent.getNoImage());
            loadingPicture(false);
            return;
        }
        if (tampon.length == 2)
            readComponent.setImage(tampon[0], tampon[1]);
        else
            readComponent.setImage(tampon[0]);
        loadingPicture(false);
        setImageLoader();
        imageLoader.start();
    }

    private void loadingPicture(boolean value) {
        if (value) {
            waitingCursor(value);
            readScrollPane.removeMouseWheelListener(mouseWheelListener);
            leftBorderReadComponent.removeMouseListener(previousPageMouseAdapter);
            readComponent.removeMouseListener(nextPageMouseAdapter);
            rightBorderReadComponent.removeMouseListener(nextPageMouseAdapter);
            removeKeyListener(keyAdapter);
            if (BookNavigerApp.IS_MAC())
                tpa.removeListenerFrom(rootPane);
        } else {
            waitingCursor(false);
            readScrollPane.addMouseWheelListener(mouseWheelListener);
            leftBorderReadComponent.addMouseListener(previousPageMouseAdapter);
            readComponent.addMouseListener(nextPageMouseAdapter);
            rightBorderReadComponent.addMouseListener(nextPageMouseAdapter);
            addKeyListener(keyAdapter);
            if (BookNavigerApp.IS_MAC())
                tpa.addListenerOn(rootPane);
        }
    }

    private void setImageLoader() {
        imageLoader = new Thread(new Runnable() {

            @Override
            public void run() {
                int page = 0;
                if (doublePages)
                    page = currentPage + 2;
                else
                    page = currentPage + 1;
                if (page > numberOfPage) {
                    if ( page == numberOfPage) {
                        return;
                    }
                    page = numberOfPage;
                }
                if (doublePages && page == numberOfPage) {
                    doublePages = false;
                    doublePagesCheckBoxMenuItem.setSelected(false);
                }
                Image[] tampon = readImage(page - 1);
                int memoryHorizontalScrollBarPosition = previousHorizontalScrollBarPosition;
                previousHorizontalScrollBarPosition = horizontalScrollBar.getValue();
                if (previousPageAsked) {
                    readComponent.setScrollBarPosition(memoryHorizontalScrollBarPosition, verticalScrollBar.getMaximum());
                    previousPageAsked = false;
                } else {
                    readComponent.setScrollBarPosition(0, 0);
                }
                if (tampon == null) {
                    readComponent.setPreImage(readComponent.getNoImage());
                    return;
                }
                if (tampon.length == 2)
                    readComponent.setPreImage(tampon[0], tampon[1]);
                else
                    readComponent.setPreImage(tampon[0]);
            }
        });
    }

    private Image[] readImage(int page) {
        Image[] tampon = null;
        if (doublePages)
            tampon = new Image[2];
        else
            tampon = new Image[1];
        if (album.isDirectory()) {
            if (doublePages) {
                if (imageFilesToolkit[page] && imageFilesToolkit[page + 1]) {
                    tampon[0] = Toolkit.getDefaultToolkit().createImage(imageFiles[page].toString());
                    tampon[1] = Toolkit.getDefaultToolkit().createImage(imageFiles[page + 1].toString());
                    MediaTracker mt = new MediaTracker(readComponent);
                    mt.addImage(tampon[0], 0);
                    mt.addImage(tampon[1], 0);
                    try {
                        mt.waitForID(0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, "wait for image loading interrupted", ex);
                    }
                    if (tampon[0].getWidth(null) == -1 && tampon[0].getHeight(null) == -1) {
                        new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imageFiles[page].toString());
                        return null;
                    }
                    if (tampon[1].getWidth(null) == -1 && tampon[1].getHeight(null) == -1) {
                        new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imageFiles[page + 1].toString());
                        return null;
                    }
                } else {
                    try {
                        tampon[0] = ImageIO.read(imageFiles[page]).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                        tampon[1] = ImageIO.read(imageFiles[page + 1]).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    } catch (IOException ex) {
                        new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Images", imageFiles[page].toString(), imageFiles[page + 1].toString());
                        return null;
                    } catch (NullPointerException ex) {
                        new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Images", imageFiles[page].toString(), imageFiles[page + 1].toString());
                        return null;
                    }
                }
            }
            else {
                if (imageFilesToolkit[page]) {
                        tampon[0] = Toolkit.getDefaultToolkit().createImage(imageFiles[page].toString());
                        MediaTracker mt = new MediaTracker(readComponent);
                        mt.addImage(tampon[0], 0);
                        try {
                            mt.waitForID(0);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ReadComponent.class.getName()).log(Level.SEVERE, "wait for image loading interrupted", ex);
                        }
                        if (tampon[0].getWidth(null) == -1 && tampon[0].getHeight(null) == -1) {
                            new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imageFiles[page].toString());
                            return null;
                        }
                } else {
                    try {
                        tampon[0] = ImageIO.read(imageFiles[page]).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    } catch (IOException ex) {
                        new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imageFiles[page].toString());
                        return null;
                    } catch (NullPointerException ex) {
                        new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imageFiles[page].toString());
                        return null;
                    }
                }
            }
        }
        if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
            InputStream is = null;
            if (doublePages) {
                try {
                    is = zipFile.getInputStream(zipArchiveEntries[page]);
                    tampon[0] = ImageIO.read(is).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    is.close();
                } catch (ZipException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Unsupported_Zip_Compression", zipArchiveEntries[page].getName());
                    return null;
                } catch (IOException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", zipArchiveEntries[page].getName());
                    return null;
                } catch (NullPointerException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", zipArchiveEntries[page].getName());
                    return null;
                }
                try {
                    is = zipFile.getInputStream(zipArchiveEntries[page + 1]);
                    tampon[1] = ImageIO.read(is).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    is.close();
                } catch (ZipException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Unsupported_Zip_Compression", zipArchiveEntries[page + 1].getName());
                    return null;
                } catch (IOException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", zipArchiveEntries[page + 1].getName());
                    return null;
                } catch (NullPointerException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", zipArchiveEntries[page + 1].getName());
                    return null;
                }
            }
            else {
                try {
                    is = zipFile.getInputStream(zipArchiveEntries[page]);
                    tampon[0] = ImageIO.read(is).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    is.close();
                } catch (ZipException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Unsupported_Zip_Compression", zipArchiveEntries[page].getName());
                    return null;
                } catch (IOException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", zipArchiveEntries[page].getName());
                    return null;
                } catch (NullPointerException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", zipArchiveEntries[page].getName());
                    return null;
                }
            }
        }
        if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
            class ExtractFileFromRarToOs extends Thread {
                Archive a = null;
                FileHeader fh = null;
                OutputStream os = null;

                public ExtractFileFromRarToOs(Archive a, FileHeader fh, OutputStream os) {
                    this.a = a;
                    this.fh = fh;
                    this.os = os;
                }

                @Override
                public void run() {
                    try {
                        a.extractFile(fh, os);
                    } catch (RarException ex) {
                        if (!ex.getType().equals(RarException.RarExceptionType.unkownError))
                            new KnownErrorBox(getThis(), KnownErrorBox.ERROR_LOGO, "Error_Rar_Entity_Malformed", fh.getFileNameString());
                    } finally {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            new KnownErrorBox(getThis(), KnownErrorBox.WARNING_lOGO, "Warning_Close_Entity_Stream", fh.getFileNameString());
                        }
                    }
                }

            }
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = null;
            try {
                pos = new PipedOutputStream(pis);
            } catch (IOException ex) {
                Logger.getLogger(BookNavigerReadView.class.getName()).log(Level.SEVERE, null, ex);
            }
            new ExtractFileFromRarToOs(rarFile, rarArchiveEntries[page], pos).start();
            if (doublePages) {
                try {
                    tampon[0] = ImageIO.read(pis).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    pis.close();
                } catch (IOException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", rarArchiveEntries[page].getFileNameString());
                    return null;
                } catch (NullPointerException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", rarArchiveEntries[page].getFileNameString());
                    return null;
                }
                PipedInputStream pis2 = new PipedInputStream();
                PipedOutputStream pos2 = null;
                try {
                    pos2 = new PipedOutputStream(pis2);
                } catch (IOException ex) {
                    Logger.getLogger(BookNavigerReadView.class.getName()).log(Level.SEVERE, null, ex);
                }
                new ExtractFileFromRarToOs(rarFile, rarArchiveEntries[page + 1], pos2).start();
                try {
                    tampon[1] = ImageIO.read(pis2).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    pis2.close();
                } catch (IOException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", rarArchiveEntries[page + 1].getFileNameString());
                    return null;
                } catch (NullPointerException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", rarArchiveEntries[page + 1].getFileNameString());
                    return null;
                }
            } else {
                try {
                    tampon[0] = ImageIO.read(pis).getScaledInstance(-1, -1, Image.SCALE_SMOOTH);
                    pis.close();
                } catch (IOException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", rarArchiveEntries[page].getFileNameString());
                    return null;
                } catch (NullPointerException ex) {
                    new KnownErrorBox(this, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", rarArchiveEntries[page].getFileNameString());
                    return null;
                }
            }
        }
        if (album.getName().toLowerCase().endsWith(".pdf")) {
            if (doublePages) {
                Rectangle rect = new Rectangle(pdfPages[page].getBBox().getBounds());
                Rectangle rect2 = new Rectangle(pdfPages[page + 1].getBBox().getBounds());
                tampon[0] = pdfPages[page].getImage(rect.width * readComponent.imagePdfZoom , rect.height * readComponent.imagePdfZoom, //width & height
                                                    pdfPages[page].getBBox(), // clip rect
                                                    null, // null for the ImageObserver
                                                    true, // fill background with white
                                                    true  // block until drawing is done
                                                    );
                tampon[1] = pdfPages[page + 1].getImage(rect2.width * readComponent.imagePdfZoom, rect2.height * readComponent.imagePdfZoom, //width & height
                                                      pdfPages[page + 1].getBBox(), // clip rect
                                                      null, // null for the ImageObserver
                                                      true, // fill background with white
                                                      true  // block until drawing is done
                                                      );
            } else {
                Rectangle rect = new Rectangle(pdfPages[page].getBBox().getBounds());
                tampon[0] = pdfPages[page].getImage(rect.width * readComponent.imagePdfZoom, rect.height * readComponent.imagePdfZoom, //width & height
                                                    pdfPages[page].getBBox(), // clip rect
                                                    null, // null for the ImageObserver
                                                    true, // fill background with white
                                                    true  // block until drawing is done
                                                    );
            }
        }
        return tampon;
    }


    /**
     * Changement du curseur en mode chargement
     * @param state true si chargement, false sinon
     */
    protected final void waitingCursor(boolean state) {
        if (state == true) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            leftBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            rightBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            readComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        else {
            this.setCursor(Cursor.getDefaultCursor());
            leftBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rightBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            readComponent.setCursor(invisibleCursor);
        }
    }

    private void disableCursor(boolean state) {
        if (state == true) {
            this.setCursor(Cursor.getDefaultCursor());
            leftBorderReadComponent.setCursor(Cursor.getDefaultCursor());
            rightBorderReadComponent.setCursor(Cursor.getDefaultCursor());
            readComponent.setCursor(Cursor.getDefaultCursor());
        }
        else {
            this.setCursor(Cursor.getDefaultCursor());
            leftBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rightBorderReadComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            readComponent.setCursor(invisibleCursor);
        }
    }

    private void readKeyPressed(java.awt.event.KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        // special event
        if (keyCode == KeyEvent.VK_ESCAPE) {
            exitViewer();
        }
        if (keyCode == KeyEvent.VK_L) {
            displayPageSelector();
        }
        if (keyCode == KeyEvent.VK_M) {
            reduceToSystray();
        }
        // inter-page navigation
        if (keyCode == KeyEvent.VK_END) {
            goLastPage();
        }
        if (keyCode == KeyEvent.VK_HOME) {
            goFirstPage();
        }
        if (keyCode == KeyEvent.VK_PAGE_DOWN) {
            go10PagesAfter();
        }
        if (keyCode == KeyEvent.VK_PAGE_UP) {
            go10PagesBefore();
        }
        // intra-page navigation
        if (keyCode == KeyEvent.VK_DOWN) {
            modifier = 1;
            mouseWheel = false;
            goDown();
        }
        if (keyCode == KeyEvent.VK_UP) {
            modifier = 1;
            mouseWheel = false;
            goUp();
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            modifier = 1;
            mouseWheel = false;
            goRight();
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            modifier = 1;
            mouseWheel = false;
            goLeft();
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            spaceNavigation();
        }
        // zoom key
        if (keyCode == KeyEvent.VK_1) {
            decreaseZoom();
        }
        if (keyCode == KeyEvent.VK_2) {
            increaseZoom();
        }
        if (keyCode == KeyEvent.VK_0) {
            setDefaultZoom();
        }
    }

    private void readKeyReleased(java.awt.event.KeyEvent evt) {
        int keyCode = evt.getKeyCode();

        if (keyCode == KeyEvent.VK_DOWN) {
            reinitUDHits();
        }
        if (keyCode == KeyEvent.VK_UP) {
            reinitUDHits();
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            reiniLRtHits();
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            reiniLRtHits();
        }
    }

    private void previousPageMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            goPreviousPage();
        }
    }

    private void nextPageMousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            goNextPage();
        }
    }

    private void readScrollPaneMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        modifier = 3;
        mouseWheel = true;
        if (evt.isShiftDown()) {
            if (evt.getWheelRotation() > 0) {
                goRight();
            }
            if (evt.getWheelRotation() < 0) {
                goLeft();
            }
            reiniLRtHits();
        } else {
            if (evt.getWheelRotation() > 0) {
                goDown();
            }
            if (evt.getWheelRotation() < 0) {
                goUp();
            }
            reinitUDHits();
        }
    }

    /**
     * Quite le viewer
     */
    @Action
    public void exitViewer() {
        this.dispose();
    }

    /**
     * Aller à la première page
     */
    @Action
    public void goFirstPage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                displayPage(1);
            }
        }).start();
    }

    /**
     * Aller à la dernière page
     */
    @Action
    public void goLastPage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                displayPage(numberOfPage);
            }
        }).start();
    }

    /**
     * Reculer de 10 pages
     */
    @Action
    public void go10PagesBefore() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                displayPage(currentPage - 10);
            }
        }).start();
    }

    /**
     * Avancer de 10 pages
     */
    @Action
    public void go10PagesAfter() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                displayPage(currentPage + 10);
            }
        }).start();
    }

    /**
     * Affiche la fenêtre de sélection d'une page
     */
    @Action
    public void displayPageSelector() {
        final BookNavigerListImagesBox bnlib = new BookNavigerListImagesBox(getThis());
        bnlib.setVisible(true);
        new Thread(new Runnable() {

            @Override
            public void run() {
                String[] imagesFileName = new String[numberOfPage];
                for (int i = 0; i < numberOfPage; i++) {
                    if (album.isDirectory()) {
                        imagesFileName[i] = imageFiles[i].getName();
                    }
                    if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
                        imagesFileName[i] = zipArchiveEntries[i].getName();
                    }
                    if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
                        imagesFileName[i] = rarArchiveEntries[i].getFileNameString();
                    }
                    if (album.getName().toLowerCase().endsWith(".pdf")) {
                        imagesFileName[i] = "Page " + i;
                    }
                }
                bnlib.setImagesNames(imagesFileName, currentPage - 1);
            }
        }).start();
    }

    /**
     * Retourne une instance de cette classe
     * @return instance de cette classe
     */
    public BookNavigerReadView getThis() {
        return this;
    }

    private void reinitHits() {
        reiniLRtHits();
        reinitUDHits();
    }

    private void reiniLRtHits() {
        rightHit = 0;
        leftHit = 0;
    }

    private void reinitUDHits() {
        upHit = 0;
        downHit = 0;
    }

    /**
     * Affiche la partie inférieure de l'image (ou va à l'image suivante si bas de l'image)
     */
    @Action
    public void goDown() {
        if (!verticalScrollBar.isVisible()) {
            if (mouseWheel)
                return;
            goNextPage();
            return;
        }
        int value = verticalScrollBar.getValue();
        upHit = 0;
        if (value == (verticalScrollBar.getMaximum() - verticalScrollBar.getVisibleAmount())) {
            if (downHit == 0 && modifier == 1)
                goNextPage();
        } else {
            downHit = 1;
            verticalScrollBar.setValue(value + scrollUnit * modifier);
        }
    }

    /**
     * Affiche la partie supérieure de l'image (ou va à l'image précédente si haut de l'image)
     */
    @Action
    public void goUp() {
        if (!verticalScrollBar.isVisible()) {
            if (mouseWheel)
                return;
            goPreviousPage();
            return;
        }
        int value = verticalScrollBar.getValue();
        downHit = 0;
        if (value == 0) {
            if (upHit == 0 && currentPage != 1  && modifier == 1) {
                previousPageAsked = true;
                goPreviousPage();
            }
        } else {
            upHit = 1;
            verticalScrollBar.setValue(value - scrollUnit * modifier);
        }
    }

    /**
     * Affiche la partie droite de l'image (ou va à l'image suivante si limite de l'image)
     */
    @Action
    public void goRight() {
        if (!horizontalScrollBar.isVisible()) {
            if (mouseWheel)
                return;
            goNextPage();
            return;
        }
        int value = horizontalScrollBar.getValue();
        leftHit = 0;
        if (value == (horizontalScrollBar.getMaximum() - horizontalScrollBar.getVisibleAmount())) {
            if (rightHit ==  0 && modifier == 1) {
                goNextPage();
            }
        } else {
            rightHit = 1;
            horizontalScrollBar.setValue(value + scrollUnit * modifier);
        }
    }

    /**
     * Affiche la partie gauche de l'image (ou va à l'image précédente si limite de l'image)
     */
    @Action
    public void goLeft() {
        if (!horizontalScrollBar.isVisible()) {
            if (mouseWheel)
                return;
            goPreviousPage();
            return;
        }
        int value = horizontalScrollBar.getValue();
        rightHit = 0;
        if (value == 0) {
            if (leftHit == 0 && modifier == 1 && currentPage != 1) {
                previousPageAsked = true;
                goPreviousPage();
            }
        } else {
            leftHit = 1;
            horizontalScrollBar.setValue(value - scrollUnit * modifier);
        }
    }

    /**
     * Réduire dans le systray
     */
    @Action
    public void reduceToSystray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(BookNavigerReadView.class);
            TrayIcon trayIcon = new TrayIcon(resourceMap.getImageIcon("Application.logo").getImage(), resourceMap.getString("Application.title"));
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    bnv.getFrame().setVisible(true);
                    setVisible(true);
                    toFront();
                    SystemTray sysTray = SystemTray.getSystemTray();
                    sysTray.remove(sysTray.getTrayIcons()[0]);
                }
            });
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                Logger.getLogger(BookNavigerReadView.class.getName()).log(Level.SEVERE, "Exception with the creation of the systray", ex);
            }
            this.setVisible(false);
            bnv.getFrame().setVisible(false);
        } else {
            this.setExtendedState(JFrame.ICONIFIED);
            bnv.getFrame().setExtendedState(JFrame.ICONIFIED);
        }
    }

    /**
     * Aller à la page précédente
     */
    @Action
    public void goPreviousPage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (doublePages)
                    displayPage(currentPage - 2);
                else
                    displayPage(currentPage - 1);
            }
        }).start();
    }

    /**
     * Aller à la page suivante
     */
    @Action
    public void goNextPage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (doublePages)
                    displayPage(currentPage + 2);
                else
                    displayPage(currentPage + 1);
            }
        }).start();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Défiler avec la barre de navigation
     */
    public void spaceNavigation() {
        if (!verticalScrollBar.isVisible()) {
            goNextPage();
            return;
        }
        int value = verticalScrollBar.getValue();
        upHit = 0;
        int extent = verticalScrollBar.getVisibleAmount();
        if (value == (verticalScrollBar.getMaximum() - extent))
            goNextPage();
        else
            verticalScrollBar.setValue(value + extent - 100);
    }

    /**
     * Zoom 1:1
     */
    @Action
    public void setDefaultZoom() {
        readComponent.setDefaultZoom();
    }

    /**
     * augmente le zoom
     */
    @Action
    public void increaseZoom() {
        readComponent.setZoomBigger();
    }

    /**
     * Diminue le zoom
     */
    @Action
    public void decreaseZoom() {
        readComponent.setZoomSmaller();
    }

    /**
     * Zoom horizontal max = taille horizontal de la fenêtre
     */
    @Action
    public void horizontalMaxZoomChange() {
        readComponent.setMaxHorizon(horizontalMaxZoomCheckBoxMenuItem.isSelected());
    }

    /**
     * Zoom vertical max = taille vertical de la fenêtre
     */
    @Action
    public void verticalMaxZoomChange() {
        readComponent.setMaxVerti(verticalMaxZoomCheckBoxMenuItem.isSelected());
    }

    /**
     * Rotation initiale
     */
    @Action
    public void rotationInitial() {
        readComponent.rotateOriginal();
    }

    /**
     * Rotation à 90° de l'image initiale
     */
    @Action
    public void rotation90CW() {
        readComponent.rotate90();
    }

    /**
     * Rotation à 180° de l'image initiale
     */
    @Action
    public void rotation180() {
        readComponent.rotate180();
    }

    /**
     * Rotation à -90° de l'image initiale
     */
    @Action
    public void rotation90CCW() {
        readComponent.rotateMinus90();
    }

    /**
     * Active ou désactive l'affichage de deux pages en simultané
     */
    @Action
    public void activateDoublePages() {
        doublePages = doublePagesCheckBoxMenuItem.isSelected();
        new Thread(new Runnable() {

            @Override
            public void run() {
                displayPage(currentPage);
            }
        }).start();
    }

    /**
     * Retourne instance de ReadComponent
     * @return instance de ReadComponent
     */
    public ReadComponent getReadComponent() {
        return readComponent;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu PopupMenu;
    private javax.swing.JMenuItem decreaseZoomMenuItem;
    private javax.swing.JMenuItem defaultZoomMenuItem;
    private javax.swing.JMenu displayMenu;
    private javax.swing.JCheckBoxMenuItem doublePagesCheckBoxMenuItem;
    private javax.swing.JMenuItem exitViewerMenuItem;
    private javax.swing.JMenuItem firstPageMenuItem;
    private javax.swing.JCheckBoxMenuItem horizontalMaxZoomCheckBoxMenuItem;
    private javax.swing.JMenuItem increaseZoomMenuItem;
    private javax.swing.JMenuItem lastPageMenuItem;
    protected booknaviger.BorderReadComponent leftBorderReadComponent;
    private javax.swing.JMenuItem minimizeMenuItem;
    private javax.swing.JMenu navigateMenu;
    private javax.swing.JSeparator navigateMenuSeparator1;
    private javax.swing.JSeparator navigateMenuSeparator2;
    private javax.swing.JSeparator navigateMenuSeparator3;
    private javax.swing.JMenuItem nextPageMenuItem;
    private javax.swing.JMenuItem previousPageMenuItem;
    private booknaviger.ReadComponent readComponent;
    private booknaviger.ReadPanel readPanel;
    protected javax.swing.JScrollPane readScrollPane;
    protected booknaviger.BorderReadComponent rightBorderReadComponent;
    private javax.swing.JMenu rotateMenu;
    protected javax.swing.JRadioButtonMenuItem rotation180RadioButtonMenuItem;
    protected javax.swing.JRadioButtonMenuItem rotation90CCWRadioButtonMenuItem;
    protected javax.swing.JRadioButtonMenuItem rotation90CWRadioButtonMenuItem;
    private javax.swing.ButtonGroup rotationButtonGroup;
    protected javax.swing.JRadioButtonMenuItem rotationInitialRadioButtonMenuItem;
    private javax.swing.JMenuItem showListMenuItem;
    private javax.swing.JMenuItem tenPageAfterMenuItem;
    private javax.swing.JMenuItem tenPageBeforeMenuItem;
    private javax.swing.JCheckBoxMenuItem verticalMaxZoomCheckBoxMenuItem;
    private javax.swing.JMenu zoomMenu;
    private javax.swing.JSeparator zoomMenuSeparator1;
    // End of variables declaration//GEN-END:variables
}
