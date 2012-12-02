/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package booknaviger;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author Inervo
 */
public class BorderReadComponent extends JComponent {

    private Color backgroundColor;

    /**
     * Constructeur, ne fait rien
     */
    public BorderReadComponent() {
    }

    /**
     * Défini la nouvelle couleur du component (soit de la bordure de la bd)
     * @param newColor la nouvelle couleur de la bordure
     */
    public void setNewBackgroundColor(final Color newColor) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                backgroundColor = newColor;
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(backgroundColor);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

}
