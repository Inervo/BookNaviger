/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ReadPanel.java
 *
 * Created on 13 oct. 2009, 13:48:48
 */

package booknaviger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Inervo
 */
public class ReadPanel extends JPanel implements Scrollable {

    boolean showFirstPage = false;
    boolean showLastPage = false;
    ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(StaticWorld.class);

    /** Creates new form BeanForm */
    public ReadPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setInheritsPopupMenu(true);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (getParent() instanceof JViewport) {
		return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
	}
	return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
		return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
	}
	return false;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        String text = null;
        Graphics2D g2d = (Graphics2D) g.create();
        Font font = new Font(g2d.getFont().getName(), Font.BOLD, 15);
        FontRenderContext frc = new FontRenderContext(null, false, false);
        g2d.setFont(font);
        int fontWidth = 0;
        if (showLastPage) {
            text = resourceMap.getString("lastPage.text");
            fontWidth = (int) font.getStringBounds(text, frc).getWidth();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(5, 5, fontWidth + 10, 20);
            g2d.fillRect(this.getWidth() - fontWidth - 15, 5, fontWidth + 10, 20);
            g2d.fillRect(5, this.getHeight() - 25, fontWidth + 10, 20);
            g2d.fillRect(this.getWidth() - fontWidth - 15, this.getHeight() - 25, fontWidth + 10, 20);
            g2d.setColor(Color.YELLOW);
            g2d.drawString(text, 10, 20);
            g2d.drawString(text, this.getWidth() - fontWidth - 10, 20);
            g2d.drawString(text, 10, this.getHeight() - 10);
            g2d.drawString(text, this.getWidth() - fontWidth - 10, this.getHeight() - 10);
        }
        if (showFirstPage) {
            text = resourceMap.getString("firstPage.text");
            fontWidth = (int) font.getStringBounds(text, frc).getWidth();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(5, 5, fontWidth + 10, 20);
            g2d.fillRect(this.getWidth() - fontWidth - 15, 5, fontWidth + 10, 20);
            g2d.fillRect(5, this.getHeight() - 25, fontWidth + 10, 20);
            g2d.fillRect(this.getWidth() - fontWidth - 15, this.getHeight() - 25, fontWidth + 10, 20);
            g2d.setColor(Color.YELLOW);
            g2d.drawString(text, 10, 20);
            g2d.drawString(text, this.getWidth() - fontWidth - 10, 20);
            g2d.drawString(text, 10, this.getHeight() - 10);
            g2d.drawString(text, this.getWidth() - fontWidth - 10, this.getHeight() - 10);
        }
        g2d.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
