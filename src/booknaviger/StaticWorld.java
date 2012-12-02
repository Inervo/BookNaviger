/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package booknaviger;

import booknaviger.errorhandler.ErrorBox;
import booknaviger.errorhandler.ErrorOutputStream;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Inervo
 */
public class StaticWorld {

    /**
     * Crée un champ de recherche sur une jtable
     * @param table table sur laquelle chercher le texte
     * @param usePattern utilisation des pattern
     * @param parent fenêtre parente
     */
    public static void setQuickSearch(final JTable table, final boolean usePattern, final JDialog parent) {
        final JTextField searchField = new JTextField();
        class Search {

            void search() {
                String text = searchField.getText();
                if (text.length() == 0) {
                    return;
                }
                if (usePattern) {
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
                } else {
                    for (int row = 0; row < table.getRowCount(); row++) {
                        String value = table.getValueAt(row, 0).toString();
                        if (value.toLowerCase().startsWith(text.toLowerCase())) {
                            searchField.setForeground(Color.BLACK);
                            table.changeSelection(row, 0, false, false);
                            return;
                        }
                    }
                    searchField.setForeground(Color.RED);
                }
            }
        }
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent evt) {
                char ch = evt.getKeyChar();
                if (!Character.isLetterOrDigit(ch) || evt.isMetaDown()) {
                    return;
                }
                searchField.setText(searchField.getText() + String.valueOf(ch));
                final Search s = new Search();
                final JDialog d = new JDialog((parent != null) ? parent : null);
                d.setUndecorated(true);
                d.setSize(150, 20);
                d.setLocation(table.getTableHeader().getLocationOnScreen());
                ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(StaticWorld.class);
                final JLabel lb = new JLabel(resourceMap.getString("TableSearcher.text"));
                d.add(lb, BorderLayout.LINE_START);
                d.add(searchField);
                d.setVisible(true);
                searchField.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        s.search();
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        s.search();
                    }

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        s.search();
                    }
                });
                searchField.addFocusListener(new FocusListener() {

                    @Override
                    public void focusGained(final FocusEvent e) {
                        searchField.setCaretPosition(searchField.getText().length());
                    }

                    @Override
                    public void focusLost(final FocusEvent e) {
                        d.dispose();
                        searchField.setText(null);
                    }
                });
                AbstractAction exit = new AbstractAction() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        d.dispose();
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

    /**
     * Retourne si le type de fichier est une image
     * @param name nom du fichier
     * @return true si image, false sinon
     */
    public static boolean typeIsImage(String name) {
        if (typeSupportToolkit(name) || typeSupportImageIO(name)) {
            return true;
        }
        return false;
    }

    /**
     * Image supporte chargement par toolkit
     * @param name nom du fichier
     * @return true si supporte, false sinon
     */
    public static boolean typeSupportToolkit(String name) {
        name = name.toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".png")) {
            return true;
        }
        return false;
    }

    /**
     * Image supporte chargement par ImageIO
     * @param name nom du fichier
     * @return true si supporte, false sinon
     */
    public static boolean typeSupportImageIO(String name) {
        name.toLowerCase();
        if (name.endsWith(".bmp")) {
            return true;
        }
        return false;
    }

    /**
     * Enregistre la redirection de sortie d'erreur
     */
    public static void registerErrorDialogWindow() {
        final ErrorBox eb = new ErrorBox();
        ErrorOutputStream jtaos = new ErrorOutputStream(eb, "error.log");
        System.setErr(new PrintStream(jtaos));
    }

    /**
     * Retourne la taille d'un dossier
     * @param folder dossier racine
     * @return long de la taille du dossier
     */
    public static long getFolderSizeAsLong(File folder) {
            long foldersize = 0;

            File[] filelist = folder.listFiles();
            for (int i = 0; i < filelist.length; i++) {
                if (filelist[i].isDirectory())
                    foldersize += getFolderSizeAsLong(filelist[i]);
                else
                    foldersize += filelist[i].length();
            }
            return foldersize;
    }

    /**
     * Retourne la taille d'un dossier
     * @param folder dossier racine
     * @return String formatée de la taille du dossier
     */
    public static String getFolderSize(File folder) {
        double size = getFolderSizeAsLong(folder);
        ResourceMap rm = Application.getInstance().getContext().getResourceMap(StaticWorld.class);

        DecimalFormat df =new DecimalFormat("#.##");
        String sizeString = df.format(size / (1024*1024));
        if (Double.valueOf(sizeString.replace(',', '.')) > 1024)
            return df.format(size / (1024*1024*1024)) + " " + rm.getString("GB.text");
        return sizeString + " " + rm.getString("MB.text");
    }

    /**
     * Compare deux numéros de version du format 88.88.88
     * @param firstVer premier numéro de version
     * @param secondVer deuxième numéro de version
     * @return true si firstVer plus petit que secondVer, false sinon
     */
    public static boolean compareVersions2GT1(String firstVer, String secondVer) {
        int major1 = 0;
        int major2 = 0;
        int minor1 = 0;
        int minor2 = 0;
        int build1 = 0;
        int build2 = 0;

        if (firstVer == null || secondVer == null)
            return false;
        StringTokenizer st1 = new StringTokenizer(firstVer, ".");
        StringTokenizer st2 = new StringTokenizer(secondVer, ".");
        if (!st1.hasMoreTokens() || !st2.hasMoreTokens())
            return false;
        major1 = Integer.parseInt(st1.nextToken());
        major2 = Integer.parseInt(st2.nextToken());
        if (major2 > major1)
            return true;
        if (major1 > major2)
            return false;
        if (!st1.hasMoreTokens() && st2.hasMoreTokens())
            return true;
        if (!st1.hasMoreTokens() || !st2.hasMoreTokens())
            return false;
        minor1 = Integer.parseInt(st1.nextToken());
        minor2 = Integer.parseInt(st2.nextToken());
        if (minor2 > minor1)
            return true;
        if (minor1 > minor2)
            return false;
        if (!st1.hasMoreTokens() && st2.hasMoreTokens())
            return true;
        if (!st1.hasMoreTokens() || !st2.hasMoreTokens())
            return false;
        build1 = Integer.parseInt(st1.nextToken());
        build2 = Integer.parseInt(st2.nextToken());
        if (build2 > build1)
            return true;
        return false;
    }

}
