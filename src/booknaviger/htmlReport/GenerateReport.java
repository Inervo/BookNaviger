/*
 */

package booknaviger.htmlReport;

import booknaviger.BookNavigerView;
import java.io.File;
import javax.swing.SwingUtilities;

/**
 * @author Inervo
 *
 */
public class GenerateReport {

    /**
     * Génère un rapport html
     * @param advancedMode Mode simple vs. advanced
     * @param baseDir Dossier contenant les séries
     * @param bnv Instance de BookNavigerView
     */
    public GenerateReport(final boolean advancedMode, final File baseDir, final BookNavigerView bnv) {
        SwingUtilities.invokeLater(new Thread() {

            @Override
            public void run() {
                GenerationProgress gp = new GenerationProgress(bnv, false);
                gp.setVisible(true);
                CreateHTMLReport chr = new CreateHTMLReport(advancedMode, baseDir, bnv.getFrame(), gp);
                gp.setChr(chr);
                chr.execute();
            }
        });
    }

}
