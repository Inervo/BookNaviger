/*
 */
package booknaviger.readinterface;

import booknaviger.MainInterface;
import booknaviger.macworld.MacOSXApplicationAdapter;
import booknaviger.macworld.TrackPadAdapter;
import booknaviger.picturehandler.AbstractImageHandler;
import booknaviger.picturehandler.ImageReader;
import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;

/**
 *
 * @author Inervo
 */
public class ReadInterface extends javax.swing.JFrame {
    
    private AbstractImageHandler imageHandler = null;
    private int pageNbr = 0;
    private boolean dualPageReadMode = false;
    private ResourceBundle resourceBundle = java.util.ResourceBundle.getBundle("booknaviger/resources/ReadInterface");
    TrackPadAdapter tpa = null;

    /**
     * Creates new form ReadInterface
     */
    public ReadInterface(AbstractImageHandler abstractImageHandler) {
        this.imageHandler = abstractImageHandler;
        initComponents();
        if (MacOSXApplicationAdapter.isMac()) {
            System.out.println("add Mac Gesture adapter - doesn't work...");
            tpa = new TrackPadAdapter(this); // TODO : don't work !! WHYYYYY ???
            tpa.addListenerOn(getRootPane());
        }
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        readInterfacePopupMenu = new javax.swing.JPopupMenu();
        navigationReadInterfaceMenu = new javax.swing.JMenu();
        previousPageReadInterfaceMenuItem = new javax.swing.JMenuItem();
        nextPageReadInterfaceMenuItem = new javax.swing.JMenuItem();
        navigationReadInterfaceSeparator1 = new javax.swing.JPopupMenu.Separator();
        tenPagesBeforeReadInterfaceMenuItem = new javax.swing.JMenuItem();
        tenPagesAfterReadInterfaceMenuItem = new javax.swing.JMenuItem();
        navigationReadInterfaceSeparator2 = new javax.swing.JPopupMenu.Separator();
        firstPageReadInterfaceMenuItem = new javax.swing.JMenuItem();
        lastPageReadInterfaceMenuItem = new javax.swing.JMenuItem();
        displayReadInterfaceMenu = new javax.swing.JMenu();
        zoomMenu = new javax.swing.JMenu();
        zoomDefaultReadInterfaceMenuItem = new javax.swing.JMenuItem();
        zoomInReadInterfaceMenuItem = new javax.swing.JMenuItem();
        zoomOutReadInterfaceMenuItem = new javax.swing.JMenuItem();
        zoomSeparator = new javax.swing.JPopupMenu.Separator();
        fitHorizontallyReadInterfaceCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        fitVerticallyReadInterfaceCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        rotateMenu = new javax.swing.JMenu();
        rotateInitialRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rotate90RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rotate180RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rotate270RadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        doublePagesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        minimizeReadInterfaceMenuItem = new javax.swing.JMenuItem();
        exitReadInterfaceMenuItem = new javax.swing.JMenuItem();
        rotateButtonGroup = new javax.swing.ButtonGroup();
        readInterfaceScrollPane = new javax.swing.JScrollPane();
        readComponent = new booknaviger.readinterface.ReadComponent();

        navigationReadInterfaceMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/navigate.png"))); // NOI18N
        navigationReadInterfaceMenu.setText(resourceBundle.getString("navigateReadInterfaceMenu")); // NOI18N

        previousPageReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/previousPage.png"))); // NOI18N
        previousPageReadInterfaceMenuItem.setText(resourceBundle.getString("previousPageReadInterfaceMenuItemText")); // NOI18N
        previousPageReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        navigationReadInterfaceMenu.add(previousPageReadInterfaceMenuItem);

        nextPageReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/nextPage.png"))); // NOI18N
        nextPageReadInterfaceMenuItem.setText(resourceBundle.getString("nextPageReadInterfaceMenuItemText")); // NOI18N
        nextPageReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        navigationReadInterfaceMenu.add(nextPageReadInterfaceMenuItem);
        navigationReadInterfaceMenu.add(navigationReadInterfaceSeparator1);

        tenPagesBeforeReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/previous10.png"))); // NOI18N
        tenPagesBeforeReadInterfaceMenuItem.setText(resourceBundle.getString("tenPagesBeforeReadInterfaceMenuItemText")); // NOI18N
        tenPagesBeforeReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tenPagesBeforeReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        navigationReadInterfaceMenu.add(tenPagesBeforeReadInterfaceMenuItem);

        tenPagesAfterReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/next10.png"))); // NOI18N
        tenPagesAfterReadInterfaceMenuItem.setText(resourceBundle.getString("tenPagesAfterReadInterfaceMenuItemText")); // NOI18N
        tenPagesAfterReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tenPagesAfterReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        navigationReadInterfaceMenu.add(tenPagesAfterReadInterfaceMenuItem);
        navigationReadInterfaceMenu.add(navigationReadInterfaceSeparator2);

        firstPageReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/homePage.png"))); // NOI18N
        firstPageReadInterfaceMenuItem.setText(resourceBundle.getString("firstPageReadInterfaceMenuItemText")); // NOI18N
        firstPageReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstPageReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        navigationReadInterfaceMenu.add(firstPageReadInterfaceMenuItem);

        lastPageReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/endPage.png"))); // NOI18N
        lastPageReadInterfaceMenuItem.setText(resourceBundle.getString("lastPageReadInterfaceMenuItemText")); // NOI18N
        lastPageReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastPageReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        navigationReadInterfaceMenu.add(lastPageReadInterfaceMenuItem);

        readInterfacePopupMenu.add(navigationReadInterfaceMenu);

        displayReadInterfaceMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/display.png"))); // NOI18N
        displayReadInterfaceMenu.setText(resourceBundle.getString("displayReadInterfaceMenuText")); // NOI18N

        zoomMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/zoom.png"))); // NOI18N
        zoomMenu.setText(resourceBundle.getString("zoomReadInterfaceMenuText")); // NOI18N

        zoomDefaultReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/normalZoom.png"))); // NOI18N
        zoomDefaultReadInterfaceMenuItem.setText(resourceBundle.getString("zoomDefaultReadInterfaceMenuItemText")); // NOI18N
        zoomDefaultReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomDefaultReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(zoomDefaultReadInterfaceMenuItem);

        zoomInReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/zoomIn.png"))); // NOI18N
        zoomInReadInterfaceMenuItem.setText(resourceBundle.getString("zoomInReadInterfaceMenuItemText")); // NOI18N
        zoomInReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(zoomInReadInterfaceMenuItem);

        zoomOutReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/zoomOut.png"))); // NOI18N
        zoomOutReadInterfaceMenuItem.setText(resourceBundle.getString("zoomOutReadInterfaceMenuItemText")); // NOI18N
        zoomOutReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(zoomOutReadInterfaceMenuItem);
        zoomMenu.add(zoomSeparator);

        fitHorizontallyReadInterfaceCheckBoxMenuItem.setText(resourceBundle.getString("fitHorizontallyReadInterfaceMenuItemText")); // NOI18N
        fitHorizontallyReadInterfaceCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/fitHorizontal.png"))); // NOI18N
        fitHorizontallyReadInterfaceCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitHorizontallyReadInterfaceCheckBoxMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(fitHorizontallyReadInterfaceCheckBoxMenuItem);

        fitVerticallyReadInterfaceCheckBoxMenuItem.setText(resourceBundle.getString("fitVerticallyReadInterfaceMenuItemText")); // NOI18N
        fitVerticallyReadInterfaceCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/fitVertical.png"))); // NOI18N
        fitVerticallyReadInterfaceCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitVerticallyReadInterfaceCheckBoxMenuItemActionPerformed(evt);
            }
        });
        zoomMenu.add(fitVerticallyReadInterfaceCheckBoxMenuItem);

        displayReadInterfaceMenu.add(zoomMenu);

        rotateMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/rotation.png"))); // NOI18N
        rotateMenu.setText(resourceBundle.getString("rotateReadInterfaceMenuText")); // NOI18N

        rotateButtonGroup.add(rotateInitialRadioButtonMenuItem);
        rotateInitialRadioButtonMenuItem.setSelected(true);
        rotateInitialRadioButtonMenuItem.setText(resourceBundle.getString("rotateDefaultReadInterfaceRadioButtonMenuItemText")); // NOI18N
        rotateInitialRadioButtonMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/rotationInitial.png"))); // NOI18N
        rotateInitialRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateInitialRadioButtonMenuItemActionPerformed(evt);
            }
        });
        rotateMenu.add(rotateInitialRadioButtonMenuItem);

        rotateButtonGroup.add(rotate90RadioButtonMenuItem);
        rotate90RadioButtonMenuItem.setText(resourceBundle.getString("rotate90ReadInterfaceRadioButtonMenuItemText")); // NOI18N
        rotate90RadioButtonMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/90CW.png"))); // NOI18N
        rotate90RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotate90RadioButtonMenuItemActionPerformed(evt);
            }
        });
        rotateMenu.add(rotate90RadioButtonMenuItem);

        rotateButtonGroup.add(rotate180RadioButtonMenuItem);
        rotate180RadioButtonMenuItem.setText(resourceBundle.getString("rotateReverseReadInterfaceRadioButtonMenuItemText")); // NOI18N
        rotate180RadioButtonMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/180.png"))); // NOI18N
        rotate180RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotate180RadioButtonMenuItemActionPerformed(evt);
            }
        });
        rotateMenu.add(rotate180RadioButtonMenuItem);

        rotateButtonGroup.add(rotate270RadioButtonMenuItem);
        rotate270RadioButtonMenuItem.setText(resourceBundle.getString("rotate270ReadInterfaceRadioButtonMenuItemText")); // NOI18N
        rotate270RadioButtonMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/90CCW.png"))); // NOI18N
        rotate270RadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotate270RadioButtonMenuItemActionPerformed(evt);
            }
        });
        rotateMenu.add(rotate270RadioButtonMenuItem);

        displayReadInterfaceMenu.add(rotateMenu);

        doublePagesCheckBoxMenuItem.setText(resourceBundle.getString("twoPagesReadInterfaceCheckboxMenuItemText")); // NOI18N
        doublePagesCheckBoxMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/doublePage.png"))); // NOI18N
        doublePagesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doublePagesCheckBoxMenuItemActionPerformed(evt);
            }
        });
        displayReadInterfaceMenu.add(doublePagesCheckBoxMenuItem);

        readInterfacePopupMenu.add(displayReadInterfaceMenu);

        minimizeReadInterfaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, 0));
        minimizeReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/minimize.png"))); // NOI18N
        minimizeReadInterfaceMenuItem.setText(resourceBundle.getString("minimizeReadInterfaceMenuItemText")); // NOI18N
        minimizeReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        readInterfacePopupMenu.add(minimizeReadInterfaceMenuItem);

        exitReadInterfaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        exitReadInterfaceMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/readmenu/exitViewer.png"))); // NOI18N
        exitReadInterfaceMenuItem.setText(resourceBundle.getString("exitReadInterfaceMenuItemText")); // NOI18N
        exitReadInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitReadInterfaceMenuItemActionPerformed(evt);
            }
        });
        readInterfacePopupMenu.add(exitReadInterfaceMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setExtendedState(MAXIMIZED_BOTH);
        setUndecorated(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                readInterfaceKeyPressed(evt);
            }
        });

        readInterfaceScrollPane.setBorder(null);

        readComponent.setComponentPopupMenu(readInterfacePopupMenu);
        readComponent.setDoubleBuffered(true);
        readComponent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                readComponentMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout readComponentLayout = new org.jdesktop.layout.GroupLayout(readComponent);
        readComponent.setLayout(readComponentLayout);
        readComponentLayout.setHorizontalGroup(
            readComponentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 500, Short.MAX_VALUE)
        );
        readComponentLayout.setVerticalGroup(
            readComponentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 285, Short.MAX_VALUE)
        );

        readInterfaceScrollPane.setViewportView(readComponent);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(readInterfaceScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(readInterfaceScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void readInterfaceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readInterfaceKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            goNextImage(); // TODO: image plus large que l'écran
        } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            goPreviousImage(); // TODO: image plus large que l'écran
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            goPrevious10Image();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            goNext10Image();
        } else if (evt.getKeyCode() == KeyEvent.VK_HOME) {
            goFirstImage();
        } else if (evt.getKeyCode() == KeyEvent.VK_END) {
            goLastImage();
        } else if (evt.getKeyChar() == '-') {
            readComponent.zoomOut();
        } else if (evt.getKeyChar() == '+') {
            readComponent.zoomIn();
        } else if (evt.getKeyCode() == KeyEvent.VK_0) {
            readComponent.normalZoom();
        } else if (Locale.getDefault().getLanguage().equals(new Locale("fr").getLanguage()) && evt.getKeyCode() == KeyEvent.VK_Z) {
            rotate180RadioButtonMenuItem.setSelected(true);
            readComponent.rotateImage(180);
        } else if (Locale.getDefault().getLanguage().equals(new Locale("fr").getLanguage()) && evt.getKeyCode() == KeyEvent.VK_Q) {
            rotate90RadioButtonMenuItem.setSelected(true);
            readComponent.rotateImage(90);
        } else if (!Locale.getDefault().getLanguage().equals(new Locale("fr").getLanguage()) && evt.getKeyCode() == KeyEvent.VK_W) {
            rotate180RadioButtonMenuItem.setSelected(true);
            readComponent.rotateImage(180);
        } else if (!Locale.getDefault().getLanguage().equals(new Locale("fr").getLanguage()) && evt.getKeyCode() == KeyEvent.VK_A) {
            rotate90RadioButtonMenuItem.setSelected(true);
            readComponent.rotateImage(90);
        } else if (evt.getKeyCode() == KeyEvent.VK_D) {
            rotate270RadioButtonMenuItem.setSelected(true);
            readComponent.rotateImage(270);
        } else if (evt.getKeyCode() == KeyEvent.VK_S) {
            rotateInitialRadioButtonMenuItem.setSelected(true);
            readComponent.rotateImage(0);
        } else if (evt.getKeyCode() == KeyEvent.VK_1) {
            dualPageReadMode = false;
            doublePagesCheckBoxMenuItem.setSelected(false);
            readPageNbrImage();
        } else if (evt.getKeyCode() == KeyEvent.VK_2) {
            dualPageReadMode = true;
            doublePagesCheckBoxMenuItem.setSelected(true);
            readPageNbrImage();
        } else if (evt.getKeyCode() == KeyEvent.VK_H) {
            fitHorizontallyReadInterfaceCheckBoxMenuItem.setSelected(!fitHorizontallyReadInterfaceCheckBoxMenuItem.isSelected());
            readComponent.changeFitToScreenHorizontally();
        } else if (evt.getKeyCode() == KeyEvent.VK_V) {
            fitVerticallyReadInterfaceCheckBoxMenuItem.setSelected(!fitVerticallyReadInterfaceCheckBoxMenuItem.isSelected());
            readComponent.changeFitToScreenVertically();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exit();
        } else if (evt.getKeyCode() == KeyEvent.VK_M) {
            minimize();
        }
    }//GEN-LAST:event_readInterfaceKeyPressed

    private void readComponentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_readComponentMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1) {
            goNextImage();
        }
    }//GEN-LAST:event_readComponentMouseClicked

    private void exitReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitReadInterfaceMenuItemActionPerformed
        exit();
    }//GEN-LAST:event_exitReadInterfaceMenuItemActionPerformed

    private void minimizeReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeReadInterfaceMenuItemActionPerformed
        minimize();
    }//GEN-LAST:event_minimizeReadInterfaceMenuItemActionPerformed

    private void previousPageReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousPageReadInterfaceMenuItemActionPerformed
        goPreviousImage();
    }//GEN-LAST:event_previousPageReadInterfaceMenuItemActionPerformed

    private void nextPageReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageReadInterfaceMenuItemActionPerformed
        goNextImage();
    }//GEN-LAST:event_nextPageReadInterfaceMenuItemActionPerformed

    private void tenPagesBeforeReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenPagesBeforeReadInterfaceMenuItemActionPerformed
        goPrevious10Image();
    }//GEN-LAST:event_tenPagesBeforeReadInterfaceMenuItemActionPerformed

    private void tenPagesAfterReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenPagesAfterReadInterfaceMenuItemActionPerformed
        goNext10Image();
    }//GEN-LAST:event_tenPagesAfterReadInterfaceMenuItemActionPerformed

    private void firstPageReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstPageReadInterfaceMenuItemActionPerformed
        goFirstImage();
    }//GEN-LAST:event_firstPageReadInterfaceMenuItemActionPerformed

    private void lastPageReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastPageReadInterfaceMenuItemActionPerformed
        goLastImage();
    }//GEN-LAST:event_lastPageReadInterfaceMenuItemActionPerformed

    private void doublePagesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doublePagesCheckBoxMenuItemActionPerformed
        dualPageReadMode = !dualPageReadMode;
        readPageNbrImage();
    }//GEN-LAST:event_doublePagesCheckBoxMenuItemActionPerformed

    private void zoomDefaultReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomDefaultReadInterfaceMenuItemActionPerformed
        readComponent.normalZoom();
    }//GEN-LAST:event_zoomDefaultReadInterfaceMenuItemActionPerformed

    private void zoomInReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInReadInterfaceMenuItemActionPerformed
        readComponent.zoomIn();
    }//GEN-LAST:event_zoomInReadInterfaceMenuItemActionPerformed

    private void zoomOutReadInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutReadInterfaceMenuItemActionPerformed
        readComponent.zoomOut();
    }//GEN-LAST:event_zoomOutReadInterfaceMenuItemActionPerformed

    private void fitHorizontallyReadInterfaceCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitHorizontallyReadInterfaceCheckBoxMenuItemActionPerformed
        readComponent.changeFitToScreenHorizontally();
    }//GEN-LAST:event_fitHorizontallyReadInterfaceCheckBoxMenuItemActionPerformed

    private void fitVerticallyReadInterfaceCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitVerticallyReadInterfaceCheckBoxMenuItemActionPerformed
        readComponent.changeFitToScreenVertically();
    }//GEN-LAST:event_fitVerticallyReadInterfaceCheckBoxMenuItemActionPerformed

    private void rotateInitialRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateInitialRadioButtonMenuItemActionPerformed
        readComponent.rotateImage(0);
    }//GEN-LAST:event_rotateInitialRadioButtonMenuItemActionPerformed

    private void rotate90RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotate90RadioButtonMenuItemActionPerformed
        readComponent.rotateImage(90);
    }//GEN-LAST:event_rotate90RadioButtonMenuItemActionPerformed

    private void rotate180RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotate180RadioButtonMenuItemActionPerformed
        readComponent.rotateImage(180);
    }//GEN-LAST:event_rotate180RadioButtonMenuItemActionPerformed

    private void rotate270RadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotate270RadioButtonMenuItemActionPerformed
        readComponent.rotateImage(270);
    }//GEN-LAST:event_rotate270RadioButtonMenuItemActionPerformed
    
    private void exit() {
        this.setVisible(false);
        this.dispose();
    }
    
    private void minimize() {
        // TODO: la restauration des fenetres doit aussi pouvoir se faire via un clic dans le dock
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/Application").getString("appLogoIcon"))).getImage(), java.util.ResourceBundle.getBundle("booknaviger/resources/Application").getString("appTitle"));
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MainInterface.getInstance().setVisible(true);
                    setVisible(true);
                    toFront();
                    SystemTray sysTray = SystemTray.getSystemTray();
                    sysTray.remove(sysTray.getTrayIcons()[0]);
                }
            });
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception with the creation of the systray", ex);
            }
            this.setVisible(false);
            MainInterface.getInstance().setVisible(false);
        } else {
            this.setExtendedState(javax.swing.JFrame.ICONIFIED);
            MainInterface.getInstance().setExtendedState(javax.swing.JFrame.ICONIFIED);
        }
    }
    
    public void goFirstImage() {
        pageNbr = 1;
        readPageNbrImage();
    }
    
    private void goLastImage() {
        pageNbr = imageHandler.getNbrOfPages();
        readPageNbrImage();
    }
    
    public void goNextImage() {
        if (dualPageReadMode) {
            pageNbr += 2;
        } else {
            pageNbr++;
        }
        if (!readPageNbrImage()) {
            if (dualPageReadMode) {
                pageNbr = imageHandler.getNbrOfPages() - 1;
            } else {
                pageNbr--;
            }
            readPageNbrImage();
            readComponent.setLastPageReached();
        }
    }
    
    public void goPreviousImage() {
        if (dualPageReadMode) {
            pageNbr -= 2;
        } else {
            pageNbr--;
        }
        if (!readPageNbrImage()) {
            goFirstImage();
            readComponent.setFirstPageReached();
        }
    }
    
    private void goNext10Image() {
        pageNbr += 10;
        if (!readPageNbrImage()) {
            goLastImage();
        }
    }
    
    private void goPrevious10Image() {
        pageNbr -= 10;
        if (!readPageNbrImage()) {
            goFirstImage();
        }
    }
    
    private boolean readPageNbrImage() {
        if (!imageHandler.isImageInRange(pageNbr)) {
            return false;
        }
        if (dualPageReadMode && !imageHandler.isImageInRange(pageNbr+1)) {
            return false;
        }
        readComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Ended in readComponent.setImage(...)
        BufferedImage readImage;
        if (dualPageReadMode) {
            readImage = ImageReader.combine2Images(imageHandler.getImage(pageNbr), imageHandler.getImage(pageNbr+1));
        } else {
            readImage = imageHandler.getImage(pageNbr);
        }
        if (readImage == null) {
            readImage = new ImageReader(new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/ReadComponent").getString("no_image"))).getImage()).convertImageToBufferedImage();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "can't read image");
        }
        readComponent.setImage(readImage, true, readInterfaceScrollPane);
        // TODO: preload next page
        return true;
    }

    public JScrollPane getReadInterfaceScrollPane() {
        return readInterfaceScrollPane;
    }

    public ReadComponent getReadComponent() {
        return readComponent;
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu displayReadInterfaceMenu;
    private javax.swing.JCheckBoxMenuItem doublePagesCheckBoxMenuItem;
    private javax.swing.JMenuItem exitReadInterfaceMenuItem;
    private javax.swing.JMenuItem firstPageReadInterfaceMenuItem;
    private javax.swing.JCheckBoxMenuItem fitHorizontallyReadInterfaceCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem fitVerticallyReadInterfaceCheckBoxMenuItem;
    private javax.swing.JMenuItem lastPageReadInterfaceMenuItem;
    private javax.swing.JMenuItem minimizeReadInterfaceMenuItem;
    private javax.swing.JMenu navigationReadInterfaceMenu;
    private javax.swing.JPopupMenu.Separator navigationReadInterfaceSeparator1;
    private javax.swing.JPopupMenu.Separator navigationReadInterfaceSeparator2;
    private javax.swing.JMenuItem nextPageReadInterfaceMenuItem;
    private javax.swing.JMenuItem previousPageReadInterfaceMenuItem;
    private booknaviger.readinterface.ReadComponent readComponent;
    private javax.swing.JPopupMenu readInterfacePopupMenu;
    private javax.swing.JScrollPane readInterfaceScrollPane;
    private javax.swing.JRadioButtonMenuItem rotate180RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem rotate270RadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem rotate90RadioButtonMenuItem;
    private javax.swing.ButtonGroup rotateButtonGroup;
    private javax.swing.JRadioButtonMenuItem rotateInitialRadioButtonMenuItem;
    private javax.swing.JMenu rotateMenu;
    private javax.swing.JMenuItem tenPagesAfterReadInterfaceMenuItem;
    private javax.swing.JMenuItem tenPagesBeforeReadInterfaceMenuItem;
    private javax.swing.JMenuItem zoomDefaultReadInterfaceMenuItem;
    private javax.swing.JMenuItem zoomInReadInterfaceMenuItem;
    private javax.swing.JMenu zoomMenu;
    private javax.swing.JMenuItem zoomOutReadInterfaceMenuItem;
    private javax.swing.JPopupMenu.Separator zoomSeparator;
    // End of variables declaration//GEN-END:variables
}
