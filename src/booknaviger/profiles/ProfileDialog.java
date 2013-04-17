/*
 */
package booknaviger.profiles;

import booknaviger.MainInterface;
import booknaviger.booksfolder.BooksFolderSelector;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

/**
 * Class to the dialog to add / remove / modify the profiles
 * @author Inervo
 */
public class ProfileDialog extends javax.swing.JDialog {
    
    private ResourceBundle resourceBundle = java.util.ResourceBundle.getBundle("booknaviger/resources/ProfileDialog");

    /**
     * Creates new form ProfileDialog
     */
    public ProfileDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        Logger.getLogger(ProfileDialog.class.getName()).entering(ProfileDialog.class.getName(), "ProfileDialog", new Object[] {parent, modal});
        initComponents();
        final DefaultTableModel dtm = (DefaultTableModel) profileTable.getModel();
        for (int i = 0; i < MainInterface.getInstance().getProfiles().getProfilesCount(); i++) {
            dtm.addRow(new String[] {MainInterface.getInstance().getProfiles().getProfilesNames()[i], MainInterface.getInstance().getProfiles().getProfilesFolders()[i]});
        }
        Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "ProfileDialog");
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newProfileButton = new javax.swing.JButton();
        deleteProfileButton = new javax.swing.JButton();
        profileScrollPane = new javax.swing.JScrollPane();
        profileTable = new javax.swing.JTable();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(resourceBundle.getString("profileDialogString")); // NOI18N

        newProfileButton.setText(resourceBundle.getString("newProfileButtonString")); // NOI18N
        newProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProfileButtonActionPerformed(evt);
            }
        });

        deleteProfileButton.setText(resourceBundle.getString("deleteProfileButtonString")); // NOI18N
        deleteProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteProfileButtonActionPerformed(evt);
            }
        });

        profileTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Folder"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        profileTable.setFillsViewportHeight(true);
        profileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                profileTableMouseClicked(evt);
            }
        });
        profileScrollPane.setViewportView(profileTable);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(okButton)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(71, 71, 71)
                .add(newProfileButton)
                .add(18, 18, 18)
                .add(deleteProfileButton)
                .addContainerGap(66, Short.MAX_VALUE))
            .add(profileScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(newProfileButton)
                    .add(deleteProfileButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(profileScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(okButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * The new profile buton is pushed. Add a row in the table and select it to modify the text
     * @param evt The event associated
     */
    private void newProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProfileButtonActionPerformed
        Logger.getLogger(ProfileDialog.class.getName()).entering(ProfileDialog.class.getName(), "newProfileButtonActionPerformed");
        DefaultTableModel dtm = (DefaultTableModel) profileTable.getModel();
        String profileName = "profile " + profileTable.getRowCount();
        dtm.addRow(new String[] {profileName, ""});
        profileTable.requestFocusInWindow();
        profileTable.editCellAt(profileTable.getRowCount() - 1, 0);
        if (profileTable.getEditorComponent() instanceof JTextComponent) {
            ((JTextComponent)profileTable.getEditorComponent()).requestFocus();
            ((JTextComponent)profileTable.getEditorComponent()).selectAll();
        }
        Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "newProfileButtonActionPerformed");
    }//GEN-LAST:event_newProfileButtonActionPerformed

    /**
     * The delete profile buton is pushed. Delete the selected row in the table
     * @param evt The event associated
     */
    private void deleteProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteProfileButtonActionPerformed
        Logger.getLogger(ProfileDialog.class.getName()).entering(ProfileDialog.class.getName(), "deleteProfileButtonActionPerformed");
        int selectedRow = profileTable.getSelectedRow();
        if (selectedRow == -1) {
            Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "deleteProfileButtonActionPerformed");
            return;
        }
        if (profileTable.getRowCount() == 1) {
            Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "deleteProfileButtonActionPerformed");
            return;
        }
        DefaultTableModel dtm = (DefaultTableModel) profileTable.getModel();
        dtm.removeRow(selectedRow);
        Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "deleteProfileButtonActionPerformed");
    }//GEN-LAST:event_deleteProfileButtonActionPerformed

    /**
     * The OK button is pressed. All the changes are saved
     * @param evt the event associated
     */
    @SuppressWarnings("unchecked")
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        Logger.getLogger(ProfileDialog.class.getName()).entering(ProfileDialog.class.getName(), "okButtonActionPerformed");
        DefaultTableModel dtm = (DefaultTableModel) profileTable.getModel();
        MainInterface.getInstance().getProfiles().setProfiles(dtm.getDataVector());
        setVisible(false);
        dispose();
        Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "okButtonActionPerformed");
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * A double click is performed on the path column of the table. Open the {@link booknaviger.booksfolder.BooksFolderSelector} to select the folder of the profile
     * @param evt  the event associated
     * @see BooksFolderSelector
     */
    private void profileTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_profileTableMouseClicked
        Logger.getLogger(ProfileDialog.class.getName()).entering(ProfileDialog.class.getName(), "profileTableMouseClicked");
        if (evt.getClickCount() == 2) {
            if (profileTable.getSelectedColumn() == 1) {
                BooksFolderSelector booksFolderselector = new BooksFolderSelector(null, true);
                String selectedFolder = booksFolderselector.selectFolder();
                if (selectedFolder != null) {
                    profileTable.getModel().setValueAt(selectedFolder, profileTable.getSelectedRow(), profileTable.getSelectedColumn());
                }
                evt.consume();
            }
        }
        Logger.getLogger(ProfileDialog.class.getName()).exiting(ProfileDialog.class.getName(), "profileTableMouseClicked");
    }//GEN-LAST:event_profileTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteProfileButton;
    private javax.swing.JButton newProfileButton;
    private javax.swing.JButton okButton;
    private javax.swing.JScrollPane profileScrollPane;
    private javax.swing.JTable profileTable;
    // End of variables declaration//GEN-END:variables
}
