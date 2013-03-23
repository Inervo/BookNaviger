/*
 */

package booknaviger.searcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Inervo
 *
 */
public class TableSearcher {
    private final JTextField searchField = new JTextField();
    private final JTable table;
    private final JDialog parentJDialog;
    
    public TableSearcher(JTable table, JDialog parentJDialog) {
        this.table = table;
        this.parentJDialog = parentJDialog;
    }
    
    public void activateQuickSearch() {
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent evt) {
                char character = evt.getKeyChar();
                if (!Character.isLetterOrDigit(character) || evt.isMetaDown() || evt.isAltDown() || evt.isAltGraphDown() || evt.isControlDown()) {
                    return;
                }
                searchField.setText(searchField.getText() + String.valueOf(character));
                final Search searchClass = new Search();
                final JDialog jdialog = new JDialog((parentJDialog != null) ? parentJDialog : null);
                jdialog.setUndecorated(true);
                jdialog.setSize(150, 20);
                jdialog.setLocation(table.getTableHeader().getLocationOnScreen());
                jdialog.add(searchField);
                jdialog.setVisible(true);
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
                        searchField.setText(null);
                    }
                });
                AbstractAction exit = new AbstractAction() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        jdialog.dispose();
                        searchField.setText(null);
                    }
                };
                searchField.setAction(exit);
                searchField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
                searchField.getActionMap().put("exit", exit);
            }
        });
    }
    
    class Search {

            void search() {
                String text = searchField.getText();
                if (text.length() == 0) {
                    return;
                }
                    Pattern pattern = Pattern.compile(text);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        String value = table.getValueAt(row, 0).toString().toLowerCase();
                        Matcher matcher = pattern.matcher(value.toLowerCase());
                        if (matcher.find()) {
                            searchField.setForeground(Color.BLACK);
                            table.changeSelection(row, 0, false, false);
                            return;
                        }
                        searchField.setForeground(Color.RED);
                    }
            }
        }

}
