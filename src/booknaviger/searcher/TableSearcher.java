/*
 */

package booknaviger.searcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Search in a JTable the value of the key pressed
 * @author Inervo
 */
public class TableSearcher {
    private final JTextField searchField = new JTextField();
    private final JTable table;
    private final JDialog parentJDialog;
    private TableRowSorter tableRowSorter;
    
    
    /**
     * Constructor. Set the table and the parent dialog
     * @param table The table in which we will search
     * @param parentJDialog the parent jdialog (or null) to use
     */
    public TableSearcher(JTable table, JDialog parentJDialog) {
        Logger.getLogger(TableSearcher.class.getName()).entering(TableSearcher.class.getName(), "TableSearcher", new Object[] {table, parentJDialog});
        this.table = table;
        this.parentJDialog = parentJDialog;
        Logger.getLogger(TableSearcher.class.getName()).exiting(TableSearcher.class.getName(), "TableSearcher");
    }
    
    /**
     * Create and activate the quick search feature on the jtable
     */
    public void activateQuickSearch() {
        Logger.getLogger(TableSearcher.class.getName()).entering(TableSearcher.class.getName(), "activateQuickSearch");
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent evt) {
                Logger.getLogger(TableSearcher.class.getName()).entering(TableSearcher.class.getName(), "QuickSearchListener.keyPressed");
                char character = evt.getKeyChar();
                if (!Character.isLetterOrDigit(character) || evt.isMetaDown() || evt.isAltDown() || evt.isAltGraphDown() || evt.isControlDown()) {
                    Logger.getLogger(TableSearcher.class.getName()).exiting(TableSearcher.class.getName(), "QuickSearchListener.keyPressed");
                    return;
                }
                searchField.setText(String.valueOf(character));
                final Search searchClass = new Search();
                final JDialog jdialog = new JDialog((parentJDialog != null) ? parentJDialog : null);
                jdialog.setUndecorated(true);
                jdialog.setSize(150, 20);
                jdialog.setLocation(table.getTableHeader().getLocationOnScreen());
                jdialog.add(searchField);
                searchClass.search();
                searchField.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        searchClass.search();
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        searchClass.search();
                    }

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        searchClass.search();
                    }
                });
                searchField.addFocusListener(new FocusListener() {

                    @Override
                    public void focusGained(final FocusEvent e) {
                        searchField.setCaretPosition(searchField.getText().length());
                    }

                    @Override
                    public void focusLost(final FocusEvent e) {
                        jdialog.dispose();
                    }
                });
                AbstractAction exit = new AbstractAction() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        jdialog.dispose();
                    }
                };
                searchField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
                searchField.getActionMap().put("exit", exit);
                searchField.setAction(exit);
                jdialog.setVisible(true);
                Logger.getLogger(TableSearcher.class.getName()).exiting(TableSearcher.class.getName(), "QuickSearchListener.keyPressed");
            }
        });
        TableModel tableModel = table.getModel();
        tableRowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(tableRowSorter);
        Logger.getLogger(TableSearcher.class.getName()).entering(TableSearcher.class.getName(), "activateQuickSearch");
    }
    
    /**
     * Search class which search the entered text (in {@link TableSearcher#searchField}) into the table, and the select the row
     * containing this value. The search is compotible with regex
     */
    class Search {

            void search() {
                Logger.getLogger(Search.class.getName()).entering(Search.class.getName(), "search");
                String text = searchField.getText();
                tableRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                Pattern pattern = Pattern.compile("(?i)" + text);
                for (int row = 0; row < table.getRowCount(); row++) {
                    String value = table.getValueAt(row, 0).toString().toLowerCase();
                    Matcher matcher = pattern.matcher(value.toLowerCase());
                    if (matcher.find()) {
                        searchField.setForeground(Color.BLACK);
                        table.changeSelection(row, 0, false, false);
                        Logger.getLogger(Search.class.getName()).exiting(Search.class.getName(), "search");
                        return;
                    }
                }
                if (table.getRowCount() == 0) {
                    searchField.setForeground(Color.RED);
                }
                Logger.getLogger(Search.class.getName()).exiting(Search.class.getName(), "search");
            }
        }

}
