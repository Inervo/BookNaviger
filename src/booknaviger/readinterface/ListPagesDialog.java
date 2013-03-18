/*
 */
package booknaviger.readinterface;

import booknaviger.searcher.TableSearcher;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Inervo
 */
public class ListPagesDialog extends javax.swing.JDialog {
    ReadInterface readInterface = null;

    /**
     * Creates new form ListPagesDialog
     * @param readInterface 
     */
    public ListPagesDialog(ReadInterface readInterface) {
        super(readInterface, true);
        this.readInterface = readInterface;
        initComponents();
    }
    
    /**
     *
     * @param pagesName
     * @param currentPage
     */
    protected void fillPagesName(final List<String> pagesName, final int currentPage) {
        final DefaultTableModel dtm = (DefaultTableModel) pagesListTable.getModel();
        List<Thread> rows = new ArrayList<>();
        for (int i = 0; i < pagesName.size(); i++) {
            final int index = i;
            Thread tampon = new Thread(new Runnable() {

                @Override
                public void run() {
                    dtm.addRow(new String[] {pagesName.get(index)});
                }
            });
            SwingUtilities.invokeLater(tampon);
            rows.add(tampon);
        }
        for (Thread thread : rows) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ListPagesDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                pagesListTable.getSelectionModel().setSelectionInterval(currentPage, currentPage);
                Rectangle cellRect = pagesListTable.getCellRect(currentPage, 0, true);
                cellRect.y += 1;
                pagesListTable.scrollRectToVisible(cellRect);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pagesListScrollPane = new javax.swing.JScrollPane();
        pagesListTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pagesListTable.setBackground(new java.awt.Color(0, 0, 0));
        pagesListTable.setForeground(new java.awt.Color(255, 255, 255));
        pagesListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pages"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        pagesListTable.setFillsViewportHeight(true);
        pagesListTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pagesListTable.setShowHorizontalLines(false);
        pagesListTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pagesListTableMouseClicked(evt);
            }
        });
        pagesListTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pagesListTableKeyPressed(evt);
            }
        });
        pagesListScrollPane.setViewportView(pagesListTable);
        new TableSearcher(pagesListTable, this).activateQuickSearch();

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pagesListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pagesListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pagesListTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pagesListTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            goReadSelectedPage();
        }
    }//GEN-LAST:event_pagesListTableKeyPressed

    private void pagesListTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pagesListTableMouseClicked
        if (evt.getClickCount() == 2) {
            goReadSelectedPage();
        }
    }//GEN-LAST:event_pagesListTableMouseClicked

    private void goReadSelectedPage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                readInterface.goPage(pagesListTable.getSelectionModel().getMinSelectionIndex() + 1);
            }
        }).start();
        this.dispose();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane pagesListScrollPane;
    private javax.swing.JTable pagesListTable;
    // End of variables declaration//GEN-END:variables
}
