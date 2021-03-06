/*
 */
package booknaviger.exceptioninterface;

import java.awt.Frame;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Info interface to show information / warning / error messages to the user
 * @author Inervo
 */
public class InfoInterface extends javax.swing.JDialog {

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("booknaviger/resources/InfoInterface");

    /**
     * Constructor. Everything is done here.
     * @param infoLevel The info level {@link #INFO}, {@link #WARNING} or {@link #ERROR}
     * @param infoMessage The message which will be get from the properties of this class
     * @param parameters The optional parameters which must be set with the message
     */
    public InfoInterface(InfoLevel infoLevel, String infoMessage, Object... parameters) {
        super((Frame)null, false);
        Logger.getLogger(InfoInterface.class.getName()).entering(InfoInterface.class.getName(), "InfoInterface", new Object[] {infoLevel, infoMessage, parameters});
        initComponents();
        this.setLocationRelativeTo(this.getParent());
        setInfo(infoLevel, infoMessage, parameters);
        setVisible(true);
        requestFocus();
        okButton.requestFocusInWindow();
        Logger.getLogger(InfoInterface.class.getName()).exiting(InfoInterface.class.getName(), "InfoInterface");
    }
    
    public static final class InfoLevel {
        public static final InfoLevel INFO = new InfoLevel("INFO", 0);
        public static final InfoLevel WARNING = new InfoLevel("WARNING", 1);
        @SuppressWarnings("FieldNameHidesFieldInSuperclass")
        public static final InfoLevel ERROR = new InfoLevel("ERROR", 2);
        
        private final String levelName;
        private final int levelValue;
        

        protected InfoLevel(String levelName, int levelValue) {
            this.levelName = levelName;
            this.levelValue = levelValue;
        }
        
        /**
        * Get the integer value for this level.  This integer value
        * can be used for efficient ordering comparisons between
        * Level objects.
        * @return the integer value for this level.
        */
       public final int intValue() {
           return levelValue;
       }
       
       /**
        * Get the String value for this level.  This String value
        * can be used for understanding the meaning of the
        * Level objects.
        * @return the String value for this level.
        */
       public final String stringValue() {
           return levelName;
       }
        
    }

    /**
     * Set the information of this event
     * @param infoLevel The info level {@link #INFO}, {@link #WARNING} or {@link #ERROR}
     * @param infoMessage The message which will be get from the properties of this class
     * @param parameters The optional parameters which must be set with the message
     */
    private void setInfo(InfoLevel infoLevel, String infoMessage, Object... parameters) {
        Logger.getLogger(InfoInterface.class.getName()).entering(InfoInterface.class.getName(), "InfoInterface", new Object[] {infoLevel, infoMessage, parameters});
        switch (infoLevel.stringValue()) {
            case "INFO":
                setTitle(resourceBundle.getString("infoInterface.info.title"));
                logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/infologos/info.png")));
                break;
            case "WARNING":
                setTitle(resourceBundle.getString("infoInterface.warning.title"));
                logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/infologos/warning.png")));
                break;
            case "ERROR":
                setTitle(resourceBundle.getString("infoInterface.error.title"));
                logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/booknaviger/resources/graphics/infologos/error.png")));
                break;
        }
        infoLabel.setText("<html>" + resourceBundle.getString(infoMessage + ".title") + "</html>");
        if (parameters == null || parameters.length == 0) {
            messageLabel.setText("<html>" + resourceBundle.getString(infoMessage + ".message") + "</html>");
        } else {
            messageLabel.setText("<html>" + MessageFormat.format(resourceBundle.getString(infoMessage + ".message"), parameters) + "</html>");
        }
        Logger.getLogger(InfoInterface.class.getName()).exiting(InfoInterface.class.getName(), "InfoInterface");
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modeButtonGroup = new javax.swing.ButtonGroup();
        logoLabel = new javax.swing.JLabel();
        infoLabel = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        logoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logoLabel.setFocusable(false);
        logoLabel.setRequestFocusEnabled(false);

        infoLabel.setFocusable(false);
        infoLabel.setRequestFocusEnabled(false);

        messageLabel.setFocusable(false);
        messageLabel.setRequestFocusEnabled(false);

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
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(logoLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(infoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton))
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(logoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                    .add(infoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(okButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event triggered when the ok button is selected
     * @param evt the event associated
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        Logger.getLogger(InfoInterface.class.getName()).exiting(InfoInterface.class.getName(), "okButtonActionPerformed");
        setVisible(false);
        dispose();
        Logger.getLogger(InfoInterface.class.getName()).exiting(InfoInterface.class.getName(), "okButtonActionPerformed");
    }//GEN-LAST:event_okButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.ButtonGroup modeButtonGroup;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
