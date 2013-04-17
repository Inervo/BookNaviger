/*
 */
package booknaviger;

import booknaviger.booksfolder.BooksFolderAnalyser;
import booknaviger.booksfolder.BooksFolderSelector;
import booknaviger.exceptioninterface.ExceptionHandler;
import booknaviger.exceptioninterface.InfoInterface;
import booknaviger.exceptioninterface.LogInterface;
import booknaviger.inet.htmlreport.ReportModeSelector;
import booknaviger.inet.updater.NewUpdateAvailableDialog;
import booknaviger.inet.updater.Updater;
import booknaviger.macworld.MacOSXApplicationAdapter;
import booknaviger.osbasics.OSBasics;
import booknaviger.picturehandler.AbstractImageHandler;
import booknaviger.picturehandler.FolderHandler;
import booknaviger.picturehandler.PdfHandler;
import booknaviger.picturehandler.RarHandler;
import booknaviger.picturehandler.ZipHandler;
import booknaviger.profiles.ProfileDialog;
import booknaviger.profiles.Profiles;
import booknaviger.properties.PropertiesManager;
import booknaviger.readinterface.ReadInterface;
import booknaviger.searcher.TableSearcher;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Inervo
 */
public final class MainInterface extends javax.swing.JFrame {
    
    private Profiles profiles = new Profiles();
    private List<JRadioButtonMenuItem> profilesListRadioButtonMenuItem = new ArrayList<>();
    private File booksDirectory = null;
    private Timer busyIconTimer;
    private int busyIconIndex = 0;
    private Icon idleIcon;
    private Icon[] busyIcons = new Icon[15];
    private File serie = null;
    private File album = null;
    private PreviewImageLoader threadedPreviewLoader = new PreviewImageLoader();
    private AbstractImageHandler imageHandler = null;
    private ResourceBundle resourceBundle = java.util.ResourceBundle.getBundle("booknaviger/resources/MainInterface");
    private volatile ReadInterface readInterface = null;
    private Thread actionThread = null;
    private Preferences preferences = Preferences.userNodeForPackage(MainInterface.class);
    private boolean firstLaunch = true;
    
    /**
     * The holder of the unique MainInterface instance
     */
    private static class MainInterfaceHolder {

        private static MainInterface INSTANCE = new MainInterface();
    }
    
