/*
 */

package booknaviger.readinterface;

import booknaviger.osbasics.OSBasics;
import booknaviger.picturehandler.AbstractImageHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 *
 * @author Inervo
 */
public class FXReadInterface extends Application {
    final private SwingNode swingNode = new SwingNode();
    private AbstractImageHandler abstractImageHandler;
    private ReadInterfacePane readInterface = null;
    private Stage stage = null;
    public static FXReadInterface INSTANCE = null;

    @SuppressWarnings("LeakingThisInConstructor")
    public FXReadInterface() {
        INSTANCE = this;
        Platform.setImplicitExit(false);
    }
    
    public void setImageHandler(AbstractImageHandler abstractImageHandler) {
        this.abstractImageHandler = abstractImageHandler;
    }
    
    @Override
    public void start(Stage stage) {
        StackPane pane = new StackPane();
        pane.getChildren().add(swingNode);
        
        Scene scene = new Scene(pane, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight());
        
        // TODO : passdown the events to the readInterface
        scene.setOnZoom((ZoomEvent event) -> {
            System.out.println("zoom in progress");
        });
        
        this.stage = stage;
        stage.setTitle("BookNaviger");
        stage.setScene(scene);
        showTime();
    }
    
    public void showTime() {
        stage.show();
    }
    
    public void close() {
        Platform.runLater(() -> {
            stage.close();
        });
        
    }

    public void createAndSetSwingContent() {
            readInterface = new ReadInterfacePane(abstractImageHandler);
            if (OSBasics.isMac()) {
                RepaintManager.currentManager(readInterface).setDoubleBufferingEnabled(false);
            }
        
            SwingUtilities.invokeLater(() -> {
                swingNode.setContent(readInterface);
            });
    }

    public ReadInterfacePane getReadInterface() {
        return readInterface;
    }
}
