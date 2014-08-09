/*
 */

package booknaviger.readinterface;

import booknaviger.picturehandler.AbstractImageHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author Inervo
 */
public class FXReadInterface extends Application {
    final SwingNode swingNode = new SwingNode();
    private AbstractImageHandler abstractImageHandler;
    public ReadInterfacePane rip;
    public static FXReadInterface INSTANCE;

    public FXReadInterface() {
        INSTANCE = this;
    }
    
    public void setImageHandler(AbstractImageHandler abstractImageHandler) {
        this.abstractImageHandler = abstractImageHandler;
    }
    
    @Override
    public void start(Stage primaryStage) {
//        createAndSetSwingContent();

        StackPane pane = new StackPane();
        pane.getChildren().add(swingNode);
        
        Scene scene = new Scene(pane, 800, 600);
        
        scene.setOnZoom((ZoomEvent event) -> {
            System.out.println("zoom in progress");
        });
        
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
//        createAndSetSwingContent(swingNode);
    }

    public ReadInterfacePane createAndSetSwingContent() {
        rip = new ReadInterfacePane(abstractImageHandler);
//        swingNode.setCache(true);
//        swingNode.setCacheHint(CacheHint.SPEED);
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(rip);
        });
        return rip;
    }
}
