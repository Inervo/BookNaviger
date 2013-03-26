/*
 */

package booknaviger.inet.htmlreport;

import booknaviger.MainInterface;
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
    public GenerateReport(final boolean advancedMode, final File baseDir) {
        SwingUtilities.invokeLater(new Thread() {

            @Override
            public void run() {
                GenerationProgress generationProgressDialog = new GenerationProgress(MainInterface.getInstance(), false);
                generationProgressDialog.setVisible(true);
                HTMLReporter htmlReporter = new HTMLReporter(advancedMode, baseDir, generationProgressDialog);
                generationProgressDialog.setHtmlReport(htmlReporter);
                htmlReporter.execute();
            }
        });
    }

}
