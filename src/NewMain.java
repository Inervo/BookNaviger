
import java.awt.EventQueue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;

/*
 */

/**
 *
 * @author Inervo
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final JFXPanel fxPanel = new JFXPanel();
        fxPanel.setVisible(true);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                final NewClass newClass = new NewClass();
                newClass.show();
            }
        });
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                NewClass newClass = new NewClass();
//                newClass.setVisible(true);
//            }
//        });
    }
    
    public void launchthebullshit() {
        
    }
    
}