    /**
     * Get the unique instance of the MaintInterface
     * @return the unique instance
     */
    public static MainInterface getInstance() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "getInstance");
        synchronized(MainInterfaceHolder.class) {
            if (MainInterfaceHolder.INSTANCE == null) {
                MainInterfaceHolder.INSTANCE = new MainInterface();
            }
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "getInstance");
            return MainInterfaceHolder.INSTANCE;
        }
    }
    
    /**
     * Forget the unique instance previously set of MainInterface
     */
    public static void reinitializeMainInterface() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "reinitializeMainInterface");
        synchronized(MainInterfaceHolder.class) {
            MainInterfaceHolder.INSTANCE = null;
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "reinitializeMainInterface");
    }
    
    /**
     * Creates new form MainInterface
     */
    private MainInterface() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "MainInterface");
        macInit();
        initComponents();
        setTimer();
        refreshProfilesList();
        previewComponent.setStatusToolBarHeigh(statusToolBar.getHeight() + mainToolBar.getHeight());
        if (shoudCheckNewVersion()) {
            checkForNewVersion();
        }
        initializeLanguageMenuSelection();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "MainInterface");
    }
    
    /**
     * initialize Mac OS X settings if OS is Mac
     */
    private void macInit() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "macInit");
        if (OSBasics.isMac()) {
            new MacOSXApplicationAdapter(this);
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "macInit");
    }
    
    /**
     * Set the timer component animation
     */
    private void setTimer() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "setTimer");
        int busyAnimationRate = 30;
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = new javax.swing.ImageIcon(getClass().getResource(java.text.MessageFormat.format(resourceBundle.getString("busy_icon_{0}"), new Object[] {i})));
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("idle_icon")));
        statusAnimationLabel.setIcon(idleIcon);
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "setTimer");
    }
    
    /**
     * Verify if the software must check for a new version
     * @return true if a check must be done<br />false otherwise
     */
    private boolean shoudCheckNewVersion() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "shoudCheckNewVersion");
        Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Checking if the software should check for a new version");
        String autoCheckUpdates = PropertiesManager.getInstance().getKey("autoCheckUpdates");
        if (autoCheckUpdates != null && autoCheckUpdates.equals("false")) {
            autoUpdatesCheckBoxMenuItem.setSelected(false);
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "shoudCheckNewVersion", false);
            return false;
        }
        String lastUpdateCheckString = PropertiesManager.getInstance().getKey("lastUpdateCheck");
        if (lastUpdateCheckString == null) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "No previous check date found. Should check");
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "shoudCheckNewVersion", true);
            return true;
        }
        Date lastUpdateCheckDate;
        try {
            lastUpdateCheckDate = DateFormat.getDateInstance().parse(lastUpdateCheckString);
        } catch (ParseException ex) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.WARNING, "Couldn't parse the previous update check date", ex);
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "shoudCheckNewVersion", true);
            return true;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(lastUpdateCheckDate);
        cal.add(Calendar.DATE, 7);
        if (new Date().after(cal.getTime())) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Should check for a new version of the software");
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "shoudCheckNewVersion", true);
            return true;
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "shoudCheckNewVersion", false);
        return false;
    }
    
    /**
     * Check that a new version of this software is available
     */
    private void checkForNewVersion() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "checkForNewVersion");
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Updater updater = new Updater();
                    if (updater.isNewVersionAvailable()) {
                        Logger.getLogger(MainInterface.class.getName()).log(Level.FINE, "A new version is available. Showing info interface");
                        new NewUpdateAvailableDialog(MainInterface.getInstance(), updater.getVersionNumber(), updater.getDownloadURLString()).setVisible(true);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                    new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
                }
                Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "checkForNewVersion");
            }
        }).start();
    }
    
    /**
     * Initialize the language menu with the desired language found in the properties
     */
    private void initializeLanguageMenuSelection() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "initializeLanguageMenuSelection");
        String languageWanted = PropertiesManager.getInstance().getKey("language");
        if (languageWanted != null) {
            switch (languageWanted) {
                case "fr":
                    frenchLanguageCheckBoxMenuItem.setSelected(true);
                    Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Setting language menu selection to French");
                    break;
                case "en":
                    englishLanguageCheckBoxMenuItem.setSelected(true);
                    Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Setting language menu selection to English");
                    break;
            }
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "initializeLanguageMenuSelection");
    }
    
    /**
     * Read the dimension of {@link MainInterface} in the {@link #preferences}
     * @return The dimension to set for MainInterface
     */
    private Dimension getMainInterfaceWantedDimension() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "getMainInterfaceWantedDimension");
        Dimension mainInterfaceDimension = new Dimension(preferences.getInt("width", 700), preferences.getInt("height", 500));
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "getMainInterfaceWantedDimension", mainInterfaceDimension);
        return mainInterfaceDimension;
    }
    
    /**
     * Read the dimension of {@link MainInterface} in the {@link #preferences}
     * @return The dimension to set for MainInterface
     */
    private Point getMainInterfaceWantedLocation() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "getMainInterfaceWantedLocation");
        Point mainInterfaceLocation = new Point(preferences.getInt("X-location", 0), preferences.getInt("Y-location", 0));
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "getMainInterfaceWantedLocation", mainInterfaceLocation);
        return mainInterfaceLocation;
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languageButtonGroup = new javax.swing.ButtonGroup();
        profileButtonGroup = new javax.swing.ButtonGroup();
        aboutDialog = new javax.swing.JDialog(this);
        closeAboutDialogButton = new javax.swing.JButton();
        imageLabel = new javax.swing.JLabel();
        appTitleLabel = new javax.swing.JLabel();
        appDescLabel = new javax.swing.JLabel();
        productVersionLabel = new javax.swing.JLabel();
        vendorLabel = new javax.swing.JLabel();
        homepageLabel = new javax.swing.JLabel();
        appVersionLabel = new javax.swing.JLabel();
        appVendorLabel = new javax.swing.JLabel();
        appHomepageLabel = new javax.swing.JLabel();
        mainToolBar = new javax.swing.JToolBar();
        resumeButton = new javax.swing.JButton();
        toolbarSeparator1 = new javax.swing.JToolBar.Separator();
        refreshAllButton = new javax.swing.JButton();
        refreshCurrentButton = new javax.swing.JButton();
        toolbarSeparator2 = new javax.swing.JToolBar.Separator();
        profileComboBox = new javax.swing.JComboBox<>();
        booksPreviewSplitPane = new javax.swing.JSplitPane();
        seriesScrollPane = new javax.swing.JScrollPane();
        seriesTable = new javax.swing.JTable();
        albumsScrollPane = new javax.swing.JScrollPane();
        albumsTable = new javax.swing.JTable();
        previewComponent = new booknaviger.PreviewComponent();
        statusToolBar = new javax.swing.JToolBar();
        statusToolBarFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        statusAnimationLabel = new javax.swing.JLabel();
        toolBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        generateReportMenuItem = new javax.swing.JMenuItem();
        bookFolderMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        optionMenu = new javax.swing.JMenu();
        resumeMenuItem = new javax.swing.JMenuItem();
        profileMenu = new javax.swing.JMenu();
        profilesMenuItem = new javax.swing.JMenuItem();
        profileSeparator = new javax.swing.JPopupMenu.Separator();
        optionsSeparator = new javax.swing.JPopupMenu.Separator();
        refreshAllMenuItem = new javax.swing.JMenuItem();
        refreshCurrentMenuItem = new javax.swing.JMenuItem();
        languageMenu = new javax.swing.JMenu();
        defaultLanguageCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        languageSeparator = new javax.swing.JPopupMenu.Separator();
        englishLanguageCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        frenchLanguageCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        autoUpdatesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        aboutDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        aboutDialog.setLocationRelativeTo(null);

        closeAboutDialogButton.setText("Close");
        closeAboutDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAboutBox(evt);
            }
        });

        imageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/about.png"))); // NOI18N

        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize()+4));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("booknaviger/resources/Application"); // NOI18N
        appTitleLabel.setText(bundle.getString("appTitle")); // NOI18N

        appDescLabel.setText(bundle.getString("appDesc")); // NOI18N

        productVersionLabel.setFont(productVersionLabel.getFont().deriveFont(productVersionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        productVersionLabel.setText(bundle.getString("productVersion")); // NOI18N

        vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));
        vendorLabel.setText(bundle.getString("vendor")); // NOI18N

        homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | java.awt.Font.BOLD));
        homepageLabel.setText(bundle.getString("homepage")); // NOI18N

        appVersionLabel.setText(bundle.getString("appVersion")); // NOI18N

        appVendorLabel.setText(bundle.getString("appVendor")); // NOI18N

        appHomepageLabel.setText(bundle.getString("appHomepage")); // NOI18N
        appHomepageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homepageLabelMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout aboutDialogLayout = new org.jdesktop.layout.GroupLayout(aboutDialog.getContentPane());
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);
        aboutDialogLayout.setHorizontalGroup(
            aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(aboutDialogLayout.createSequentialGroup()
                .add(imageLabel)
                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(aboutDialogLayout.createSequentialGroup()
                        .add(38, 38, 38)
                        .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(appTitleLabel)
                            .add(appDescLabel)
                            .add(aboutDialogLayout.createSequentialGroup()
                                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(productVersionLabel)
                                    .add(vendorLabel)
                                    .add(homepageLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(appHomepageLabel)
                                    .add(appVersionLabel)
                                    .add(appVendorLabel)))))
                    .add(aboutDialogLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(closeAboutDialogButton)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        aboutDialogLayout.setVerticalGroup(
            aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(aboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(appTitleLabel)
                .add(18, 18, 18)
                .add(appDescLabel)
                .add(18, 18, 18)
                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(aboutDialogLayout.createSequentialGroup()
                        .add(appVersionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(vendorLabel)
                            .add(appVendorLabel)))
                    .add(productVersionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(appHomepageLabel)
                    .add(homepageLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(closeAboutDialogButton)
                .addContainerGap())
            .add(imageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BookNaviger"); // NOI18N
        setLocation(getMainInterfaceWantedLocation());
        setPreferredSize(getMainInterfaceWantedDimension());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);
        mainToolBar.setPreferredSize(new java.awt.Dimension(166, 25));

        resumeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/resume.png"))); // NOI18N
        resumeButton.setFocusable(false);
        resumeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resumeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        resumeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        resumeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resumeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(resumeButton);
        mainToolBar.add(toolbarSeparator1);

        refreshAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/refreshSeries.png"))); // NOI18N
        refreshAllButton.setFocusable(false);
        refreshAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshAllButton.setMaximumSize(new java.awt.Dimension(20, 20));
        refreshAllButton.setMinimumSize(new java.awt.Dimension(20, 20));
        refreshAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllActionPerformed(evt);
            }
        });
        mainToolBar.add(refreshAllButton);

        refreshCurrentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/refreshAlbums.png"))); // NOI18N
        refreshCurrentButton.setFocusable(false);
        refreshCurrentButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshCurrentButton.setMaximumSize(new java.awt.Dimension(20, 20));
        refreshCurrentButton.setMinimumSize(new java.awt.Dimension(20, 20));
        refreshCurrentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshCurrentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshCurrentAlbumActionPerformed(evt);
            }
        });
        mainToolBar.add(refreshCurrentButton);
        mainToolBar.add(toolbarSeparator2);

        profileComboBox.setFocusable(false);
        profileComboBox.setMaximumSize(new java.awt.Dimension(200, 20));
        profileComboBox.setMinimumSize(new java.awt.Dimension(96, 20));
        profileComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileComboBoxActionPerformed(evt);
            }
        });
        mainToolBar.add(profileComboBox);

        getContentPane().add(mainToolBar, java.awt.BorderLayout.PAGE_START);

        booksPreviewSplitPane.setDividerLocation(preferences.getInt("divider-location", 280));
        booksPreviewSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        booksPreviewSplitPane.setMinimumSize(new java.awt.Dimension(400, 400));
        booksPreviewSplitPane.setPreferredSize(new java.awt.Dimension(458, 400));

        seriesScrollPane.setMinimumSize(new java.awt.Dimension(400, 200));

        seriesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        seriesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        seriesTable.setShowHorizontalLines(false);
        seriesTable.setShowVerticalLines(false);
        seriesTable.getTableHeader().setReorderingAllowed(false);
        seriesTable.getColumnModel().getColumn(0).setHeaderValue(resourceBundle.getString("seriesTable.title"));
        seriesTable.getColumnModel().getColumn(1).setMaxWidth(50);
        seriesTable.getColumnModel().getColumn(1).setMinWidth(25);
        seriesTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                seriesTableKeyPressed(evt);
            }
        });
        seriesScrollPane.setViewportView(seriesTable);
        ListSelectionModel seriesListSelectionModel = seriesTable.getSelectionModel();
        seriesListSelectionModel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                seriesTableValueChanged(evt);
            }
        });
        new TableSearcher(seriesTable, null).activateQuickSearch();

        booksPreviewSplitPane.setTopComponent(seriesScrollPane);

        albumsScrollPane.setMinimumSize(new java.awt.Dimension(400, 100));

        albumsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Album", "Extension"
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
        albumsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        albumsTable.setShowHorizontalLines(false);
        albumsTable.setShowVerticalLines(false);
        albumsTable.getTableHeader().setReorderingAllowed(false);
        albumsTable.getColumnModel().getColumn(1).setMinWidth(0);
        albumsTable.getColumnModel().getColumn(1).setMaxWidth(0);
        albumsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                albumsTableMouseClicked(evt);
            }
        });
        albumsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                albumsTableKeyPressed(evt);
            }
        });
        albumsScrollPane.setViewportView(albumsTable);
        ListSelectionModel albumsListSelectionModel = albumsTable.getSelectionModel();
        albumsListSelectionModel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                albumsTableValueChanged(evt);
            }
        });
        new TableSearcher(albumsTable, null).activateQuickSearch();

        booksPreviewSplitPane.setRightComponent(albumsScrollPane);

        getContentPane().add(booksPreviewSplitPane, java.awt.BorderLayout.CENTER);

        previewComponent.setDoubleBuffered(true);
        previewComponent.setMinimumSize(new java.awt.Dimension(100, 400));
        previewComponent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                previewComponentMouseClicked(evt);
            }
        });
        previewComponent.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                previewComponentComponentResized(evt);
            }
        });
        previewComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        org.jdesktop.layout.GroupLayout previewComponentLayout = new org.jdesktop.layout.GroupLayout(previewComponent);
        previewComponent.setLayout(previewComponentLayout);
        previewComponentLayout.setHorizontalGroup(
            previewComponentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 266, Short.MAX_VALUE)
        );
        previewComponentLayout.setVerticalGroup(
            previewComponentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );

        getContentPane().add(previewComponent, java.awt.BorderLayout.EAST);

        statusToolBar.setFloatable(false);
        statusToolBar.setRollover(true);
        statusToolBar.setPreferredSize(new java.awt.Dimension(166, 25));
        statusToolBar.add(statusToolBarFiller);

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusToolBar.add(statusAnimationLabel);

        getContentPane().add(statusToolBar, java.awt.BorderLayout.PAGE_END);

        fileMenu.setMnemonic('f');
        fileMenu.setText(resourceBundle.getString("file_menu")); // NOI18N

        generateReportMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/report.png"))); // NOI18N
        generateReportMenuItem.setText(resourceBundle.getString("report-generate_menu")); // NOI18N
        generateReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateReportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(generateReportMenuItem);

        bookFolderMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        bookFolderMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/library.png"))); // NOI18N
        bookFolderMenuItem.setText(resourceBundle.getString("books-folder_menu")); // NOI18N
        bookFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookFolderMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(bookFolderMenuItem);

        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/quit.png"))); // NOI18N
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(resourceBundle.getString("exit_menu")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        toolBar.add(fileMenu);

        optionMenu.setText(resourceBundle.getString("controls_menu")); // NOI18N

        resumeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        resumeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/resume.png"))); // NOI18N
        resumeMenuItem.setText(resourceBundle.getString("resume_menu")); // NOI18N
        resumeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeMenuItemActionPerformed(evt);
            }
        });
        optionMenu.add(resumeMenuItem);

        profileMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/profile.png"))); // NOI18N
        profileMenu.setText(resourceBundle.getString("profile-list_menu")); // NOI18N

        profilesMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/profileBox.png"))); // NOI18N
        profilesMenuItem.setText(resourceBundle.getString("profiles_menu")); // NOI18N
        profilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profilesMenuItemActionPerformed(evt);
            }
        });
        profileMenu.add(profilesMenuItem);
        profileMenu.add(profileSeparator);

        optionMenu.add(profileMenu);
        optionMenu.add(optionsSeparator);

        refreshAllMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/refreshSeries.png"))); // NOI18N
        refreshAllMenuItem.setText(resourceBundle.getString("refresh-all-series_menu")); // NOI18N
        refreshAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAllActionPerformed(evt);
            }
        });
        optionMenu.add(refreshAllMenuItem);

        refreshCurrentMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/refreshAlbums.png"))); // NOI18N
        refreshCurrentMenuItem.setText(resourceBundle.getString("refresh-current-album_menu")); // NOI18N
        refreshCurrentMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshCurrentAlbumActionPerformed(evt);
            }
        });
        optionMenu.add(refreshCurrentMenuItem);

        toolBar.add(optionMenu);

        languageMenu.setText(resourceBundle.getString("language_menu")); // NOI18N

        languageButtonGroup.add(defaultLanguageCheckBoxMenuItem);
        defaultLanguageCheckBoxMenuItem.setSelected(true);
        defaultLanguageCheckBoxMenuItem.setText(resourceBundle.getString("default-language_menu")); // NOI18N
        defaultLanguageCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/defaultLanguage.png"))); // NOI18N
        defaultLanguageCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultLanguageCheckBoxMenuItemActionPerformed(evt);
            }
        });
        languageMenu.add(defaultLanguageCheckBoxMenuItem);
        languageMenu.add(languageSeparator);

        languageButtonGroup.add(englishLanguageCheckBoxMenuItem);
        englishLanguageCheckBoxMenuItem.setText(resourceBundle.getString("english-language_menu")); // NOI18N
        englishLanguageCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/en_US.png"))); // NOI18N
        englishLanguageCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                englishLanguageCheckBoxMenuItemActionPerformed(evt);
            }
        });
        languageMenu.add(englishLanguageCheckBoxMenuItem);

        languageButtonGroup.add(frenchLanguageCheckBoxMenuItem);
        frenchLanguageCheckBoxMenuItem.setText(resourceBundle.getString("french-language_menu")); // NOI18N
        frenchLanguageCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/fr_FR.png"))); // NOI18N
        frenchLanguageCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frenchLanguageCheckBoxMenuItemActionPerformed(evt);
            }
        });
        languageMenu.add(frenchLanguageCheckBoxMenuItem);

        toolBar.add(languageMenu);

        helpMenu.setText(resourceBundle.getString("help_menu")); // NOI18N

        autoUpdatesCheckBoxMenuItem.setSelected(true);
        autoUpdatesCheckBoxMenuItem.setText(resourceBundle.getString("auto-update_menu")); // NOI18N
        autoUpdatesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoUpdatesCheckBoxMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(autoUpdatesCheckBoxMenuItem);

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/aboutIcon.png"))); // NOI18N
        aboutMenuItem.setText(resourceBundle.getString("about_menu")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openAboutDialogActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        toolBar.add(helpMenu);

        setJMenuBar(toolBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Menu item exit has been triggered
     * @param evt the event associated
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "exitMenuItemActionPerformed");
        exit();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "exitMenuItemActionPerformed");
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * Menu item book folder has been triggered
     * @param evt the event associated
     */
    private void bookFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookFolderMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "bookFolderMenuItemActionPerformed");
        BooksFolderSelector booksFolderselector = new BooksFolderSelector(this, true);
        String selectedFolder = booksFolderselector.selectFolder();
        if (selectedFolder != null) {
            getProfiles().setCurrentProfileFolder(selectedFolder);
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "bookFolderMenuItemActionPerformed");
    }//GEN-LAST:event_bookFolderMenuItemActionPerformed

    /**
     * Menu item refresh all has been triggered
     * @param evt the event associated
     */
    private void refreshAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "refreshAllActionPerformed");
        listSeries();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "refreshAllActionPerformed");
    }//GEN-LAST:event_refreshAllActionPerformed

    /**
     * Menu item refresh current serie has been triggered
     * @param evt the event associated
     */
    private void refreshCurrentAlbumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshCurrentAlbumActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "refreshCurrentAlbumActionPerformed");
        int selectedRow = seriesTable.getSelectedRow();
        if (selectedRow != -1) {
            listAlbums(selectedRow);
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "refreshCurrentAlbumActionPerformed");
    }//GEN-LAST:event_refreshCurrentAlbumActionPerformed

    /**
     * Close the about box has been triggered
     * @param evt the event associated
     */
    private void closeAboutBox(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAboutBox
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "closeAboutBox");
        aboutDialog.setVisible(false);
        aboutDialog.dispose();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "closeAboutBox");
    }//GEN-LAST:event_closeAboutBox

    /**
     * Open the about box has been triggered
     * @param evt the event associated
     */
    private void openAboutDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openAboutDialogActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "openAboutDialogActionPerformed");
        openAboutDialog();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "openAboutDialogActionPerformed");
    }//GEN-LAST:event_openAboutDialogActionPerformed

    /**
     * Open the about box
     */
    public void openAboutDialog() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "openAboutDialog");
        aboutDialog.pack();
        aboutDialog.setVisible(true);
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "openAboutDialog");
    }
    
    /**
     * Click on the homepage link has been triggered
     * @param evt the event associated
     */
    private void homepageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homepageLabelMouseClicked
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "homepageLabelMouseClicked");
        OSBasics.openURI(appHomepageLabel.getText());
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "homepageLabelMouseClicked");
    }//GEN-LAST:event_homepageLabelMouseClicked

    /**
     * Click on the album table has been triggered
     * @param evt the event associated
     */
    private void albumsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_albumsTableMouseClicked
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "albumsTableMouseClicked");
        if (evt.getClickCount() == 2 && album != null) {
            startReading();
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "albumsTableMouseClicked");
    }//GEN-LAST:event_albumsTableMouseClicked

    /**
     * The component is rezised
     * @param evt the event associated
     */
    private void previewComponentComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_previewComponentComponentResized
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "previewComponentComponentResized");
        previewComponent.refresh();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "previewComponentComponentResized");
    }//GEN-LAST:event_previewComponentComponentResized

    /**
     * Click on the preview component has been triggered
     * @param evt the event associated
     */
    private void previewComponentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_previewComponentMouseClicked
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "previewComponentMouseClicked");
        if (album != null) {
            startReading();
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "previewComponentMouseClicked");
    }//GEN-LAST:event_previewComponentMouseClicked

    /**
     * A key has been pressed while the focus was on the serietable
     * @param evt the event associated
     */
    private void seriesTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_seriesTableKeyPressed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "seriesTableKeyPressed");
        if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            evt.consume();
            albumsTable.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            startReading();
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "seriesTableKeyPressed");
    }//GEN-LAST:event_seriesTableKeyPressed

    /**
     * A key has been pressed while the focus was on the albumtable
     * @param evt the event associated
     */
    private void albumsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumsTableKeyPressed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "albumsTableKeyPressed");
        if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            evt.consume();
            seriesTable.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            startReading();
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "albumsTableKeyPressed");
    }//GEN-LAST:event_albumsTableKeyPressed

    /**
     * The profile combo box has triggered an action
     * @param evt the event associated
     */
    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileComboBoxActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "profileComboBoxActionPerformed");
        String profileName = profileComboBox.getSelectedItem().toString();
        if (!profileName.equals(profiles.getCurrentProfileName())) {
            profiles.setNewCurrentProfile(profileName);
            refreshProfilesList();
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "profileComboBoxActionPerformed");
    }//GEN-LAST:event_profileComboBoxActionPerformed

    /**
     * The menu item profiles has been triggered
     * @param evt the event associated
     */
    private void profilesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profilesMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "profilesMenuItemActionPerformed");
        new ProfileDialog(this, true).setVisible(true);
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "profilesMenuItemActionPerformed");
    }//GEN-LAST:event_profilesMenuItemActionPerformed

    /**
     * The button resume has been triggered
     * @param evt the event associated
     */
    private void resumeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeButtonActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "resumeButtonActionPerformed");
        resumeReading();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "resumeButtonActionPerformed");
    }//GEN-LAST:event_resumeButtonActionPerformed

    /**
     * The menu item resume has been triggered
     * @param evt the event associated
     */
    private void resumeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "resumeMenuItemActionPerformed");
        resumeReading();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "resumeMenuItemActionPerformed");
    }//GEN-LAST:event_resumeMenuItemActionPerformed

    /**
     * The menu item generate report has been triggered
     * @param evt the event associated
     */
    private void generateReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "generateReportMenuItemActionPerformed");
        new ReportModeSelector(this, true).setVisible(true);
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "generateReportMenuItemActionPerformed");
    }//GEN-LAST:event_generateReportMenuItemActionPerformed

    /**
     * The menu item auto updates has been triggered
     * @param evt the event associated
     */
    private void autoUpdatesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoUpdatesCheckBoxMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "autoUpdatesCheckBoxMenuItemActionPerformed");
        if (autoUpdatesCheckBoxMenuItem.isSelected()) {
            PropertiesManager.getInstance().setKey("autoCheckUpdates", "true");
            checkForNewVersion();
        } else {
            PropertiesManager.getInstance().setKey("autoCheckUpdates", "false");
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "autoUpdatesCheckBoxMenuItemActionPerformed");
    }//GEN-LAST:event_autoUpdatesCheckBoxMenuItemActionPerformed

    /**
     * The menu item default language has been triggered
     * @param evt the event associated
     */
    private void defaultLanguageCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultLanguageCheckBoxMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "defaultLanguageCheckBoxMenuItemActionPerformed");
        Locale.setDefault(new Locale(System.getProperty("user.language")));
        PropertiesManager.getInstance().removeKey("language");
        restartInterface();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "defaultLanguageCheckBoxMenuItemActionPerformed");
    }//GEN-LAST:event_defaultLanguageCheckBoxMenuItemActionPerformed

    /**
     * The menu item english language has been triggered
     * @param evt the event associated
     */
    private void englishLanguageCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_englishLanguageCheckBoxMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "englishLanguageCheckBoxMenuItemActionPerformed");
        Locale.setDefault(Locale.ENGLISH);
        PropertiesManager.getInstance().setKey("language", "en");
        restartInterface();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "englishLanguageCheckBoxMenuItemActionPerformed");
    }//GEN-LAST:event_englishLanguageCheckBoxMenuItemActionPerformed

    /**
     * The menu item french language has been triggered
     * @param evt the event associated
     */
    private void frenchLanguageCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frenchLanguageCheckBoxMenuItemActionPerformed
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "frenchLanguageCheckBoxMenuItemActionPerformed");
        Locale.setDefault(Locale.FRENCH);
        PropertiesManager.getInstance().setKey("language", "fr");
        restartInterface();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "frenchLanguageCheckBoxMenuItemActionPerformed");
    }//GEN-LAST:event_frenchLanguageCheckBoxMenuItemActionPerformed

    /**
     * The window closing action (with the X) has been triggered
     * @param evt the event associated
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "formWindowClosing");
        exit();
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "formWindowClosing");
    }//GEN-LAST:event_formWindowClosing

    /**
     * restart the mainInterface (mainly to take new language parameters)
     */
    private void restartInterface() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "restartInterface");
        try {
            this.setVisible(false);
            this.dispose();
            reinitializeMainInterface();
            MainInterface.getInstance().setVisible(true);
            MainInterface.getInstance().changeSelectedBook(PropertiesManager.getInstance().getKey("lastSelectedSerie"), PropertiesManager.getInstance().getKey("lastSelectedAlbum")).start();
            LogInterface.getInstance().dispose();
            LogInterface.reinitializeLogInterface();
            ExceptionHandler.reinitializeLogInterfaceLink();
        } catch (Exception ex) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "restartInterface");
    }
    
    /**
     * When an action is in progress, this method should be called
     * @param inProgress status of the action. <br />true : action in progress<br />false : action finished
     * @param actionThread The thread which is active
     */
    private void setActionInProgress(boolean inProgress, Thread actionThread) {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "setActionInProgress", new Object[] {inProgress, actionThread});
        if (inProgress) {
            this.actionThread = actionThread;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
            }
        } else {
            this.setCursor(Cursor.getDefaultCursor());
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "setActionInProgress");
    }
    
    /**
     * Listener sur le changement d'une série
     * @param evt the event associated
     */
    private synchronized void seriesTableValueChanged(javax.swing.event.ListSelectionEvent evt) {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "seriesTableValueChanged");
        if (booksDirectory == null) {
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "seriesTableValueChanged");
            return;
        }
        final int selectedRow = seriesTable.getSelectedRow();
        if (!evt.getValueIsAdjusting() && selectedRow != -1) {
            listAlbums(selectedRow);
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "seriesTableValueChanged");
    }
    
    /**
     * Select a specific serie and then a specific album in this serie
     * @param serieToSelect the serie path string to select
     * @param albumToSelect the album path string to select
     * @return the thread that run the selection
     */
    protected Thread changeSelectedBook(final String serieToSelect, final String albumToSelect) {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "changeSelectedBook", new Object[] {serieToSelect, albumToSelect});
        Thread changeSelectBookThread =  new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "changeSelectedBookThread");
                try {
                    try {
                        while (firstLaunch) {                    
                            Thread.sleep(1);
                        }
                        if (actionThread != null) {
                            if (actionThread.getState() != Thread.State.TERMINATED) {
                                actionThread.join();
                            }
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (serieToSelect != null) {
                        serie = new File(serieToSelect);
                        if (!serie.exists()) {
                            Logger.getLogger(MainInterface.class.getName()).log(Level.WARNING, "The serie to select doesn't exist : {0}", serie);
                            serie = null;
                            album = null;
                            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "changeSelectedBookThread");
                            return;
                        }
                        Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Selecting the serie : {0}", serie);
                        for (int i = 0; i < seriesTable.getRowCount(); i++) {
                            String rowValue = (String) seriesTable.getValueAt(i, 0);
                            if (rowValue.equals(serie.getName())) {
                                seriesTable.getSelectionModel().setSelectionInterval(i, i);
                                seriesTable.scrollRectToVisible(seriesTable.getCellRect(i, 0, true));
                                break;
                            }
                        }
                        try {
                            if (actionThread != null) {
                                if (actionThread.getState() != Thread.State.TERMINATED) {
                                    actionThread.join();
                                }
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (albumToSelect == null) {
                            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "changeSelectedBookThread");
                            return;
                        }
                        album = new File(albumToSelect);
                        if (!album.exists()) {
                            Logger.getLogger(MainInterface.class.getName()).log(Level.WARNING, "The album to select doesn't exist : {0}", album);
                            album = null;
                            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "changeSelectedBookThread");
                            return;
                        }
                        Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Selecting the album : {0}", album);
                        for (int i = 0; i < albumsTable.getRowCount(); i++) {
                            String rowValue = (String) albumsTable.getValueAt(i, 0) + albumsTable.getValueAt(i, 1);
                            if (rowValue.equals(album.getName())) {
                                albumsTable.getSelectionModel().setSelectionInterval(i, i);
                                albumsTable.scrollRectToVisible(albumsTable.getCellRect(i, 0, true));
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                    new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
                }
            }
        });
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "changeSelectedBook", changeSelectBookThread);
        return changeSelectBookThread;
    }

    /**
     * Refresh the profiles combo box and menu items
     */
    public void refreshProfilesList() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "refreshProfilesList");
        Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Refresh the profiles list items and combo box");
        try {
            refreshProfileComboBox();
            refreshProfileRadioButtonMenuItem();
            if (booksDirectory == null) {
                if (!profiles.getCurrentProfileFolder().equals("")) {
                    booksDirectory = new File(profiles.getCurrentProfileFolder());
                    listSeries();
                }
            } else if (!booksDirectory.toString().equals(profiles.getCurrentProfileFolder())) {
                booksDirectory = new File(profiles.getCurrentProfileFolder());
                listSeries();
            }
        } catch (Exception ex) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "refreshProfilesList");
    }
    
    /**
     * Refresh the profiles combo box
     */
    private void refreshProfileComboBox() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "refreshProfileComboBox");
        profileComboBox.setModel(new DefaultComboBoxModel<>(profiles.getProfilesNames()));
        profileComboBox.setSelectedItem(profiles.getCurrentProfileName());
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "refreshProfileComboBox");
    }
    
    /**
     * Refresh the profiles menu item
     */
    private void refreshProfileRadioButtonMenuItem() {
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "refreshProfileRadioButtonMenuItem");
        for (JRadioButtonMenuItem profileRadioButtonMenuItem : profilesListRadioButtonMenuItem) {
            profileButtonGroup.remove(profileRadioButtonMenuItem);
            profileMenu.remove(profileRadioButtonMenuItem);
        }
        profilesListRadioButtonMenuItem.clear();
        for (String profileName : profiles.getProfilesNames()) {
            JRadioButtonMenuItem profileRadioButtonMenuItem = new JRadioButtonMenuItem();
            profileRadioButtonMenuItem.setText(profileName);
            profileRadioButtonMenuItem.setName(profileName.concat("ProfileRadioButtonMenuItem"));
            if (profiles.getCurrentProfileName().equals(profileName)) {
                profileRadioButtonMenuItem.setSelected(true);
            }
            profileRadioButtonMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < profilesListRadioButtonMenuItem.size(); i++) {
                        if (profilesListRadioButtonMenuItem.get(i).isSelected()) {
                            String profileName = profilesListRadioButtonMenuItem.get(i).getText();
                            if (!profileName.equals(profiles.getCurrentProfileName())) {
                                profiles.setNewCurrentProfile(profileName);
                            }
                        }
                    }
                    refreshProfilesList();
                }
            });
            profileButtonGroup.add(profileRadioButtonMenuItem);
            profileMenu.add(profileRadioButtonMenuItem);
            profilesListRadioButtonMenuItem.add(profileRadioButtonMenuItem);
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "refreshProfileRadioButtonMenuItem");
    }
    
    /**
     * Refresh all the series
     */
    private void listSeries() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "listSeries");
        previewComponent.setNoPreviewImage();
        Thread listSeriesThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Refreshing the serie list");
                try {
                    serie = null;
                    album = null;

                    final DefaultTableModel seriesTableModel = (DefaultTableModel) seriesTable.getModel();
                    final DefaultTableModel albumsTableModel = (DefaultTableModel) albumsTable.getModel();
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                seriesTableModel.setRowCount(0);
                                albumsTableModel.setRowCount(0);
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Object[][] seriesData =  new BooksFolderAnalyser(booksDirectory).listSeries();
                    if (seriesData == null) {
                        Logger.getLogger(MainInterface.class.getName()).log(Level.WARNING, "The profile folder doesn't exist or can't be read : {0}", booksDirectory);
                        firstLaunch = false;
                        setActionInProgress(false, null);
                        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "listSeries");
                        return;
                    }
                    for (final Object[] serieData : seriesData) {
                        Thread tampon = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                seriesTableModel.addRow(new Object[]{serieData[0], serieData[1]});
                            }
                        });
                        try {
                            SwingUtilities.invokeAndWait(tampon);
                        } catch (InterruptedException | InvocationTargetException ex) {
                            Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    firstLaunch = false;
                } catch (Exception ex) {
                    Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                    new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
                }
                setActionInProgress(false, null);
                Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "listSeries");
            }
        });
        setActionInProgress(true, listSeriesThread);
        if (SwingUtilities.isEventDispatchThread()) {
            listSeriesThread.start();
        }
        else {
            listSeriesThread.run();
        }
    }

    /**
     * Refresh the albums of the selected serie
     * @param selectedRow the row of the selected serie
     */
    @SuppressWarnings("deprecation")
    private void listAlbums(final int selectedRow) {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "listAlbums");
        Thread listAlbumsThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Refreshing the album list");
                try {
                    threadedPreviewLoader.stop();
                    serie = null;
                    album = null;
                    while (threadedPreviewLoader.isAlive()) {
                        try {
                        Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    serie = new File(booksDirectory.toString() + File.separator + seriesTable.getValueAt(selectedRow, 0).toString());
                    if (!serie.exists()) {
                        Logger.getLogger(MainInterface.class.getName()).log(Level.WARNING, "The serie folder doesn't exist : {0}", serie);
                        setActionInProgress(false, null);
                        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "listAlbums");
                        return;
                    }
                    Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Serie \"{0}\" Selected", serie);
                    final DefaultTableModel albumsTableModel = (DefaultTableModel) albumsTable.getModel();
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                albumsTableModel.setRowCount(0);
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    previewComponent.setNoPreviewImage();
                    String[][] albumsData = new BooksFolderAnalyser(serie).listAlbums();
                    if (albumsData == null) {
                        setActionInProgress(false, null);
                        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "listAlbums");
                        return;
                    }
                    for (final String[] albumData : albumsData) {
                        Thread tampon = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                albumsTableModel.addRow(new Object[]{albumData[0], albumData[1]});
                            }
                        });
                        try {
                        SwingUtilities.invokeAndWait(tampon);
                        } catch (InterruptedException | InvocationTargetException ex) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    }       
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                if (albumsTableModel.getRowCount() != 0) {
                                    albumsTable.getSelectionModel().setSelectionInterval(0, 0);
                                }
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                    new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
                }
                setActionInProgress(false, null);
                Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "listAlbums");
            }
        });
        setActionInProgress(true, listAlbumsThread);
        listAlbumsThread.start();
    }
    
    /**
     * listener on a new album selected
     * @param evt the event associated
     */
    @SuppressWarnings("deprecation")
    private synchronized void albumsTableValueChanged(javax.swing.event.ListSelectionEvent evt) {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "albumsTableValueChanged");
        album = null;
        if (booksDirectory == null) {
            return;
        }
        int selectedRow = albumsTable.getSelectedRow();
        if (!evt.getValueIsAdjusting() && selectedRow != -1) {
            threadedPreviewLoader.stop();
            while (threadedPreviewLoader.isAlive()) {
                try {
                Thread.sleep(1);
                } catch (InterruptedException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
            album = new File(serie.toString() + File.separator + albumsTable.getValueAt(selectedRow, 0).toString() + albumsTable.getValueAt(selectedRow, 1).toString());
            Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Album \"{0}\" Selected", album);
            threadedPreviewLoader = new PreviewImageLoader();
            threadedPreviewLoader.start();
        }
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "albumsTableValueChanged");
    }

    /**
     * Start reading the selected album on page 1
     */
    private void startReading() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "startReading");
        launchReadInterface(1);
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "startReading");
    }
    
    /**
     * Launch the reading interface on the indicated page
     * @param page the page number to show first. First page is page 1
     */
    private void launchReadInterface(final int page) {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "launchReadInterface", page);
        new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Start the read interface on page {0}", page);
                if (readInterface != null) {
                    readInterface.setVisible(false);
                    readInterface.dispose();
                }
                try {
                    readInterface = new ReadInterface(imageHandler);
                    readInterface.setVisible(true);
                    readInterface.requestFocus();
                    readInterface.revalidate();
                    readInterface.goPage(page);
                    PropertiesManager.getInstance().setKey("lastReadedSerie", serie.toString());
                    PropertiesManager.getInstance().setKey("lastReadedAlbum", album.toString());
                } catch (Exception ex) {
                    Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                    new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
                }
                Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "launchReadInterface");
            }
        }).start();
    }
    
    /**
     * Resume the reading from the previously saved state (previous serie, previous album, previous page)
     */
    private void resumeReading() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "resumeReading");
        new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Resume the reading");
                Thread changeSelectedBookThread = changeSelectedBook(PropertiesManager.getInstance().getKey("lastReadedSerie"), PropertiesManager.getInstance().getKey("lastReadedAlbum"));
                changeSelectedBookThread.start();
                try {
                    changeSelectedBookThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (album != null && serie != null) {
                    if (PropertiesManager.getInstance().getKey("lastReadedPage") != null) {
                        int page = Integer.parseInt(PropertiesManager.getInstance().getKey("lastReadedPage"));
                        launchReadInterface(page);
                    }
                }
                Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "resumeReading");
            }
        }).start();
    }
  
    /**
     * This class have in charge the preview of the selected album
     */
    private class PreviewImageLoader extends Thread {

        @Override
        public void run() {
            Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "PreviewImageLoader");
            setActionInProgress(true, this);
            Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Preview the album {0}", album);
            try {
                if (album.isDirectory()) {
                    imageHandler = new FolderHandler(album);
                    BufferedImage previewImage = imageHandler.getImage(1);
                    if (previewImage != null) {
                        previewComponent.setImage(previewImage);
                    } else {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for directory. album : {0}", album);
                        previewComponent.setNoPreviewImage();
                    }
                }
                else if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
                    imageHandler = new ZipHandler(album);
                    BufferedImage previewImage = imageHandler.getImage(1);
                    if (previewImage != null) {
                        previewComponent.setImage(previewImage);
                    } else {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for zip. album : {0}", album);
                        previewComponent.setNoPreviewImage();
                    }
                }
                else if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
                    imageHandler = new RarHandler(album);
                    BufferedImage previewImage = imageHandler.getImage(1);
                    if (previewImage != null) {
                        previewComponent.setImage(previewImage);
                    } else {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for rar. album : {0}", album);
                        previewComponent.setNoPreviewImage();
                    }
                }
                else if (album.getName().toLowerCase().endsWith(".pdf")) {
                    imageHandler = new PdfHandler(album);
                    BufferedImage previewImage = imageHandler.getImage(1);
                    if (previewImage != null) {
                        previewComponent.setImage(previewImage);
                    } else {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for pdf. album : {0}", album);
                        previewComponent.setNoPreviewImage();
                    }
                }
                else {
                    previewComponent.setNoPreviewImage();
                }
            } catch (Exception ex) {
                Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
                new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
            }
            setActionInProgress(false, null);
            Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "PreviewImageLoader");
        }
    }
    
    /**
     * Save the properties and exit this application
     */
    public void exit() {
       Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "exit");
       this.setVisible(false);
       if (readInterface != null) {
           readInterface.exit();
       }
       savePreferences();
       this.dispose();
       profiles.saveProfilesProperties();
       if (serie != null) {
           PropertiesManager.getInstance().setKey("lastSelectedSerie", serie.toString());
       }
       if (album != null) {
           PropertiesManager.getInstance().setKey("lastSelectedAlbum", album.toString());
       }
       PropertiesManager.getInstance().saveProperties();
       Logger.getLogger(MainInterface.class.getName()).log(Level.INFO, "Software is quitting normally !");
       LogInterface.getInstance().dispose();
       System.exit(0);
       Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "exit");
    }
    
    /**
     * Save in the preferences the size and location of the mainInterface
     */
    private void savePreferences() {
        Logger.getLogger(MainInterface.class.getName()).entering(MainInterface.class.getName(), "savePreferences");
        preferences.putInt("width", getWidth());
        preferences.putInt("height", getHeight());
        preferences.putInt("X-location", getX());
        preferences.putInt("Y-location", getY());
        preferences.putInt("divider-location", booksPreviewSplitPane.getDividerLocation());
        Logger.getLogger(MainInterface.class.getName()).exiting(MainInterface.class.getName(), "savePreferences");
    }
   
    /**
     * Get the preview component
     * @return The component that preview the selected album
     */
    public static Component getPreviewComponent() {
       return previewComponent;
   }

    /**
     * Get the ReadInterface instance
     * @return the readInterface instance
     */
    public ReadInterface getReadInterface() {
        return readInterface;
    }

    /**
     * Get the profiles instance
     * @return The profiles instance
     */
    public Profiles getProfiles() {
        return profiles;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JScrollPane albumsScrollPane;
    private javax.swing.JTable albumsTable;
    private javax.swing.JLabel appDescLabel;
    private javax.swing.JLabel appHomepageLabel;
    private javax.swing.JLabel appTitleLabel;
    private javax.swing.JLabel appVendorLabel;
    private javax.swing.JLabel appVersionLabel;
    private javax.swing.JCheckBoxMenuItem autoUpdatesCheckBoxMenuItem;
    private javax.swing.JMenuItem bookFolderMenuItem;
    private javax.swing.JSplitPane booksPreviewSplitPane;
    private javax.swing.JButton closeAboutDialogButton;
    private javax.swing.JCheckBoxMenuItem defaultLanguageCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem englishLanguageCheckBoxMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JCheckBoxMenuItem frenchLanguageCheckBoxMenuItem;
    private javax.swing.JMenuItem generateReportMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel homepageLabel;
    private javax.swing.JLabel imageLabel;
    private javax.swing.ButtonGroup languageButtonGroup;
    private javax.swing.JMenu languageMenu;
    private javax.swing.JPopupMenu.Separator languageSeparator;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JMenu optionMenu;
    private javax.swing.JPopupMenu.Separator optionsSeparator;
    private static booknaviger.PreviewComponent previewComponent;
    private javax.swing.JLabel productVersionLabel;
    private javax.swing.ButtonGroup profileButtonGroup;
    private javax.swing.JComboBox<String> profileComboBox;
    private javax.swing.JMenu profileMenu;
    private javax.swing.JPopupMenu.Separator profileSeparator;
    private javax.swing.JMenuItem profilesMenuItem;
    private javax.swing.JButton refreshAllButton;
    private javax.swing.JMenuItem refreshAllMenuItem;
    private javax.swing.JButton refreshCurrentButton;
    private javax.swing.JMenuItem refreshCurrentMenuItem;
    private javax.swing.JButton resumeButton;
    private javax.swing.JMenuItem resumeMenuItem;
    private javax.swing.JScrollPane seriesScrollPane;
    private javax.swing.JTable seriesTable;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JToolBar statusToolBar;
    private javax.swing.Box.Filler statusToolBarFiller;
    private javax.swing.JMenuBar toolBar;
    private javax.swing.JToolBar.Separator toolbarSeparator1;
    private javax.swing.JToolBar.Separator toolbarSeparator2;
    private javax.swing.JLabel vendorLabel;
    // End of variables declaration//GEN-END:variables
}
