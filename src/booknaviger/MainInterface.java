/*
 */
package booknaviger;

import booknaviger.booksfolder.BooksFolderSelector;
import booknaviger.exceptioninterface.LogInterface;
import booknaviger.inet.InetBasics;
import booknaviger.inet.htmlreport.ReportModeSelector;
import booknaviger.inet.updater.NewUpdateAvailableDialog;
import booknaviger.inet.updater.Updater;
import booknaviger.macworld.MacOSXApplicationAdapter;
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    /**
     *
     * @return
     */
    private static class MainInterfaceHolder {

        private static final MainInterface INSTANCE = new MainInterface();
    }
    
    public static MainInterface getInstance() {
        return MainInterfaceHolder.INSTANCE;
    }
    
    /**
     * Creates new form MainInterface
     */
    private MainInterface() {
        macInit();
        initComponents();
        setTimer();
        refreshProfilesList();
        previewComponent.setStatusToolBarHeigh(statusToolBar.getHeight() + mainToolBar.getHeight());
        if (shoudCheckNewVersion()) {
            checkForNewVersion();
        }
    }
    
    private void macInit() {
        if (MacOSXApplicationAdapter.isMac()) {
            new MacOSXApplicationAdapter(this);
        }
    }
    
    private void setTimer() {
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
    }
    
    private boolean shoudCheckNewVersion() {
        String autoCheckUpdates = PropertiesManager.getInstance().getKey("autoCheckUpdates");
        if (autoCheckUpdates != null && autoCheckUpdates.equals("false")) {
            autoUpdatesCheckBoxMenuItem.setSelected(false);
            return false;
        }
        String lastUpdateCheckString = PropertiesManager.getInstance().getKey("lastUpdateCheck");
        if (lastUpdateCheckString == null) {
            return true;
        }
        Date lastUpdateCheckDate;
        try {
            lastUpdateCheckDate = DateFormat.getDateInstance().parse(lastUpdateCheckString);
        } catch (ParseException ex) {
            Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(lastUpdateCheckDate);
        cal.add(Calendar.DATE, 7);
        if (new Date().after(cal.getTime())) {
            return true;
        }
        return false;
    }
    
    private void checkForNewVersion() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Updater updater = new Updater();
                if (updater.isNewVersionAvailable()) {
                    new NewUpdateAvailableDialog(MainInterface.getInstance(), updater.getVersionNumber(), updater.getDownloadURLString()).setVisible(true);
                }
            }
        }).start();
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

        booksPreviewSplitPane.setDividerLocation(280);
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
        languageMenu.add(defaultLanguageCheckBoxMenuItem);
        languageMenu.add(languageSeparator);

        languageButtonGroup.add(englishLanguageCheckBoxMenuItem);
        englishLanguageCheckBoxMenuItem.setText(resourceBundle.getString("english-language_menu")); // NOI18N
        englishLanguageCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/en_US.png"))); // NOI18N
        languageMenu.add(englishLanguageCheckBoxMenuItem);

        languageButtonGroup.add(frenchLanguageCheckBoxMenuItem);
        frenchLanguageCheckBoxMenuItem.setText(resourceBundle.getString("french-language_menu")); // NOI18N
        frenchLanguageCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/mainmenu/fr_FR.png"))); // NOI18N
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
                openAboutDialog(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        toolBar.add(helpMenu);

        setJMenuBar(toolBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exit();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void bookFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookFolderMenuItemActionPerformed
        BooksFolderSelector booksFolderselector = new BooksFolderSelector(this, true);
        String selectedFolder = booksFolderselector.selectFolder();
        if (selectedFolder != null) {
            getProfiles().setCurrentProfileFolder(selectedFolder);
        }
    }//GEN-LAST:event_bookFolderMenuItemActionPerformed

    private void refreshAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAllActionPerformed
        listSeries();
    }//GEN-LAST:event_refreshAllActionPerformed

    private void refreshCurrentAlbumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshCurrentAlbumActionPerformed
        int selectedRow = seriesTable.getSelectedRow();
        if (selectedRow != -1) {
            listAlbums(selectedRow);
        }
    }//GEN-LAST:event_refreshCurrentAlbumActionPerformed

    private void closeAboutBox(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAboutBox
        aboutDialog.setVisible(false);
        aboutDialog.dispose();
    }//GEN-LAST:event_closeAboutBox

    private void openAboutDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openAboutDialog
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_openAboutDialog

    private void homepageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homepageLabelMouseClicked
        InetBasics.openURI(appHomepageLabel.getText());
    }//GEN-LAST:event_homepageLabelMouseClicked

    private void albumsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_albumsTableMouseClicked
        if (evt.getClickCount() == 2 && album != null) {
            startReading();
        }
    }//GEN-LAST:event_albumsTableMouseClicked

    private void previewComponentComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_previewComponentComponentResized
        previewComponent.refresh();
    }//GEN-LAST:event_previewComponentComponentResized

    private void previewComponentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_previewComponentMouseClicked
        if (album != null) {
            startReading();
        }
    }//GEN-LAST:event_previewComponentMouseClicked

    private void seriesTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_seriesTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            evt.consume();
            albumsTable.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            startReading();
        }
    }//GEN-LAST:event_seriesTableKeyPressed

    private void albumsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_albumsTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            evt.consume();
            seriesTable.requestFocusInWindow();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            startReading();
        }
    }//GEN-LAST:event_albumsTableKeyPressed

    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileComboBoxActionPerformed
        String profileName = profileComboBox.getSelectedItem().toString();
        if (!profileName.equals(profiles.getCurrentProfileName())) {
            profiles.setNewCurrentProfile(profileName);
            refreshProfilesList();
        }
    }//GEN-LAST:event_profileComboBoxActionPerformed

    private void profilesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profilesMenuItemActionPerformed
        new ProfileDialog(this, true).setVisible(true);
    }//GEN-LAST:event_profilesMenuItemActionPerformed

    private void resumeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeButtonActionPerformed
        resumeReading();
    }//GEN-LAST:event_resumeButtonActionPerformed

    private void resumeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeMenuItemActionPerformed
        resumeReading();
    }//GEN-LAST:event_resumeMenuItemActionPerformed

    private void generateReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportMenuItemActionPerformed
        new ReportModeSelector(this, true).setVisible(true);
    }//GEN-LAST:event_generateReportMenuItemActionPerformed

    private void autoUpdatesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoUpdatesCheckBoxMenuItemActionPerformed
        if (autoUpdatesCheckBoxMenuItem.isSelected()) {
            PropertiesManager.getInstance().setKey("autoCheckUpdates", "true");
            checkForNewVersion();
        } else {
            PropertiesManager.getInstance().setKey("autoCheckUpdates", "false");
        }
    }//GEN-LAST:event_autoUpdatesCheckBoxMenuItemActionPerformed

    private void setActionInProgress(boolean inProgress, Thread actionThread) {
        if (inProgress) {
            this.actionThread = actionThread;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
            }
        } else {
            this.actionThread = null;
            this.setCursor(Cursor.getDefaultCursor());
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
        }
    }
    
    /**
     * Listener sur le changement d'une série
     */
    private synchronized void seriesTableValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (booksDirectory == null) {
            return;
        }
        final int selectedRow = seriesTable.getSelectedRow();
        if (!evt.getValueIsAdjusting() && selectedRow != -1) {
            listAlbums(selectedRow);
        }
    }
    
    protected Thread changeSelectedBook(final String serieToSelect, final String albumToSelect) {
        return new Thread(new Runnable() {

            @Override
            public void run() {
                try {
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
                        serie = null;
                        album = null;
                        return;
                    }
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
                        return;
                    }
                    album = new File(albumToSelect);
                    if (!album.exists()) {
                        album = null;
                        return;
                    }
                    for (int i = 0; i < albumsTable.getRowCount(); i++) {
                        String rowValue = (String) albumsTable.getValueAt(i, 0) + albumsTable.getValueAt(i, 1);
                        if (rowValue.equals(album.getName())) {
                            albumsTable.getSelectionModel().setSelectionInterval(i, i);
                            albumsTable.scrollRectToVisible(albumsTable.getCellRect(i, 0, true));
                            break;
                        }
                    }
                }
            }
        });
    }

    
    public void refreshProfilesList() {
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
    }
    
    private void refreshProfileComboBox() {
        profileComboBox.setModel(new DefaultComboBoxModel<>(profiles.getProfilesNames()));
        profileComboBox.setSelectedItem(profiles.getCurrentProfileName());
    }
    
    /**
     * Crée un nouveau radiobuttonmenuitem pour un nouveau profil
     * @param text le titre du profil à mettre sur le button
     * @return le button créé
     */
    private void refreshProfileRadioButtonMenuItem() {
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
    }
    
    /**
     * Rafraichi la liste de toutes les séries
     */
    private void listSeries() {
        previewComponent.setNoPreviewImage();
        Thread listSeriesThread = new Thread(new Runnable() {

            @Override
            public void run() {
                File[] allfiles = null;
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
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                if (booksDirectory == null) {
                    setActionInProgress(false, null);
                    return;
                }
                try {
                    allfiles = booksDirectory.listFiles();
                } catch(SecurityException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    //new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rights", directory.toString());
                }
                if (allfiles == null) {
                    setActionInProgress(false, null);
                    return;
                }
                Arrays.sort(allfiles);
                final File[] allFilesValue = allfiles;
                final List<Thread> rows = new ArrayList<>();
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
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
                setActionInProgress(false, null);
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

    @SuppressWarnings("deprecation")
    private void listAlbums(final int selectedRow) {
        Thread listAlbumsThread = new Thread(new Runnable() {

            @Override
            public void run() {
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
                    setActionInProgress(false, null);
                    return;
                }
                File[] allfiles = null;
                try {
                    allfiles = serie.listFiles();
                } catch(SecurityException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    // new KnownErrorBox(getFrame(), KnownErrorBox.ERROR_LOGO, "Error_Read_Rights", serie.toString());
                }
                final DefaultTableModel albumsTableModel = (DefaultTableModel) albumsTable.getModel();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            albumsTableModel.setRowCount(0);
                        }
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                if (allfiles == null) {
                    previewComponent.setNoPreviewImage();
                    setActionInProgress(false, null);
                    return;
                }
                Arrays.sort(allfiles);
                final File[] allFilesValue = allfiles;
                List<Thread> rows = new ArrayList<>();
                for (int i = 0; i < allFilesValue.length; i++) {
                    String name = allFilesValue[i].getName();
                    if (!allFilesValue[i].isHidden() && (allFilesValue[i].isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf"))) {
                        final int index = i;
                        Thread tampon = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                if (allFilesValue[index].isDirectory()) {
                                    albumsTableModel.addRow(new Object[]{allFilesValue[index].getName(), ""});
                                }
                                else {
                                    String albumFullName = allFilesValue[index].getName();
                                    int indexOfExtension = albumFullName.lastIndexOf(".");
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
                            //albumScrollPane.getVerticalScrollBar().setValue(0);
                        }
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                setActionInProgress(false, null);
            }
        });
        setActionInProgress(true, listAlbumsThread);
        listAlbumsThread.start();
    }
    
    /**
     * listener sur le changement d'un album (au sein d'une série)
     */
    @SuppressWarnings("deprecation")
    private synchronized void albumsTableValueChanged(javax.swing.event.ListSelectionEvent evt) {
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
            threadedPreviewLoader = new PreviewImageLoader();
            threadedPreviewLoader.start();
        }
    }

    private void startReading() {
        launchReadInterface(1);
    }
    
    private void launchReadInterface(final int page) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (readInterface != null) {
                    readInterface.setVisible(false);
                    readInterface.dispose();
                }
                readInterface = new ReadInterface(imageHandler);
                readInterface.setVisible(true);
                readInterface.requestFocus();
                readInterface.revalidate();
                readInterface.goPage(page);
                PropertiesManager.getInstance().setKey("lastReadedSerie", serie.toString());
                PropertiesManager.getInstance().setKey("lastReadedAlbum", album.toString());
            }
        }).start();
    }
    
    private void resumeReading() {
        new Thread(new Runnable() {

            @Override
            public void run() {
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
            }
        }).start();
    }
  
    private class PreviewImageLoader extends Thread {
        
        public PreviewImageLoader() {
        }

        @Override
        public void run() {
            setActionInProgress(true, this);
            if (album.isDirectory()) {
                imageHandler = new FolderHandler(album);
                BufferedImage previewImage = imageHandler.getImage(1);
                if (previewImage != null) {
                    previewComponent.setImage(previewImage);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for directory. album : {0}", album);
                }
            }
            else if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
                imageHandler = new ZipHandler(album);
                BufferedImage previewImage = imageHandler.getImage(1);
                if (previewImage != null) {
                    previewComponent.setImage(previewImage);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for zip. album : {0}", album);
                }
            }
            else if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
                imageHandler = new RarHandler(album);
                BufferedImage previewImage = imageHandler.getImage(1);
                if (previewImage != null) {
                    previewComponent.setImage(previewImage);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for rar. album : {0}", album);
                }
            }
            else if (album.getName().toLowerCase().endsWith(".pdf")) {
                imageHandler = new PdfHandler(album);
                BufferedImage previewImage = imageHandler.getImage(1);
                if (previewImage != null) {
                    previewComponent.setImage(previewImage);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "no preview for pdf. album : {0}", album);
                }
            }
            else {
                previewComponent.setNoPreviewImage();
            }
            setActionInProgress(false, null);
        }
    }
    
    /**
     *
     */
    public void exit() {
       this.setVisible(false);
       if (readInterface != null) {
           readInterface.exit();
       }
       LogInterface.getInstance().dispose();
       this.dispose();
       profiles.saveProfilesProperties();
       if (serie != null) {
           PropertiesManager.getInstance().setKey("lastSelectedSerie", serie.toString());
       }
       if (album != null) {
           PropertiesManager.getInstance().setKey("lastSelectedAlbum", album.toString());
       }
       PropertiesManager.getInstance().saveProperties();
       System.exit(0);
   }
   
    /**
     *
     * @return
     */
    public static Component getPreviewComponent() {
       return previewComponent;
   }

    /**
     *
     * @return
     */
    public ReadInterface getReadInterface() {
        return readInterface;
    }

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
