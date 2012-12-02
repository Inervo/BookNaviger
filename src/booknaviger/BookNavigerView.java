/*
 * BookNavigerView.java
 */
package booknaviger;

import booknaviger.errorhandler.KnownErrorBox;
import booknaviger.htmlReport.GenerateReport;
import booknaviger.htmlReport.ModeSelector;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Filter;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.jdesktop.application.Application;

/**
 * The application's main frame.
 */
public class BookNavigerView extends FrameView {

    File directory = null;
    File serie = null;
    File album = null;
    String lastReadedProfile = null;
    String lastReadedSerie = null;
    String lastReadedAlbum = null;
    Integer lastReadedPage = 0;
    Date lastUpdateCheck = null;
    ImageIcon previewIcon = null;
    ThreadedPreviewLoader threadedPreviewLoader = new ThreadedPreviewLoader();
    BookNavigerApp bnp = null;
    boolean isAdjusting = false;
    String[][] profiles = new String[1][2];
    short currentProfile = 0;
    ArrayList<JRadioButtonMenuItem> profilesListRadioButtonMenuItem = new ArrayList<JRadioButtonMenuItem>(1);

    /**
     * Initialisation de la frame
     * @param app
     */
    public BookNavigerView(SingleFrameApplication app) {
        super(app);
        bnp = (BookNavigerApp) app;
        
        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        getFrame().setIconImage(resourceMap.getImageIcon("Application.logo").getImage());
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    /**
     * Charge et applique les propriétés lus depuis le fichier de propriétés
     */
    protected void loadAndApplyProperties() {
        String value = bnp.p.getProperty("Profiles");
        if (value == null) {
            profiles[0][0] = Application.getInstance(BookNavigerApp.class).getContext().getResourceMap(BookNavigerView.class).getString("Default.string");
            profiles[0][1] = "";
            currentProfile = 0;
            createProfileRadioButtonMenuItem(profiles[0][0]).setSelected(true);
            createProfileComboBox();
        } else {
            String[] unparsedProfiles = value.split(";");
            profiles = new String[unparsedProfiles.length][2];
            for (int i = 0; i < unparsedProfiles.length; i++) {
                String[] profileData = unparsedProfiles[i].split(",");
                if (profileData.length == 2)
                    profiles[i] = profileData;
                else {
                    profiles[i][0] = profileData[0];
                    profiles[i][1] = "";
                }
                createProfileRadioButtonMenuItem(this.profiles[i][0]);
                createProfileComboBox();
            }
            value = bnp.p.getProperty("CurrentProfile");
            if (value == null)
                currentProfile = 0;
            else
                currentProfile = Short.decode(value);
            applyProfile();
        }
        value = bnp.p.getProperty("CurrentSerieSelected");
        if (value != null && directory != null) {
            serie = new File(directory.toString() + File.separator + value);
            final String finalValue = value;
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        int rows = serieTable.getModel().getRowCount();
                        for (int i = 0; i < rows; i++) {
                            String rowValue = (String) serieTable.getModel().getValueAt(i, 0);
                            if (rowValue.equals(finalValue)) {
                                serieTable.changeSelection(i, 0, false, false);
                                break;
                            }
                        }
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        while (isAdjusting) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        value = bnp.p.getProperty("CurrentAlbumSelected");
        if (value != null && serie != null) {
            File lastSelectedAlbum = new File(serie.toString() + File.separator + value);
            if (lastSelectedAlbum.exists()) {
                this.album = lastSelectedAlbum;
                final String finalValue = value;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        int rows = albumTable.getModel().getRowCount();
                        for (int i = 0; i < rows; i++) {
                            String rowValue = (String) albumTable.getModel().getValueAt(i, 0) + albumTable.getModel().getValueAt(i, 1);
                            if (rowValue.equals(finalValue)) {
                                albumTable.changeSelection(i, 0, false, false);
                                break;
                            }
                        }
                    }
                });
            }
        }
        value = bnp.p.getProperty("LastReadedProfile");
        if (value != null)
            lastReadedProfile = value;
        value = bnp.p.getProperty("LastReadedSerie");
        if (value != null)
            lastReadedSerie = value;
        value = bnp.p.getProperty("LastReadedAlbum");
        if (value != null)
            lastReadedAlbum = value;
        value = bnp.p.getProperty("LastReadedPage");
        if (value != null)
            lastReadedPage = Integer.decode(value);
        value = bnp.p.getProperty("AutoCheckUpdates");
        if (value != null) {
            if (value.equals("false")) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            checkUpdatesCheckBoxMenuItem.setSelected(false);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            checkUpdatesCheckBoxMenuItem.setSelected(true);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (checkUpdatesCheckBoxMenuItem.isSelected())
            new CheckForUpdates(bnp.p.getProperty("LastUpdatesChecked")).start();
        if (BookNavigerApp.language != null) {
            if (BookNavigerApp.language.equals("en_US"))
                languageEnglishRadioButtonMenuItem.setSelected(true);
            if (BookNavigerApp.language.equals("fr_FR"))
                languageFrenchRadioButtonMenuItem.setSelected(true);
        }
    }

    /**
     * Affichage de l'about
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = BookNavigerApp.getApplication().getMainFrame();
            aboutBox = new BookNavigerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        BookNavigerApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainToolBar = new javax.swing.JToolBar();
        resumeReadingButton = new javax.swing.JButton();
        toolbarSeparator1 = new javax.swing.JToolBar.Separator();
        refreshSeriesButton = new javax.swing.JButton();
        refreshAlbumsButton = new javax.swing.JButton();
        toolbarSeparator2 = new javax.swing.JToolBar.Separator();
        profilesComboBox = new javax.swing.JComboBox();
        listSplitPane = new javax.swing.JSplitPane();
        serieScrollPane = new javax.swing.JScrollPane();
        serieTable = new javax.swing.JTable();
        albumScrollPane = new javax.swing.JScrollPane();
        albumTable = new javax.swing.JTable();
        previewComponent = new booknaviger.PreviewComponent();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        resumeReadingMenuItem = new javax.swing.JMenuItem();
        generateReportMenuItem = new javax.swing.JMenuItem();
        folderMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        listingMenu = new javax.swing.JMenu();
        profileMenu = new javax.swing.JMenu();
        listProfileMenuItem = new javax.swing.JMenuItem();
        profileSeparator1 = new javax.swing.JPopupMenu.Separator();
        listingSeparator1 = new javax.swing.JPopupMenu.Separator();
        refreshSeriesMenuItem = new javax.swing.JMenuItem();
        refreshAlbumsMenuItem = new javax.swing.JMenuItem();
        languageMenu = new javax.swing.JMenu();
        languageDefaultRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        languageSeparator = new javax.swing.JPopupMenu.Separator();
        languageEnglishRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        languageFrenchRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        checkUpdatesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        folderChooser = new javax.swing.JFileChooser();
        profileButtonGroup = new javax.swing.ButtonGroup();
        languageButtonGroup = new javax.swing.ButtonGroup();

        mainPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(640, 480));
        mainPanel.setLayout(new java.awt.BorderLayout(1, 0));

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);
        mainToolBar.setName("mainToolBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getActionMap(BookNavigerView.class, this);
        resumeReadingButton.setAction(actionMap.get("resumeReading")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getResourceMap(BookNavigerView.class);
        resumeReadingButton.setIcon(resourceMap.getIcon("resumeReadingButton.icon")); // NOI18N
        resumeReadingButton.setToolTipText(resourceMap.getString("resumeReadingButton.toolTipText")); // NOI18N
        resumeReadingButton.setFocusable(false);
        resumeReadingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        resumeReadingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        resumeReadingButton.setName("resumeReadingButton"); // NOI18N
        resumeReadingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        mainToolBar.add(resumeReadingButton);

        toolbarSeparator1.setName("toolbarSeparator1"); // NOI18N
        mainToolBar.add(toolbarSeparator1);

        refreshSeriesButton.setAction(actionMap.get("refreshSeries")); // NOI18N
        refreshSeriesButton.setIcon(resourceMap.getIcon("refreshSeriesButton.icon")); // NOI18N
        refreshSeriesButton.setToolTipText(resourceMap.getString("refreshSeriesButton.toolTipText")); // NOI18N
        refreshSeriesButton.setFocusable(false);
        refreshSeriesButton.setMaximumSize(new java.awt.Dimension(20, 20));
        refreshSeriesButton.setMinimumSize(new java.awt.Dimension(20, 20));
        refreshSeriesButton.setName("refreshSeriesButton"); // NOI18N
        refreshSeriesButton.setPreferredSize(new java.awt.Dimension(20, 20));
        mainToolBar.add(refreshSeriesButton);

        refreshAlbumsButton.setAction(actionMap.get("refreshAlbums")); // NOI18N
        refreshAlbumsButton.setIcon(resourceMap.getIcon("refreshAlbumsButton.icon")); // NOI18N
        refreshAlbumsButton.setToolTipText(resourceMap.getString("refreshAlbumsButton.toolTipText")); // NOI18N
        refreshAlbumsButton.setFocusable(false);
        refreshAlbumsButton.setMaximumSize(new java.awt.Dimension(20, 20));
        refreshAlbumsButton.setMinimumSize(new java.awt.Dimension(20, 20));
        refreshAlbumsButton.setName("refreshAlbumsButton"); // NOI18N
        refreshAlbumsButton.setPreferredSize(new java.awt.Dimension(20, 20));
        mainToolBar.add(refreshAlbumsButton);

        toolbarSeparator2.setName("toolbarSeparator2"); // NOI18N
        mainToolBar.add(toolbarSeparator2);

        profilesComboBox.setAction(actionMap.get("newProfileComboBoxSelected")); // NOI18N
        profilesComboBox.setFocusable(false);
        profilesComboBox.setMaximumSize(new java.awt.Dimension(200, 32767));
        profilesComboBox.setName("profilesComboBox"); // NOI18N
        profilesComboBox.setPreferredSize(new java.awt.Dimension(30, 27));
        profilesComboBox.setSize(new java.awt.Dimension(30, 27));
        mainToolBar.add(profilesComboBox);

        mainPanel.add(mainToolBar, java.awt.BorderLayout.PAGE_START);

        listSplitPane.setDividerLocation(300);
        listSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        listSplitPane.setLastDividerLocation(300);
        listSplitPane.setName("listSplitPane"); // NOI18N

        serieScrollPane.setName("serieScrollPane"); // NOI18N

        serieTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Series", "Nb"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        serieTable.setFillsViewportHeight(true);
        serieTable.setName("serieTable"); // NOI18N
        serieTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serieTable.setShowGrid(false);
        serieTable.getTableHeader().setReorderingAllowed(false);
        serieTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                serieTableKeyPressed(evt);
            }
        });
        serieScrollPane.setViewportView(serieTable);
        serieTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("serieTable.columnModel.title0")); // NOI18N
        serieTable.getColumnModel().getColumn(1).setMaxWidth(50);
        serieTable.getColumnModel().getColumn(1).setMinWidth(25);
        ListSelectionModel serieListSelectionModel = serieTable.getSelectionModel();
        serieListSelectionModel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                serieTableValueChanged(evt);
            }
        });
        booknaviger.StaticWorld.setQuickSearch(serieTable, false, null);

        listSplitPane.setLeftComponent(serieScrollPane);

        albumScrollPane.setName("albumScrollPane"); // NOI18N

        albumTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Albums", "Extension"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        albumTable.setFillsViewportHeight(true);
        albumTable.setName("albumTable"); // NOI18N
        albumTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        albumTable.setShowHorizontalLines(false);
        albumTable.setShowVerticalLines(false);
        albumTable.getTableHeader().setReorderingAllowed(false);
        albumTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                albumTableMouseClicked(evt);
            }
        });
        albumTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                albumTableKeyPressed(evt);
            }
        });
        albumScrollPane.setViewportView(albumTable);
        albumTable.getColumnModel().getColumn(1).setMinWidth(0);
        albumTable.getColumnModel().getColumn(1).setMaxWidth(0);
        ListSelectionModel albumListSelectionModel = albumTable.getSelectionModel();
        albumListSelectionModel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                albumTableValueChanged(evt);
            }
        });
        booknaviger.StaticWorld.setQuickSearch(albumTable, true, null);

        listSplitPane.setRightComponent(albumScrollPane);

        mainPanel.add(listSplitPane, java.awt.BorderLayout.CENTER);

        previewComponent.setBnv(this);
        previewComponent.setName("previewComponent"); // NOI18N
        previewComponent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                previewComponentMouseClicked(evt);
            }
        });
        previewComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mainPanel.add(previewComponent, java.awt.BorderLayout.EAST);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setMnemonic('F');
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        resumeReadingMenuItem.setAction(actionMap.get("resumeReading")); // NOI18N
        resumeReadingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        resumeReadingMenuItem.setIcon(resourceMap.getIcon("resumeReadingMenuItem.icon")); // NOI18N
        resumeReadingMenuItem.setText(resourceMap.getString("resumeReadingMenuItem.text")); // NOI18N
        resumeReadingMenuItem.setName("resumeReadingMenuItem"); // NOI18N
        fileMenu.add(resumeReadingMenuItem);

        generateReportMenuItem.setAction(actionMap.get("generateHTMLReport")); // NOI18N
        generateReportMenuItem.setIcon(resourceMap.getIcon("generateReportMenuItem.icon")); // NOI18N
        generateReportMenuItem.setText(resourceMap.getString("generateReportMenuItem.text")); // NOI18N
        generateReportMenuItem.setName("generateReportMenuItem"); // NOI18N
        fileMenu.add(generateReportMenuItem);

        folderMenuItem.setAction(actionMap.get("changeFolder")); // NOI18N
        folderMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        folderMenuItem.setIcon(resourceMap.getIcon("folderMenuItem.icon")); // NOI18N
        folderMenuItem.setText(resourceMap.getString("folderMenuItem.text")); // NOI18N
        folderMenuItem.setToolTipText(resourceMap.getString("folderMenuItem.toolTipText")); // NOI18N
        folderMenuItem.setName("folderMenuItem"); // NOI18N
        fileMenu.add(folderMenuItem);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        if(!bnp.IS_MAC())
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        listingMenu.setText(resourceMap.getString("listingMenu.text")); // NOI18N
        listingMenu.setName("listingMenu"); // NOI18N

        profileMenu.setIcon(resourceMap.getIcon("profileMenu.icon")); // NOI18N
        profileMenu.setText(resourceMap.getString("profileMenu.text")); // NOI18N
        profileMenu.setName("profileMenu"); // NOI18N

        listProfileMenuItem.setAction(actionMap.get("showProfileDetail")); // NOI18N
        listProfileMenuItem.setIcon(resourceMap.getIcon("listProfileMenuItem.icon")); // NOI18N
        listProfileMenuItem.setText(resourceMap.getString("listProfileMenuItem.text")); // NOI18N
        listProfileMenuItem.setName("listProfileMenuItem"); // NOI18N
        profileMenu.add(listProfileMenuItem);

        profileSeparator1.setName("profileSeparator1"); // NOI18N
        profileMenu.add(profileSeparator1);

        listingMenu.add(profileMenu);

        listingSeparator1.setName("listingSeparator1"); // NOI18N
        listingMenu.add(listingSeparator1);

        refreshSeriesMenuItem.setAction(actionMap.get("refreshSeries")); // NOI18N
        refreshSeriesMenuItem.setIcon(resourceMap.getIcon("refreshSeriesMenuItem.icon")); // NOI18N
        refreshSeriesMenuItem.setText(resourceMap.getString("refreshSeriesMenuItem.text")); // NOI18N
        refreshSeriesMenuItem.setName("refreshSeriesMenuItem"); // NOI18N
        listingMenu.add(refreshSeriesMenuItem);

        refreshAlbumsMenuItem.setAction(actionMap.get("refreshAlbums")); // NOI18N
        refreshAlbumsMenuItem.setIcon(resourceMap.getIcon("refreshAlbumsMenuItem.icon")); // NOI18N
        refreshAlbumsMenuItem.setText(resourceMap.getString("refreshAlbumsMenuItem.text")); // NOI18N
        refreshAlbumsMenuItem.setName("refreshAlbumsMenuItem"); // NOI18N
        listingMenu.add(refreshAlbumsMenuItem);

        menuBar.add(listingMenu);

        languageMenu.setText(resourceMap.getString("languageMenu.text")); // NOI18N
        languageMenu.setName("languageMenu"); // NOI18N

        languageDefaultRadioButtonMenuItem.setAction(actionMap.get("changeLanguage")); // NOI18N
        languageButtonGroup.add(languageDefaultRadioButtonMenuItem);
        languageDefaultRadioButtonMenuItem.setSelected(true);
        languageDefaultRadioButtonMenuItem.setText(resourceMap.getString("languageDefaultRadioButtonMenuItem.text")); // NOI18N
        languageDefaultRadioButtonMenuItem.setActionCommand("Default"); // NOI18N
        languageDefaultRadioButtonMenuItem.setIcon(resourceMap.getIcon("languageDefaultRadioButtonMenuItem.icon")); // NOI18N
        languageDefaultRadioButtonMenuItem.setName("languageDefaultRadioButtonMenuItem"); // NOI18N
        languageMenu.add(languageDefaultRadioButtonMenuItem);

        languageSeparator.setName("languageSeparator"); // NOI18N
        languageMenu.add(languageSeparator);

        languageEnglishRadioButtonMenuItem.setAction(actionMap.get("changeLanguage")); // NOI18N
        languageButtonGroup.add(languageEnglishRadioButtonMenuItem);
        languageEnglishRadioButtonMenuItem.setText(resourceMap.getString("languageEnglishRadioButtonMenuItem.text")); // NOI18N
        languageEnglishRadioButtonMenuItem.setActionCommand("English"); // NOI18N
        languageEnglishRadioButtonMenuItem.setIcon(resourceMap.getIcon("languageEnglishRadioButtonMenuItem.icon")); // NOI18N
        languageEnglishRadioButtonMenuItem.setName("languageEnglishRadioButtonMenuItem"); // NOI18N
        languageMenu.add(languageEnglishRadioButtonMenuItem);

        languageFrenchRadioButtonMenuItem.setAction(actionMap.get("changeLanguage")); // NOI18N
        languageButtonGroup.add(languageFrenchRadioButtonMenuItem);
        languageFrenchRadioButtonMenuItem.setText(resourceMap.getString("languageFrenchRadioButtonMenuItem.text")); // NOI18N
        languageFrenchRadioButtonMenuItem.setActionCommand("French"); // NOI18N
        languageFrenchRadioButtonMenuItem.setIcon(resourceMap.getIcon("languageFrenchRadioButtonMenuItem.icon")); // NOI18N
        languageFrenchRadioButtonMenuItem.setName("languageFrenchRadioButtonMenuItem"); // NOI18N
        languageMenu.add(languageFrenchRadioButtonMenuItem);

        menuBar.add(languageMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        checkUpdatesCheckBoxMenuItem.setAction(actionMap.get("checkUpdatesStatusChange")); // NOI18N
        checkUpdatesCheckBoxMenuItem.setSelected(true);
        checkUpdatesCheckBoxMenuItem.setText(resourceMap.getString("checkUpdatesCheckBoxMenuItem.text")); // NOI18N
        checkUpdatesCheckBoxMenuItem.setToolTipText(resourceMap.getString("checkUpdatesCheckBoxMenuItem.toolTipText")); // NOI18N
        checkUpdatesCheckBoxMenuItem.setName("checkUpdatesCheckBoxMenuItem"); // NOI18N
        helpMenu.add(checkUpdatesCheckBoxMenuItem);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        if(!bnp.IS_MAC())
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 498, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        folderChooser.setDialogTitle(resourceMap.getString("folderChooser.dialogTitle")); // NOI18N
        folderChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setName("folderChooser"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Listener sur le changement d'une série
     */
    private synchronized void serieTableValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (directory == null) {
            return;
        }
        final int selectedRow = serieTable.getSelectedRow();
        if (!evt.getValueIsAdjusting() && selectedRow != -1) {
            listAlbums(selectedRow);
        }
    }

    @SuppressWarnings("deprecation")
    private void listAlbums(final int selectedRow) {
        waitingCursor(true);
        new Thread(new Runnable() {

            @Override
            public void run() {
                threadedPreviewLoader.stop();
                while (threadedPreviewLoader.isAlive() || isAdjusting)
                    try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                isAdjusting = true;
                serie = new File(directory.toString() + File.separator + serieTable.getValueAt(selectedRow, 0).toString());
                File[] allfiles = null;
                try {
                    allfiles = serie.listFiles();
                } catch(SecurityException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rights", serie.toString());
                }
                if (allfiles == null) {
                    return;
                }
                Arrays.sort(allfiles);
                final File[] allFilesValue = allfiles;
                final DefaultTableModel albumsTableModel = (DefaultTableModel) albumTable.getModel();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            albumsTableModel.setRowCount(0);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                List<Thread> rows = new ArrayList<Thread>();
                for (int i = 0; i < allFilesValue.length; i++) {
                    String name = allFilesValue[i].getName();
                    if (!allFilesValue[i].isHidden() && (allFilesValue[i].isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf"))) {
                        final int index = i;
                        Thread tampon = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                if (allFilesValue[index].isDirectory())
                                    albumsTableModel.addRow(new Object[]{allFilesValue[index].getName(), ""});
                                else {
                                    String albumFullName = allFilesValue[index].getName();
                                    int indexOfExtension = albumFullName.length() - 4;
                                    albumsTableModel.addRow(new Object[]{albumFullName.substring(0, indexOfExtension), albumFullName.substring(indexOfExtension)});
                                }
                            }
                        });
                        SwingUtilities.invokeLater(tampon);
                        rows.add(tampon);
                    }
                }
                for (Thread thread : rows) {
                    try {
                        thread.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            if (albumsTableModel.getRowCount() != 0)
                                albumTable.getSelectionModel().setSelectionInterval(0, 0);
                            albumScrollPane.getVerticalScrollBar().setValue(0);
                            isAdjusting = false;
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    /**
     * listener sur le changement d'un album (au sein d'une série)
     */
    @SuppressWarnings("deprecation")
    private synchronized void albumTableValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (directory == null) {
            return;
        }
        int selectedRow = albumTable.getSelectedRow();
        if (!evt.getValueIsAdjusting() && selectedRow != -1) {
            threadedPreviewLoader.stop();
            while (threadedPreviewLoader.isAlive())
                try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
            }
            album = new File(serie.toString() + File.separator + albumTable.getValueAt(selectedRow, 0).toString() + albumTable.getValueAt(selectedRow, 1).toString());
            threadedPreviewLoader = new ThreadedPreviewLoader();
            threadedPreviewLoader.start();
        }
    }

    class ThreadedPreviewLoader extends Thread {
        
        public ThreadedPreviewLoader() {
        }

        @Override
        public void run() {
            waitingCursor(true);
            if (album.isDirectory()) {
                previewNormalFile();
            }
            if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
                previewZipFile();
            }
            if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
                previewRarFile();
            }
            if (album.getName().toLowerCase().endsWith(".pdf")) {
                previewPdfFile();
            }
        }
    }

    /**
     * Changement de forme du curseur mode normal / chargement
     * @param state true si chargement, false si normal
     */
    public void waitingCursor(boolean state) {
        if (state) {
            this.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            previewComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
            }
        } else {
            this.getFrame().setCursor(Cursor.getDefaultCursor());
            previewComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
        }
    }

    private void previewComponentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_previewComponentMouseClicked
        if (album != null) {
            startReading();
        }
    }//GEN-LAST:event_previewComponentMouseClicked

    private void albumTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_albumTableMouseClicked
        if (evt.getClickCount() == 2 && album != null) {
            startReading();
        }
    }//GEN-LAST:event_albumTableMouseClicked

    private void albumTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumTableKeyPressed
        int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER) {
            evt.consume();
            startReading();
        }
        if (keyCode == KeyEvent.VK_TAB) {
            evt.consume();
            serieTable.requestFocusInWindow();
        }
    }//GEN-LAST:event_albumTableKeyPressed

    private void serieTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_serieTableKeyPressed
        int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER && album != null) {
            evt.consume();
            startReading();
        }
        if (keyCode == KeyEvent.VK_TAB) {
            evt.consume();
            albumTable.requestFocusInWindow();
        }
    }//GEN-LAST:event_serieTableKeyPressed

    /**
     * Lancement de la BookNavigerReadView, et passage de l'album sélectionné.
     */
    @Action
    public void startReading() {
        if (readView != null) {
            readView.dispose();
        }
        readView = new BookNavigerReadView(album, this);
        readView.initialize(1);
        lastReadedProfile = profiles[currentProfile][0];
        lastReadedSerie = serie.getName();
        lastReadedAlbum = album.getName();
    }

    /**
     * Affichage dans le previewComponent d'une firstImage simple
     * <p>Format de fichiers supportés :
     * <ul><li>jpg = firstImage/jpeg (géré - Toolkit)
     * <li>bmp = null (géré par extension - ImageIO)
     * <li>gif = firstImage/gif (géré - Toolkit)
     * <li>png = firstImage/png (géré - Toolkit)
     * <li>tif = firstImage/tiff (pas géré)</ul>
     */
    private void previewNormalFile() {
        File[] allfiles = null;
        File imagePath = null;

        try {
            allfiles = album.listFiles();
        } catch(SecurityException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rights", serie.toString());
            previewComponent.setNoPreviewImage();
        }
        if (allfiles != null) {
            Arrays.sort(allfiles);
            boolean useToolkit = false;
            for (int i = 0; i < allfiles.length; i++) {
                if (!allfiles[i].isHidden()) {
                    if (StaticWorld.typeSupportToolkit(allfiles[i].getName())) {
                        useToolkit = true;
                        imagePath = allfiles[i];
                        break;
                    }
                    if (StaticWorld.typeSupportImageIO(allfiles[i].getName())) {
                        imagePath = allfiles[i];
                        break;
                    }
                }
            }
            if (imagePath == null) {
                return;
            }
            if (useToolkit) {
                previewComponent.setNewImage(imagePath);
            } else {
                try {
                    previewComponent.setNewImageIO(imagePath);
                } catch (IOException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imagePath.toString());
                    previewComponent.setNoPreviewImage();
                } catch (NullPointerException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", imagePath.toString());
                    previewComponent.setNoPreviewImage();
                }
            }
        }
        waitingCursor(false);
    }

    /**
     * Affichage dans le previewComponent d'une firstImage simple provenant d'un archive zip
     * <p>Format de fichiers supportés :
     * Tout ce que ImageIO.read supporte
     */
    @Action
    @SuppressWarnings("unchecked")
    private void previewZipFile() {
        ZipFile zf = null;

        try {
            zf = new ZipFile(album, "IBM437");
        } catch (IOException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Zip", album.toString());
            previewComponent.setNoPreviewImage();
            return;
        }
        Enumeration zipEntries = zf.getEntries();
        List<ZipArchiveEntry> zipEntriesList = Collections.list(zipEntries);
        Collections.sort(zipEntriesList, new Comparator<ZipArchiveEntry>() {

        @Override
        public int compare(ZipArchiveEntry o1, ZipArchiveEntry o2) {
            return o1.getName().compareTo(o2.getName());
        }
        });
        ListIterator it = zipEntriesList.listIterator();
        while(it.hasNext()) {
            ZipArchiveEntry currentEntry = (ZipArchiveEntry) it.next();
            if (!currentEntry.isDirectory() && StaticWorld.typeIsImage(currentEntry.getName())) {
                try {
                    previewComponent.setNewImageStream(zf.getInputStream(currentEntry));
                } catch (ZipException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Unsupported_Zip_Compression", currentEntry.getName());
                    previewComponent.setNoPreviewImage();
                } catch (IOException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getName());
                    previewComponent.setNoPreviewImage();
                } catch (NullPointerException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getName());
                    previewComponent.setNoPreviewImage();
                }
                break;
            }
        }
        try {
            if (zf != null)
                zf.close();
        } catch (IOException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
        }
        waitingCursor(false);
    }

    /**
     * Affichage dans le previewComponent d'une firstImage simple provenant d'un archive rar
     * <p>Format de fichiers supportés :
     * Tout ce que ImageIO.read supporte
     */
    @Action
    private void previewRarFile() {
        Archive archive = null;

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
                    if (!ex.getType().equals(RarException.RarExceptionType.unkownError)) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Rar_Entity_Malformed", fh.getFileNameString());
                        previewComponent.setNoPreviewImage();
                    }
                } finally {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.WARNING_lOGO, "Warning_Close_Entity_Stream", fh.getFileNameString());
                    }
                }
            }

        }
        try {
            Logger.getLogger(Archive.class.getName()).setFilter(new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    if (record.getMessage().equals("exception in archive constructor maybe file is encrypted or currupt")) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
                        previewComponent.setNoPreviewImage();
                        return false;
                    }
                    return true;
                }
            });
            archive = new Archive(new File(album.toString()));
        } catch (RarException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
            previewComponent.setNoPreviewImage();
        } catch (IOException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
            previewComponent.setNoPreviewImage();
        }
        if (archive != null) {
            List<FileHeader> fhl = archive.getFileHeaders();
            Collections.sort(fhl, new Comparator<FileHeader>() {

                @Override
                public int compare(FileHeader o1, FileHeader o2) {
                    return o1.getFileNameString().compareTo(o2.getFileNameString());
                }
            });
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = null;
            try {
                pos = new PipedOutputStream(pis);
            } catch (IOException ex) {
                Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
            }
            ListIterator it = fhl.listIterator();
            while(it.hasNext()) {
                FileHeader currentEntry = (FileHeader) it.next();
                if (!currentEntry.isDirectory() && StaticWorld.typeIsImage(currentEntry.getFileNameString())) {
                    new ExtractFileFromRarToOs(archive, currentEntry, pos).start();
                    try {
                        previewComponent.setNewImageStream(pis);
                    } catch (IOException ex) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getFileNameString());
                        previewComponent.setNoPreviewImage();
                    } catch (NullPointerException ex) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getFileNameString());
                        previewComponent.setNoPreviewImage();
                    }
                    break;
                }
            }
        }
        try {
            if (archive != null)
                archive.close();
        } catch (IOException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
        }
        waitingCursor(false);
    }

    

    /**
     * Affichage dans le previewComponent d'une firstImage simple provenant d'un pdf
     */
    @Action
    private void previewPdfFile() {
        RandomAccessFile raf = null;
        FileChannel channel = null;
        try {
            raf = new RandomAccessFile(album, "r");
            channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            PDFFile pdfFile = new PDFFile(buf);
            PDFPage page = pdfFile.getPage(0);
            Rectangle rect = new Rectangle(page.getBBox().getBounds());
            previewComponent.setImage(page.getImage(rect.width * 2, rect.height * 2, //width & height
                page.getBBox(), // clip rect
                null, // null for the ImageObserver
                true, // fill background with white
                true  // block until drawing is done
                ));
        } catch (IOException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Pdf", album.toString());
            previewComponent.setNoPreviewImage();
        } catch (IllegalArgumentException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Pdf", album.toString());
            previewComponent.setNoPreviewImage();
        }
        try {
            channel.close();
            raf.close();
            waitingCursor(false);
        } catch (IOException ex) {
            new KnownErrorBox(getFrame(), KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
        }
    }

    /**
     * Modification du dossier contenant les bouquins
     */
    @Action
    public void changeFolder() {
        folderChooser.setCurrentDirectory((directory == null) ? null : directory.getParentFile());
        if (folderChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            directory = folderChooser.getSelectedFile();
            profiles[currentProfile][1] = directory.toString();
            listFilesInBaseDir();
        }
    }

    private Thread listFilesInBaseDir() {
        waitingCursor(true);
        previewComponent.setNoPreviewImage();
        Thread toExecute = new Thread(new Runnable() {

            @Override
            public void run() {
                File[] allfiles = null;

                try {
                    allfiles = directory.listFiles();
                } catch(SecurityException ex) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rights", directory.toString());
                }
                if (allfiles == null) {
                    return;
                }
                Arrays.sort(allfiles);
                final File[] allFilesValue = allfiles;
                final DefaultTableModel seriesTableModel = (DefaultTableModel) serieTable.getModel();
                final DefaultTableModel albumsTableModel = (DefaultTableModel) albumTable.getModel();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            seriesTableModel.setRowCount(0);
                            albumsTableModel.setRowCount(0);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                final List<Thread> rows = new ArrayList<Thread>();
                for (int i = 0; allFilesValue.length > i; i++) {
                    if (allFilesValue[i].isDirectory() && !allFilesValue[i].isHidden()) {
                        final int nbrOfAlbums = new File(allFilesValue[i].getPath()).listFiles(new FileFilter() {

                            @Override
                            public boolean accept(File pathname) {
                                String name = pathname.getName();
                                if (!pathname.isHidden() && (pathname.isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf"))) {
                                    return true;
                                }
                                return false;
                            }
                        }).length;
                        final int index = i;
                        Thread tampon = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                seriesTableModel.addRow(new Object[]{allFilesValue[index].getName(), nbrOfAlbums});
                            }
                        });
                        SwingUtilities.invokeLater(tampon);
                        rows.add(tampon);
                    }
                }
                for (Thread thread : rows) {
                    try {
                        thread.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                waitingCursor(false);
            }
        });
        if (SwingUtilities.isEventDispatchThread())
            toExecute.start();
        else
            toExecute.run();
        return toExecute;
    }

    /**
     * Reprise de la lecture
     */
    @Action
    public void resumeReading() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (lastReadedProfile == null || lastReadedSerie == null || lastReadedAlbum == null) {
                    new KnownErrorBox(getFrame(), KnownErrorBox.INFO_LOGO, "Info_No_previously_readed_Comics");
                    return;
                }
                if (!directory.toString().equals(profiles[currentProfile][1])) {
                    String profileDir = null;
                    for (int i = 0; i < profiles.length; i++) {
                        if (profiles[i][0].equals(lastReadedProfile)) {
                            profileDir = profiles[i][1];
                            currentProfile = (short) i;
                        }
                    }
                    if (profileDir == null) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.INFO_LOGO, "Info_No_previously_readed_Comics");
                        return;
                    }
                    directory = new File(profileDir);
                    try {
                        listFilesInBaseDir().join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                serie = new File(directory.toString() + File.separator + lastReadedSerie);
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            int rows = serieTable.getModel().getRowCount();
                            boolean foundSearchedLine = false;
                            for (int i = 0; i < rows; i++) {
                                String rowValue = (String) serieTable.getModel().getValueAt(i, 0);
                                if (rowValue.equals(lastReadedSerie)) {
                                    serieTable.changeSelection(i, 0, false, false);
                                    foundSearchedLine = true;
                                }
                            }
                            if (!foundSearchedLine) {
                                new KnownErrorBox(getFrame(), KnownErrorBox.INFO_LOGO, "Info_No_previously_readed_Comics");
                                return;
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (isAdjusting) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                album = new File(serie.toString() + File.separator + lastReadedAlbum);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        boolean foundSearchedLine = false;
                        int rows = albumTable.getModel().getRowCount();
                        for (int i = 0; i < rows; i++) {
                            String rowValue = (String) albumTable.getModel().getValueAt(i, 0) + albumTable.getModel().getValueAt(i, 1);
                            if (rowValue.equals(lastReadedAlbum)) {
                                albumTable.changeSelection(i, 0, false, false);
                                foundSearchedLine = true;
                            }
                        }
                        if (!foundSearchedLine) {
                            new KnownErrorBox(getFrame(), KnownErrorBox.INFO_LOGO, "Info_No_previously_readed_Comics");
                            return;
                        }
                        if (readView != null) {
                            readView.dispose();
                        }
                        readView = new BookNavigerReadView(album, getThis());
                        readView.initialize(lastReadedPage);
                    }
                });
            }
        }).start();
    }

    /**
     * Retourne une instance de cette classe
     * @return instance de BookNavigerView
     */
    public BookNavigerView getThis() {
        return this;
    }

    /**
     * Génère un rapport html
     */
    @Action
    public void generateHTMLReport() {
        ModeSelector ms = new ModeSelector(this.getFrame(), true);
        ms.setVisible(true);
        if (ms.isOkPressed()) {
            new GenerateReport(ms.getModeSelected(), directory, this);
        }
    }

    /**
     * Retourne BookNavigerReadView
     * @return instance de bnrv
     */
    public BookNavigerReadView getReadView() {
        return readView;
    }

    class CheckForUpdates extends Thread {

        String checkedVer = null;
        boolean force = false;

        public CheckForUpdates() {
            this(false);
        }

        public CheckForUpdates(boolean force) {
            lastUpdateCheck = null;
            this.force = force;
        }

        public CheckForUpdates(String lastCheckedDateString) {
            if (lastCheckedDateString == null)
                return;
            try {
                lastUpdateCheck = DateFormat.getDateInstance().parse(lastCheckedDateString);
            } catch (ParseException ex) {
                lastUpdateCheck = null;
            }
        }

        private boolean testNeedRecheck() {
            if (lastUpdateCheck == null)
                return true;
            Calendar cal = new GregorianCalendar();
            cal.setTime(lastUpdateCheck);
            cal.add(Calendar.DATE, 7);
            if (new Date().after(cal.getTime()))
                return true;
            return false;
        }

        private boolean recheck() throws IOException {
            try {
                URL checkedVerURL = new URL("http://software.inervo.fr/logiciels/BookNaviger_version.txt");
                URLConnection urlConn = checkedVerURL.openConnection();
                urlConn.setDoInput(true);
                urlConn.setUseCaches(false);
                BufferedReader bis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                checkedVer = bis.readLine();
                bis.close();
                return StaticWorld.compareVersions2GT1(getResourceMap().getString("Application.version"), checkedVer);
            } catch (MalformedURLException ex) {
                Logger.getLogger(BookNavigerView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        @Override
        public void run() {
            if (testNeedRecheck()) {
                try {
                    if (recheck()) {
                        new UpdateAvailBox(getFrame(), checkedVer).setVisible(true);
                    } else if (force) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.INFO_LOGO, "Info_No_Updates");
                    }
                    lastUpdateCheck = new Date();
                } catch (IOException ex) {
                    if (force) {
                        new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Unable_Verify_Updates");
                    }
                }
            }
        }
    }

    @Action
    public void checkUpdatesStatusChange() {
        if (checkUpdatesCheckBoxMenuItem.isSelected())
            new CheckForUpdates(true).start();
    }

    @Action
    public void refreshSeries() {
        listFilesInBaseDir();
    }

    @Action
    public void refreshAlbums() {
        final int selectedRow = serieTable.getSelectedRow();
        if (selectedRow != -1)
            listAlbums(selectedRow);
    }

    @Action
    public void showProfileDetail() {
        ProfileDialog pd = new ProfileDialog(this, true);
        pd.setDataToTable(profiles);
        pd.setVisible(true);
    }

    /**
     * Applique un nouveau profil selon la valeur de currentProfile.
     * Liste les séries dans ce profil et selectionne le profil dans le menu
     */
    protected void applyProfile() {
        final DefaultTableModel seriesTableModel = (DefaultTableModel) serieTable.getModel();
        final DefaultTableModel albumsTableModel = (DefaultTableModel) albumTable.getModel();
        seriesTableModel.setRowCount(0);
        albumsTableModel.setRowCount(0);
        profilesListRadioButtonMenuItem.get(currentProfile).setSelected(true);
        profilesComboBox.setSelectedIndex(currentProfile);
        if (profiles[currentProfile][1].equals("")) {
            previewComponent.setNoPreviewImage();
            directory = null;
            return;
        }
        directory = new File(profiles[currentProfile][1]);
        listFilesInBaseDir();
    }

    @Action
    public void newProfileRadioButtonMenuItemSelected() {
        for (int i = 0; i < profilesListRadioButtonMenuItem.size(); i++) {
            if (profilesListRadioButtonMenuItem.get(i).isSelected()) {
                if (i == currentProfile)
                    return;
                currentProfile = (short) i;
                applyProfile();
            }
        }
    }

    /**
     * Crée un nouveau radiobuttonmenuitem pour un nouveau profil
     * @param text le titre du profil à mettre sur le button
     * @return le button créé
     */
    protected JRadioButtonMenuItem createProfileRadioButtonMenuItem(String text) {
        ActionMap actionMap = Application.getInstance(BookNavigerApp.class).getContext().getActionMap(BookNavigerView.class, this);
        JRadioButtonMenuItem profileRadioButtonMenuItem = new JRadioButtonMenuItem();
        profileRadioButtonMenuItem.setAction(actionMap.get("newProfileRadioButtonMenuItemSelected"));
        profileRadioButtonMenuItem.setText(text);
        profileRadioButtonMenuItem.setName(text.concat("ProfileRadioButtonMenuItem"));
        profileButtonGroup.add(profileRadioButtonMenuItem);
        profileMenu.add(profileRadioButtonMenuItem);
        profilesListRadioButtonMenuItem.add(profileRadioButtonMenuItem);
        return profileRadioButtonMenuItem;
    }

    protected void createProfileComboBox() {
        final DefaultComboBoxModel dcbm = (DefaultComboBoxModel) profilesComboBox.getModel();
        dcbm.removeAllElements();
        for (String[] profile : profiles) {
            dcbm.addElement(profile[0]);
        }
    }

    @Action
    public void newProfileComboBoxSelected() {
        short newProfile = (short) profilesComboBox.getSelectedIndex();
        if (newProfile == currentProfile || newProfile == -1 || profilesComboBox.getItemCount() != profiles.length)
            return;
        currentProfile = (short) newProfile;
        applyProfile();
    }

    @Action
    public void changeLanguage() {
        String selected = languageButtonGroup.getSelection().getActionCommand();
        if (selected.equals("Default"))
            BookNavigerApp.language = null;
        if (selected.equals("English"))
            BookNavigerApp.language = "en_US";
        if (selected.equals("French"))
            BookNavigerApp.language = "fr_FR";
        new KnownErrorBox(getFrame(), KnownErrorBox.INFO_LOGO, "Info_Change_Language_Restart");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane albumScrollPane;
    private javax.swing.JTable albumTable;
    protected javax.swing.JCheckBoxMenuItem checkUpdatesCheckBoxMenuItem;
    private javax.swing.JFileChooser folderChooser;
    private javax.swing.JMenuItem folderMenuItem;
    private javax.swing.JMenuItem generateReportMenuItem;
    private javax.swing.ButtonGroup languageButtonGroup;
    private javax.swing.JRadioButtonMenuItem languageDefaultRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem languageEnglishRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem languageFrenchRadioButtonMenuItem;
    private javax.swing.JMenu languageMenu;
    private javax.swing.JPopupMenu.Separator languageSeparator;
    private javax.swing.JMenuItem listProfileMenuItem;
    private javax.swing.JSplitPane listSplitPane;
    private javax.swing.JMenu listingMenu;
    private javax.swing.JPopupMenu.Separator listingSeparator1;
    private javax.swing.JPanel mainPanel;
    protected javax.swing.JToolBar mainToolBar;
    private javax.swing.JMenuBar menuBar;
    private booknaviger.PreviewComponent previewComponent;
    javax.swing.ButtonGroup profileButtonGroup;
    javax.swing.JMenu profileMenu;
    private javax.swing.JPopupMenu.Separator profileSeparator1;
    javax.swing.JComboBox profilesComboBox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton refreshAlbumsButton;
    private javax.swing.JMenuItem refreshAlbumsMenuItem;
    private javax.swing.JButton refreshSeriesButton;
    private javax.swing.JMenuItem refreshSeriesMenuItem;
    private javax.swing.JButton resumeReadingButton;
    private javax.swing.JMenuItem resumeReadingMenuItem;
    private javax.swing.JScrollPane serieScrollPane;
    private javax.swing.JTable serieTable;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar.Separator toolbarSeparator1;
    private javax.swing.JToolBar.Separator toolbarSeparator2;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private BookNavigerReadView readView;
}
